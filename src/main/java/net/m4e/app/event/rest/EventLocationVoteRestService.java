/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.rest;

import io.swagger.annotations.*;
import net.m4e.app.auth.AuthRole;
import net.m4e.app.auth.AuthorityConfig;
import net.m4e.app.event.business.*;
import net.m4e.app.event.rest.comm.LocationVote;
import net.m4e.app.notification.NotifyUsersEvent;
import net.m4e.app.user.business.UserEntity;
import net.m4e.common.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.lang.invoke.MethodHandles;
import java.util.List;

/**
 * REST services for event location voting.
 *
 * @author boto
 * Date of creation Nov 11, 2017
 */
@Stateless
@Path("/rest/locationvoting")
@Api(value = "Location voting service")
public class EventLocationVoteRestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Event<NotifyUsersEvent> notifyUsersEvent;

    private final Events events;

    private final Entities entities;

    private final EventLocations eventLocations;

    /**
     * Make the EJB container happy.
     */
    protected EventLocationVoteRestService() {
        events = null;
        entities = null;
        eventLocations = null;
        notifyUsersEvent = null;
    }

    /**
     * Create the location service.
     */
    @Inject
    public EventLocationVoteRestService(@NotNull Events events,
                                        @NotNull Entities entities,
                                        @NotNull EventLocations eventLocations,
                                        @NotNull Event<NotifyUsersEvent> notifyUsersEvent) {
        this.events = events;
        this.entities = entities;
        this.eventLocations = eventLocations;
        this.notifyUsersEvent = notifyUsersEvent;
    }

    /**
     * Set/unset a location vote for requesting user.
     */
    @PUT
    @Path("setvote/{eventId}/{locationId}/{vote}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    @ApiOperation(value = "Vote/unvote for an event location")
    public GenericResponseResult<LocationVote> setVote(@PathParam("eventId") Long eventId, @PathParam("locationId") Long locationId, @PathParam("vote") Boolean vote, @Context HttpServletRequest request) {
        UserEntity sessionUser = AuthorityConfig.getInstance().getSessionUser(request);
        EventEntity event = entities.find(EventEntity.class, eventId);
        if ((event == null) || !event.getStatus().getIsActive()) {
            LOGGER.warn("*** Cannot update event location vote, event does not exit!");
            return GenericResponseResult.badRequest("Failed to set location vote, invalid event.");
        }
        if (!events.getUserIsEventOwnerOrMember(sessionUser, event)) {
            LOGGER.warn("*** Cannot update event location vote, user is no member of event!");
            return GenericResponseResult.unauthorized("Failed to set location vote, you are not a member of event.");
        }
        EventLocationEntity locationEntity = events.findEventLocation(eventId, locationId);
        if (locationEntity == null) {
            LOGGER.warn("*** Cannot update event location vote, event location does not exist!");
            return GenericResponseResult.badRequest("Failed to set location vote, invalid event location.");
        }

        EventLocationVoteEntity voteEntity = eventLocations.createOrUpdateVote(sessionUser, event, locationEntity, vote);
        if (voteEntity == null) {
            LOGGER.warn("*** Cannot update event location vote, outside of voting time window!");
            return GenericResponseResult.badRequest("Failed to set location vote, invalid voting time window.");
        }

        // notify all event members about the vote
        EventNotifications notifications = new EventNotifications(notifyUsersEvent, null);
        notifications.sendNotifyLocationVote(EventNotifications.ChangeType.Modify, sessionUser, event, locationId, vote);

        LocationVote locationVote = new LocationVote(vote, voteEntity.getId().toString(), eventId.toString(), locationId.toString());
        return GenericResponseResult.ok("Location vote was successfully updated.", locationVote);
    }

    /**
     * Get all location votes for a given event and voting time window.
     */
    @GET
    @Path("getvotes/{eventId}/{timeBegin}/{timeEnd}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    @ApiOperation(value = "Get all location votes for a given time range")
    public GenericResponseResult<List<LocationVoteInfo>> getVotesByTime(@PathParam("eventId") Long eventId, @PathParam("timeBegin") Long timeBegin, @PathParam("timeEnd") Long timeEnd, @Context HttpServletRequest request) {
        UserEntity sessionUser = AuthorityConfig.getInstance().getSessionUser(request);
        EventEntity event = entities.find(EventEntity.class, eventId);
        if ((event == null) || !event.getStatus().getIsActive()) {
            LOGGER.warn("*** Cannot get event location votes, event does not exit!");
            return GenericResponseResult.badRequest("Failed to get location votes, invalid event.");
        }

        if (!events.getUserIsEventOwnerOrMember(sessionUser, event)) {
            LOGGER.warn("*** Cannot get event location votes, user is no member of event!");
            return GenericResponseResult.unauthorized("Failed to get location votes, you are not a member of event.");
        }

        List<LocationVoteInfo> votes = eventLocations.exportLocationVotes(eventLocations.getVotes(event, timeBegin, timeEnd));
        return GenericResponseResult.ok("Event location votes were successfully exported.", votes);
    }

    /**
     * Get all location votes given its ID.
     */
    @GET
    @Path("getvotes/{votesId}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    @ApiOperation(value = "Get location votes given its ID")
    public GenericResponseResult<LocationVoteInfo> getVotesById(@PathParam("votesId") Long votesId, @Context HttpServletRequest request) {
        UserEntity sessionUser = AuthorityConfig.getInstance().getSessionUser(request);

        EventLocationVoteEntity locationVotes = entities.find(EventLocationVoteEntity.class, votesId);
        if (locationVotes == null) {
            LOGGER.warn("*** Cannot get event location votes, invalid ID!");
            return GenericResponseResult.badRequest("Failed to get location votes, invalid ID.");
        }

        EventEntity event = entities.find(EventEntity.class, locationVotes.getEventId());
        if ((event == null) || !event.getStatus().getIsActive()) {
            LOGGER.warn("*** Cannot get event location votes, event does not exit!");
            return GenericResponseResult.badRequest("Failed to get location votes, invalid event.");
        }

        if (!events.getUserIsEventOwnerOrMember(sessionUser, event)) {
            LOGGER.warn("*** Cannot get event location votes, user is no member of event!");
            return GenericResponseResult.unauthorized("Failed to get location votes, you are not a member of event.");
        }

        LocationVoteInfo voteInfo = eventLocations.exportLocationVotes(locationVotes);

        return GenericResponseResult.ok("Event location votes were successfully exported.", voteInfo);
    }
}
