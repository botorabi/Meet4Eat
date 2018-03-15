/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.event.business;

import net.m4e.app.event.rest.comm.*;
import net.m4e.app.resources.*;
import net.m4e.app.user.business.UserEntity;
import net.m4e.common.*;
import net.m4e.system.core.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.*;
import javax.persistence.*;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.util.*;


/**
 * A collection of event location related utilities
 *
 * @author boto
 * Date of creation Sep 13, 2017
 */
@ApplicationScoped
public class EventLocations {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final EntityManager entityManager;

    private final Entities entities;

    private final AppInfos appInfos;

    private final DocumentPool docPool;

    private long voteEnd;

    private long voteBegin;


    /**
     * Default constructor needed by the container.
     */
    protected EventLocations() {
        entityManager = null;
        entities = null;
        appInfos = null;
        docPool = null;
    }

    /**
     * Create an instance of Events.
     */
    @Inject
    public EventLocations(@NotNull EntityManager entityManager,
                          @NotNull Entities entities,
                          @NotNull AppInfos appInfos,
                          @NotNull DocumentPool docPool) {
        this.entityManager = entityManager;
        this.entities = entities;
        this.appInfos = appInfos;
        this.docPool = docPool;
    }

    /**
     * Create a new event location in database.
     * 
     * @param inputEntity  Entity containing the new location data
     * @param creatorID    Creator ID
     * @param event        The event getting the new location
     * @return             A new created event location entity if successfully, otherwise null.
     */
    public EventLocationEntity createNewLocation(final EventLocationEntity inputEntity, final Long creatorID, EventEntity event) {
        // setup the new entity
        EventLocationEntity newLocation = new EventLocationEntity();
        newLocation.setName(inputEntity.getName());
        newLocation.setDescription(inputEntity.getDescription());
        if (inputEntity.getPhoto() != null) {
            updateEventLocationImage(newLocation, inputEntity.getPhoto());
        }

        // setup the status
        StatusEntity status = new StatusEntity();
        status.setIdCreator(creatorID);
        status.setIdOwner(creatorID);
        Date now = new Date();
        status.setDateCreation(now.getTime());
        status.setDateLastUpdate(now.getTime());
        newLocation.setStatus(status);

        entities.create(newLocation);
        Collection<EventLocationEntity> locs = event.getLocations();
        if (locs == null) {
            locs = new ArrayList<>();
            event.setLocations(locs);
        }
        event.getLocations().add(newLocation);
        entities.update(event);
        return newLocation;
    }

    /**
     * Update the given entity in database.
     * 
     * @param inputLocation  The location containing the updates
     * @return               Updated location
     * @throws Exception     Throws an exception if something went wrong.
     */
    public EventLocationEntity updateLocation(EventLocationEntity inputLocation) throws Exception {
        EventLocationEntity location = entities.find(EventLocationEntity.class, inputLocation.getId());
        if ((location == null) || !location.getStatus().getIsActive()) {
            throw new Exception("Entity location does not exist.");
        }

        if (inputLocation.getName() != null) {
            location.setName(inputLocation.getName());
        }
        if (inputLocation.getDescription() != null) {
            location.setDescription(inputLocation.getDescription());
        }
        if (inputLocation.getPhoto() != null) {
            updateEventLocationImage(location, inputLocation.getPhoto());
        }

        entities.update(location);
        return location;
    }

    /**
     * Update the event location image with the content of given image. The given image
     * is checked in document pool and if there is no such image in pool then a new one
     * will be created.
     * 
     * @param location      Event location entity
     * @param image         Image to set to given event
     */
    public void updateEventLocationImage(EventLocationEntity location, DocumentEntity image) {
        // make sure that the resource URL is set
        image.setResourceURL("/EventLocation/Image");
        docPool.updatePhoto(location, image);
    }

    /**
     * Try to find an event location with given location ID.
     * 
     * @param id    Event location ID
     * @return      Return an entity if found, otherwise return null.
     */
    public EventLocationEntity findLocation(Long id) {
        return entities.find(EventLocationEntity.class, id);
    }

