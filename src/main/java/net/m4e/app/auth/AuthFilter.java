/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.auth;

import net.m4e.app.user.business.UserEntity;
import net.m4e.common.ResponseResults;
import net.m4e.system.core.AppConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Authentication/authorization filter which checks user's access to resources
 * 
 * @author boto
 * Date of creation Aug 18, 2017
 */
public class AuthFilter implements Filter {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Configuration parameter name for "basePath"
     */
    private static final String PARAM_BASE_PATH = "basePath";

    /**
     * Configuration parameter name for "publicBasePath" used for defining path
     * which is not restricted in access.
     */
    private static final String PARAM_BASE_PATH_PUBLIC = "publicBasePath";

    /**
     * Configuration parameter name for "protectedBasePath" used for a base path
     * which is restricted in access.
     */
    private static final String PARAM_BASE_PATH_PROTECTED = "protectedBasePath";

    /**
     * The filter configuration object we are associated with. if
     * this value is null, this filter instance is not currently configured.
     */
    private FilterConfig filterConfig = null;

    /**
     * Access authorization checker
     */
    private final AuthChecker authChecker;

    private String basePath = "";
    private String publicBasePath = "";
    private String protectedBasePath = "";

    /**
     * Create the filter.
     */
    public AuthFilter() {
        authChecker = new AuthChecker();
    }

    /**
     * Destroy method for this filter
     */
    @Override
    public void destroy() {
        LOGGER.debug("Destroy");
    }

    /**
     * Initialize method for this filter
     * @param filterConfig  The filter configuration
     */
    @Override
    public void init(FilterConfig filterConfig) {        
        this.filterConfig = filterConfig;
        if (filterConfig != null) {
            basePath = filterConfig.getInitParameter(PARAM_BASE_PATH);
            publicBasePath = filterConfig.getInitParameter(PARAM_BASE_PATH_PUBLIC);
            protectedBasePath = filterConfig.getInitParameter(PARAM_BASE_PATH_PROTECTED);

            LOGGER.debug("Initializing filter: basePath({}) | publicBasePath({}) | protectedBasePath({})",
                    basePath, basePath + "/" + publicBasePath, basePath + "/" + protectedBasePath);
        }
        LOGGER.debug("Setup authorization check for protected path: {}/{}", basePath, protectedBasePath);
        // setup the auth checker
        authChecker.initialize(AuthorityConfig.getInstance().getAccessBeanClasses());
        // ADMIN role gets always access to resources
        authChecker.setGrantAlwaysRoles(Arrays.asList(AuthRole.USER_ROLE_ADMIN));
    }

