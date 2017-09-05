/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.event;

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
 * REST services for Event entity operations
 * 
 * @author boto
 * Date of creation Aug 18, 2017
 */
@Stateless
@Path("/rest/events")
@TransactionManagement(TransactionManagementType.BEAN)
public class EventEntityFacadeREST extends net.m4e.common.AbstractFacade<EventEntity> {

    /**
     * Used for logging
     */
    private final static String TAG = "EventEntityFacadeREST";

    @PersistenceContext(unitName = net.m4e.core.AppConfiguration.PERSITENCE_UNIT_NAME)
    private EntityManager entityManager;

    @Resource
    private UserTransaction userTransaction;

    public EventEntityFacadeREST() {
        super(EventEntity.class);
    }

    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grantRoles={AuthRole.VIRT_ENDPOINT_CHECK})
    public String createEvent(String eventJson, @Context HttpServletRequest request) {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        if (Objects.isNull(sessionuser)) {
            Log.error(TAG, "*** Internal error, cannot create event, no user in session found!");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to create event, no authentication.", ResponseResults.CODE_NOT_UNAUTHORIZED, jsonresponse.build().toString());
        }

        EventEntity reqentity;
        try {
            reqentity = validateNewEntityInput(eventJson);
        }
        catch (Exception ex) {
            Log.warning(TAG, "*** Could not create new event, validation failed, reason: " + ex.getLocalizedMessage());
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, ex.getLocalizedMessage(), ResponseResults.CODE_BAD_REQUEST, jsonresponse.build().toString());
        }

        EventEntity newevent;
        try {
            newevent = createNewEvent(reqentity, sessionuser.getId());
        }
        catch (Exception ex) {
            Log.warning(TAG, "*** Could not create new event, reaon: " + ex.getLocalizedMessage());
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to create new event.", ResponseResults.CODE_INTERNAL_SRV_ERROR, jsonresponse.build().toString());
        }

        //! NOTE on successful entity creation the new event ID is sent back by results.data field.
        jsonresponse.add("id", newevent.getId());
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "Event was successfully created.", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grantRoles={AuthRole.VIRT_ENDPOINT_CHECK})
    public String edit(@PathParam("id") Long id, String eventJson, @Context HttpServletRequest request) {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        jsonresponse.add("id", id);
        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        if (Objects.isNull(sessionuser)) {
            Log.error(TAG, "*** Cannot update user, no user in session found!");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to update event, no authentication.", ResponseResults.CODE_NOT_UNAUTHORIZED, jsonresponse.build().toString());
        }

        EventUtils eventutils = new EventUtils(entityManager, userTransaction);
        EventEntity reqentity = eventutils.importEventJSON(eventJson);
        if (reqentity == null) {
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to update event, invalid input.", ResponseResults.CODE_BAD_REQUEST, jsonresponse.build().toString());
        }

        EventEntity event = super.find(id);
        if (Objects.isNull(event)) {
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to find event for updating.", ResponseResults.CODE_NOT_FOUND, jsonresponse.build().toString());
        }

        // check if the event owner or a user with higher privilege is trying to modify the event
        UserUtils userutils = new UserUtils(entityManager, userTransaction);
        if (!userutils.userIsOwnerOrAdmin(sessionuser, event.getStatus())) {
            Log.warning(TAG, "*** User was attempting to update an event without proper privilege!");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to update event, insufficient privilege.", ResponseResults.CODE_FORBIDDEN, jsonresponse.build().toString());
        }
        
        // take over non-empty fields
        if (Objects.nonNull(reqentity.getName()) && !reqentity.getName().isEmpty()) {
            event.setName(reqentity.getName());
        }
        if (Objects.nonNull(reqentity.getDescription()) && !reqentity.getDescription().isEmpty()) {
            event.setDescription(reqentity.getDescription());
        }
        if (reqentity.getEventStart() > 0L) {
            event.setEventStart(reqentity.getEventStart());
        }
        event.setRepeatWeekDays(reqentity.getRepeatWeekDays());
        event.setRepeatDayTime(reqentity.getRepeatDayTime());

        try {
            eventutils.updateEvent(event);
        }
        catch (Exception ex) {
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to update event.", ResponseResults.CODE_INTERNAL_SRV_ERROR, jsonresponse.build().toString());
        }
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "Event successfully updated", ResponseResults.CODE_OK, jsonresponse.build().toString());
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
            Log.error(TAG, "*** Cannot delete event, no user in session found!");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to delete event.", ResponseResults.CODE_NOT_UNAUTHORIZED, jsonresponse.build().toString());
        }

        EventEntity event = super.find(id);
        if (event == null) {
            Log.warning(TAG, "*** User was attempting to delete non-existing event!");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to find user for deletion.", ResponseResults.CODE_NOT_FOUND, jsonresponse.build().toString());
        }

        // check if the event owner or a user with higher privilege is trying to remove the event
        UserUtils userutils = new UserUtils(entityManager, userTransaction);
        if (!userutils.userIsOwnerOrAdmin(sessionuser, event.getStatus())) {
            Log.warning(TAG, "*** User was attempting to remove an event without proper privilege!");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to remove event, insufficient privilege.", ResponseResults.CODE_FORBIDDEN, jsonresponse.build().toString());
        }

        EventUtils utils = new EventUtils(entityManager, userTransaction);
        try {
            utils.markEventAsDeleted(event);
        }
        catch (Exception ex) {
            Log.warning(TAG, "*** Could not mark event as deleted, reason: " + ex.getLocalizedMessage());
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to delete event.", ResponseResults.CODE_INTERNAL_SRV_ERROR, jsonresponse.build().toString());
        }

        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "Event successfully deleted", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grantRoles={AuthRole.USER_ROLE_ADMIN})
    public String find(@PathParam("id") Long id) {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        EventEntity event = super.find(id);
        if (Objects.isNull(event) || event.getStatus().getIsDeleted()) {
            jsonresponse.add("id", id);
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Event was not found.", ResponseResults.CODE_NOT_FOUND, jsonresponse.build().toString());
        }
        EventUtils utils = new EventUtils(entityManager, userTransaction);
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "Event was found.", ResponseResults.CODE_OK, utils.exportEventJSON(event).build().toString());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grantRoles={AuthRole.USER_ROLE_ADMIN})
    public String findAllEvents() {
        EventUtils utils = new EventUtils(entityManager, userTransaction);
        JsonArrayBuilder allevents = Json.createArrayBuilder();
        List<EventEntity> events = super.findAll();
        for (EventEntity event: events) {
            // events which are marked as deleted are excluded from export
            if (!event.getStatus().getIsDeleted()) {
                allevents.add(utils.exportEventJSON(event));
            }
        }
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "List of events", ResponseResults.CODE_OK, allevents.build().toString());
    }

    @GET
    @Path("{from}/{to}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grantRoles={AuthRole.USER_ROLE_ADMIN})
    public String findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        EventUtils utils = new EventUtils(entityManager, userTransaction);
        JsonArrayBuilder allevents = Json.createArrayBuilder();
        List<EventEntity> events = super.findRange(new int[]{from, to});
        for (EventEntity event: events) {
            if (!event.getStatus().getIsDeleted()) {
                allevents.add(utils.exportEventJSON(event));
            }
        }
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "List of events", ResponseResults.CODE_OK, allevents.build().toString());
    }

    @GET
    @Path("count")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public String countREST() {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        // NOTE the final event count is the count of EventEntity entries in database minus the count of events to be purged
        AppInfoUtils autils = new AppInfoUtils(entityManager, userTransaction);
        AppInfoEntity appinfo = autils.getAppInfoEntity();
        Long eventpurges = appinfo.getEventCountPurge();
        jsonresponse.add("count", super.count() - eventpurges);
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "Count of events", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }

    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }

   /**
     * Given a JSON string as input containing data for creating a new event, validate 
     * all fields and return an EventEntity, or throw an exception if the validation failed.
     * 
     * @param eventJson      Data for creating a new event in JSON format
     * @return               A EventEntity created out of given input
     * @throws Exception     Throws an exception if the validation fails.
     */
    private EventEntity validateNewEntityInput(String eventJson) throws Exception {
        EventUtils eventutils = new EventUtils(entityManager, userTransaction);
        EventEntity reqentity = eventutils.importEventJSON(eventJson);
        if (reqentity == null) {
            throw new Exception("Failed to created event, invalid input.");
        }

        // perform some checks
        if (Objects.isNull(reqentity.getName()) || reqentity.getName().isEmpty()) {
            throw new Exception("Missing event name.");
        }

        return reqentity;
    }

    /**
     * Create a new event entity basing on data in given input entity.
     * 
     * @param inputEntity   Input data for new entity
     * @param creatorID     ID of creator
     * @return              New created entity
     * @throws Exception    Throws exception if something went wrong.
     */
    private EventEntity createNewEvent(EventEntity inputEntity, Long creatorID) throws Exception {
        EventUtils eventutils = new EventUtils(entityManager, userTransaction);
        // setup the new entity
        EventEntity newevent = new EventEntity();
        newevent.setName(inputEntity.getName());
        newevent.setDescription(inputEntity.getDescription());
        newevent.setEventStart(inputEntity.getEventStart());
        newevent.setRepeatWeekDays(inputEntity.getRepeatWeekDays());
        newevent.setRepeatDayTime(inputEntity.getRepeatDayTime());

        // setup the status
        StatusEntity status = new StatusEntity();
        status.setIdCreator(creatorID);
        status.setIdOwner(creatorID);
        Date now = new Date();
        status.setDateCreation(now.getTime());
        status.setDateLastUpdate(now.getTime());
        newevent.setStatus(status);

        try {
            eventutils.createEvent(newevent);
        }
        catch (Exception ex) {
            throw ex;
        }
        return newevent;
    }
}
