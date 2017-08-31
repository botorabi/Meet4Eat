/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.groups;

import java.util.List;
import java.util.Objects;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.json.Json;
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

        //! NOTE on successful entity creation the new ID is sent back by results.data field.
        jsonresponse.add("id", newgroup.getId());
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "Group was successfully created.", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grantRoles={AuthRole.VIRT_ENDPOINT_CHECK})
    public String edit(@PathParam("id") Long id, GroupEntity entity) {
        if (entity == null) {
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to update group", 400, null);
        }
        super.edit(entity);
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "Group successfully updated", 200, null);
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grantRoles={AuthRole.VIRT_ENDPOINT_CHECK})
    public String remove(@PathParam("id") Long id) {
        GroupEntity entity = super.find(id);
        if (entity == null) {
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to find group for deletion", 400, null);
        }
        super.remove(entity);
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "Group successfully deleted", 200, null);
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public GroupEntity find(@PathParam("id") Long id) {
        return super.find(id);
    }

    @GET
    @Override
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public List<GroupEntity> findAll() {
        return super.findAll();
    }

    @GET
    @Path("{from}/{to}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public List<GroupEntity> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        return super.findRange(new int[]{from, to});
    }

    @GET
    @Path("count")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public String countREST() {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        // NOTE the final user count is the count of GroupEntity entries in database minus the count of groups to be purged
        AppInfoUtils autils = new AppInfoUtils(entityManager, userTransaction);
        AppInfoEntity appinfo = autils.getAppInfoEntity();
        Long userpurges = appinfo.getGroupCountPurge();
        jsonresponse.add("count", super.count() - userpurges);
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
     * @return               A UserEntity created out of given input
     * @throws Exception     Throws an exception if the validation fails.
     */
    private GroupEntity validateNewEntityInput(String groupJson) throws Exception {
        //! TODO
        /*
        GroupUtils grouputils = new GroupUtils(entityManager, userTransaction);
        GroupEntity reqentity = grouputils.importUserJSON(groupJson);
        if (reqentity == null) {
            throw new Exception("Failed to created user, invalid input.");
        }

        // perform some checks
        if (Objects.isNull(reqentity.getName()) || reqentity.getName().isEmpty()) {
            throw new Exception("Missing group name.");
        }

        return reqentity;
        */
        return null;
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
        //! TODO
        return null;
    }
}
