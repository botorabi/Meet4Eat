/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.update;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import net.m4e.app.auth.AuthRole;
import net.m4e.common.ResponseResults;
import net.m4e.system.core.Log;


/**
 * REST services for client update checks.
 * 
 * @author boto
 * Date of creation Dec 5, 2017
 */
@Stateless
@Path("/rest/update")
public class UpdateCheckEntityFacadeREST extends net.m4e.common.AbstractFacade<UpdateCheckEntity> {

    /**
     * Used for logging
     */
    private final static String TAG = "UpdateCheckEntityFacadeREST";

    /**
     * Entity manager needed for entity retrieval and modifications.
     */
    @PersistenceContext(unitName = net.m4e.system.core.AppConfiguration.PERSITENCE_UNIT_NAME)
    private EntityManager entityManager;

    public UpdateCheckEntityFacadeREST() {
        super(UpdateCheckEntity.class);
    }

    /**
     * Create a new update entry in database.
     * 
     * @param entity    The entity to create
     * @return          JSON response
     */
    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.USER_ROLE_ADMIN})
    public String createUpdate(UpdateCheckEntity entity) {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        super.create(entity);
        jsonresponse.add("id", entity.getId().toString());
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "Update entry was successfully created.", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }

    /**
     * Remove an update entry from database.
     * 
     * @param id    ID of the update entry
     * @return      JSON response
     */
    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.USER_ROLE_ADMIN})
    public String remove(@PathParam("id") Long id) {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        if (id == null) {
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Cannot remove update entry, no ID given.", ResponseResults.CODE_BAD_REQUEST, jsonresponse.build().toString());            
        }

        jsonresponse.add("id", id.toString());
        UpdateCheckEntity entity = super.find(id);
        if (entity == null) {
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Cannot remove update entry, invalid ID.", ResponseResults.CODE_BAD_REQUEST, jsonresponse.build().toString());
        }
        super.remove(entity);
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "Update entry was successfully removed.", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }

    /**
     * Find an update entry by its ID.
     * 
     * @param id  Update entry's ID
     * @return    JSON Response
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.USER_ROLE_ADMIN})
    public String find(@PathParam("id") Long id) {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        if (id == null) {
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Cannot get update entry, no ID given.", ResponseResults.CODE_BAD_REQUEST, jsonresponse.build().toString());            
        }

        jsonresponse.add("id", id.toString());
        UpdateCheckEntity entity = super.find(id);
        if (entity == null) {
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Cannot get update entry, invalid ID.", ResponseResults.CODE_BAD_REQUEST, jsonresponse.build().toString());
        }
        UpdateChecks checks = new UpdateChecks(entityManager);
        JsonObjectBuilder exp = checks.exportUpdateJSON(entity);
        return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Update entry", ResponseResults.CODE_OK, exp.build().toString());
    }

    /**
     * Get all available update entries
     * 
     * @return  JSON result containing an array with all entities
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.USER_ROLE_ADMIN})
    public String findAllUpdates() {
        List<UpdateCheckEntity> entities = super.findAll();
        UpdateChecks checks = new UpdateChecks(entityManager);
        JsonArrayBuilder exp = checks.exportUpdatesJSON(entities);
        return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Update entries", ResponseResults.CODE_OK, exp.build().toString());
    }

    /**
     * Get update entries in given range.
     * 
     * @param from  Range begin
     * @param to    Range end
     * @return      JSON result containing an array with entities
     */
    @GET
    @Path("{from}/{to}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.USER_ROLE_ADMIN})
    public String findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        List<UpdateCheckEntity> entities =  super.findRange(new int[]{from, to});
        UpdateChecks checks = new UpdateChecks(entityManager);
        JsonArrayBuilder exp = checks.exportUpdatesJSON(entities);
        return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Update entries", ResponseResults.CODE_OK, exp.build().toString());
    }

    /**
     * Get the count of update entries in database.
     * 
     * @return      JSON response containing the count
     */
    @GET
    @Path("count")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.USER_ROLE_ADMIN})
    public String countREST() {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        jsonresponse.add("count", String.valueOf(super.count()));
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "Count of update entries.", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }

    /**
     * Given the specs of a requester return the availability info of an update.
     * 
     * @param clientJson    The client specs
     * @return              JSON response containing the results of update check
     */
    @POST
    @Path("check")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_GUEST, AuthRole.VIRT_ROLE_USER})
    public String checkForUpdate(String clientJson) {
        if (clientJson == null) {
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Cannot get update info, no valid client info.", ResponseResults.CODE_BAD_REQUEST, null);
        }

        UpdateChecks checks = new UpdateChecks(entityManager);
        JsonObjectBuilder checkresults;
        try {
            checkresults = checks.checkForUpdate(clientJson);
        }
        catch (Exception ex) {
            Log.warning(TAG, "cannot check for update, reason: " + ex.getLocalizedMessage());
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Cannot get update info, reason: " + ex.getLocalizedMessage(), ResponseResults.CODE_BAD_REQUEST, null);            
        }
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "Count of update entries.", ResponseResults.CODE_OK, checkresults.build().toString());
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
}
