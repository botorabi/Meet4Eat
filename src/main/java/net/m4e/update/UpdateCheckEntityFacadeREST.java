/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.update;

import net.m4e.app.auth.AuthRole;
import net.m4e.common.Entities;
import net.m4e.common.ResponseResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.List;


/**
 * REST services for client update checks.
 * 
 * @author boto
 * Date of creation Dec 5, 2017
 */
@Stateless
@Path("/rest/update")
public class UpdateCheckEntityFacadeREST {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Entities entities;

    private final UpdateChecks updateChecks;

    private final UpdateCheckEntityInputValidator validator;

    /**
     * EJB's default constructor
     */
    protected UpdateCheckEntityFacadeREST() {
        entities = null;
        updateChecks = null;
        validator = null;
    }

    /**
     * Create the bean
     * 
     * @param entities
     * @param updateChecks
     * @param validator 
     */
    @Inject
    public UpdateCheckEntityFacadeREST(Entities entities, UpdateChecks updateChecks, UpdateCheckEntityInputValidator validator) {
        this.entities = entities;
        this.updateChecks = updateChecks;
        this.validator = validator;
    }

    /**
     * Create a new client update entry in database.
     * 
     * @param entityJson    The update check entity in JSON format
     * @return              JSON response
     */
    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.USER_ROLE_ADMIN})
    public String createUpdate(String entityJson) {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        UpdateCheckEntity entity;
        try {
            entity = validator.validateNewEntityInput(entityJson);
            entity.setReleaseDate((new Date()).getTime());
        }
        catch (Exception ex) {
            LOGGER.warn("*** Could not create new update check entity, validation failed, reason: " + ex.getLocalizedMessage());
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, ex.getLocalizedMessage(), ResponseResults.CODE_BAD_REQUEST, null);
        }

        entities.create(entity);
        jsonresponse.add("id", entity.getId().toString());
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "Update entry was successfully created.", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }

    /**
     * Update an existing client update entry in database.
     * 
     * @param id            Entity ID
     * @param entityJson    The update check entity in JSON format
     * @return              JSON response
     */
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.USER_ROLE_ADMIN})
    public String editUpdate(@PathParam("id") Long id, String entityJson) {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        // validate the entity
        UpdateCheckEntity reqentity;
        try {
            reqentity = validator.validateUpdateEntityInput(entityJson);
        }
        catch (Exception ex) {
            LOGGER.warn("*** Could not update an update check entity, validation failed, reason: " + ex.getLocalizedMessage());
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, ex.getLocalizedMessage(), ResponseResults.CODE_BAD_REQUEST, null);
        }
        UpdateCheckEntity entity = entities.find(UpdateCheckEntity.class, id);
        if (entity == null) {
            LOGGER.warn("*** Could not update an update check entity, invalid ID");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Invalid ID", ResponseResults.CODE_BAD_REQUEST, null);            
        }
        // take over the new values
        entity.setName(reqentity.getName());
        entity.setOS(reqentity.getOS());
        entity.setFlavor(reqentity.getFlavor());
        entity.setVersion(reqentity.getVersion() );
        entity.setUrl(reqentity.getUrl());
        entity.setIsActive(reqentity.getIsActive());
        // update the entry in database
        entities.update(entity);

        jsonresponse.add("id", entity.getId().toString());
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "Update entry was successfully updated.", ResponseResults.CODE_OK, jsonresponse.build().toString());
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
        UpdateCheckEntity entity = entities.find(UpdateCheckEntity.class, id);
        if (entity == null) {
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Cannot remove update entry, invalid ID.", ResponseResults.CODE_BAD_REQUEST, jsonresponse.build().toString());
        }
        entities.delete(entity);
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
        UpdateCheckEntity entity = entities.find(UpdateCheckEntity.class, id);
        if (entity == null) {
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Cannot get update entry, invalid ID.", ResponseResults.CODE_BAD_REQUEST, jsonresponse.build().toString());
        }
        JsonObjectBuilder exp = updateChecks.exportUpdateJSON(entity);
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "Update entry", ResponseResults.CODE_OK, exp.build().toString());
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
        List<UpdateCheckEntity> foundentities = entities.findAll(UpdateCheckEntity.class);
        JsonArrayBuilder exp = updateChecks.exportUpdatesJSON(foundentities);
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "Update entries", ResponseResults.CODE_OK, exp.build().toString());
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
        List<UpdateCheckEntity> foundentities =  entities.findRange(UpdateCheckEntity.class, from, to);
        JsonArrayBuilder exp = updateChecks.exportUpdatesJSON(foundentities);
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "Update entries", ResponseResults.CODE_OK, exp.build().toString());
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
        jsonresponse.add("count", String.valueOf(entities.getCount(UpdateCheckEntity.class)));
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

        JsonObjectBuilder checkresults;
        try {
            checkresults = updateChecks.checkForUpdate(clientJson);
        }
        catch (Exception ex) {
            LOGGER.warn("cannot check for update, reason: " + ex.getLocalizedMessage());
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Cannot get update info, reason: " + ex.getLocalizedMessage(), ResponseResults.CODE_BAD_REQUEST, null);            
        }
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "Count of update entries.", ResponseResults.CODE_OK, checkresults.build().toString());
    }
}