    /**
     * Get all location votes for given event in a given voting time window.
     * 
     * @param event         The event the locations belong to
     * @param timeBegin     Begin of voting time window (in seconds)
     * @param timeEnd       End of voting time window (in seconds)
     * @return              List of location vote entities
     */
    public List<EventLocationVoteEntity> getVotes(EventEntity event, Long timeBegin, Long timeEnd) {
        int MAX_CNT_RESULTS = 100;
        TypedQuery<EventLocationVoteEntity> query = entityManager.createNamedQuery("EventLocationVoteEntity.findVotes", EventLocationVoteEntity.class);
        query.setParameter("timeBegin", timeBegin);
        query.setParameter("timeEnd", timeEnd);
        query.setParameter("eventId", event.getId());

        return query.setMaxResults(MAX_CNT_RESULTS).getResultList();
    }

    /**
     * Update a vote entry for a given event location. If the entry does not exist, then one is created. Voting is
     * accepted only during a particular time (voting window). If this method is called outside of voting window time
     * then a null is returned and nothing happens.
     * 
     * The voting window ends at event start time or repeated day time (for repeated events) and begins the amount of
     * 'voting time begin' before the end.
     * 
     * @param voter     Voting user
     * @param event     The event the location belongs to
     * @param location  Event location the vote goes for
     * @param vote      true for voting, false for unvoting the location.
     * @return          Return the vote entity, or null if it is currently outside the voting time window.
     */
    public EventLocationVoteEntity createOrUpdateVote(UserEntity voter, EventEntity event, EventLocationEntity location, boolean vote) {
        if (!calculateVoteBeginAndEndTime(event)) {
            return null;
        }

        EventLocationVoteEntity voteEntity = getOrCreateVoteEntity(event, location);

        if (vote) {
            voteEntity.addUserId(voter.getId());
            voteEntity.addUserName(voter.getName());
        }
        else {
            voteEntity.removeUserId(voter.getId());
            voteEntity.removeUserName(voter.getName());
        }

        entities.update(voteEntity);

        return voteEntity;
    }

    protected boolean calculateVoteBeginAndEndTime(EventEntity event) {
        long now = Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis() / 1000;

        // for repeated events check the current day
        Long weekDays = event.getRepeatWeekDays();
        if (weekDays > 0L) {
            int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            //! NOTE Calendar.DAY_OF_WEEK begins with a 1 for Sunday, convert the day flag
            currentDay = (1 << ((currentDay + 5) % 7));
            if ((currentDay & weekDays) == 0) {
                return false;
            }
            //! NOTE the repeat day time is expected to be in UTC
            voteEnd = event.getRepeatDayTime();
            long daysDivider = 60 * 60 * 24;
            voteEnd += (now / daysDivider) * daysDivider;
            voteBegin = voteEnd - event.getVotingTimeBegin();
        }
        else {
            voteEnd   = event.getEventStart();
            voteBegin = voteEnd - event.getVotingTimeBegin();
        }

        // check if the vote is happening in the right time window
        if ((now < voteBegin) || (now > voteEnd)) {
            return false;
        }

        return true;
    }

    protected EventLocationVoteEntity getOrCreateVoteEntity(EventEntity event, EventLocationEntity location) {
        TypedQuery<EventLocationVoteEntity> query = entityManager.createNamedQuery("EventLocationVoteEntity.findLocationVotes", EventLocationVoteEntity.class);
        query.setParameter("timeBegin", voteBegin);
        query.setParameter("timeEnd", voteEnd);
        query.setParameter("locationId", location.getId());

        EventLocationVoteEntity voteEntity;
        //! NOTE we expect maximal 1 result here
        List<EventLocationVoteEntity> voteEntities = query.getResultList();
        if (voteEntities.isEmpty()) {
            voteEntity = new EventLocationVoteEntity();
            voteEntity.setEventId(event.getId());
            voteEntity.setLocationId(location.getId());
            voteEntity.setLocationName(location.getName());
            voteEntity.setVoteTimeBegin(voteBegin);
            voteEntity.setVoteTimeEnd(voteEnd);
            voteEntity.setCreationTime((new Date()).getTime() / 1000);
            entities.create(voteEntity);
        }
        else {
            voteEntity = voteEntities.get(0);
        }
        return voteEntity;
    }

