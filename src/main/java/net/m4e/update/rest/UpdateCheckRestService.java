/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.update.rest;

import io.swagger.annotations.*;
import net.m4e.app.auth.AuthRole;
import net.m4e.common.*;
import net.m4e.update.business.*;
import net.m4e.update.rest.comm.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.*;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.lang.invoke.MethodHandles;
import java.util.*;


/**
 * REST services for client update checks.
 * 
 * @author boto
 * Date of creation Dec 5, 2017
 */
@Stateless
@Path("/rest/update")
@Api(value = "Service for client update checks")
public class UpdateCheckRestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Entities entities;

    private final UpdateChecks updateChecks;

    private final UpdateCheckValidator validator;

    /**
     * The default constructor may be needed by the EJB container.
     */
    protected UpdateCheckRestService() {
        this.entities = null;
        this.updateChecks = null;
        this.validator = null;
    }

    /**
     * Create the bean
     */
    @Inject
    public UpdateCheckRestService(@NotNull Entities entities,
                                  @NotNull UpdateChecks updateChecks,
                                  @NotNull UpdateCheckValidator validator) {
        this.entities = entities;
        this.updateChecks = updateChecks;
        this.validator = validator;
    }

    /**
     * Create a new client update entry in database.
     */
    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.USER_ROLE_ADMIN})
    @ApiOperation(value = "Create a new update check entry")
    public GenericResponseResult<UpdateCheckId> createUpdate(UpdateCheckEntity inputEntity) {
        try {
            validator.validateNewEntityInput(inputEntity);
            inputEntity.setReleaseDate((new Date()).getTime());
        }
        catch (Exception ex) {
            LOGGER.warn("*** Could not create new update check entity, validation failed, reason: {}", ex.getMessage());
            return GenericResponseResult.badRequest(ex.getMessage());
        }
        inputEntity.setId(null);
        entities.create(inputEntity);
        return GenericResponseResult.ok("Update entry was successfully created.", new UpdateCheckId(inputEntity.getId().toString()));
    }

    /**
     * Update an existing client update entry in database.
     */
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.USER_ROLE_ADMIN})
    @ApiOperation(value = "Update an existing update check entry")
    public GenericResponseResult<UpdateCheckId> editUpdate(@PathParam("id") Long id, UpdateCheckEntity inputEntity) {
        try {
            validator.validateUpdateEntityInput(inputEntity);
        }
        catch (Exception ex) {
            LOGGER.warn("*** Could not update an update check entity, validation failed, reason: {}", ex.getMessage());
            return GenericResponseResult.badRequest(ex.getMessage());
        }
        UpdateCheckEntity updateCheckEntity = entities.find(UpdateCheckEntity.class, id);
        if (updateCheckEntity == null) {
            LOGGER.warn("*** Could not update an update check entity, invalid ID");
            return GenericResponseResult.notFound("Invalid ID");
        }

        updateCheckEntry(inputEntity, updateCheckEntity);

        return GenericResponseResult.ok("Update entry was successfully updated.", new UpdateCheckId(updateCheckEntity.getId().toString()));
    }

    private void updateCheckEntry(UpdateCheckEntity inputEntity, UpdateCheckEntity updateCheckEntity) {
        if (inputEntity.getName() != null) {
            updateCheckEntity.setName(inputEntity.getName());
        }
        if (inputEntity.getOs() != null) {
            updateCheckEntity.setOs(inputEntity.getOs());
        }
        if (inputEntity.getFlavor() != null) {
            updateCheckEntity.setFlavor(inputEntity.getFlavor());
        }
        if (inputEntity.getVersion() != null) {
            updateCheckEntity.setVersion(inputEntity.getVersion());
        }
        if (inputEntity.getUrl() != null) {
            updateCheckEntity.setUrl(inputEntity.getUrl());
        }
        updateCheckEntity.setActive(inputEntity.isActive());

        entities.update(updateCheckEntity);
    }

    /**
     * Remove an update entry with given ID from database.
     */
    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.USER_ROLE_ADMIN})
    @ApiOperation(value = "Remove an existing update check entry")
    public GenericResponseResult<UpdateCheckId> remove(@PathParam("id") Long id) {
        UpdateCheckEntity entity = entities.find(UpdateCheckEntity.class, id);
        if (entity == null) {
            return GenericResponseResult.notFound("Cannot remove update entry, invalid ID.", new UpdateCheckId(id.toString()));
        }
        entities.delete(entity);
        return GenericResponseResult.ok("Update entry was successfully removed.", new UpdateCheckId(id.toString()));
    }

    /**
     * Find an update entry by its ID.
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.USER_ROLE_ADMIN})
    @ApiOperation(value = "Find an update check entry")
    public GenericResponseResult<UpdateCheckEntity> find(@PathParam("id") Long id) {
        UpdateCheckEntity updateCheckEntity = entities.find(UpdateCheckEntity.class, id);
        if (updateCheckEntity == null) {
            return GenericResponseResult.notFound("Cannot get update entry, invalid ID.");
        }
        return GenericResponseResult.ok("Update entry", updateCheckEntity);
    }

    /**
     * Get all available update entries
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.USER_ROLE_ADMIN})
    @ApiOperation(value = "Get all update check entries")
    public GenericResponseResult<List<UpdateCheckEntity>> findAllUpdates() {
        List<UpdateCheckEntity> foundEntities = entities.findAll(UpdateCheckEntity.class);
        return GenericResponseResult.ok("Update entries", foundEntities);
    }

    /**
     * Get update entries in given range.
     */
    @GET
    @Path("{from}/{to}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.USER_ROLE_ADMIN})
    @ApiOperation(value = "Get all update check entries in given range")
    public GenericResponseResult<List<UpdateCheckEntity>> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        List<UpdateCheckEntity> foundEntities =  entities.findRange(UpdateCheckEntity.class, from, to);
        return GenericResponseResult.ok("Update entries", foundEntities);
    }

    /**
     * Get the count of update entries in database.
     */
    @GET
    @Path("count")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.USER_ROLE_ADMIN})
    @ApiOperation(value = "Get total count of update check entries")
    public GenericResponseResult<UpdateCheckCount> count() {
        UpdateCheckCount response = new UpdateCheckCount(entities.getCount(UpdateCheckEntity.class));
        return GenericResponseResult.ok("Count of update entries.", response);
    }

    /**
     * Given the specs of a requesting client, return the availability info of an update.
     */
    @POST
    @Path("check")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_GUEST, AuthRole.VIRT_ROLE_USER})
    @ApiOperation(value = "Check if there is an update for given client")
    public GenericResponseResult<UpdateCheckResult> checkForUpdate(UpdateCheckCmd updateCheckCmd) {
        UpdateCheckResult checkResults;
        try {
            checkResults = updateChecks.checkForUpdate(updateCheckCmd);
        }
        catch (Exception ex) {
            LOGGER.warn("cannot check for update, reason: {}", ex.getMessage());
            return GenericResponseResult.badRequest("Cannot get update info, reason: " + ex.getMessage());
        }

        return GenericResponseResult.ok("Update information.", checkResults);
    }
}
