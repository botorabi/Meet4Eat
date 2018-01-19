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
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


/**
 * This class holds information about authorization rules and checks a resource 
 * access against those rules.
 * 
 * @author boto
 * Date of creation August 23, 2017
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
        resourcePathRegexp = createPathRegExp(path);
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
     * found in resource path. If the path was invalid, then null will be returned.
     * 
     * @return  Resource path as regular expression, or null if path was invalid
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
        if ((accessMethod == null) || (accessRoles == null)){
            return false;
        }
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
        if ((path == null) || (accessMethod == null)){
            return false;
        }
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
        if ((complexPath == null) || (accessMethod == null)) {
            return false;
        }
        if (!complexPath.matches(resourcePathRegexp)) {
            return false;
        }
        return checkRoles(accessMethod, userRoles);
    }

    /**
     * Try to create a proper regular expression for later pattern matching. If the path is not complex
     * then the passed string is returned. If a complex path was malformed then null is returned.
     *
     * @param regularExpression The path to create a regular expression for
     * @return                  The reg exp for the path, or null if the path was malformed
     */
    protected String createPathRegExp(String regularExpression) {
        String regexp = regularExpression.replaceAll("\\{([^}]+)\\}", "(.+)");
        if (regexp.contains("{") || regexp.contains("}")) {
            LOGGER.error("Bad access rule checker path detected: " + regularExpression);
            return null;
        }

        try {
            Pattern.compile(regexp);
        } catch (PatternSyntaxException ex) {
            LOGGER.error("Bad access rule checker path detected: " + ex.getLocalizedMessage());
            return null;
        }
        return regexp;
    }

    /**
     * Given an access method (such as GET, POST, etc.) check if any of user roles
     * match to required ones.
     * 
     * NOTE: Access to paths with roles AuthRole.NO_CHECK or AuthRole.VIRT_ROLE_GUEST are always granted.
     * 
     * @param accessMethod  Access method
     * @param userRoles     List of user roles
     * @return              Return true if at least one role matched, otherwise false.
     */
    protected boolean checkRoles(String accessMethod, List<String> userRoles) {
        if ((accessMethod == null) || (userRoles == null)) {
            return false;
        }
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
