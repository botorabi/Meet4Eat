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

import javax.servlet.http.HttpServletRequest;
import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class performing authorization checks for accessing resources provided by Java Beans.
 * It uses annotations of type 'AuthRole' on Java Bean classes in order to setup
 * the access control rules.
 * 
 * A List of super user roles can be set via method setGrantAlwaysRoles which is
 * allowed access to any available resource.
 * 
 * Example: The following annotation on a REST bean method defines an access
 * authorization rule.
 * 
 *   This grants access to role MODERATOR.
 *   @AuthRole(grant={AuthRole.USER_ROLE_MODERATOR})
 * 
 *   This grants access to authenticated users.
 *   @AuthRole(grant={AuthRole.VIRT_ROLE_USER})
 *
 *   This grants access to any role, the business logic has to check for authorization.
 *   @AuthRole(grant={AuthRole.VIRT_ENDPOINT_CHECK})
 * 
 * For available user roles see annotation interface AuthRole.
 * 
 * @author boto
 * Date of creation Aug 23, 2017
 */
public class AuthChecker {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * A lookup for access rules with fix paths. In order to speed up the lookup, the resource path is used as key.
     */
    private final Map<String /*resource path*/, AuthAccessRuleChecker> accessRulesFixPath;

    /**
     * A list of access rules with complex paths consisting of regular expressions.
     */
    private final List<AuthAccessRuleChecker> accessRulesComplexPath;

    /**
     * A list of roles getting access to any protected resource (e.g. super user roles)
     */
    private final List<String> grantAlwaysRoles = new ArrayList<>();


    /**
     * Create the authorization checker.
     */
    public AuthChecker() {
        accessRulesFixPath = new HashMap<>();
        accessRulesComplexPath = new ArrayList<>();
    }

    /**
     * Initialize the checker given a list of java beans providing resource
     * access (e.g. REST facades).
     * 
     * @param beanClasses   List of java beans
     */
    public void initialize(List<Class<?>> beanClasses) {
        LOGGER.info("Initializing authorization checker");
        if (beanClasses != null) {
            setupRules(beanClasses);
        }
    }

    /**
     * Set all roles with no access restriction. A previous list is replaced by the given list.
     * 
     * @param roles List of roles which are always granted access
     */
    public void setGrantAlwaysRoles(List<String> roles) {
        grantAlwaysRoles.clear();
        grantAlwaysRoles.addAll(roles);
        LOGGER.debug("Setting grant-always roles: {}", String.join(",", roles));
    }

    /**
     * Get all roles which have no access restriction (e.g. an admin role).
     * 
     * @return List of roles without access restriction
     */
    public List<String> getGrantAlwaysRoles() {
        return grantAlwaysRoles;
    }

    /**
     * Setup all authorization rules found on methods of given classes.
     * 
     * @param beanClasses   List of java beans
     */
    private void setupRules(List<Class<?>> beanClasses) {
        // gather information from all bean classes about authorization relevant annotations
        Annotations annotations = new Annotations();
        beanClasses.stream().map((cls) -> {
            LOGGER.debug("Adding rules for bean class {}", cls.getName());
            return cls;
        }).forEach((cls) -> {
            // get the base path of the bean class (checking for class' Path annotation)
            String classrulepath = annotations.getClassPath(cls);

            //! NOTE Currently we check access only against roles, but AuthRole provides also definition of
            //        permissions (see Annotations.getMethodsAuthPermissions), so in future it is possible
            //        to provide a more fine-grained access control if needed.
            annotations.getMethodsAuthRoles(cls).entrySet().stream().forEach((pathentry) -> {

                String fullrespath = classrulepath + (pathentry.getKey().isEmpty() ? "" : "/" + pathentry.getKey());
                AuthAccessRuleChecker rule = new AuthAccessRuleChecker(fullrespath);

                pathentry.getValue().entrySet().stream().forEach((accessentry) -> {
                    rule.addAccessRoles(accessentry.getKey(), accessentry.getValue());
                });
                // path entry is relative to class' path
                // check if a complex path was defined
                if (fullrespath.contains("{")) {
                    accessRulesComplexPath.add(rule);
                }
                else {
                    accessRulesFixPath.put(fullrespath, rule);
                }
                LOGGER.debug("Adding rule: {}", rule.toString());
            });
        });
    }

    /**
     * Check authorization of incoming request.
     * 
     * @param basePath  The base path of requesting resource
     * @param request   Incoming request
     * @param userRoles User roles
     * @return          Return true if access authorization was ok, otherwise false.
     */
    public boolean checkAccess(String basePath, HttpServletRequest request, List<String> userRoles) {

        // check for no-restriction access
        if (grantAlwaysRoles.stream().anyMatch((r) -> userRoles.contains(r))) {
            LOGGER.trace("Access granted to Grant-Always roles");
            return true;
        }

        boolean grantaccess = false;
        try {
            URL url = new URL(request.getRequestURL().toString());
            String path = url.getPath();

            // do some checks first
            if (!path.startsWith(basePath)) {
                LOGGER.trace("Access denied: given path '{}' does not start with expected base path '{}'", path, basePath);
                return false;
            }

            String respath = path.substring(basePath.length());
            LOGGER.trace("Checking resource path [{}]: {}", request.getMethod(), respath);

            // first check for fix path match
            AuthAccessRuleChecker accrule = accessRulesFixPath.get(respath);
            if (accrule != null) {
                grantaccess = accrule.checkFixPath(respath, request.getMethod(), userRoles);
            }
            else {
                // if no hit then check for complex path match
                for (AuthAccessRuleChecker acc: accessRulesComplexPath) {
                    if (acc.checkComplexPath(respath, request.getMethod(), userRoles)) {
                        grantaccess = true;
                        break;
                    }
                }
            }
            LOGGER.trace("Access granted: {}", (grantaccess ? "Yes" : "No"));
        }
        catch(MalformedURLException | SecurityException ex) {
            LOGGER.warn("An exception happened during auth check: {}", ex.getLocalizedMessage());
            return false;
        }
        return grantaccess;
    }
}
