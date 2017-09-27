/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.user;

import java.io.StringReader;
import java.util.Objects;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
public class UserAuthenticationFacadeREST extends net.m4e.common.AbstractFacade<UserEntity> {

    /**
     * Used for logging
     */
    private final static String TAG = "UserAuthenticationFacadeREST";

    /**
     * Entity manager needed for entity retrieval and modifications.
     */
    @PersistenceContext(unitName = net.m4e.system.core.AppConfiguration.PERSITENCE_UNIT_NAME)
    private EntityManager entityManager;

    /**
     * Construct the stateless bean.
     */
    public UserAuthenticationFacadeREST() {
        super(UserEntity.class);
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
        if (Objects.nonNull(user)) {
            UserEntity userentity = (UserEntity)user;
            json.add("auth", "yes");
            json.add("id", userentity.getId());
        }
        else {
            json.add("auth", "no");
            json.add("id", "0");
        }

        json.add("sid", session.getId());
        String respdata = json.build().toString();
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "Session state", ResponseResults.CODE_OK, respdata);
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
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to login user.", ResponseResults.CODE_BAD_REQUEST, null);
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
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to login user.", ResponseResults.CODE_BAD_REQUEST, null);           
        }
        Log.verbose(TAG, "User tries to login: " + login);

        HttpSession session = request.getSession();
        Object      user    = session.getAttribute(AuthorityConfig.SESSION_ATTR_USER);
        if (Objects.nonNull(user)) {
            Log.debug(TAG, "  User login attempt failed, user is already logged in, user (" + login+ ")");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to login user. A user is already logged in.", ResponseResults.CODE_NOT_ACCEPTABLE, null);
        }

        // try to find the user in database
        Users userutils = new Users(entityManager);
        UserEntity existinguser = userutils.findUser(login);
        if (Objects.isNull(existinguser) || !existinguser.getStatus().getIsActive()) {
            Log.debug(TAG, "  User login attempt failed, no user with this login found, user (" + login+ ")");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to login user.", ResponseResults.CODE_NOT_FOUND, null);
        }
        // check user password
        String saltedpasswd = AuthorityConfig.getInstance().createPassword(existinguser.getPassword() + session.getId());
        if (!saltedpasswd.contentEquals(passwd)) {
            Log.debug(TAG, "  User login attempt failed, wrong password, user (" + login + ")");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to login user.", ResponseResults.CODE_UNAUTHORIZED, null);
        }

        Log.verbose(TAG, " User successfully logged in: " + login);
        // store the user in client session
        session.setAttribute(AuthorityConfig.SESSION_ATTR_USER, existinguser);
        // update user
        userutils.updateUserLastLogin(existinguser);

        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("id", existinguser.getId());
        json.add("sid", session.getId());
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "User was successfully logged in.", ResponseResults.CODE_OK, json.build().toString());
    }

    @POST
    @Path("logout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public String logout(@Context HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        Object user = session.getAttribute(AuthorityConfig.SESSION_ATTR_USER);
        if (Objects.isNull(user)) {
            Log.debug(TAG, "*** Invalid logout attempt");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to logout user. User was not logged in before.", ResponseResults.CODE_NOT_ACCEPTABLE, null);
        }
        session.invalidate();
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "User was successfully logged out.", ResponseResults.CODE_OK, null);
    }

    /**
     * Get the entity manager.
     * 
     * @return Entity manager
     */
    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }
}
