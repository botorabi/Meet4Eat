/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user;

import net.m4e.app.auth.AuthRole;
import net.m4e.app.auth.AuthorityConfig;
import net.m4e.app.communication.ConnectedClients;
import net.m4e.app.notification.SendEmailEvent;
import net.m4e.common.Entities;
import net.m4e.common.ResponseResults;
import net.m4e.system.core.AppConfiguration;
import net.m4e.system.core.AppInfoEntity;
import net.m4e.system.core.AppInfos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.json.*;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * REST services for User entity operations
 * 
 * @author boto
 * Date of creation Aug 18, 2017
 */
@Stateless
@Path("/rest/users")
public class UserEntityFacadeREST {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * The entities
     */
    private final Entities entities;

    /**
     * The users
     */
    private final Users users;

    /**
     * User input validator
     */
    private final UserEntityInputValidator validator;

    /**
     * User registration are handled by this instance.
     */
    private final UserRegistrations registration;

    /**
     * App information
     */
    private final AppInfos appInfos;


    /**
     * Event for sending e-mail to user.
     */
    @Inject
    private Event<SendEmailEvent> sendMailEvent;

    /**
     * User's online status is fetched from connected clients.
     */
    @Inject
    private ConnectedClients connections;


    /**
     * EJB's default constructor.
     */
    protected UserEntityFacadeREST() {
        users = null;
        entities = null;
        validator = null;
        registration = null;
        appInfos = null;
    }

    /**
     * Create the user entity REST facade.
     *
     * @param users         Users instance
     * @param entities      Entities instance
     * @param validator     User input validator
     * @param registration  User registration related functionality
     * @param appInfos      Application information
     */
    @Inject
    public UserEntityFacadeREST(Users users,
                                Entities entities,
                                UserEntityInputValidator validator,
                                UserRegistrations registration,
                                AppInfos appInfos) {

        this.users = users;
        this.entities = entities;
        this.validator = validator;
        this.registration = registration;
        this.appInfos = appInfos;
    }

