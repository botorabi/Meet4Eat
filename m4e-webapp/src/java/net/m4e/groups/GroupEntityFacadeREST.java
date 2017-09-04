/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.groups;

import java.util.Arrays;
import java.util.Date;
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
import net.m4e.auth.AuthRole;
import net.m4e.auth.AuthorityConfig;
import net.m4e.common.ResponseResults;
import net.m4e.common.StatusEntity;
import net.m4e.core.AppInfoEntity;
import net.m4e.core.AppInfoUtils;
import net.m4e.core.Log;
import net.m4e.user.UserEntity;
import net.m4e.user.UserUtils;

/**
 * REST services for Group entity operations
 * 
 * @author boto
 * Date of creation Aug 18, 2017
 */
@Stateless
@Path("/rest/groups")
@TransactionManagement(TransactionManagementType.BEAN)
public class GroupEntityFacadeREST extends net.m4e.common.AbstractFacade<GroupEntity> {

    /**
     * Used for logging
     */
    private final static String TAG = "GroupEntityFacadeREST";

    @PersistenceContext(unitName = net.m4e.core.AppConfiguration.PERSITENCE_UNIT_NAME)
    private EntityManager entityManager;

    @Resource
    private UserTransaction userTransaction;

    public GroupEntityFacadeREST() {
        super(GroupEntity.class);
    }

    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grantRoles={AuthRole.VIRT_ENDPOINT_CHECK})
    public String createGroup(String groupJson, @Context HttpServletRequest request) {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        if (Objects.isNull(sessionuser)) {
            Log.error(TAG, "*** Internal error, cannot create group, no user in session found!");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to create group, no authentication.", ResponseResults.CODE_NOT_UNAUTHORIZED, jsonresponse.build().toString());
        }

        GroupEntity reqentity;
        try {
            reqentity = validateNewEntityInput(groupJson);
        }
        catch (Exception ex) {
            Log.warning(TAG, "*** Could not create new group, validation failed, reason: " + ex.getLocalizedMessage());
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, ex.getLocalizedMessage(), ResponseResults.CODE_BAD_REQUEST, jsonresponse.build().toString());
        }

        GroupEntity newgroup;
        try {
            newgroup = createNewGroup(reqentity, sessionuser.getId());
        }
        catch (Exception ex) {
            Log.warning(TAG, "*** Could not create new group, reaon: " + ex.getLocalizedMessage());
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to create new group.", ResponseResults.CODE_INTERNAL_SRV_ERROR, jsonresponse.build().toString());
        }

        //! NOTE on successful entity creation the new group ID is sent back by results.data field.
        jsonresponse.add("id", newgroup.getId());
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "Group was successfully created.", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grantRoles={AuthRole.VIRT_ENDPOINT_CHECK})
    public String edit(@PathParam("id") Long id, String groupJson, @Context HttpServletRequest request) {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        jsonresponse.add("id", id);
        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        if (Objects.isNull(sessionuser)) {
            Log.error(TAG, "*** Cannot update user, no user in session found!");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to update group, no authentication.", ResponseResults.CODE_NOT_UNAUTHORIZED, jsonresponse.build().toString());
        }

        GroupUtils grouputils = new GroupUtils(entityManager, userTransaction);
        GroupEntity reqentity = grouputils.importGroupJSON(groupJson);
        if (reqentity == null) {
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to update group, invalid input.", ResponseResults.CODE_BAD_REQUEST, jsonresponse.build().toString());
        }

        GroupEntity group = super.find(id);
        if (Objects.isNull(group)) {
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to find group for updating.", ResponseResults.CODE_NOT_FOUND, jsonresponse.build().toString());
        }

        // check if the group owner or a user with higher privilege is trying to modify the group
        UserUtils userutils = new UserUtils(entityManager, userTransaction);
        if (!userutils.userIsOwnerOrAdmin(sessionuser, group.getStatus())) {
            Log.warning(TAG, "*** User was attempting to update a group without proper privilege!");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to update group, insufficient privilege.", ResponseResults.CODE_FORBIDDEN, jsonresponse.build().toString());
        }
        
        // take over non-empty fields
        if (Objects.nonNull(reqentity.getName()) && !reqentity.getName().isEmpty()) {
            group.setName(reqentity.getName());
        }
        if (Objects.nonNull(reqentity.getDescription()) && !reqentity.getDescription().isEmpty()) {
            group.setDescription(reqentity.getDescription());
        }
        if (reqentity.getEventStart() > 0L) {
            group.setEventStart(reqentity.getEventStart());
        }
        if (reqentity.getEventInterval() > 0L) {
            group.setEventInterval(reqentity.getEventInterval());
        }

        try {
            grouputils.updateGroup(group);
        }
        catch (Exception ex) {
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to update group.", ResponseResults.CODE_INTERNAL_SRV_ERROR, jsonresponse.build().toString());
        }
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "Group successfully updated", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grantRoles={AuthRole.VIRT_ENDPOINT_CHECK})
    public String remove(@PathParam("id") Long id, @Context HttpServletRequest request) {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        jsonresponse.add("id", id);

        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        if (Objects.isNull(sessionuser)) {
            Log.error(TAG, "*** Cannot delete group, no user in session found!");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to delete group.", ResponseResults.CODE_NOT_UNAUTHORIZED, jsonresponse.build().toString());
        }

        GroupEntity group = super.find(id);
        if (group == null) {
            Log.warning(TAG, "*** User was attempting to delete non-existing group!");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to find user for deletion.", ResponseResults.CODE_NOT_FOUND, jsonresponse.build().toString());
        }

        // check if the group owner or a user with higher privilege is trying to remove the group
        UserUtils userutils = new UserUtils(entityManager, userTransaction);
        if (!userutils.userIsOwnerOrAdmin(sessionuser, group.getStatus())) {
            Log.warning(TAG, "*** User was attempting to remove a group without proper privilege!");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to remove group, insufficient privilege.", ResponseResults.CODE_FORBIDDEN, jsonresponse.build().toString());
        }

        GroupUtils utils = new GroupUtils(entityManager, userTransaction);
        try {
            utils.markGroupAsDeleted(group);
        }
        catch (Exception ex) {
            Log.warning(TAG, "*** Could not mark group as deleted, reason: " + ex.getLocalizedMessage());
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to delete group.", ResponseResults.CODE_INTERNAL_SRV_ERROR, jsonresponse.build().toString());
        }

        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "Group successfully deleted", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grantRoles={AuthRole.USER_ROLE_ADMIN})
    public String find(@PathParam("id") Long id) {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        GroupEntity group = super.find(id);
        if (Objects.isNull(group) || group.getStatus().getIsDeleted()) {
            jsonresponse.add("id", id);
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Group was not found.", ResponseResults.CODE_NOT_FOUND, jsonresponse.build().toString());
        }
        GroupUtils utils = new GroupUtils(entityManager, userTransaction);
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "Group was found.", ResponseResults.CODE_OK, utils.exportGroupJSON(group).build().toString());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grantRoles={AuthRole.USER_ROLE_ADMIN})
    public String findAllGroups() {
        GroupUtils utils = new GroupUtils(entityManager, userTransaction);
        JsonArrayBuilder allgroups = Json.createArrayBuilder();
        List<GroupEntity> groups = super.findAll();
        for (GroupEntity group: groups) {
            // groups which are marked as deleted are excluded from export
            if (!group.getStatus().getIsDeleted()) {
                allgroups.add(utils.exportGroupJSON(group));
            }
        }
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "List of groups", ResponseResults.CODE_OK, allgroups.build().toString());
    }

    @GET
    @Path("{from}/{to}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grantRoles={AuthRole.USER_ROLE_ADMIN})
    public String findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        GroupUtils utils = new GroupUtils(entityManager, userTransaction);
        JsonArrayBuilder allgroups = Json.createArrayBuilder();
        List<GroupEntity> groups = super.findRange(new int[]{from, to});
        for (GroupEntity group: groups) {
            if (!group.getStatus().getIsDeleted()) {
                allgroups.add(utils.exportGroupJSON(group));
            }
        }
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "List of groups", ResponseResults.CODE_OK, allgroups.build().toString());
    }

    @GET
    @Path("count")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public String countREST() {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        // NOTE the final group count is the count of GroupEntity entries in database minus the count of groups to be purged
        AppInfoUtils autils = new AppInfoUtils(entityManager, userTransaction);
        AppInfoEntity appinfo = autils.getAppInfoEntity();
        Long grouppurges = appinfo.getGroupCountPurge();
        jsonresponse.add("count", super.count() - grouppurges);
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "Count of groups", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }

    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }

   /**
     * Given a JSON string as input containing data for creating a new group, validate 
     * all fields and return an GroupEntity, or throw an exception if the validation failed.
     * 
     * @param groupJson      Data for creating a new group in JSON format
     * @return               A GroupEntity created out of given input
     * @throws Exception     Throws an exception if the validation fails.
     */
    private GroupEntity validateNewEntityInput(String groupJson) throws Exception {
        GroupUtils grouputils = new GroupUtils(entityManager, userTransaction);
        GroupEntity reqentity = grouputils.importGroupJSON(groupJson);
        if (reqentity == null) {
            throw new Exception("Failed to created group, invalid input.");
        }

        // perform some checks
        if (Objects.isNull(reqentity.getName()) || reqentity.getName().isEmpty()) {
            throw new Exception("Missing group name.");
        }

        return reqentity;
    }

    /**
     * Create a new group entity basing on data in given input entity.
     * 
     * @param inputEntity   Input data for new entity
     * @param creatorID     ID of creator
     * @return              New created entity
     * @throws Exception    Throws exception if something went wrong.
     */
    private GroupEntity createNewGroup(GroupEntity inputEntity, Long creatorID) throws Exception {
        GroupUtils grouputils = new GroupUtils(entityManager, userTransaction);
        // setup the new entity
        GroupEntity newgroup = new GroupEntity();
        newgroup.setName(inputEntity.getName());
        newgroup.setDescription(inputEntity.getDescription());
        newgroup.setEventStart(inputEntity.getEventStart());
        newgroup.setEventInterval(inputEntity.getEventInterval());

        // setup the status
        StatusEntity status = new StatusEntity();
        status.setIdCreator(creatorID);
        status.setIdOwner(creatorID);
        Date now = new Date();
        status.setDateCreation(now.getTime());
        status.setDateLastUpdate(now.getTime());
        newgroup.setStatus(status);

        try {
            grouputils.createGroup(newgroup);
        }
        catch (Exception ex) {
            throw ex;
        }
        return newgroup;
    }
}
