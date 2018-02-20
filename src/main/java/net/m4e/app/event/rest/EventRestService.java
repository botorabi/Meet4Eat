/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.rest;

import io.swagger.annotations.*;
import net.m4e.app.auth.*;
import net.m4e.app.event.business.*;
import net.m4e.app.event.rest.comm.*;
import net.m4e.app.notification.Notification;
import net.m4e.app.user.business.*;
import net.m4e.common.*;
import net.m4e.system.core.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.*;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.*;
import javax.json.bind.JsonbBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.lang.invoke.MethodHandles;
import java.util.*;

/**
 * REST services for Event related operations.
 *
 * @author boto
 * Date of creation Aug 18, 2017
 */
@Stateless
@Path("/rest/events")
@Api(value = "Event service")
public class EventRestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Entities entities;

    private final Events events;

    private final Users users;

    private final EventLocations eventLocations;

    private final EventNotifications eventNotifications;

    private final AppInfos appInfos;

    private final EventValidator validator;

    /**
     * The default constructor is needed fon an EJB.
     */
    protected EventRestService() {
        entities = null;
        events = null;
        users = null;
        validator = null;
        eventLocations = null;
        eventNotifications = null;
        appInfos = null;
    }

    /**
     * Create the event entity REST facade.
     */
    @Inject
    public EventRestService(@NotNull Entities entities,
                            @NotNull Events events,
                            @NotNull Users users,
                            @NotNull EventValidator validator,
                            @NotNull EventLocations eventLocations,
                            @NotNull EventNotifications eventNotifications,
                            @NotNull AppInfos appInfos) {

        this.entities = entities;
        this.events = events;
        this.users = users;
        this.validator = validator;
        this.eventLocations = eventLocations;
        this.eventNotifications = eventNotifications;
        this.appInfos = appInfos;
    }

    /**
     * Create a new event.
     */
    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    @ApiOperation(value = "Create an event")
    public GenericResponseResult<EventId> createEvent(EventCmd eventCmd, @Context HttpServletRequest request) {
        UserEntity sessionUser = AuthorityConfig.getInstance().getSessionUser(request);
        EventEntity eventEntity;
        try {
            eventEntity = validator.validateNewEntityInput(eventCmd);
        }
        catch (Exception ex) {
            LOGGER.warn("*** Could not create new event, validation failed, reason: {}", ex.getMessage());
            return GenericResponseResult.badRequest(ex.getMessage());
        }

        EventEntity newEvent;
        try {
            newEvent = events.createNewEvent(eventEntity, sessionUser.getId());
        }
        catch (Exception ex) {
            LOGGER.warn("*** Could not create new event, reason: {}", ex.getMessage());
            return GenericResponseResult.internalError("Failed to create new event.");
        }

        // notify all event members about its creation, usually only the event owner is the only member at this point
        eventNotifications.sendNotifyEventChanged(EventNotifications.ChangeType.Add, sessionUser, newEvent);

        EventId response = new EventId(newEvent.getId().toString());
        return GenericResponseResult.ok("Event was successfully created.", response);
    }

    /**
     * Modify the event with given ID.
     */
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    @ApiOperation(value = "Modify an event")
    public GenericResponseResult<EventId> edit(@PathParam("id") Long id, EventCmd eventCmd, @Context HttpServletRequest request) {
        UserEntity sessionUser = AuthorityConfig.getInstance().getSessionUser(request);
        EventId response = new EventId(id.toString());

        EventEntity updateEvent;
        try {
            updateEvent = validator.validateUpdateEntityInput(eventCmd);
        } catch (Exception e) {
            return GenericResponseResult.badRequest("Failed to update event, invalid input.", response);
        }

        EventEntity existingEvent = entities.find(EventEntity.class, id);
        if (existingEvent == null) {
            return GenericResponseResult.notFound("Failed to update event, event does not exist.", response);
        }

        // check if the event owner or a user with higher privilege is trying to modify the event
        if (!users.userIsOwnerOrAdmin(sessionUser, existingEvent.getStatus())) {
            LOGGER.warn("*** User was attempting to update an event without proper privilege!");
            return GenericResponseResult.forbidden("Failed to update event, insufficient privilege.", response);
        }

        performEventUpdate(updateEvent, existingEvent);

        // notify all event members about its change
        eventNotifications.sendNotifyEventChanged(EventNotifications.ChangeType.Modify, sessionUser, existingEvent);

        return GenericResponseResult.ok("Event successfully updated.", response);
    }

    private void performEventUpdate(EventEntity updateEvent, EventEntity existingEvent) {
        if ((updateEvent.getName() != null) && !updateEvent.getName().isEmpty()) {
            existingEvent.setName(updateEvent.getName());
        }
        if ((updateEvent.getDescription() != null) && !updateEvent.getDescription().isEmpty()) {
            existingEvent.setDescription(updateEvent.getDescription());
        }
        if (updateEvent.getPhoto() != null) {
            try {
                events.updateEventImage(existingEvent, updateEvent.getPhoto());
            }
            catch (Exception ex) {
                LOGGER.warn("*** Event image could not be updated, reason: " + ex.getMessage());
            }
        }
        if (updateEvent.getEventStart() > 0L) {
            existingEvent.setEventStart(updateEvent.getEventStart());
        }
        existingEvent.setIsPublic(updateEvent.getIsPublic());
        existingEvent.setRepeatWeekDays(updateEvent.getRepeatWeekDays());
        existingEvent.setRepeatDayTime(updateEvent.getRepeatDayTime());
        existingEvent.setVotingTimeBegin(updateEvent.getVotingTimeBegin());

        events.updateEvent(existingEvent);
    }

    /**
     * Delete an event with given ID. The event will be marked as deleted, so it can be
     * purged later.
     */
    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    @ApiOperation(value = "Delete an event")
    public GenericResponseResult<EventId> remove(@PathParam("id") Long id, @Context HttpServletRequest request) {
        UserEntity sessionUser = AuthorityConfig.getInstance().getSessionUser(request);
        EventId response = new EventId(id.toString());

        EventEntity event = entities.find(EventEntity.class, id);
        if (event == null) {
            LOGGER.warn("*** User was attempting to delete non-existing event!");
            return GenericResponseResult.notFound("Failed to find user for deletion.", response);
        }

        if (!users.userIsOwnerOrAdmin(sessionUser, event.getStatus())) {
            LOGGER.warn("*** User was attempting to delete an event without proper privilege!");
            return GenericResponseResult.forbidden("Failed to delete event, insufficient privilege.", response);
        }

        eventNotifications.sendNotifyEventChanged(EventNotifications.ChangeType.Remove, AuthorityConfig.getInstance().getSessionUser(request), event);

        try {
            events.markEventAsDeleted(event);
        }
        catch (Exception ex) {
            LOGGER.warn("*** Could not mark event as deleted, reason: " + ex.getLocalizedMessage());
            return GenericResponseResult.internalError("Failed to delete event.", response);
        }

        return GenericResponseResult.ok("Event successfully deleted", response);
    }

    /**
     * Find an event with given ID.
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    @ApiOperation(value = "Find an event")
    public GenericResponseResult<EventInfo> find(@PathParam("id") Long id, @Context HttpServletRequest request) {
        UserEntity sessionUser = AuthorityConfig.getInstance().getSessionUser(request);
        EventInfo response = new EventInfo();
        response.setId(id.toString());

        EventEntity event = entities.find(EventEntity.class, id);
        if ((event == null) || !event.getStatus().getIsActive()) {
            return GenericResponseResult.notFound("Event was not found.", response);
        }

        boolean privilegedUser = users.checkUserRoles(sessionUser, Arrays.asList(AuthRole.USER_ROLE_ADMIN));
        boolean doExport = (privilegedUser || event.getIsPublic() || events.getUserIsEventOwnerOrMember(sessionUser, event));
        if (!doExport) {
            return GenericResponseResult.unauthorized("Missing privilege for accessing the event.", response);
        }

        return GenericResponseResult.ok("Event was found.", events.exportEvent(event));
    }

    /**
     * Get all events.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    @ApiOperation(value = "Find all events accessible by user")
    public GenericResponseResult<List<EventInfo>> findAllEvents(@Context HttpServletRequest request) {
        UserEntity sessionUser = AuthorityConfig.getInstance().getSessionUser(request);
        List<EventEntity> foundEvents = entities.findAll(EventEntity.class);

        return createEventsResponse(sessionUser, foundEvents);
    }

    /**
     * Get events in given range.
     */
    @GET
    @Path("{from}/{to}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    @ApiOperation(value = "Find all events accessible by user in given range")
    public GenericResponseResult<List<EventInfo>> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to, @Context HttpServletRequest request) {
        UserEntity sessionUser = AuthorityConfig.getInstance().getSessionUser(request);
        List<EventEntity> foundEvents = entities.findRange(EventEntity.class, from, to);

        return createEventsResponse(sessionUser, foundEvents);
    }

    @NotNull
    private GenericResponseResult<List<EventInfo>> createEventsResponse(UserEntity sessionUser, List<EventEntity> foundEvents) {
        boolean privilegedUser = users.checkUserRoles(sessionUser, Arrays.asList(AuthRole.USER_ROLE_ADMIN));
        List<EventInfo> exportedEvents = new ArrayList<>();
        foundEvents.stream()
                .filter(event -> (event.getStatus().getIsActive() && (privilegedUser || event.getIsPublic() || events.getUserIsEventOwnerOrMember(sessionUser, event))))
                .forEach(event -> exportedEvents.add(events.exportEvent(event)));

        return GenericResponseResult.ok("List of events", exportedEvents);
    }

    /**
     * Get the total count of events.
     */
    @GET
    @Path("count")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    @ApiOperation(value = "Get the count of events")
    public GenericResponseResult<EventCount> count() {
        AppInfoEntity appInfo = appInfos.getAppInfoEntity();
        EventCount count = new EventCount(entities.getCount(EventEntity.class) - appInfo.getEventCountPurge());
        return GenericResponseResult.ok("Count of users", count);
    }

    /**
     * Add a member to given event.
     */
    @PUT
    @Path("addmember/{eventId}/{memberId}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public String addMember(@PathParam("eventId") Long eventId, @PathParam("memberId") Long memberId, @Context HttpServletRequest request) {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        if ((eventId == null) || (memberId == null)) {
            LOGGER.error("*** Cannot add member to event, no valid inputs!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to add member to event, invalid input.", ResponseResults.CODE_NOT_ACCEPTABLE, jsonresponse.build().toString());
        }

        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);

        jsonresponse.add("eventId", eventId.toString());
        jsonresponse.add("memberId", memberId.toString());

        // check if both, member and event exist
        UserEntity  user2add = users.findUser(memberId);
        EventEntity event = entities.find(EventEntity.class, eventId);
        if ((user2add == null) || !user2add.getStatus().getIsActive()) {
            user2add = null;
        }
        if ((event == null) || !event.getStatus().getIsActive()) {
            event = null;
        }
        if ((event == null) || (user2add == null)) {
            LOGGER.warn("*** Cannot add member to event: non-existing member or event!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to add member to event.", ResponseResults.CODE_NOT_FOUND, jsonresponse.build().toString());
        }

        // check if the event owner or a user with higher privilege is trying to modify the event
        if (!users.userIsOwnerOrAdmin(sessionuser, event.getStatus())) {
            LOGGER.warn("*** User was attempting to modify (add member) an event without proper privilege!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to add member to event, insufficient privilege.", ResponseResults.CODE_FORBIDDEN, jsonresponse.build().toString());
        }

        try {
            events.addMember(event, user2add);
        }
        catch (Exception ex) {
            LOGGER.warn("*** Could not add member to event, reason: " + ex.getLocalizedMessage());
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to add member to event. Reason: " + ex.getLocalizedMessage(), ResponseResults.CODE_INTERNAL_SRV_ERROR, jsonresponse.build().toString());
        }

        // notify all event members about a new member
        eventNotifications.sendNotifyMemberChanged(EventNotifications.ChangeType.Add, AuthorityConfig.getInstance().getSessionUser(request), event, memberId);

        events.createEventJoiningMail(event, user2add);

        jsonresponse.add("memberName", user2add.getName());
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "Member was added to event.", ResponseResults.CODE_OK, jsonresponse.build().toString());
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
        if ((eventId == null) || (memberId == null)) {
            LOGGER.error("*** Cannot remove member from event, no valid inputs!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to remove member from event, invalid input.", ResponseResults.CODE_NOT_ACCEPTABLE, jsonresponse.build().toString());
        }

        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);

        jsonresponse.add("eventId", eventId.toString());
        jsonresponse.add("memberId", memberId.toString());

        // check if both, member and event exist
        UserEntity  user2remove = users.findUser(memberId);
        EventEntity event = entities.find(EventEntity.class, eventId);
        if ((user2remove == null) || !user2remove.getStatus().getIsActive()) {
            user2remove = null;
        }
        if ((event == null) || !event.getStatus().getIsActive()) {
            event = null;
        }
        if ((event == null) || (user2remove == null)) {
            LOGGER.warn("*** Cannot remove member from event: non-existing member or event!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to remove member from event.", ResponseResults.CODE_NOT_FOUND, jsonresponse.build().toString());
        }

        // check if the member himself, event owner, or a user with higher privilege is trying to modify the event
        if ((!Objects.equals(memberId, sessionuser.getId())) && !users.userIsOwnerOrAdmin(sessionuser, event.getStatus())) {
            LOGGER.warn("*** User was attempting to modify (remove member) an event without proper privilege!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to remove member from event, insufficient privilege.", ResponseResults.CODE_FORBIDDEN, jsonresponse.build().toString());
        }

        try {
            events.removeMember(event, user2remove);
        }
        catch (Exception ex) {
            LOGGER.warn("*** Could not remove member from event, reason: " + ex.getLocalizedMessage());
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to remove member from event. Reason: " + ex.getLocalizedMessage(), ResponseResults.CODE_INTERNAL_SRV_ERROR, jsonresponse.build().toString());
        }

        // notify all event members about removing a member
        eventNotifications.sendNotifyMemberChanged(EventNotifications.ChangeType.Remove, AuthorityConfig.getInstance().getSessionUser(request), event, memberId);

        events.createEventLeavingMail(event, user2remove);

        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "Member was removed from event.", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }

    /**
     * Notify all event members. This service can be used e.g. for buzzing all members.
     * 
     * @param eventId           Event ID
     * @param notificationJson  Notification content
     * @param request           HTTP request
     * @return                  JSON response
     */
    @POST
    @Path("notifyMembers/{eventId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public String notifyMembers(@PathParam("eventId") Long eventId, String notificationJson, @Context HttpServletRequest request) {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        if (eventId == null) {
            LOGGER.error("*** Cannot notify event members, no valid inputs!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to notify event members, invalid input.", ResponseResults.CODE_NOT_ACCEPTABLE, jsonresponse.build().toString());
        }

        jsonresponse.add("eventId", eventId.toString());

        EventEntity event = entities.find(EventEntity.class, eventId);
        if ((event == null) || !event.getStatus().getIsActive()) {
            LOGGER.warn("*** Cannot notify event members: non-existing event!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed notify event members, invalid event.", ResponseResults.CODE_NOT_FOUND, jsonresponse.build().toString());
        }

        Notification notification = JsonbBuilder.create().fromJson(notificationJson, Notification.class);

        eventNotifications.notifyEventMembers(AuthorityConfig.getInstance().getSessionUser(request), event, notification);

        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "Event members were notified.", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }

    /**
     * Get the location with given ID.
     * 
     * @param eventId      Event ID
     * @param locationId   Location ID
     * @param request      HTTP request
     * @return             JSON response
     */
    @GET
    @Path("location/{eventId}/{locationId}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public String getLocation(@PathParam("eventId") Long eventId, @PathParam("locationId") Long locationId, @Context HttpServletRequest request) {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        if ((eventId == null) || (locationId == null)) {
            LOGGER.error("*** Cannot get event location, no valid inputs!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to get event location, invalid input.", ResponseResults.CODE_NOT_ACCEPTABLE, jsonresponse.build().toString());
        }

        EventEntity event = events.findEvent(eventId);
        if (event == null) {
            LOGGER.warn("*** Cannot get location: non-existing event!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to get event location. Event does not exist.", ResponseResults.CODE_NOT_FOUND, jsonresponse.build().toString());
        }

        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        // check if the event owner or a user with higher privilege is trying to modify the event locations
        if (!event.getIsPublic() && !users.userIsOwnerOrAdmin(sessionuser, event.getStatus())) {
            LOGGER.warn("*** User was attempting to get event information without proper privilege!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to get event location, insufficient privilege.", ResponseResults.CODE_FORBIDDEN, jsonresponse.build().toString());
        }

        EventLocationEntity location = eventLocations.findLocation(locationId);
        if ((location == null) || !location.getStatus().getIsActive()) {
            LOGGER.warn("*** Failed to get event location, it does not exist!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to get event location. Location does not exist.", ResponseResults.CODE_NOT_FOUND, jsonresponse.build().toString());
        }

        JsonObjectBuilder exportedlocation = eventLocations.exportEventLocationJSON(location);
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "Location was successfully added/update.", ResponseResults.CODE_OK, exportedlocation.build().toString());
    }

    /**
     * Add a new or update an existing location. If the input has an id field, then
     * an update attempt for that location entity with given ID is performed. If no id
     * field exists, then a new location entity is created and added to given event.
     * 
     * @param eventId      Event ID
     * @param eventLocationCmd Location to add in JSON format
     * @param request      HTTP request
     * @return             JSON response
     */
    @PUT
    @Path("putlocation/{eventId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public String putLocation(@PathParam("eventId") Long eventId, EventLocationCmd eventLocationCmd, @Context HttpServletRequest request) {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        if ((eventId == null) || (eventLocationCmd == null)) {
            LOGGER.error("*** Cannot add location to event, no valid inputs!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to add/update event location, invalid input.", ResponseResults.CODE_NOT_ACCEPTABLE, jsonresponse.build().toString());
        }

        EventEntity event = events.findEvent(eventId);
        if (event == null) {
            LOGGER.warn("*** Cannot add location to event: non-existing event!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to add/update member from event.", ResponseResults.CODE_NOT_FOUND, jsonresponse.build().toString());
        }

        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        // check if the event owner or a user with higher privilege is trying to modify the event locations
        if (!users.userIsOwnerOrAdmin(sessionuser, event.getStatus())) {
            LOGGER.warn("*** User was attempting to update an event without proper privilege!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to add/update location to event, insufficient privilege.", ResponseResults.CODE_FORBIDDEN, jsonresponse.build().toString());
        }

        EventLocationEntity inputlocation;
        try {
            inputlocation = validator.validateLocationInput(eventLocationCmd, event);
        }
        catch (Exception ex) {
            LOGGER.warn("*** Could not add location, validation failed, reason: " + ex.getLocalizedMessage());
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, ex.getLocalizedMessage(), ResponseResults.CODE_BAD_REQUEST, jsonresponse.build().toString());
        }

        EventNotifications.ChangeType changetype;
        EventLocationEntity location;
        try {
            // add new location or update an existing one?
            if ((inputlocation.getId() != null) && (inputlocation.getId() > 0)) {
                location = eventLocations.updateLocation(inputlocation);   
                changetype = EventNotifications.ChangeType.Add;
            }
            else {
                if (!validator.validateUniqueLocationName(event, inputlocation.getName())) {
                    throw new Exception("There is already a location with this name.");
                }
                location = eventLocations.createNewLocation(event, inputlocation, sessionuser.getId());
                changetype = EventNotifications.ChangeType.Modify;
            }
        }
        catch (Exception ex) {
            LOGGER.warn("*** Could not add/update location, reaon: " + ex.getLocalizedMessage());
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to add/update location. " + ex.getLocalizedMessage(), ResponseResults.CODE_INTERNAL_SRV_ERROR, jsonresponse.build().toString());
        }

        // notify all event members about the location change
        eventNotifications.sendNotifyLocationChanged(changetype, AuthorityConfig.getInstance().getSessionUser(request), event, location.getId());

        //! NOTE on successful entity location creation the new ID is sent back by results.data field.
        jsonresponse.add("eventId", event.getId().toString());
        jsonresponse.add("locationId", location.getId().toString());
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "Location was successfully added/update.", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }

    /**
     * Remove a location from given event.
     * 
     * @param eventId      Event ID
     * @param locationId   Location ID
     * @param request      HTTP request
     * @return             JSON response
     */
    @POST
    @Path("removelocation/{eventId}/{locationId}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public String removeLocation(@PathParam("eventId") Long eventId, @PathParam("locationId") Long locationId, @Context HttpServletRequest request) {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        if ((eventId == null) || (locationId == null)) {
            LOGGER.error("*** Cannot remove location from event, no valid inputs!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to remove location from event, invalid input.", ResponseResults.CODE_NOT_ACCEPTABLE, jsonresponse.build().toString());
        }

        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);

        jsonresponse.add("eventId", eventId.toString());
        jsonresponse.add("locationId", locationId.toString());

        // check if both, member and event exist
        EventLocationEntity loc2remove = eventLocations.findLocation(locationId);
        EventEntity event = entities.find(EventEntity.class, eventId);

        if ((loc2remove == null) || !loc2remove.getStatus().getIsActive()) {
            loc2remove = null;
        }
        if ((event == null) || !event.getStatus().getIsActive()) {
            event = null;
        }
        if ((event == null) || (loc2remove == null)) {
            LOGGER.warn("*** Cannot remove location from event: non-existing location or event!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to remove location from event. Event or location does not exist.", ResponseResults.CODE_NOT_FOUND, jsonresponse.build().toString());
        }

        // check if the event owner or a user with higher privilege is trying to modify the event
        if (!users.userIsOwnerOrAdmin(sessionuser, event.getStatus())) {
            LOGGER.warn("*** User was attempting to modify (remove location) an event without proper privilege!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to remove location from event, insufficient privilege.", ResponseResults.CODE_FORBIDDEN, jsonresponse.build().toString());
        }

        try {
            eventLocations.markLocationAsDeleted(event, loc2remove);
        }
        catch (Exception ex) {
            LOGGER.warn("*** Could not remove location from event, reason: " + ex.getLocalizedMessage());
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to remove location from event. Reason: " + ex.getLocalizedMessage(), ResponseResults.CODE_INTERNAL_SRV_ERROR, jsonresponse.build().toString());
        }

        // notify all event members about removing a location
        eventNotifications.sendNotifyLocationChanged(EventNotifications.ChangeType.Remove, AuthorityConfig.getInstance().getSessionUser(request), event, locationId);

        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "Location was succssfully removed from event.", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }
}
