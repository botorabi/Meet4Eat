/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.rest;

import io.swagger.annotations.*;
import net.m4e.app.auth.*;
import net.m4e.app.user.business.*;
import net.m4e.app.user.rest.comm.*;
import net.m4e.common.GenericResponseResult;
import org.jetbrains.annotations.NotNull;
import org.slf4j.*;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.lang.invoke.MethodHandles;

/**
 * REST services for user authentication and creation
 * 
 * @author boto
 * Date of creation Aug 18, 2017
 */
@Stateless
@Path("/rest/authentication")
@Api(value = "User authentication service")
public class UserAuthenticationRestService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Users users;

    /**
     * Create the bean.
     * 
     * @param users Injected Users instance
     */
    @Inject
    public UserAuthenticationRestService(@NotNull Users users) {
        this.users = users;
    }

    @GET
    @Path("state")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ENDPOINT_CHECK})
    @ApiOperation(value = "Get the authentication state")
    public GenericResponseResult<AuthState> state(@Context HttpServletRequest request) {
        HttpSession session = request.getSession();
        UserEntity userEntity = (UserEntity)session.getAttribute(AuthorityConfig.SESSION_ATTR_USER);
        boolean auth = false;
        String  uid = "";
        if (userEntity != null) {
            auth = true;
            uid = userEntity.getId().toString();
        }

        return GenericResponseResult.ok("Authentication state", new AuthState(auth, uid, session.getId()));
    }

    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ENDPOINT_CHECK})
    @ApiOperation(value = "Login user")
    public GenericResponseResult<LoggedIn> login(LoginCmd loginCmd, @Context HttpServletRequest request) {
        if (loginCmd.getLogin().isEmpty() || loginCmd.getPassword().isEmpty()) {
            LOGGER.debug("*** No valid login data");
            return GenericResponseResult.badRequest("Failed to login user.");
        }
        LOGGER.trace("User tries to login: " + loginCmd.getLogin());

        HttpSession session = request.getSession();
        Object      user    = session.getAttribute(AuthorityConfig.SESSION_ATTR_USER);
        if (user != null) {
            LOGGER.debug("  User login attempt failed, user is already logged in, user ({})", loginCmd.getLogin());
            return GenericResponseResult.notAcceptable("Failed to login user. A user is already logged in.");
        }

        // try to find the user in database
        UserEntity existingUser = users.findUser(loginCmd.getLogin());
        if ((existingUser == null) || !existingUser.getStatus().getIsActive()) {
            LOGGER.debug("  User login attempt failed, no user with this login found, user ({})", loginCmd.getLogin());
            return GenericResponseResult.notFound("Failed to login user.");
        }
        // check user password
        String saltedPassword = AuthorityConfig.getInstance().createPassword(existingUser.getPassword() + session.getId());
        if (!saltedPassword.contentEquals(loginCmd.getPassword())) {
            LOGGER.debug("  User login attempt failed, wrong password, user ({})", loginCmd.getLogin());
            return GenericResponseResult.unauthorized("Failed to login user.");
        }

        LOGGER.trace(" User successfully logged in: {}", loginCmd.getLogin());
        // store the user in client session
        session.setAttribute(AuthorityConfig.SESSION_ATTR_USER, existingUser);
        // update user
        users.updateUserLastLogin(existingUser);

        return GenericResponseResult.ok("User was successfully logged in.", new LoggedIn(existingUser.getId().toString(), session.getId()));
    }

    @POST
    @Path("logout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    @ApiOperation(value = "Logout user")
    public GenericResponseResult<Void> logout(@Context HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object user = session.getAttribute(AuthorityConfig.SESSION_ATTR_USER);
        if (user == null) {
            LOGGER.debug("*** Invalid logout attempt");
            return GenericResponseResult.notAcceptable("Failed to logout user. User was not logged in before.");
        }
        session.invalidate();
        return GenericResponseResult.ok("User was successfully logged out.");
    }
}
