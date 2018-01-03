/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class holds information about authorization rules and checks a resource 
 * access against those rules.
 * 
 * @author boto
 * Date of creation 23.08.2017
 */
public class AuthAccessRuleChecker {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Resource path
     */
    private String resourcePath;

    /**
     * Resource path as regular expression (used to detect {} place holders in path)
     */
    private String resourcePathRegexp;

    /**
     * Lookup for access methods and rules for defined resourcePath
     */
    private final Map<String /*access method*/, List<String /*role*/>> accessRules;

    /**
     * Create a rule for given resource path.
     * 
     * @param path  Resource path
     */
    public AuthAccessRuleChecker(String path) {
        resourcePath = path;
        resourcePathRegexp = resourcePath.replaceAll("\\{([^}]+)\\}", "(.+)");
        accessRules = new HashMap<>();
    }

    /**
     * Get the resource path for this rule.
     * 
     * @return  Resource path
     */
    public String getResourcePath() {
        return resourcePath;
    }

    /**
     * Get the resource path as regular expression so far any placeholder {} were
     * found in resource path.
     * 
     * @return  Resource path as regular expression
     */
    public String getResourcePathRegexp() {
        return resourcePathRegexp;
    }

    /**
     * Get all access methods and corresponding roles.
     * 
     * @return Lookup with access methods and roles
     */
    public Map<String /*access*/, List<String /*role*/>> getAccessRules() {
        return accessRules;
    }

    /**
     * Add a new role for given access method.
     * 
     * @param accessMethod  Access method (GET, PUT, POST, DELETE)
     * @param accessRoles   Access roles
     * @return              Return true if successful, if the role already exists then return false.
     */
    public boolean addAccessRoles(String accessMethod, List<String> accessRoles) {
        List<String /*role*/> roles = accessRules.get(accessMethod);
        if (roles == null) {
            roles = new ArrayList<>();
            accessRules.put(accessMethod, roles);
        }
        roles.addAll(accessRoles);
        return true;
    }

    /**
     * Check if the given role has access to resource considering a simple fix path.
     * 
     * @param path              Resource path
     * @param accessMethod      Access method such as GET, PUT, POST, and DELETE
     * @param userRoles         User roles to check against for authorization
     * @return                  Return true if authorization is granted, otherwise false
     */
    public boolean checkFixPath(String path, String accessMethod, List<String> userRoles) {
        if (!path.contentEquals(resourcePath)) {
            return false;
        }
        return checkRoles(accessMethod, userRoles);
    }

    /**
     * Check if the given role has access to resource considering a complex path consisting
     * on regular expressions like {id} as specified in JAX-RS.
     * 
     * @param complexPath       Resource path
     * @param accessMethod      Access method such as GET, PUT, POST, and DELETE
     * @param userRoles         User roles to check against for authorization
     * @return                  Return true if authorization is granted, otherwise false
     */
    public boolean checkComplexPath(String complexPath, String accessMethod, List<String> userRoles) {
        try {
            if (!complexPath.matches(resourcePathRegexp)) {
                return false;
            }
        }
        catch(Exception ex) {
            LOGGER.error("Error while checking the resource path: " + ex.getLocalizedMessage());
            return false;
        }

        return checkRoles(accessMethod, userRoles);        
    }

    /**
     * Given an access method (such as GET, POST, etc.) check if any of user roles
     * match to required ones.
     * 
     * NOTE: Access to special role AuthRole.NO_CHECK are always granted.
     * 
     * @param accessMethod  Access method
     * @param userRoles     List of user roles
     * @return              Return true if at least one role matched, otherwise false.
     */
    private boolean checkRoles(String accessMethod, List<String> userRoles) {
        List<String /*role*/> roles = accessRules.get(accessMethod);
        if (roles == null) {
            return false;
        }
        // check for no-check and guest resource
        if (roles.contains(AuthRole.VIRT_ENDPOINT_CHECK) || roles.contains(AuthRole.VIRT_ROLE_GUEST)) {
            return true;
        }
        return userRoles.stream().anyMatch((role) -> (roles.contains(role)));
    }

    /**
     * Convert the rule to a readable string.
     * 
     * @return String representing the rule
     */
    @Override
    public String toString() {
        String text = "Resource path: '" + resourcePath;
        if (!resourcePath.contentEquals(resourcePathRegexp)) {
            text += " | Compiled path: " + resourcePathRegexp;
        }

        for (Map.Entry<String /*access*/, List<String /*role*/>> acc: accessRules.entrySet()) {
            text +=" | Access roles [" + acc.getKey() + "]: " + String.join(",", acc.getValue());
        }
        return text;
    }
}
