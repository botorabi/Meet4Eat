/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event;

import net.m4e.app.auth.AuthRole;
import net.m4e.app.auth.AuthorityConfig;
import net.m4e.app.notification.NotifyUsersEvent;
import net.m4e.app.user.business.UserEntity;
import net.m4e.common.Entities;
import net.m4e.common.ResponseResults;
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
public class EventLocationVoteEntityFacadeREST {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Event used for notifying other users
     */
    @Inject
    private Event<NotifyUsersEvent> notifyUsersEvent;

    private final Events events;

    private final Entities entities;

    private final EventLocations eventLocations;


    /**
     * EJB's default constructor
     */
    protected EventLocationVoteEntityFacadeREST() {
        events = null;
        entities = null;
        eventLocations = null;
    }

    /**
     * Create the REST facade.
     * 
     * @param events
     * @param entities
     * @param eventLocations
     */
    @Inject
    public EventLocationVoteEntityFacadeREST(Events events, Entities entities, EventLocations eventLocations) {
        this.events = events;
        this.entities = entities;
        this.eventLocations = eventLocations;
    }

    /**
     * Set/unset a location vote for requesting user.
     * 
     * @param eventId       The event ID
     * @param locationId    The event location ID
     * @param vote          1 for vote, 0 for unvote
     * @param request       HTTP request
     * @return              JSON response
     */
    @PUT
    @Path("setvote/{eventId}/{locationId}/{vote}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public String setVote(@PathParam("eventId") Long eventId, @PathParam("locationId") Long locationId, @PathParam("vote") Long vote, @Context HttpServletRequest request) {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        if (sessionuser == null) {
            LOGGER.error("*** Internal error, cannot set location vote, no user in session found!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to set location vote, no authentication.", ResponseResults.CODE_UNAUTHORIZED, null);
        }

        EventEntity event = entities.find(EventEntity.class, eventId);
        if ((event == null) || !event.getStatus().getIsActive()) {
            LOGGER.warn("*** Cannot update event location vote, event does not exit!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to set location vote, invalid event.", ResponseResults.CODE_BAD_REQUEST, null);
        }
        if (!events.getUserIsEventOwnerOrMember(sessionuser, event)) {
            LOGGER.warn("*** Cannot update event location vote, user is no member of event!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to set location vote, you are not a member of event.", ResponseResults.CODE_UNAUTHORIZED, null);
        }
        EventLocationEntity loc = events.findEventLocation(eventId, locationId);
        if (loc == null) {
            LOGGER.warn("*** Cannot update event location vote, event location does not exist!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to set location vote, invalid event location.", ResponseResults.CODE_BAD_REQUEST, null);
        }

        EventLocationVoteEntity voteentity = eventLocations.createOrUpdateVote(sessionuser, event, loc, (vote > 0));
        if (voteentity == null) {
            LOGGER.warn("*** Cannot update event location vote, outside of voting time window!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to set location vote, invalid voting time window.", ResponseResults.CODE_BAD_REQUEST, null);
        }

        // notify all event members about removing a location
        EventNotifications notifications = new EventNotifications(notifyUsersEvent, null);
        notifications.sendNotifyLocationVote(EventNotifications.ChangeType.Modify, sessionuser, event, locationId, (vote > 0));

        jsonresponse.add("votesId", voteentity.getId().toString());
        jsonresponse.add("eventId", eventId.toString());
        jsonresponse.add("locationId", locationId.toString());
        jsonresponse.add("vote", (vote > 0));
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "Location vote was successfully udpated.", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }

    /**
     * Get all location votes for a given event and voting time window.
     * 
     * @param eventId       The event ID
     * @param timeBegin     Begin of voting time window
     * @param timeEnd       End of voting time window
     * @param request       HTTP request
     * @return              JSON response containing all location votes
     */
    @GET
    @Path("getvotes/{eventId}/{timeBegin}/{timeEnd}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public String getVotesByTime(@PathParam("eventId") Long eventId, @PathParam("timeBegin") Long timeBegin, @PathParam("timeEnd") Long timeEnd, @Context HttpServletRequest request) {
        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        if (sessionuser == null) {
            LOGGER.error("*** Internal error, cannot get location votes, no user in session found!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to get location votes, no authentication.", ResponseResults.CODE_UNAUTHORIZED, null);
        }

        EventEntity event = entities.find(EventEntity.class, eventId);
        if ((event == null) || !event.getStatus().getIsActive()) {
            LOGGER.warn("*** Cannot get event location votes, event does not exit!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to get location votes, invalid event.", ResponseResults.CODE_BAD_REQUEST, null);
        }
        if (!events.getUserIsEventOwnerOrMember(sessionuser, event)) {
            LOGGER.warn("*** Cannot get event location votes, user is no member of event!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to get location votes, you are not a member of event.", ResponseResults.CODE_UNAUTHORIZED, null);
        }

        List<EventLocationVoteEntity> votes = eventLocations.getVotes(event, timeBegin, timeEnd);
        JsonArrayBuilder jsonresponse = eventLocations.exportLocationVotesJSON(votes);

        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "Event location votes were successfully exported.", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }

    /**
     * Get all location votes given its ID.
     * 
     * @param votesId       The event location votes ID
     * @param request       HTTP request
     * @return              JSON response containing all location votes
     */
    @GET
    @Path("getvotes/{votesId}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public String getVotesById(@PathParam("votesId") Long votesId, @Context HttpServletRequest request) {
        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        if (sessionuser == null) {
            LOGGER.error("*** Internal error, cannot get location votes, no user in session found!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to get location votes, no authentication.", ResponseResults.CODE_UNAUTHORIZED, null);
        }

        EventLocationVoteEntity locationvotes = entities.find(EventLocationVoteEntity.class, votesId);
        if (locationvotes == null) {
            LOGGER.warn("*** Cannot get event location votes, invalid ID!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to get location votes, invalid ID.", ResponseResults.CODE_BAD_REQUEST, null);
        }

        JsonObjectBuilder jsonresponse = eventLocations.exportLocationVotesJSON(locationvotes);

        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "Event location votes were successfully exported.", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }
}