    /**
     * Mark the given location as deleted.
     * 
     * @param event             Event location to mark
     * @param locationToRemove  Location to mark as deleted
     * @throws Exception        Throws an exception if something went wrong.
     */
    public void markLocationAsDeleted(EventEntity event, EventLocationEntity locationToRemove) throws Exception {
        if (locationToRemove.getStatus().getIsDeleted()) {
            throw new Exception("Location is already deleted.");            
        }
        Collection<EventLocationEntity> locations = event.getLocations();
        if ((locations == null) || !locations.contains(locationToRemove)) {
            throw new Exception("Location is not part of event.");
        }
        // mark the location entity as deleted
        locationToRemove.getStatus().setDateDeletion((new Date()).getTime());
        entities.update(locationToRemove);

        // update the app stats
        AppInfoEntity appinfo = appInfos.getAppInfoEntity();
        if (appinfo == null) {
            throw new Exception("Problem occurred while retrieving AppInfo entity!");
        }
        appinfo.incrementEventLocationCountPurge(1L);
        entities.update(appinfo);
    }

    /**
     * Get all event locations which are marked as deleted.
     *
     * @return List of event locations which are marked as deleted.
     */
    public List<EventLocationEntity> getLocationsMarkedAsDeleted() {
        List<EventLocationEntity> locations = entities.findAll(EventLocationEntity.class);
        List<EventLocationEntity> deletedLocations = new ArrayList<>();
        // speed up the task by using parallel processing
        locations.stream().parallel()
                .filter((loc) -> (loc.getStatus().getIsDeleted()))
                .forEach((loc) -> {
                    deletedLocations.add(loc);
                });

        return deletedLocations;
    }

    /**
     * Given an event location input data, import the necessary fields and create an event location entity.
     * 
     * @param locationCmd Event location data
     * @return            Event location entity or null if the input was invalid.
     */
    public EventLocationEntity importLocation(@NotNull EventLocationCmd locationCmd) {
        EventLocationEntity entity = new EventLocationEntity();

        try {
            long id = Long.parseLong(locationCmd.getId());
            entity.setId(id);
        }
        catch(NumberFormatException ex) {}

        entity.setName(locationCmd.getName());
        entity.setDescription(locationCmd.getDescription());
        entity.setName(locationCmd.getName());

        if (locationCmd.getPhoto() != null) {
            entity.setPhoto(PhotoCreator.createPhoto(locationCmd.getPhoto().getBytes()));
        }

        return entity;
    }

    /**
     * Given an event location entity, export the necessary fields into a JSON object.
     *
     * @param entity    Event location entity to export
     * @return          The event location data
     */
    public EventLocation exportEventLocation(EventLocationEntity entity) {
        EventLocation location = new EventLocation(
                (entity.getId() != null) ? entity.getId().toString() : null,
                entity.getName(),
                entity.getDescription(),
                (entity.getPhoto() != null) ? entity.getPhoto().getId().toString() : null,
                (entity.getPhoto() != null) ? entity.getPhoto().getETag() : null);

        return location;
    }

    /**
     * Export the given event location vote entity.
     */
    public LocationVoteInfo exportLocationVotes(final EventLocationVoteEntity voteEntity) {
        return LocationVoteInfo.fromLocationVoteEntity(voteEntity);
    }

    /**
     * Export a list of event location votes.
     */
    public List<LocationVoteInfo> exportLocationVotes(final List<EventLocationVoteEntity> voteEntities) {
        List<LocationVoteInfo> exportVotes = new ArrayList<>();
        voteEntities.forEach((v) -> {
            LocationVoteInfo obj = exportLocationVotes(v);
            exportVotes.add(obj);
        });
        return exportVotes;
    }
}
