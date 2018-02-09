/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.auth;

import org.slf4j.*;

import javax.servlet.http.HttpServletRequest;
import java.lang.invoke.MethodHandles;
import java.net.*;
import java.util.*;

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
 *   \@AuthRole(grant={AuthRole.USER_ROLE_MODERATOR})
 * 
 *   This grants access to authenticated users.
 *   \@AuthRole(grant={AuthRole.VIRT_ROLE_USER})
 *
 *   This grants access to any role, the business logic has to check for authorization.
 *   \@AuthRole(grant={AuthRole.VIRT_ENDPOINT_CHECK})
 * 
 * For available user roles see annotation interface AuthRole.
 * 
 * @author boto
 * Date of creation Aug 23, 2017
 */
public class AuthChecker {

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
        beanClasses.stream()
                .peek(cls -> LOGGER.debug("Adding rules for bean class {}", cls.getName()))
                .forEach(clazz -> {

            // get the base path of the bean class (checking for class' Path annotation)
            String classRulePath = annotations.getClassPath(clazz);

            //! NOTE Currently we check access only against roles, but AuthRole provides also definition of
            //        permissions (see Annotations.getMethodsAuthPermissions), so in future it is possible
            //        to provide a more fine-grained access control if needed.
            annotations.getMethodsAuthRoles(clazz).forEach((path, roles) -> {

                String fullResourcePath = classRulePath + (path.isEmpty() ? "" : "/" + path);
                AuthAccessRuleChecker rule = new AuthAccessRuleChecker(fullResourcePath);

                roles.forEach(rule::addAccessRoles);

                // path entry is relative to class' path, check if a complex path was defined
                if (fullResourcePath.contains("{")) {
                    accessRulesComplexPath.add(rule);
                } else {
                    accessRulesFixPath.put(fullResourcePath, rule);
                }
                LOGGER.debug("Adding rule: {}", rule);
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
        if (grantAlwaysRoles.stream().anyMatch(userRoles::contains)) {
            LOGGER.trace("Access granted to Grant-Always roles");
            return true;
        }

        boolean grantAccess = false;
        try {
            URL url = new URL(request.getRequestURL().toString());
            String path = url.getPath();

            // do some checks first
            if (!path.startsWith(basePath)) {
                LOGGER.trace("Access denied: given path '{}' does not start with expected base path '{}'", path, basePath);
                return false;
            }

            String resourcePath = path.substring(basePath.length());
            LOGGER.trace("Checking resource path [{}]: {}", request.getMethod(), resourcePath);

            // first check for fix path match
            AuthAccessRuleChecker accessRule = accessRulesFixPath.get(resourcePath);
            if (accessRule != null) {
                grantAccess = accessRule.checkFixPath(resourcePath, request.getMethod(), userRoles);
            }
            else {
                // if no hit then check for complex path match
                for (AuthAccessRuleChecker acc: accessRulesComplexPath) {
                    if (acc.checkComplexPath(resourcePath, request.getMethod(), userRoles)) {
                        grantAccess = true;
                        break;
                    }
                }
            }
            LOGGER.trace("Access granted: {}", (grantAccess ? "Yes" : "No"));
        }
        catch(MalformedURLException | SecurityException ex) {
            LOGGER.warn("An exception happened during auth check: {}", ex.getLocalizedMessage());
            return false;
        }
        return grantAccess;
    }
}
