/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.rest;

import com.sun.tools.javah.Gen;
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
    public GenericResponseResult<EventId> edit(@PathParam("id") Long eventId, EventCmd eventCmd, @Context HttpServletRequest request) {
        UserEntity sessionUser = AuthorityConfig.getInstance().getSessionUser(request);
        EventId response = new EventId(eventId.toString());

        EventEntity updateEvent;
        try {
            updateEvent = validator.validateUpdateEntityInput(eventCmd);
        } catch (Exception e) {
            return GenericResponseResult.badRequest("Failed to update event, invalid input.", response);
        }

        EventEntity existingEvent = events.findEvent(eventId);
        if (existingEvent == null) {
            return GenericResponseResult.notFound("Failed to update event, event does not exist.", response);
        }

        // check if the event owner or a user with higher privilege is trying to modify the event
        if (!users.userIsOwnerOrAdmin(sessionUser, existingEvent.getStatus())) {
            LOGGER.warn("*** User was attempting to update an event without proper privilege!");
            return GenericResponseResult.unauthorized("Failed to update event, insufficient privilege.", response);
        }

        performEventUpdate(updateEvent, existingEvent);

        // notify all event members about its change
        eventNotifications.sendNotifyEventChanged(EventNotifications.ChangeType.Modify, sessionUser, existingEvent);

        return GenericResponseResult.ok("Event successfully updated.", response);
    }

    protected void performEventUpdate(final EventEntity updateEvent, EventEntity existingEvent) {
        if ((updateEvent.getName() != null) && !updateEvent.getName().isEmpty()) {
            existingEvent.setName(updateEvent.getName());
        }
        if ((updateEvent.getDescription() != null) && !updateEvent.getDescription().isEmpty()) {
            existingEvent.setDescription(updateEvent.getDescription());
        }
        if (updateEvent.getPhoto() != null) {
            events.updateEventImage(existingEvent, updateEvent.getPhoto());
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

        EventEntity event = events.findEvent(id);
        if ((event == null) || !event.getStatus().isEnabled()) {
            LOGGER.warn("*** User was attempting to delete non-existing event!");
            return GenericResponseResult.notFound("Failed to find event for deletion.", response);
        }

        if (!users.userIsOwnerOrAdmin(sessionUser, event.getStatus())) {
            LOGGER.warn("*** User was attempting to delete an event without proper privilege!");
            return GenericResponseResult.unauthorized("Failed to delete event, insufficient privilege.", response);
        }

        eventNotifications.sendNotifyEventChanged(EventNotifications.ChangeType.Remove, sessionUser, event);

        try {
            events.markEventAsDeleted(event);
        }
        catch (Exception ex) {
            LOGGER.warn("*** Could not mark event as deleted, reason: " + ex.getMessage());
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
    public GenericResponseResult<EventInfo> find(@PathParam("id") Long eventId, @Context HttpServletRequest request) {
        UserEntity sessionUser = AuthorityConfig.getInstance().getSessionUser(request);
        EventInfo response = new EventInfo();
        response.setId(eventId.toString());

        EventEntity event = events.findEvent(eventId);
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
    protected GenericResponseResult<List<EventInfo>> createEventsResponse(UserEntity sessionUser, List<EventEntity> foundEvents) {
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
    @ApiOperation(value = "Add a member to an events")
    public GenericResponseResult<AddRemoveEventMember> addMember(@PathParam("eventId") Long eventId, @PathParam("memberId") Long memberId, @Context HttpServletRequest request) {
        if ((eventId == null) || (memberId == null)) {
            LOGGER.error("*** Cannot add member to event, no valid inputs!");
            return GenericResponseResult.notAcceptable("Failed to add member to event, invalid input.");
        }

        UserEntity sessionUser = AuthorityConfig.getInstance().getSessionUser(request);

        AddRemoveEventMember response = new AddRemoveEventMember(eventId.toString(), memberId.toString());

        return checkAndAddMember(eventId, memberId, sessionUser, response);
    }

    @NotNull
    protected GenericResponseResult<AddRemoveEventMember> checkAndAddMember(final Long eventId, final Long memberId, final UserEntity sessionUser, AddRemoveEventMember response) {
        UserEntity  userToAdd = users.findUser(memberId);
        EventEntity event = events.findEvent(eventId);
        if ((userToAdd == null) || !userToAdd.getStatus().getIsActive()) {
            userToAdd = null;
        }
        else if ((event == null) || !event.getStatus().getIsActive()) {
            event = null;
        }
        if ((event == null) || (userToAdd == null)) {
            LOGGER.warn("*** Cannot add member to event: non-existing member or event!");
            return GenericResponseResult.notFound("Failed to add member to event, invalid user or event ID.", response);
        }

        if (!users.userIsOwnerOrAdmin(sessionUser, event.getStatus())) {
            LOGGER.warn("*** User was attempting to modify (add member) an event without proper privilege!");
            return GenericResponseResult.unauthorized("Failed to add member to event, insufficient privilege.", response);
        }

        try {
            events.addMember(event, userToAdd);
        }
        catch (Exception ex) {
            LOGGER.warn("*** Could not add member to event, reason: {}", ex.getMessage());
            return GenericResponseResult.notAcceptable("Failed to add member to event. Reason: " + ex.getMessage(), response);
        }

        eventNotifications.sendNotifyMemberChanged(EventNotifications.ChangeType.Add, sessionUser, event, memberId);

        events.createEventJoiningMail(event, userToAdd);

        return GenericResponseResult.ok("Member was added to event.", response);
    }

    /**
     * Remove a member from given event.
     */
    @PUT
    @Path("removemember/{eventId}/{memberId}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    @ApiOperation(value = "Remove a member from an events")
    public GenericResponseResult<AddRemoveEventMember> removeMember(@PathParam("eventId") Long eventId, @PathParam("memberId") Long memberId, @Context HttpServletRequest request) {
        if ((eventId == null) || (memberId == null)) {
            LOGGER.error("*** Cannot remove member from event, no valid inputs!");
            return GenericResponseResult.notAcceptable("Failed to remove member from event, invalid input.");
        }

        UserEntity sessionUser = AuthorityConfig.getInstance().getSessionUser(request);

        AddRemoveEventMember response = new AddRemoveEventMember(eventId.toString(), memberId.toString());

        return checkAndRemoveMember(eventId, memberId, sessionUser, response);
    }

    @NotNull
    protected GenericResponseResult<AddRemoveEventMember> checkAndRemoveMember(final Long eventId, final Long memberId, final UserEntity sessionUser, AddRemoveEventMember response) {
        UserEntity userToRemove = users.findUser(memberId);
        EventEntity event = events.findEvent(eventId);
        if ((userToRemove == null) || !userToRemove.getStatus().getIsActive()) {
            userToRemove = null;
        }
        else if ((event == null) || !event.getStatus().getIsActive()) {
            event = null;
        }
        if ((event == null) || (userToRemove == null)) {
            LOGGER.warn("*** Cannot remove member from event: non-existing member or event!");
            return GenericResponseResult.notFound("Failed to remove member from event.", response);
        }

        // check if the member himself, event owner, or a user with higher privilege is trying to modify the event
        if ((!Objects.equals(memberId, sessionUser.getId())) && !users.userIsOwnerOrAdmin(sessionUser, event.getStatus())) {
            LOGGER.warn("*** User was attempting to modify (remove member) an event without proper privilege!");
            return GenericResponseResult.unauthorized("Failed to remove member from event, insufficient privilege.", response);
        }

        try {
            events.removeMember(event, userToRemove);
        }
        catch (Exception ex) {
            LOGGER.warn("*** Could not remove member from event, reason: {}", ex.getMessage());
            return GenericResponseResult.notAcceptable("Failed to remove member from event. Reason: " + ex.getMessage(), response);
        }

        eventNotifications.sendNotifyMemberChanged(EventNotifications.ChangeType.Remove, sessionUser, event, memberId);

        events.createEventLeavingMail(event, userToRemove);

        return GenericResponseResult.ok("Member was removed from event.", response);
    }

    /**
     * Send a notification to all event members. This service method is only meant to be used by an admin.
     */
    @POST
    @Path("notifyMembers/{eventId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.USER_ROLE_ADMIN})
    @ApiOperation(value = "Send a notification to all event members")
    public GenericResponseResult<EventId> notifyMembers(@PathParam("eventId") Long eventId, String notificationJson, @Context HttpServletRequest request) {
        if (eventId == null) {
            LOGGER.error("*** Cannot notify event members, no valid inputs!");
            return GenericResponseResult.notAcceptable("Failed to notify event members, invalid input.");
        }

        EventId response = new EventId(eventId.toString());

        EventEntity event = events.findEvent(eventId);
        if ((event == null) || !event.getStatus().getIsActive()) {
            LOGGER.warn("*** Cannot notify event members: non-existing event!");
            return GenericResponseResult.notFound("Failed notify event members, invalid event.", response);
        }

        Notification notification = JsonbBuilder.create().fromJson(notificationJson, Notification.class);

        eventNotifications.notifyEventMembers(AuthorityConfig.getInstance().getSessionUser(request), event, notification);

        return GenericResponseResult.ok("Event members were notified.", response);
    }

    /**
     * Get the location with given ID.
     */
    @GET
    @Path("location/{eventId}/{locationId}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    @ApiOperation(value = "Get event location")
    public GenericResponseResult<EventLocation> getLocation(@PathParam("eventId") Long eventId, @PathParam("locationId") Long locationId, @Context HttpServletRequest request) {
        if ((eventId == null) || (locationId == null)) {
            LOGGER.error("*** Cannot get event location, no valid inputs!");
            return GenericResponseResult.notAcceptable("Failed to get event location, invalid input.");
        }

        EventEntity event = events.findEvent(eventId);
        if (event == null) {
            LOGGER.warn("*** Cannot get location: non-existing event!");
            return GenericResponseResult.notFound("Failed to get event location. Event does not exist.");
        }

        UserEntity sessionUser = AuthorityConfig.getInstance().getSessionUser(request);

        if (!event.getIsPublic() && !users.userIsOwnerOrAdmin(sessionUser, event.getStatus())) {
            LOGGER.warn("*** User was attempting to get event information without proper privilege!");
            return GenericResponseResult.unauthorized("Failed to get event location, insufficient privilege.");
        }

        EventLocationEntity location = eventLocations.findLocation(locationId);
        if ((location == null) || !location.getStatus().getIsActive()) {
            LOGGER.warn("*** Failed to get event location, it does not exist!");
            return GenericResponseResult.notFound("Failed to get event location. Location does not exist.");
        }

        return GenericResponseResult.ok("Location was successfully added/update.", eventLocations.exportEventLocation(location));
    }

    /**
     * Add a new or update an existing location. If the input has an id field, then
     * an update attempt for that location entity with given ID is performed. If no id
     * field exists, then a new location entity is created and added to given event.
     */
    @PUT
    @Path("putlocation/{eventId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    @ApiOperation(value = "Add a new location to an event")
    public GenericResponseResult<AddRemoveEventLocation> putLocation(@PathParam("eventId") Long eventId, EventLocationCmd eventLocationCmd, @Context HttpServletRequest request) {
        if ((eventId == null) || (eventLocationCmd == null)) {
            LOGGER.error("*** Cannot add location to event, no valid inputs!");
            return GenericResponseResult.notAcceptable("Failed to add/update event location, invalid input.");
        }

        EventEntity event = events.findEvent(eventId);
        if ((event == null) || !event.getStatus().isEnabled()) {
            LOGGER.warn("*** Cannot add location to event: non-existing event!");
            return GenericResponseResult.notFound("Failed to add/update member from event.");
        }

        UserEntity sessionUser = AuthorityConfig.getInstance().getSessionUser(request);

        if (!users.userIsOwnerOrAdmin(sessionUser, event.getStatus())) {
            LOGGER.warn("*** User was attempting to update an event without proper privilege!");
            return GenericResponseResult.unauthorized("Failed to add/update location to event, insufficient privilege.");
        }

        EventLocationEntity inputLocation;
        try {
            inputLocation = validator.validateLocationInput(eventLocationCmd, event);
        }
        catch (Exception ex) {
            LOGGER.warn("*** Could not add location, validation failed, reason: {}", ex.getMessage());
            return GenericResponseResult.badRequest(ex.getMessage());
        }

        return checkAndAddLocation(inputLocation, sessionUser, event);
    }

    @NotNull
    protected GenericResponseResult<AddRemoveEventLocation> checkAndAddLocation(final EventLocationEntity inputLocation, final UserEntity sessionUser, EventEntity event) {
        EventNotifications.ChangeType changeType;
        EventLocationEntity location;
        try {
            // add new location or update an existing one?
            if ((inputLocation.getId() != null) && (inputLocation.getId() > 0)) {
                location = eventLocations.updateLocation(inputLocation);
                changeType = EventNotifications.ChangeType.Add;
            }
            else {
                if (!validator.validateUniqueLocationName(event, inputLocation.getName())) {
                    throw new Exception("There is already a location with this name.");
                }
                location = eventLocations.createNewLocation(inputLocation, sessionUser.getId(), event);
                changeType = EventNotifications.ChangeType.Modify;
            }
        }
        catch (Exception ex) {
            LOGGER.warn("*** Could not add/update location, reason: {}", ex.getMessage());
            return GenericResponseResult.notAcceptable("Failed to add/update location. Reason: " + ex.getMessage());
         }

        eventNotifications.sendNotifyLocationChanged(changeType, sessionUser, event, location.getId());

        AddRemoveEventLocation response = new AddRemoveEventLocation(event.getId().toString(), location.getId().toString());

        return GenericResponseResult.ok("Location was successfully added/update.", response);
    }

    /**
     * Remove a location from given event.
     */
    @POST
    @Path("removelocation/{eventId}/{locationId}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    @ApiOperation(value = "Remove a location from an event")
    public GenericResponseResult<AddRemoveEventLocation>  removeLocation(@PathParam("eventId") Long eventId, @PathParam("locationId") Long locationId, @Context HttpServletRequest request) {
        if ((eventId == null) || (locationId == null)) {
            LOGGER.error("*** Cannot remove location from event, no valid inputs!");
            return GenericResponseResult.notAcceptable("Failed to remove location from event, invalid input.");
        }

        UserEntity sessionUser = AuthorityConfig.getInstance().getSessionUser(request);

        return checkAndRemoveLocation(eventId, locationId, sessionUser);
    }

    @NotNull
    protected GenericResponseResult<AddRemoveEventLocation> checkAndRemoveLocation(final Long eventId, final Long locationId, final UserEntity sessionUser) {
        EventLocationEntity locationToRemove = eventLocations.findLocation(locationId);
        EventEntity event = events.findEvent(eventId);

        if ((locationToRemove == null) || !locationToRemove.getStatus().getIsActive()) {
            locationToRemove = null;
        }
        else if ((event == null) || !event.getStatus().getIsActive()) {
            event = null;
        }
        if ((event == null) || (locationToRemove == null)) {
            LOGGER.warn("*** Cannot remove location from event: non-existing location or event!");
            return GenericResponseResult.notFound("Failed to remove location from event. Event or location does not exist.");
        }

        if (!users.userIsOwnerOrAdmin(sessionUser, event.getStatus())) {
            LOGGER.warn("*** User was attempting to modify (remove location) an event without proper privilege!");
            return GenericResponseResult.unauthorized("Failed to remove location from event, insufficient privilege.");
        }

        try {
            eventLocations.markLocationAsDeleted(event, locationToRemove);
        }
        catch (Exception ex) {
            LOGGER.warn("*** Could not remove location from event, reason: {}", ex.getMessage());
            return GenericResponseResult.notAcceptable("Failed to remove location from event. Reason: " + ex.getMessage());
        }

        eventNotifications.sendNotifyLocationChanged(EventNotifications.ChangeType.Remove, sessionUser, event, locationId);

        AddRemoveEventLocation response = new AddRemoveEventLocation(event.getId().toString(), locationId.toString());

        return GenericResponseResult.ok("Location was successfully removed from event.", response);
    }
}