    /**
     * Create a new user.
     * 
     * @param userJson   User details in JSON format
     * @param request    HTTP request
     * @return           JSON response
     */
    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.USER_ROLE_ADMIN})
    public String createUser(String userJson, @Context HttpServletRequest request) {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        //! NOTE Acutally, this check should not be needed (see AuthFilter), but just to be on the safe side!
        if (sessionuser == null) {
            LOGGER.error("*** Internal error, cannot create user, no user in session found!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to create user, no authentication.", ResponseResults.CODE_UNAUTHORIZED, null);
        }

        UserEntity reqentity;
        try {
            reqentity = validator.validateNewEntityInput(userJson);
        }
        catch (Exception ex) {
            LOGGER.warn("*** Could not create new user, validation failed, reason: " + ex.getLocalizedMessage());
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, ex.getLocalizedMessage(), ResponseResults.CODE_BAD_REQUEST, null);
        }

        // validate and adapt requested user roles
        reqentity.setRoles(users.adaptRequestedRoles(sessionuser, reqentity.getRoles()));

        UserEntity newuser;
        try {
            newuser = users.createNewUser(reqentity, sessionuser.getId());
        }
        catch (Exception ex) {
            LOGGER.warn("*** Could not create new user, reason: " + ex.getLocalizedMessage());
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to create new user.", ResponseResults.CODE_INTERNAL_SRV_ERROR, null);
        }

        //! NOTE on successful entity creation the new ID is sent back by results.data field.
        jsonresponse.add("id", newuser.getId());
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "User was successfully created.", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }

    /**
     * Register a new user. For activating the user, there is an activation process.
     * Only guests can use this service.
     * 
     * @param userJson      User details in JSON format
     * @param request       HTTP request
     * @return              JSON response
     */
    @POST
    @Path("register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_GUEST})
    public String registerUser(String userJson, @Context HttpServletRequest request) {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        if (sessionuser != null) {
            LOGGER.error("*** an already authenticated user tries a user registration!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to register user, logout first.", ResponseResults.CODE_NOT_ACCEPTABLE, jsonresponse.build().toString());
        }

        UserEntity reqentity;
        try {
            reqentity = validator.validateNewEntityInput(userJson);
        }
        catch (Exception ex) {
            LOGGER.warn("*** Could not register a new user, validation failed, reason: " + ex.getLocalizedMessage());
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, ex.getLocalizedMessage(), ResponseResults.CODE_BAD_REQUEST, jsonresponse.build().toString());
        }

        // just to be safe: no roles can be defined during registration
        reqentity.setRoles(null);

        UserEntity newuser;
        try {
            newuser = users.createNewUser(reqentity, null);
            // the user is not enabled until the registration process was completed
            newuser.getStatus().setEnabled(false);
        }
        catch (Exception ex) {
            LOGGER.warn("*** Could not register a new user, reason: " + ex.getLocalizedMessage());
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to register a new user.", ResponseResults.CODE_INTERNAL_SRV_ERROR, jsonresponse.build().toString());
        }

        // get the activation URL
        String activationurl = getAccRegCfgLinkURL("url.activation", request, "/activate.html");
        String adminemail    = getAccRegCfgNotificationMail();

        registration.registerUserAccount(newuser, activationurl, adminemail, sendMailEvent);

        //! NOTE on successful entity creation the new ID is sent back by results.data field.
        jsonresponse.add("id", newuser.getId());
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "User was successfully created.", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }

    /**
     * Activate a user by given its activation token. This is usually used during the registration process.
     * 
     * @param token         Activation token
     * @param request       HTTP request
     * @return              JSON response
     */
    @GET
    @Path("activate/{token}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_GUEST})
    public String activateUser(@PathParam("token") String token, @Context HttpServletRequest request) {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        if (sessionuser != null) {
            LOGGER.error("*** an already authenticated user tries a user activation!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to activate user account, logout first.", ResponseResults.CODE_NOT_ACCEPTABLE, jsonresponse.build().toString());
        }

        LOGGER.trace("activating user account, token: " + token);
        UserEntity user;
        try {
            user = registration.activateUserAccount(token);
        }
        catch (Exception ex) {
            LOGGER.debug("user activation failed, reason: " + ex.getLocalizedMessage());
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to activate user account! Reason: " + ex.getLocalizedMessage(), ResponseResults.CODE_NOT_ACCEPTABLE, jsonresponse.build().toString());
        }
        jsonresponse.add("userName", user.getName());
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "User was successfully activated.", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }

    /**
     * Request for resetting a user password. Only guests can use this service.
     * 
     * @param requestJson  Request data such as user email address in JSON format
     * @param request      HTTP request
     * @return             JSON response
     */
    @POST
    @Path("requestpasswordreset")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_GUEST})
    public String requestPasswordReset(String requestJson, @Context HttpServletRequest request) {
        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        if (sessionuser != null) {
            LOGGER.error("*** an already authenticated user tries to reset the password!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to reset user password, logout first.", ResponseResults.CODE_NOT_ACCEPTABLE, null);
        }

        try {
            JsonReader jreader = Json.createReader(new StringReader(requestJson));
            JsonObject jobject = jreader.readObject();
            String email = jobject.getString("email", null);
            if (email == null) {
                LOGGER.error("cannot process password reset request, invalid input");
                return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to reset user password, invalid input.", ResponseResults.CODE_NOT_ACCEPTABLE, null);
            }
            // create the activation URL
            String url = getAccRegCfgLinkURL("url.passwordReset", request, "/resetpassword.html");
            String adminemail = getAccRegCfgNotificationMail();
            registration.requestPasswordReset(email, url, adminemail, sendMailEvent);
        }
        catch(Exception ex) {
            LOGGER.error("cannot process password reset request, reason: " + ex.getLocalizedMessage());
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to reset user password! Reason: " + ex.getLocalizedMessage(), ResponseResults.CODE_NOT_ACCEPTABLE, null);           
        }
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "Request for user password reset was successfully processed.", ResponseResults.CODE_OK, null);
    }

    /**
     * Try to set a new password for an user account. The password reset token is validated,
     * on success the user password is reset to the given one in 'requestJason'.
     * 
     * @param requestJson   Request in JSON
     * @param token         Password reset token
     * @param request       HTTP request
     * @return              JSON response
     */
    @POST
    @Path("passwordreset/{token}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_GUEST})
    public String passwordReset(String requestJson, @PathParam("token") String token, @Context HttpServletRequest request) {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        if (sessionuser != null) {
            LOGGER.error("*** an already authenticated user tries to reset an user password");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to reset user password, logout first.", ResponseResults.CODE_NOT_ACCEPTABLE, jsonresponse.build().toString());
        }

        UserEntity user;
        try {
            JsonReader jreader = Json.createReader(new StringReader(requestJson));
            JsonObject jobject = jreader.readObject();
            String newpassword = jobject.getString("password", null);
            if (newpassword == null) {
                LOGGER.error("cannot process password reset request, invalid input");
                return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to reset user password, invalid input.", ResponseResults.CODE_NOT_ACCEPTABLE, null);
            }
            user = registration.processPasswordReset(token, newpassword);
        }
        catch (Exception ex) {
            LOGGER.debug("user password reset failed! Reason: " + ex.getLocalizedMessage());
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, ex.getLocalizedMessage(), ResponseResults.CODE_NOT_ACCEPTABLE, jsonresponse.build().toString());
        }
        jsonresponse.add("userName", user.getName());
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "User was successfully activated.", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }

    /**
     * Modify the user with given ID.
     * 
     * @param id        User ID
     * @param userJson  Entity modifications in JSON format
     * @param request   HTTP request
     * @return          JSON response
     */
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public String edit(@PathParam("id") Long id, String userJson, @Context HttpServletRequest request) {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        jsonresponse.add("userId", id.toString());
        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        if (sessionuser == null) {
            LOGGER.error("*** Cannot update user, no user in session found!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to update user, no authentication.",
                                             ResponseResults.CODE_UNAUTHORIZED, jsonresponse.build().toString());
        }

        UserEntity reqentity;
        try {
            reqentity = validator.validateUpdateEntityInput(userJson);
        }
        catch(Exception ex) {
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to update user, invalid input. Reason: " + 
                                             ex.getLocalizedMessage(), ResponseResults.CODE_BAD_REQUEST, jsonresponse.build().toString());
        }

        UserEntity user = entities.find(UserEntity.class, id);
        if ((user == null) || !user.getStatus().getIsActive()) {
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to find user for updating.",
                                             ResponseResults.CODE_NOT_FOUND, jsonresponse.build().toString());
        }

        // check if a user is updating itself or a user with higher privilege is trying to modify a user
        if (!users.userIsOwnerOrAdmin(sessionuser, user.getStatus())) {
            LOGGER.warn("*** User was attempting to update another user without proper privilege!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to update user, insufficient privilege.",
                                             ResponseResults.CODE_FORBIDDEN, jsonresponse.build().toString());
        }

        // validate the requested roles, check for roles, e.g. only admins can define admin role for other users
        user.setRoles(users.adaptRequestedRoles(sessionuser, reqentity.getRoles()));

        // take over non-empty fields
        boolean needupdate = false;
        if ((reqentity.getName() != null) && !reqentity.getName().isEmpty()) {
            user.setName(reqentity.getName());
            needupdate = true;
        }
        if ((reqentity.getPassword() != null) && !reqentity.getPassword().isEmpty()) {
            user.setPassword(reqentity.getPassword());
            needupdate = true;
        }
        if (reqentity.getPhoto() != null) {
            try {
                users.updateUserImage(user, reqentity.getPhoto());
                needupdate = true;
            }
            catch (Exception ex) {
                LOGGER.warn("*** User image could not be updated, reason: " + ex.getLocalizedMessage());
            }
        }

        if (needupdate) {
            try {
                users.updateUser(user);
            }
            catch (Exception ex) {
                return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to update user.",
                                                 ResponseResults.CODE_INTERNAL_SRV_ERROR, jsonresponse.build().toString());
            }
        }
        else {
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "No input for update.",
                                             ResponseResults.CODE_BAD_REQUEST, jsonresponse.build().toString());            
        }

        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "User successfully updated",
                                         ResponseResults.CODE_OK, jsonresponse.build().toString());
    }

    /**
     * Delete an user with given ID. The user will be marked as deleted, so it can be
     * purged later.
     * 
     * @param id        User ID
     * @param request   HTTP request
     * @return          JSON response
     */
    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.USER_ROLE_ADMIN})
    public String remove(@PathParam("id") Long id, @Context HttpServletRequest request) {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        jsonresponse.add("id", id.toString());

        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        if (sessionuser == null) {
            LOGGER.error("*** Cannot delete user, no user in session found!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to delete user.", ResponseResults.CODE_UNAUTHORIZED, jsonresponse.build().toString());
        }

        if (sessionuser.getId().equals(id)) {
            LOGGER.warn("*** User was attempting to delete iteself! Call a doctor.");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to delete yourself.", ResponseResults.CODE_NOT_ACCEPTABLE, jsonresponse.build().toString());            
        }

        UserEntity user = entities.find(UserEntity.class, id);
        if ((user == null) || !user.getStatus().getIsActive()) {
            LOGGER.warn("*** User was attempting to delete non-existing user!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to find user for deletion.", ResponseResults.CODE_NOT_FOUND, jsonresponse.build().toString());
        }

        try {
            users.markUserAsDeleted(user);
        }
        catch (Exception ex) {
            LOGGER.warn("*** Could not mark user as deleted, reason: " + ex.getLocalizedMessage());
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to delete user.", ResponseResults.CODE_INTERNAL_SRV_ERROR, jsonresponse.build().toString());
        }

        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "User successfully deleted", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }

    /**
     * Search for users containing given keyword in their name.
     * A maximal of 10 users are returned.
     * 
     * @param keyword  Keyword to search for
     * @return         JSON response
     */
    @GET
    @Path("/search/{keyword}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public String search(@PathParam("keyword") String keyword) {
        JsonArrayBuilder results = Json.createArrayBuilder();
        if (keyword == null) {
           return ResponseResults.toJSON(ResponseResults.STATUS_OK, "Search results", ResponseResults.CODE_OK, results.build().toString());        
        }

        List<String> searchfields = new ArrayList();
        searchfields.add("name");
        if (keyword.contains("@")) {
            searchfields.add("email");
        }
        List<UserEntity> hits = entities.searchForString(UserEntity.class, keyword, searchfields, 10);
        for (UserEntity hit: hits) {
            // exclude non-active users and admins from hit list
            if (!hit.getStatus().getIsActive() || users.checkUserRoles(hit, Arrays.asList(AuthRole.USER_ROLE_ADMIN)) ) {
                continue;
            }
            JsonObjectBuilder json = Json.createObjectBuilder();
            json.add("id", hit.getId().toString())
                .add("name", (hit.getName() != null) ? hit.getName() : "")
                .add("photoId", (hit.getPhoto() != null) ? hit.getPhoto().getId().toString() : "")
                .add("photoETag", (hit.getPhoto() != null) ? hit.getPhoto().getETag() : "");
            results.add(json);
        }
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "Search results", ResponseResults.CODE_OK, results.build().toString());
    }

    /**
     * Find an user with given ID.
     * 
     * @param id        User ID
     * @param request   HTTP request
     * @return          JSON response
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public String find(@PathParam("id") Long id, @Context HttpServletRequest request) {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        jsonresponse.add("id", id.toString());
        UserEntity user = entities.find(UserEntity.class, id);
        if ((user == null) || !user.getStatus().getIsActive()) {
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "User was not found.", ResponseResults.CODE_NOT_FOUND, jsonresponse.build().toString());
        }

        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        if (!users.userIsOwnerOrAdmin(sessionuser, user.getStatus())) {
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Insufficient privilege", ResponseResults.CODE_FORBIDDEN, jsonresponse.build().toString());            
        }

        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "User was found.", ResponseResults.CODE_OK, users.exportUserJSON(user, connections).build().toString());
    }

    /**
     * Get all users.
     * 
     * @param request       HTTP request
     * @return              JSON response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public String findAllUsers(@Context HttpServletRequest request) {
        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        List<UserEntity> foundusers = entities.findAll(UserEntity.class);
        JsonArrayBuilder allusers = users.exportUsersJSON(foundusers, sessionuser, connections);
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "List of users", ResponseResults.CODE_OK, allusers.build().toString());
    }

    /**
     * Get users in given range.
     * 
     * @param from          Range begin
     * @param to            Range end
     * @param request       HTTP request
     * @return              JSON response
     */
    @GET
    @Path("{from}/{to}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public String findRange(@PathParam("from") Integer from, @PathParam("to") Integer to, @Context HttpServletRequest request) {
        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        List<UserEntity> foundusers = entities.findRange(UserEntity.class, from, to);
        JsonArrayBuilder allusers = users.exportUsersJSON(foundusers, sessionuser, connections);
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "List of users", ResponseResults.CODE_OK, allusers.build().toString());
    }

    /**
     * Get the total count of users.
     * 
     * @return JSON response
     */
    @GET
    @Path("count")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public String countREST() {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        // NOTE the final user count is the count of UserEntity entries in database minus the count of users to be purged
        AppInfoEntity appinfo = appInfos.getAppInfoEntity();
        Long userpurges = appinfo.getUserCountPurge();
        jsonresponse.add("count", entities.getCount(UserEntity.class) - userpurges);
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "Count of users", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }

    /**
     * Get the link URL used in emails. If a valid account configuration file exists in app then the link is retrieved
     * from that file, otherwise the current request URL is used to create a link.
     * 
     * @param configName    If a valid account registration config was found in app then this is the config settings name
     * @param request       Used to create an URL basing on current http request if no valid configuration exists in app
     * @param defaultPage   Last part of the URL if no valid configuration exists in app
     * @return 
     */
    private String getAccRegCfgLinkURL(String configName, HttpServletRequest request, String defaultPage) {
        // first try to get the link from account registration config
        Properties props = AppConfiguration.getInstance().getAccountRegistrationConfig();
        String link = (props != null) ? props.getProperty(configName) : null;
        // need to fall back to current server url?
        if (link ==  null) {
            return AppConfiguration.getInstance().getHTMLBaseURL(request) + defaultPage;
        }
        return link;
    }

    /**
     * Get the notification mail address as configured in account registration configuration file.
     * 
     * @return Return the configured notification email address, or null if it is not configured.
     */
    private String getAccRegCfgNotificationMail() {
        Properties props = AppConfiguration.getInstance().getAccountRegistrationConfig();
        String mail = (props != null) ? props.getProperty("mail.notification") : null;
        return mail;
    }
}
