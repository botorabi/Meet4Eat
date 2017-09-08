/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.user;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.UserTransaction;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import net.m4e.app.auth.AuthRole;
import net.m4e.app.auth.AuthorityConfig;
import net.m4e.common.EntityUtils;
import net.m4e.common.ResponseResults;
import net.m4e.system.core.AppInfoEntity;
import net.m4e.system.core.AppInfoUtils;
import net.m4e.system.core.Log;

/**
 * REST services for User entity operations
 * 
 * @author boto
 * Date of creation Aug 18, 2017
 */
@Stateless
@Path("/rest/users")
@TransactionManagement(TransactionManagementType.BEAN)
public class UserEntityFacadeREST extends net.m4e.common.AbstractFacade<UserEntity> {

    /**
     * Used for logging
     */
    private final static String TAG = "UserEntityFacadeREST";

    /**
     * Entity manager needed for entity retrieval and modifications.
     */
    @PersistenceContext(unitName = net.m4e.system.core.AppConfiguration.PERSITENCE_UNIT_NAME)
    private EntityManager entityManager;

    /**
     * User transaction needed for entity modifications.
     */
    @Resource
    private UserTransaction userTransaction;

    /**
     * Create the user entity REST facade.
     */
    public UserEntityFacadeREST() {
        super(UserEntity.class);
    }

    /**
     * Get the entity manager.
     * 
     * @return   Entity manager
     */
    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
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
        if (Objects.isNull(sessionuser)) {
            Log.error(TAG, "*** Internal error, cannot create user, no user in session found!");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to create user, no authentication.", ResponseResults.CODE_NOT_UNAUTHORIZED, jsonresponse.build().toString());
        }

