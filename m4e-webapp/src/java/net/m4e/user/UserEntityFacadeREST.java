/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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
import net.m4e.auth.AuthRole;
import net.m4e.auth.AuthorityConfig;
import net.m4e.common.EntityUtils;
import net.m4e.common.ResponseResults;
import net.m4e.common.StatusEntity;
import net.m4e.core.AppInfoEntity;
import net.m4e.core.AppInfoUtils;
import net.m4e.core.Log;

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

    @PersistenceContext(unitName = net.m4e.core.AppConfiguration.PERSITENCE_UNIT_NAME)
    private EntityManager entityManager;

    @Resource
    private UserTransaction userTransaction;

    /**
     * Construct the stateless bean.
     */
    public UserEntityFacadeREST() {
        super(UserEntity.class);
    }

    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grant={AuthRole.USER_ROLE_MODERATOR})
    public String createUser(String userJson, @Context HttpServletRequest request) {
        UserEntity sessionuser = getSessionUser(request);
        if (Objects.isNull(sessionuser)) {
            Log.error(TAG, "*** Internal error, cannot create user, no user in session found!");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to create user, no authentication.", 400, null);
        }

        UserEntity reqentity;
        try {
            reqentity = validateNewEntityInput(userJson);
        }
        catch (Exception ex) {
            Log.warning(TAG, "*** Could not create new user, validation failed, reason: " + ex.getLocalizedMessage());
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, ex.getLocalizedMessage(), 400, null);
        }

        UserEntity newuser;
        try {
            newuser = createNewUser(reqentity, sessionuser.getId());
        }
        catch (Exception ex) {
            Log.warning(TAG, "*** Could not create new user, reaon: " + ex.getLocalizedMessage());
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to create new entity.", 400, null);
        }

        //! NOTE on successful entity creation the new ID is sent back by results.data field.
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "User successfully created", 200, "" + newuser.getId());
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grant={AuthRole.USER_ROLE_MODERATOR, AuthRole.VIRT_ROLE_USER})
    public String edit(@PathParam("id") Long id, String userJson, @Context HttpServletRequest request) {
        UserEntity sessionuser = getSessionUser(request);
        if (Objects.isNull(sessionuser)) {
            Log.error(TAG, "*** Internal error, cannot update user, no user in session found!");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to update user, no authentication.", 400, null);
        }

        UserUtils utils = new UserUtils(entityManager, userTransaction);
        UserEntity reqentity = utils.importUserJSON(userJson);
        if (reqentity == null) {
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to update user, invalid input.", 400, null);
        }

        UserEntity user = super.find(id);
        if (Objects.isNull(user)) {
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to update user, invalid user ID.", 400, null);            
        }

        // check if a user is updating itself or a user with higher privilege is trying to modify a user
        boolean owner = ((UserEntity)sessionuser).getId().equals(id);
        boolean privuser = utils.checkUserRoles((UserEntity)sessionuser, Arrays.asList(AuthRole.USER_ROLE_ADMIN, AuthRole.USER_ROLE_MODERATOR));
        if (!owner && !privuser) {
            Log.warning(TAG, "*** User was attempting to update another user without proper privilege!");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to update user, insufficient privilege.", 400, null);            
        }

        // validate the roles
        List<String> allowedroles = utils.getAvailableUserRoles();
        List<String> reqentityroles = reqentity.getRolesAsString();
        for (int i = 0; i < reqentityroles.size(); i++) {
            if (!allowedroles.contains(reqentityroles.get(i))) {
                return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK,
                        "Failed to update user, unsupported role '" + reqentityroles.get(i) + "'detected.", 400, null);
            }
        }
        
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
        //! TODO check for roles, e.g. only admins can define admin role for other users
        user.setRoles(reqentity.getRoles());

        EntityUtils eutils = new EntityUtils(entityManager, userTransaction);
        try {
            eutils.updateEntity(user);
        }
        catch (Exception ex) {
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to update user.", 400, null);
        }

        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "User successfully updated", 200, null);
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grant={AuthRole.USER_ROLE_MODERATOR})
    public String remove(@PathParam("id") Long id, @Context HttpServletRequest request) {
        UserEntity sessionuser = getSessionUser(request);
        if (Objects.isNull(sessionuser)) {
            Log.error(TAG, "*** Internal error, cannot delete user, no user in session found!");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to delete user.", 400, null);
        }

        if (sessionuser.getId().equals(id)) {
            Log.warning(TAG, "*** User was attempting to delete iteself! Call a doctor.");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to delete yourself.", 400, null);            
        }

        UserEntity entity = super.find(id);
        if (entity == null) {
            Log.warning(TAG, "*** User was attempting to delete non-existing user!");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to find user for deletion", 400, null);
        }

        UserUtils utils = new UserUtils(entityManager, userTransaction);
        try {
            utils.markUserAsDeleted(entity);
        }
        catch (Exception ex) {
            Log.warning(TAG, "*** Could not mark user as deleted, reason: " + ex.getLocalizedMessage());
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to delete user.", 400, null);
        }

        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "User successfully deleted", 200, null);
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grant={AuthRole.VIRT_ROLE_USER})
    public String find(@PathParam("id") Long id) {
        UserEntity user = super.find(id);
        if (Objects.isNull(user) || user.getStatus().getIsDeleted()) {
            return null;
        }
        UserUtils utils = new UserUtils(entityManager, userTransaction);
        return utils.exportUserJSON(user).build().toString();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grant={AuthRole.VIRT_ROLE_USER})
    public String findAllUsers() {
        UserUtils utils = new UserUtils(entityManager, userTransaction);
        JsonArrayBuilder allusers = Json.createArrayBuilder();
        List<UserEntity> users = super.findAll();
        for (UserEntity user: users) {
            // users which are marked as deleted are excluded from export
            if (!user.getStatus().getIsDeleted()) {
                allusers.add(utils.exportUserJSON(user));
            }
        }
        return allusers.build().toString();
    }

    @GET
    @Path("{from}/{to}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grant={AuthRole.VIRT_ROLE_USER})
    public String findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        UserUtils utils = new UserUtils(entityManager, userTransaction);
        JsonArrayBuilder allusers = Json.createArrayBuilder();
        List<UserEntity> users = super.findRange(new int[]{from, to});
        for (UserEntity user: users) {
            if (!user.getStatus().getIsDeleted()) {
                allusers.add(utils.exportUserJSON(user));
            }
        }
        return allusers.build().toString();
    }

    @GET
    @Path("count")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grant={AuthRole.VIRT_ROLE_USER})
    public String countREST() {
        // NOTE the final user count is the count of UserEntity entries in database minus the count of users to be purged
        AppInfoUtils autils = new AppInfoUtils(entityManager, userTransaction);
        AppInfoEntity appinfo = autils.getAppInfoEntity();
        Long userpurges = appinfo.getUserCountPurge();
        return "" + (super.count() - userpurges);
    }

    /**
     * Given a HTTP request object, return the user entity set in its session.
     * 
     * @param request   HTTP request
     * @return          Return session's UserEntity, or null if no user was set in session
     */
    private UserEntity getSessionUser(HttpServletRequest request) {
       HttpSession session = request.getSession();
        Object sessionuser = session.getAttribute(AuthorityConfig.SESSION_ATTR_USER);
        if (Objects.isNull(sessionuser) || !(sessionuser instanceof UserEntity)) {
            return null;
        }
        return (UserEntity)sessionuser;
    }

   /**
     * Given a JSON string as input containing data for creating a new user, validate 
     * all fields and return an UserEntity, or throw an exception if the validation failed.
     * 
     * @param userJson       Data for creating a new user in JSON format
     * @return               A UserEntity created out of given input
     * @throws Exception     Throws an exception if the validation fails.
     */
    private UserEntity validateNewEntityInput(String userJson) throws Exception {
        UserUtils userutils = new UserUtils(entityManager, userTransaction);
        UserEntity reqentity = userutils.importUserJSON(userJson);
        if (reqentity == null) {
            throw new Exception("Failed to created user, invalid input.");
        }

        // perform user name and passwd checks
        if (Objects.isNull(reqentity.getLogin()) || reqentity.getLogin().isEmpty() ||
            Objects.isNull(reqentity.getPassword()) || reqentity.getPassword().isEmpty()) {
            throw new Exception("Missing login name or password.");
        }

        if (Objects.nonNull(userutils.findUser(reqentity.getLogin()))) {
            throw new Exception("Login name is not available.");            
        }

        // validate the roles
        List<String> allowedroles = userutils.getAvailableUserRoles();
        List<String> reqentityroles = reqentity.getRolesAsString();
        for (int i = 0; i < reqentityroles.size(); i++) {
            if (!allowedroles.contains(reqentityroles.get(i))) {
                throw new Exception("Failed to update user, unsupported role '" + reqentityroles.get(i) + "'detected.");
            }
        }
        return reqentity;
    }

    /**
     * Create a new user entity basing on data in given input entity.
     * 
     * @param inputEntity   Input data for new entity
     * @param creatorID     ID of creator
     * @return              New created entity
     * @throws Exception    Throws exception if something went wrong.
     */
    private UserEntity createNewUser(UserEntity inputEntity, Long creatorID) throws Exception {
        UserUtils userutils = new UserUtils(entityManager, userTransaction);
        // setup the new entity
        UserEntity newuser = new UserEntity();
        newuser.setLogin(inputEntity.getLogin());
        newuser.setPassword(inputEntity.getPassword());
        newuser.setName(inputEntity.getName());
        newuser.setEmail(Objects.nonNull(inputEntity.getEmail()) ? inputEntity.getEmail() : "");
        newuser.setRoles(new ArrayList<>());
        userutils.addUserRoles(newuser, inputEntity.getRolesAsString());

        // setup the status
        StatusEntity status = new StatusEntity();
        status.setIdCreator(creatorID);
        Date now = new Date();
        status.setDateCreation(now.getTime());
        status.setDateLastUpdate(now.getTime());

        try {
            userutils.createUser(newuser);
            status.setIdOwner(newuser.getId());
            newuser.setStatus(status);
            userutils.updateUserLastLogin(newuser);
        }
        catch (Exception ex) {
            throw ex;
        }
        return newuser;
    }

    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }
}
