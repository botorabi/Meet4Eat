/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.user;

import java.io.StringReader;
import java.util.Objects;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.UserTransaction;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import net.m4e.auth.AuthRole;
import net.m4e.auth.AuthorityConfig;
import net.m4e.common.ResponseResults;
import net.m4e.core.Log;

/**
 * REST services for user authentication and creation
 * 
 * @author boto
 * Date of creation Aug 18, 2017
 */
@Stateless
@Path("/rest/authentication")
@TransactionManagement(TransactionManagementType.BEAN)
public class UserAuthenticationFacadeREST extends net.m4e.common.AbstractFacade<UserEntity> {

    /**
     * Used for logging
     */
    private final static String TAG = "UserAuthenticationFacadeREST";

    @PersistenceContext(unitName = net.m4e.core.AppConfiguration.PERSITENCE_UNIT_NAME)
    private EntityManager entityManager;

    @Resource
    private UserTransaction userTransaction;
    
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
    @net.m4e.auth.AuthRole(grant={AuthRole.VIRT_ENDPOINT_CHECK})
    public String state(@Context HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object user = session.getAttribute(AuthorityConfig.SESSION_ATTR_USER);
        JsonObjectBuilder json = Json.createObjectBuilder();

        if (Objects.nonNull(user)) {
            UserEntity userentity = (UserEntity)user;
            json.add("auth", "yes");
            json.add("userId", userentity.getId());
        }
        else {
            json.add("auth", "no");
            json.add("userId", "0");
        }

        json.add("sid", session.getId());
        String respdata = json.build().toString();
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "Session state", 200, respdata);
    }

    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grant={AuthRole.VIRT_ENDPOINT_CHECK})
    public String login(String input, @Context HttpServletRequest request) {
        // roughly check the input
        if (input == null || input.isEmpty()) {
            Log.debug(TAG, "*** Invalid login attempt");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to login user", 400, null);
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
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to login user", 400, null);           
        }
        Log.verbose(TAG, "User tries to login: " + login);

        HttpSession session = request.getSession();
        Object      user    = session.getAttribute(AuthorityConfig.SESSION_ATTR_USER);
        if (Objects.nonNull(user)) {
            Log.debug(TAG, "  User login attempt failed, user is already logged in, user (" + login+ ")");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to login user. A user is already logged in.", 400, null);
        }

        // try to find the user in database
        UserUtils userutils = new UserUtils(entityManager, userTransaction);
        UserEntity existinguser = userutils.findUser(login);
        if (Objects.isNull(existinguser)) {
            Log.debug(TAG, "  User login attempt failed, no user with this login found, user (" + login+ ")");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to login user.", 400, null);
        }
        // check user password
        String saltedpasswd = AuthorityConfig.getInstance().createPassword(existinguser.getPassword() + session.getId());
        if (!saltedpasswd.contentEquals(passwd)) {
            Log.debug(TAG, "  User login attempt failed, wrong password, user (" + login + ")");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to login user.", 400, null);
        }

        Log.verbose(TAG, " User successfully logged in: " + login);
        // store the user in client session
        session.setAttribute(AuthorityConfig.SESSION_ATTR_USER, existinguser);
        // update user
        userutils.updateUserLastLogin(existinguser);
        
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "User was successfully logged in.", 200, null);
    }

    @POST
    @Path("logout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grant={AuthRole.VIRT_ROLE_USER})
    public String logout(@Context HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        Object user = session.getAttribute(AuthorityConfig.SESSION_ATTR_USER);
        if (Objects.isNull(user)) {
            Log.debug(TAG, "*** Invalid logout attempt");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to logout user. User was not logged in before.", 400, null);
        }
        session.invalidate();
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "User was successfully logged out.", 200, null);
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

    /**
     * Get the user transaction instance.
     * @return 
     */
    protected UserTransaction getUserTransaction() {
        return userTransaction;
    }
}
