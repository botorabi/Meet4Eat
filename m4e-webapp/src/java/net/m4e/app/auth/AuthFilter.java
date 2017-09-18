/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.auth;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import net.m4e.common.ResponseResults;
import net.m4e.system.core.Log;
import net.m4e.app.user.UserEntity;

/**
 *
 * @author boto
 * Date of creation Aug 18, 2017
 */
public class AuthFilter implements Filter {
 
    /**
     * Used for logging
     */
    private final static String TAG = "AuthFilter";

    /**
     * Configuration parameter name for "basePath"
     */
    private static final String PARAM_BASE_PATH = "basePath";

    /**
     * Configuration parameter name for "publicBasePath"
     */
    private static final String PARAM_BASE_PATH_PUBLIC = "publicBasePath";

    /**
     * Configuration parameter name for "protectedBasePath"
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
        Log.debug(TAG, "Destroy");
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

            Log.debug(TAG, "Initializing filter: " +
                "basePath(" + basePath + ") | " +
                "publicBasePath(" + basePath + "/" + publicBasePath + ") | " + 
                "protectedBasePath(" + basePath + "/" + protectedBasePath + ")");
        }
        Log.debug(TAG, "Setup authorization check for protected path: " + basePath + "/" + protectedBasePath);
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

        HttpServletRequest httprequest = (HttpServletRequest)request;
        String             url         = httprequest.getRequestURL().toString();
        String             path        = new URL(url).getPath();

        Log.verbose(TAG, "Requesting for resource: " + path);

        if (path.startsWith("/" + basePath)) {

            if (path.compareTo("/" + basePath + "/index.html") == 0) {
                processRequest(request, response, chain);                    
            }
            else if (path.startsWith("/" + basePath + "/" + publicBasePath)) {
                Log.verbose(TAG, "  Fechting public resource: " + path);
                processRequest(request, response, chain);
            }
            else if (path.startsWith("/" + basePath + "/" + protectedBasePath)) {
                // get the user roles out of the http session
                UserEntity sessionuser = getSessionUser(httprequest);
                List<String> userroles;
                if (Objects.nonNull(sessionuser)) {
                    Log.verbose(TAG, "   User '" + sessionuser.getLogin() + "' accessing protected resource: " + path);
                    userroles = sessionuser.getRolesAsString();
                    // authenticated users get automatically the role USER
                    if (userroles.contains(AuthRole.VIRT_ROLE_USER)) {
                        Log.warning(TAG, "   *** Virtual user role " + AuthRole.VIRT_ROLE_USER + " was detected on user!");
                    }
                    userroles.add(AuthRole.VIRT_ROLE_USER);
                }
                else {
                    Log.verbose(TAG, "  Accessing protected resource: " + path);
                    // non-authenticated users get automatically the role GUEST
                    userroles = new ArrayList();
                    userroles.add(AuthRole.VIRT_ROLE_GUEST);
                }
                if (authChecker.checkAccess("/" + basePath + "/" + protectedBasePath, httprequest, userroles)) {
                    processRequest(request, response, chain);
                }
                else {
                    Log.warning(TAG, "*** Access denied to protected resource: " + path);
                    response.getWriter().print(ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK,
                                                "Denied access to: " + path, ResponseResults.CODE_FORBIDDEN, null));
                }
            }
            else {
                processRequest(request, response, chain);
            }
        }
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
        if (Objects.nonNull(user)) {
            if (user instanceof UserEntity) {
                return (UserEntity)user;
            }
            else {
                Log.error(TAG, "*** Unexpected session object type detected for '" + AuthorityConfig.SESSION_ATTR_USER + "'");
            }
        }
        return null;
    }

    /**
     * Process the request on the filter chain.
     * 
     * @param request
     * @param response
     * @param chain
     * @throws IOException
     * @throws ServletException 
     */
    protected void processRequest(ServletRequest request, ServletResponse response, FilterChain chain)
                                    throws IOException, ServletException{
        
        Throwable problem = null;
        try {
            chain.doFilter(request, response);
        }
        catch (IOException | ServletException ex) {
            Log.error(TAG, "*** A problem occured while executing filters, reason: " + ex.getLocalizedMessage());
            problem = ex;
        }

        if (problem != null) {
            if (problem instanceof ServletException) {
                throw (ServletException) problem;
            }
            if (problem instanceof IOException) {
                throw (IOException) problem;
            }
            response.getWriter().print(ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK,
                                       "Problem occurred while processing filter chain, reason: " + problem.getLocalizedMessage(), 500, null));
        }
    }

    /**
     * Return the filter configuration object for this filter.
     * @return Filter configuration
     */
    public FilterConfig getFilterConfig() {
        return (this.filterConfig);
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