    /**
     * Perform the filter function. Here, access is granted to public resources.
     * Protected resources are delivered upon a successful authorization.
     * Public and protected resources are defined by deployment descriptor (web.xml)
     * and by AuthRole annotation on end-point handlers such as REST methods.
     * 
     * @param request  The servlet request we are processing
     * @param response The servlet response we are creating
     * @param chain    The filter chain we are processing
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                         throws IOException, ServletException {

        // we handle only http requests in this filter
        if (!(request instanceof HttpServletRequest)) {
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest)request;
        String             url         = httpRequest.getRequestURL().toString();
        String             path        = new URL(url).getPath();

        LOGGER.trace("Requesting for resource: " + path);

        if (path.startsWith("/" + basePath)) {

            boolean allowAccess = checkResourceAccess(httpRequest, path);

            if (allowAccess) {
                processRequest(request, response, chain);
            }
            else {
                LOGGER.warn("*** Access denied to protected resource: {}", path);
                response.getWriter().print(ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK,
                        "Denied access to: " + path, ResponseResults.CODE_FORBIDDEN, null));
            }
        }
    }

    private boolean checkResourceAccess(HttpServletRequest httpRequest, String path) {
        boolean allowAccess = false;

        // check for accessing html files in base path
        if (path.equals("/" + basePath + "/") || path.matches("/" + basePath + "/.*\\.html")) {
            allowAccess = true;
        }
        // check for accessing public resources
        else if (path.startsWith("/" + basePath + "/" + publicBasePath)) {
            LOGGER.trace("  Fetching public resource: " + path);
            allowAccess = true;
        }
        // check for WebSocket endpoint access
        else if (path.equals("/" + basePath + AppConfiguration.WEBSOCKET_URL)) {
            allowAccess = true;
        }
        // check for swagger access
        else if (path.matches("/" + basePath + "/" + protectedBasePath + "/swagger\\..*")) {
            allowAccess = true;
        }
        // check for accessing protected resources such as rest-services
        else if (path.startsWith("/" + basePath + "/" + protectedBasePath)) {
            // get the user roles out of the http session
            allowAccess = checkProtectedPath(httpRequest, path);
        }
        return allowAccess;
    }

    private boolean checkProtectedPath(HttpServletRequest httpRequest, String path) {
        UserEntity sessionUser = getSessionUser(httpRequest);
        List<String> userRoles;
        if (sessionUser != null) {
            LOGGER.trace("   User '{}' accessing protected resource: {}", sessionUser.getLogin(), path);
            userRoles = sessionUser.getRolesAsString();
            // authenticated users get automatically the role USER
            if (userRoles.contains(AuthRole.VIRT_ROLE_USER)) {
                LOGGER.warn("   *** Virtual user role {} was detected on user!", AuthRole.VIRT_ROLE_USER);
            }
            userRoles.add(AuthRole.VIRT_ROLE_USER);
        }
        else {
            LOGGER.trace("  Accessing protected resource: {}", path);
            // non-authenticated users get automatically the role GUEST
            userRoles = new ArrayList<>();
            userRoles.add(AuthRole.VIRT_ROLE_GUEST);
        }
        if (authChecker.checkAccess("/" + basePath + "/" + protectedBasePath, httpRequest, userRoles)) {
            return true;
        }
        return false;
    }

    /**
     * Check if the request came form a authenticated user, if so then return the
     * user as set in session. An authenticated user is expected to be in HTTP session
     * attribute defined by AuthorityConfig.SESSION_ATTR_USER.
     * 
     * @param request   HTTP request
     * @return          UserEntity as currently set in session, or null if user is set in session.
     */
    private UserEntity getSessionUser(HttpServletRequest request) {
        HttpSession  session   = request.getSession();
        Object       user      = session.getAttribute(AuthorityConfig.SESSION_ATTR_USER);
        if (user != null) {
            if (user instanceof UserEntity) {
                return (UserEntity)user;
            }
            else {
                LOGGER.error("*** Unexpected session object type detected for '{}'", AuthorityConfig.SESSION_ATTR_USER);
            }
        }
        return null;
    }

    /**
     * Process the request on the filter chain.
     */
    protected void processRequest(ServletRequest request, ServletResponse response, FilterChain chain)
                                    throws IOException, ServletException {

        try {
            chain.doFilter(request, response);
        }
        catch (IOException | ServletException ex) {
            LOGGER.error("*** A problem occurred while executing filters, reason: {}", ex.getMessage());
            response.getWriter().print(ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK,
                                       "Problem occurred while processing filter chain, reason: " + ex.getMessage(),
                                       ResponseResults.CODE_INTERNAL_SRV_ERROR, null));
            throw ex;
        }
    }

    /**
     * Return the filter configuration object for this filter.
     * @return Filter configuration
     */
    public FilterConfig getFilterConfig() {
        return filterConfig;
    }

    /**
     * Set the filter configuration object for this filter.
     *
     * @param filterConfig The filter configuration object
     */
    public void setFilterConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    /**
     * Return a String representation of this object.
     * 
     * @return String representing this filter
     */
    @Override
    public String toString() {
        if (filterConfig == null) {
            return ("AuthFilter()");
        }
        StringBuilder sb = new StringBuilder("AuthFilter(");
        sb.append(filterConfig);
        sb.append(")");
        return (sb.toString());
    }
}
