/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.user;

import java.io.StringReader;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.json.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import net.m4e.app.auth.AuthRole;
import net.m4e.app.auth.AuthorityConfig;
import net.m4e.common.ResponseResults;
import net.m4e.system.core.Log;

/**
 * REST services for user authentication and creation
 * 
 * @author boto
 * Date of creation Aug 18, 2017
 */
@Stateless
@Path("/rest/authentication")
public class UserAuthenticationFacadeREST {

    /**
     * Used for logging
     */
    private final static String TAG = "UserAuthenticationFacadeREST";

    private final Users users;

    /**
     * EJB's default constructor.
     */
    protected UserAuthenticationFacadeREST() {
        this.users = null;
    }

    /**
     * Create the bean.
     * 
     * @param users Injected Users instance
     */
    @Inject
    public UserAuthenticationFacadeREST(Users users) {
        this.users = users;
    }

    @GET
    @Path("state")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ENDPOINT_CHECK})
    public String state(@Context HttpServletRequest request) {
        JsonObjectBuilder json = Json.createObjectBuilder();
        HttpSession session = request.getSession();
        Object user = session.getAttribute(AuthorityConfig.SESSION_ATTR_USER);
        if (user != null) {
            UserEntity userentity = (UserEntity)user;
            json.add("auth", "yes")
                .add("id", userentity.getId().toString());
        }
        else {
            json.add("auth", "no")
                .add("id", "");
        }

        json.add("sid", session.getId());
        String respdata = json.build().toString();
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "Session state", ResponseResults.CODE_OK, respdata);
    }

    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ENDPOINT_CHECK})
    public String login(String input, @Context HttpServletRequest request) {
        // roughly check the input
        if (input == null || input.isEmpty()) {
            Log.debug(TAG, "*** Invalid login attempt");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to login user.", ResponseResults.CODE_BAD_REQUEST, null);
        }
        // try to get login and password
        String login, passwd;
        try {
            JsonReader jreader = Json.createReader(new StringReader(input));
            JsonObject jobject = jreader.readObject();
            login  = jobject.getString("login");
            passwd = jobject.getString("password");
        }
        catch(Exception ex) {
            login = "";
            passwd = "";
        }
        if (login.isEmpty() || passwd.isEmpty()) {
            Log.debug(TAG, "*** No valid login data");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to login user.", ResponseResults.CODE_BAD_REQUEST, null);           
        }
        Log.verbose(TAG, "User tries to login: " + login);

        HttpSession session = request.getSession();
        Object      user    = session.getAttribute(AuthorityConfig.SESSION_ATTR_USER);
        if (user != null) {
            Log.debug(TAG, "  User login attempt failed, user is already logged in, user (" + login+ ")");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to login user. A user is already logged in.", ResponseResults.CODE_NOT_ACCEPTABLE, null);
        }

        // try to find the user in database
        UserEntity existinguser = users.findUser(login);
        if ((existinguser == null) || !existinguser.getStatus().getIsActive()) {
            Log.debug(TAG, "  User login attempt failed, no user with this login found, user (" + login+ ")");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to login user.", ResponseResults.CODE_NOT_FOUND, null);
        }
        // check user password
        String saltedpasswd = AuthorityConfig.getInstance().createPassword(existinguser.getPassword() + session.getId());
        if (!saltedpasswd.contentEquals(passwd)) {
            Log.debug(TAG, "  User login attempt failed, wrong password, user (" + login + ")");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to login user.", ResponseResults.CODE_UNAUTHORIZED, null);
        }

        Log.verbose(TAG, " User successfully logged in: " + login);
        // store the user in client session
        session.setAttribute(AuthorityConfig.SESSION_ATTR_USER, existinguser);
        // update user
        users.updateUserLastLogin(existinguser);

        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("id", existinguser.getId().toString())
            .add("sid", session.getId());
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "User was successfully logged in.", ResponseResults.CODE_OK, json.build().toString());
    }

    @POST
    @Path("logout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public String logout(@Context HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        Object user = session.getAttribute(AuthorityConfig.SESSION_ATTR_USER);
        if (user == null) {
            Log.debug(TAG, "*** Invalid logout attempt");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to logout user. User was not logged in before.", ResponseResults.CODE_NOT_ACCEPTABLE, null);
        }
        session.invalidate();
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "User was successfully logged out.", ResponseResults.CODE_OK, null);
    }
}
