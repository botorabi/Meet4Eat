/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.event;

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
import net.m4e.common.ResponseResults;
import net.m4e.system.core.AppInfoEntity;
import net.m4e.system.core.AppInfoUtils;
import net.m4e.system.core.Log;
import net.m4e.app.user.UserEntity;
import net.m4e.app.user.UserUtils;
import net.m4e.common.EntityUtils;

/**
 * REST services for Event entity operations
 * The results of operations depend on the privileges of authenticated user.
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
     * Create the event entity REST facade.
     */
    public EventEntityFacadeREST() {
        super(EventEntity.class);
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
     * Create a new event.
     * 
     * @param eventJson  Event details in JSON format
     * @param request    HTTP request
     * @return           JSON response
     */
    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public String createEvent(String eventJson, @Context HttpServletRequest request) {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        EventUtils eventutils = new EventUtils(entityManager, userTransaction);
        EventEntity reqentity;
        try {
            reqentity = eventutils.validateNewEntityInput(eventJson);
        }
        catch (Exception ex) {
            Log.warning(TAG, "*** Could not create new event, validation failed, reason: " + ex.getLocalizedMessage());
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, ex.getLocalizedMessage(), ResponseResults.CODE_BAD_REQUEST, jsonresponse.build().toString());
        }

        EventEntity newevent;
        try {
            newevent = eventutils.createNewEvent(reqentity, sessionuser.getId());
        }
        catch (Exception ex) {
            Log.warning(TAG, "*** Could not create new event, reaon: " + ex.getLocalizedMessage());
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to create new event.", ResponseResults.CODE_INTERNAL_SRV_ERROR, jsonresponse.build().toString());
        }

        //! NOTE on successful entity creation the new event ID is sent back by results.data field.
        jsonresponse.add("id", newevent.getId());
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "Event was successfully created.", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }

    /**
     * Modify the event with given ID.
     * 
     * @param id        Event ID
     * @param eventJson Entity modifications in JSON format
     * @param request   HTTP request
     * @return          JSON response
     */
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public String edit(@PathParam("id") Long id, String eventJson, @Context HttpServletRequest request) {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        jsonresponse.add("id", id);
        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        EventUtils eventutils = new EventUtils(entityManager, userTransaction);
        EventEntity reqentity = eventutils.importEventJSON(eventJson);
        if (Objects.isNull(reqentity)) {
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
        event.setIsPublic(reqentity.getIsPublic());
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

    /**
     * Delete an event with given ID. The event will be marked as deleted, so it can be
     * purged later.
     * 
     * @param id        Event ID
     * @param request   HTTP request
     * @return          JSON response
     */
    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public String remove(@PathParam("id") Long id, @Context HttpServletRequest request) {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        EventEntity event = super.find(id);
        jsonresponse.add("id", id);
        if (Objects.isNull(event)) {
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

    /**
     * Find an event with given ID.
     * 
     * @param id        Event ID
     * @param request   HTTP request
     * @return          JSON response
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public String find(@PathParam("id") Long id, @Context HttpServletRequest request) {
        EventEntity event = super.find(id);
        if (Objects.isNull(event) || event.getStatus().getIsDeleted()) {
            JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
            jsonresponse.add("id", id);
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Event was not found.", ResponseResults.CODE_NOT_FOUND, jsonresponse.build().toString());
        }

        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        EventUtils eventutils = new EventUtils(entityManager, userTransaction);
        JsonObjectBuilder exportedevent = eventutils.exportUserEventJSON(event, sessionuser);
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "Event was found.", ResponseResults.CODE_OK, exportedevent.build().toString());
    }

    /**
     * Get all events.
     * 
     * @param request       HTTP request
     * @return              JSON response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public String findAllEvents(@Context HttpServletRequest request) {
        List<EventEntity> events = super.findAll();
        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        EventUtils eventutils = new EventUtils(entityManager, userTransaction);
        JsonArrayBuilder exportedevents = eventutils.exportUserEventsJSON(events, sessionuser);
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "List of events", ResponseResults.CODE_OK, exportedevents.build().toString());
    }

    /**
     * Get events in given range.
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
        List<EventEntity> events = super.findRange(new int[]{from, to});
        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        EventUtils eventutils = new EventUtils(entityManager, userTransaction);
        JsonArrayBuilder exportedevents = eventutils.exportUserEventsJSON(events, sessionuser);
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "List of events", ResponseResults.CODE_OK, exportedevents.build().toString());
    }

    /**
     * Get the total count of events.
     * 
     * @return JSON response
     */
    @GET
    @Path("count")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public String countREST() {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        // NOTE the final event count is the count of EventEntity entries in database minus the count of events to be purged
        AppInfoUtils autils = new AppInfoUtils(entityManager, userTransaction);
        AppInfoEntity appinfo = autils.getAppInfoEntity();
        Long eventpurges = appinfo.getEventCountPurge();
        jsonresponse.add("count", super.count() - eventpurges);
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "Count of events", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }

    /**
     * Add a member to given event.
     * 
     * @param eventId      Event ID
     * @param memberId     Member ID
     * @param request      HTTP request
     * @return             JSON response
     */
    @PUT
    @Path("addmember/{eventId}/{memberId}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public String addMember(@PathParam("eventId") Long eventId, @PathParam("memberId") Long memberId, @Context HttpServletRequest request) {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        if (Objects.isNull(eventId) || Objects.isNull(memberId)) {
            Log.error(TAG, "*** Cannot add member to event, no valid inputs!");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to add member to event, invalid input.", ResponseResults.CODE_NOT_ACCEPTABLE, jsonresponse.build().toString());
        }

        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);

        jsonresponse.add("eventId", eventId);
        jsonresponse.add("memberId", memberId);

        // check if both, member and event exist
        UserUtils   userutils = new UserUtils(entityManager, userTransaction);
        UserEntity  user2add  = userutils.findUser(memberId);
        EventEntity event     = super.find(eventId);
        if (user2add.getStatus().getIsDeleted()) {
            user2add = null;
        }
        if (event.getStatus().getIsDeleted()) {
            event = null;
        }
        if (Objects.isNull(event) || Objects.isNull(user2add)) {
            Log.warning(TAG, "*** Cannot add member to event: non-existing member or event!");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to add member to event.", ResponseResults.CODE_NOT_FOUND, jsonresponse.build().toString());
        }

        // check if the event owner or a user with higher privilege is trying to modify the event
        if (!userutils.userIsOwnerOrAdmin(sessionuser, event.getStatus())) {
            Log.warning(TAG, "*** User was attempting to modify (add member) an event without proper privilege!");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to add member to event, insufficient privilege.", ResponseResults.CODE_FORBIDDEN, jsonresponse.build().toString());
        }

        EventUtils eventutils = new EventUtils(entityManager, userTransaction);
        try {
            eventutils.addMember(event, user2add);
        }
        catch (Exception ex) {
            Log.warning(TAG, "*** Could not add member to event, reason: " + ex.getLocalizedMessage());
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to add member to event. Reason: " + ex.getLocalizedMessage(), ResponseResults.CODE_INTERNAL_SRV_ERROR, jsonresponse.build().toString());
        }

        jsonresponse.add("memberName", user2add.getName());
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "Member was added to event.", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }

    /**
     * Remove a member from given event.
     * 
     * @param eventId      Event ID
     * @param memberId     Member ID
     * @param request      HTTP request
     * @return             JSON response
     */
    @PUT
    @Path("removemember/{eventId}/{memberId}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public String removeMember(@PathParam("eventId") Long eventId, @PathParam("memberId") Long memberId, @Context HttpServletRequest request) {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        if (Objects.isNull(eventId) || Objects.isNull(memberId)) {
            Log.error(TAG, "*** Cannot remove member from event, no valid inputs!");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to remove member from event, invalid input.", ResponseResults.CODE_NOT_ACCEPTABLE, jsonresponse.build().toString());
        }

        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);

        jsonresponse.add("eventId", eventId);
        jsonresponse.add("memberId", memberId);

        // check if both, member and event exist
        UserUtils   userutils    = new UserUtils(entityManager, userTransaction);
        UserEntity  user2remove  = userutils.findUser(memberId);
        EventEntity event        = super.find(eventId);
        if (user2remove.getStatus().getIsDeleted()) {
            user2remove = null;
        }
        if (event.getStatus().getIsDeleted()) {
            event = null;
        }
        if (Objects.isNull(event) || Objects.isNull(user2remove)) {
            Log.warning(TAG, "*** Cannot remove member from event: non-existing member or event!");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to remove member from event.", ResponseResults.CODE_NOT_FOUND, jsonresponse.build().toString());
        }

        // check if the event owner or a user with higher privilege is trying to modify the event
        if (!userutils.userIsOwnerOrAdmin(sessionuser, event.getStatus())) {
            Log.warning(TAG, "*** User was attempting to modify (remove member) an event without proper privilege!");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to remove member from event, insufficient privilege.", ResponseResults.CODE_FORBIDDEN, jsonresponse.build().toString());
        }

        EventUtils utils = new EventUtils(entityManager, userTransaction);
        try {
            utils.removeMember(event, user2remove);
        }
        catch (Exception ex) {
            Log.warning(TAG, "*** Could not remove member from event, reason: " + ex.getLocalizedMessage());
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to remove member from event. Reason: " + ex.getLocalizedMessage(), ResponseResults.CODE_INTERNAL_SRV_ERROR, jsonresponse.build().toString());
        }

        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "Member was removed from event.", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }

    /**
     * Add a new or update an existing location. If the input has an id field, then
     * an update attempt for that location entity with given ID is performed. If no id
     * field exists, then a new location entity is created and added to given event.
     * 
     * @param eventId      Event ID
     * @param locationJson Location to add in JSON format
     * @param request      HTTP request
     * @return             JSON response
     */
    @PUT
    @Path("putlocation/{eventId}/{locationJson}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public String addLocation(@PathParam("eventId") Long eventId, @PathParam("locationJson") String locationJson, @Context HttpServletRequest request) {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        if (Objects.isNull(eventId) || Objects.isNull(locationJson)) {
            Log.error(TAG, "*** Cannot add location to event, no valid inputs!");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to add location to event, invalid input.", ResponseResults.CODE_NOT_ACCEPTABLE, jsonresponse.build().toString());
        }

        EventUtils eventutils = new EventUtils(entityManager, userTransaction);
        EventEntity event = eventutils.findEvent(eventId);
        if (Objects.isNull(event)) {
            Log.warning(TAG, "*** Cannot add location to event: non-existing event!");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to remove member from event.", ResponseResults.CODE_NOT_FOUND, jsonresponse.build().toString());
        }

        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        // check if the event owner or a user with higher privilege is trying to modify the event locations
        UserUtils userutils = new UserUtils(entityManager, userTransaction);
        if (!userutils.userIsOwnerOrAdmin(sessionuser, event.getStatus())) {
            Log.warning(TAG, "*** User was attempting to update an event without proper privilege!");
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to add location to event, insufficient privilege.", ResponseResults.CODE_FORBIDDEN, jsonresponse.build().toString());
        }

        EventLocationEntity inputlocation;
        EventLocationUtils elutils = new EventLocationUtils(entityManager, userTransaction);
        try {
            inputlocation = elutils.validateLocationInput(locationJson);
        }
        catch (Exception ex) {
            Log.warning(TAG, "*** Could not add location, validation failed, reason: " + ex.getLocalizedMessage());
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, ex.getLocalizedMessage(), ResponseResults.CODE_BAD_REQUEST, jsonresponse.build().toString());
        }

        EventLocationEntity location;
        try {
            if (Objects.nonNull(inputlocation.getId()) && (inputlocation.getId() > 0)) {
                location = elutils.updateLocation(inputlocation);                
            }
            else {
                location = elutils.createNewLocation(event, inputlocation, sessionuser.getId());
            }
        }
        catch (Exception ex) {
            Log.warning(TAG, "*** Could not create new location, reaon: " + ex.getLocalizedMessage());
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to create new location.", ResponseResults.CODE_INTERNAL_SRV_ERROR, jsonresponse.build().toString());
        }

        //! NOTE on successful entity location creation the new ID is sent back by results.data field.
        jsonresponse.add("eventId", event.getId());
        jsonresponse.add("locationId", location.getId());
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "Location was successfully added to event.", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }
}