        UserUtils userutils = new UserUtils(entityManager, userTransaction);
        UserEntity reqentity;
        try {
            UserEntityInputValidator validator = new UserEntityInputValidator(entityManager, userTransaction);
            reqentity = validator.validateNewEntityInput(userJson);
        }
        catch (Exception ex) {
            Log.warning(TAG, "*** Could not create new user, validation failed, reason: " + ex.getLocalizedMessage());
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, ex.getLocalizedMessage(), ResponseResults.CODE_BAD_REQUEST, jsonresponse.build().toString());
        }

        // validate and adapt requested user roles
        reqentity.setRoles(userutils.adaptRequestedRoles(sessionuser, reqentity.getRoles()));

        UserEntity newuser;
        try {
            newuser = userutils.createNewUser(reqentity, sessionuser.getId());
        }
        catch (Exception ex) {
            Log.warning(TAG, "*** Could not create new user, reaon: " + ex.getLocalizedMessage());
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to create new user.", ResponseResults.CODE_INTERNAL_SRV_ERROR, jsonresponse.build().toString());
        }

        //! NOTE on successful entity creation the new ID is sent back by results.data field.
        jsonresponse.add("id", newuser.getId());
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "User was successfully created.", ResponseResults.CODE_OK, jsonresponse.build().toString());
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
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.USER_ROLE_MODERATOR, AuthRole.VIRT_ROLE_USER})
    public String edit(@PathParam("id") Long id, String userJson, @Context HttpServletRequest request) {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        jsonresponse.add("id", id);
        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        if (Objects.isNull(sessionuser)) {
            Log.error(TAG, "*** Cannot update user, no user in session found!");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to update user, no authentication.",
                                             ResponseResults.CODE_NOT_UNAUTHORIZED, jsonresponse.build().toString());
        }

        UserUtils  userutils = new UserUtils(entityManager, userTransaction);
        UserEntity reqentity;
        try {
            UserEntityInputValidator validator = new UserEntityInputValidator(entityManager, userTransaction);
            reqentity = validator.validateUpdateEntityInput(userJson);
        }
        catch(Exception ex) {
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to update user, invalid input. Reason: " + 
                                             ex.getLocalizedMessage(), ResponseResults.CODE_BAD_REQUEST, jsonresponse.build().toString());
        }

        UserEntity user = super.find(id);
        if (Objects.isNull(user) || user.getStatus().getIsDeleted()) {
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to find user for updating.",
                                             ResponseResults.CODE_NOT_FOUND, jsonresponse.build().toString());
        }

        // check if a user is updating itself or a user with higher privilege is trying to modify a user
        if (!userutils.userIsOwnerOrAdmin(sessionuser, user.getStatus())) {
            Log.warning(TAG, "*** User was attempting to update another user without proper privilege!");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to update user, insufficient privilege.",
                                             ResponseResults.CODE_FORBIDDEN, jsonresponse.build().toString());
        }

        // validate the requested roles, check for roles, e.g. only admins can define admin role for other users
        user.setRoles(userutils.adaptRequestedRoles(sessionuser, reqentity.getRoles()));

        // take over non-empty fields
        if (Objects.nonNull(reqentity.getName()) && !reqentity.getName().isEmpty()) {
            user.setName(reqentity.getName());
        }
        if (Objects.nonNull(reqentity.getEmail()) && !reqentity.getEmail().isEmpty()) {
            user.setEmail(reqentity.getEmail());
        }
        if (Objects.nonNull(reqentity.getPassword()) && !reqentity.getPassword().isEmpty()) {
            user.setPassword(reqentity.getPassword());
        }

        try {
            userutils.updateUser(user);
        }
        catch (Exception ex) {
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to update user.",
                                             ResponseResults.CODE_INTERNAL_SRV_ERROR, jsonresponse.build().toString());
        }
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "User successfully updated",
                                         ResponseResults.CODE_OK, jsonresponse.build().toString());
    }

    /**
     * Delete an user with given ID. The event will be marked as deleted, so it can be
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
        jsonresponse.add("id", id);

        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        if (Objects.isNull(sessionuser)) {
            Log.error(TAG, "*** Cannot delete user, no user in session found!");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to delete user.", ResponseResults.CODE_NOT_UNAUTHORIZED, jsonresponse.build().toString());
        }

        if (sessionuser.getId().equals(id)) {
            Log.warning(TAG, "*** User was attempting to delete iteself! Call a doctor.");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to delete yourself.", ResponseResults.CODE_NOT_ACCEPTABLE, jsonresponse.build().toString());            
        }

        UserEntity user = super.find(id);
        if (Objects.isNull(user) || user.getStatus().getIsDeleted()) {
            Log.warning(TAG, "*** User was attempting to delete non-existing user!");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to find user for deletion.", ResponseResults.CODE_NOT_FOUND, jsonresponse.build().toString());
        }

        UserUtils utils = new UserUtils(entityManager, userTransaction);
        try {
            utils.markUserAsDeleted(user);
        }
        catch (Exception ex) {
            Log.warning(TAG, "*** Could not mark user as deleted, reason: " + ex.getLocalizedMessage());
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to delete user.", ResponseResults.CODE_INTERNAL_SRV_ERROR, jsonresponse.build().toString());
        }

        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "User successfully deleted", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }

    /**
     * Search for users containing given keyword in their name.
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
        if (Objects.isNull(keyword)) {
           return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "Search results", ResponseResults.CODE_OK, results.build().toString());        
        }

        EntityUtils utils = new EntityUtils(entityManager, userTransaction);
        List<UserEntity> hits = utils.search(UserEntity.class, keyword, Arrays.asList("name"), 20);
        for (UserEntity hit: hits) {
            if (hit.getStatus().getIsDeleted()) {
                continue;
            }
            JsonObjectBuilder json = Json.createObjectBuilder();
            json.add("id", hit.getId());
            json.add("name", Objects.nonNull(hit.getName()) ? hit.getName() : "???");
            results.add(json);
        }
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "Search results", ResponseResults.CODE_OK, results.build().toString());
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
        jsonresponse.add("id", id);
        UserEntity user = super.find(id);
        if (Objects.isNull(user) || user.getStatus().getIsDeleted()) {
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "User was not found.", ResponseResults.CODE_NOT_FOUND, jsonresponse.build().toString());
        }

        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        UserUtils utils = new UserUtils(entityManager, userTransaction);
        if (!utils.userIsOwnerOrAdmin(sessionuser, user.getStatus())) {
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Insufficient privilege", ResponseResults.CODE_FORBIDDEN, jsonresponse.build().toString());            
        }
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "User was found.", ResponseResults.CODE_OK, utils.exportUserJSON(user).build().toString());
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
        UserUtils  utils       = new UserUtils(entityManager, userTransaction);
        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        List<UserEntity> users = super.findAll();

        JsonArrayBuilder allusers = utils.exportUsersJSON(users, sessionuser);
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "List of users", ResponseResults.CODE_OK, allusers.build().toString());
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
        UserUtils utils        = new UserUtils(entityManager, userTransaction);
        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        List<UserEntity> users = super.findRange(new int[]{from, to});

        JsonArrayBuilder allusers = utils.exportUsersJSON(users, sessionuser);
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "List of users", ResponseResults.CODE_OK, allusers.build().toString());
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
        AppInfoUtils autils = new AppInfoUtils(entityManager, userTransaction);
        AppInfoEntity appinfo = autils.getAppInfoEntity();
        Long userpurges = appinfo.getUserCountPurge();
        jsonresponse.add("count", super.count() - userpurges);
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "Count of users", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }
}
