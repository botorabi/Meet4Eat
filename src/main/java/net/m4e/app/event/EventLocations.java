/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.event;

import net.m4e.app.resources.DocumentEntity;
import net.m4e.app.resources.DocumentPool;
import net.m4e.app.resources.StatusEntity;
import net.m4e.app.user.business.UserEntity;
import net.m4e.common.Entities;
import net.m4e.common.Strings;
import net.m4e.system.core.AppInfoEntity;
import net.m4e.system.core.AppInfos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.*;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
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

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final EntityManager entityManager;

    private final Entities entities;

    private final AppInfos appInfos;

    private final DocumentPool docPool;


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
     * 
     * @param entityManager The entity manager
     * @param entities      The Entities instance
     * @param appInfos      The AppInfos instance
     * @param docPool       The document pool instance
     */
    @Inject
    public EventLocations(EntityManager entityManager, Entities entities, AppInfos appInfos, DocumentPool docPool) {
        this.entityManager = entityManager;
        this.entities = entities;
        this.appInfos = appInfos;
        this.docPool = docPool;
    }

    /**
     * Create a new event location in database.
     * 
     * @param event        The event getting the new location
     * @param inputEntity  Entity containing the new location data
     * @param creatorID    Creator ID
     * @return             A new created event location entity if successfully, otherwise null.
     * @throws Exception   Throws an exception if something goes wrong.
     */
    public EventLocationEntity createNewLocation(EventEntity event, EventLocationEntity inputEntity, Long creatorID) throws Exception {
        // setup the new entity
        EventLocationEntity newlocation = new EventLocationEntity();
        newlocation.setName(inputEntity.getName());
        newlocation.setDescription(inputEntity.getDescription());
        if (inputEntity.getPhoto() != null) {
            updateEventLocationImage(newlocation, inputEntity.getPhoto());
        }

        // setup the status
        StatusEntity status = new StatusEntity();
        status.setIdCreator(creatorID);
        status.setIdOwner(creatorID);
        Date now = new Date();
        status.setDateCreation(now.getTime());
        status.setDateLastUpdate(now.getTime());
        newlocation.setStatus(status);

        entities.create(newlocation);
        Collection<EventLocationEntity> locs = event.getLocations();
        if (locs == null) {
            locs = new ArrayList<>();
            event.setLocations(locs);
        }
        event.getLocations().add(newlocation);
        entities.update(event);
        return newlocation;
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
     * @throws Exception    Throws exception if any problem occurred.
     */
    public void updateEventLocationImage(EventLocationEntity location, DocumentEntity image) throws Exception {
        // make sure that the resource URL is set
        image.setResourceURL("/EventLoction/Image");
        docPool.updatePhoto(location, image);
    }

    /**
     * Try to find an event location with given location ID.
     * 
     * @param id    Event location ID
     * @return      Return an entity if found, otherwise return null.
     */
    public EventLocationEntity findLocation(Long id) {
        EventLocationEntity location = entities.find(EventLocationEntity.class, id);
        return location;
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

        List<EventLocationVoteEntity> voteentities = query.setMaxResults(MAX_CNT_RESULTS).getResultList();
        return voteentities;
    }

    /**
     * Update a vote entry for a given event location. If the entry does not exist, then one is created. Voting is
     * accepted only during a particular time (voting window). If this method is called outside of voting window time
     * then a false is returned and nothing happens.
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
        long voteend;
        long votebegin;
        long now = Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis() / 1000;

        // for repeated events check the current day
        Long wd = event.getRepeatWeekDays();
        if (wd > 0L) {
            int currentday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            //! NOTE Calendar.DAY_OF_WEEK begins with a 1 for Sunday, convert the day flag
            currentday = (1 << ((currentday + 5) % 7));
            if ((currentday & wd) == 0) {
                return null;
            }
            //! NOTE the repeat day time is expected to be in UTC
            voteend = event.getRepeatDayTime();
            long daysdivider = 60 * 60 * 24;
            voteend += (now / daysdivider) * daysdivider;
            votebegin = voteend - event.getVotingTimeBegin();
        }
        else {
            voteend   = event.getEventStart();
            votebegin = voteend - event.getVotingTimeBegin();
        }

        // check if the vote is happening in the right time window
        if ((now < votebegin) || (now > voteend)) {
            return null;
        }

        TypedQuery<EventLocationVoteEntity> query = entityManager.createNamedQuery("EventLocationVoteEntity.findLocationVotes", EventLocationVoteEntity.class);
        query.setParameter("timeBegin", votebegin);
        query.setParameter("timeEnd", voteend);
        query.setParameter("locationId", location.getId());

        EventLocationVoteEntity voteentity;
        //! NOTE we expect maximal 1 result here
        List<EventLocationVoteEntity> voteentities = query.getResultList();
        if (voteentities.isEmpty()) {
            voteentity = new EventLocationVoteEntity();
            voteentity.setEventId(event.getId());
            voteentity.setLocationId(location.getId());
            voteentity.setLocationName(location.getName());
            voteentity.setVoteTimeBegin(votebegin);
            voteentity.setVoteTimeEnd(voteend);
            voteentity.setCreationTime((new Date()).getTime() / 1000);
            entities.create(voteentity);
        }
        else {
            voteentity = voteentities.get(0);
        }

        if (vote) {
            voteentity.addUserId(voter.getId());
            voteentity.addUserName(voter.getName());
        }
        else {
            voteentity.removeUserId(voter.getId());            
            voteentity.removeUserName(voter.getName());            
        }

        entities.update(voteentity);

        return voteentity;
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
            throw new Exception("Problem occured while retrieving AppInfo entity!");
        }
        appinfo.incrementEventLocationCountPurge(1L);
        entities.update(appinfo);
    }

    /**
     * Given a JSON string, import the necessary fields and create an event location entity.
     * 
     * @param jsonString  JSON string representing an event location entity
     * @return            Event location entity or null if the JSON string was not appropriate
     */
    public EventLocationEntity importLocationJSON(String jsonString) {
        if (jsonString == null) {
            return null;
        }

        String name, idstring, description, photo;
        try {
            JsonReader jreader = Json.createReader(new StringReader(jsonString));
            JsonObject jobject = jreader.readObject();
            idstring    = jobject.getString("id", "");
            name        = jobject.getString("name", null);
            description = jobject.getString("description", null);
            photo       = jobject.getString("photo", null);
        }
        catch(Exception ex) {
            LOGGER.warn("Could not setup an event loaction given JSON string, reason: " + ex.getLocalizedMessage());
            return null;
        }

        long id = 0;
        try {
            id = Long.parseLong(idstring);
        }
        catch(NumberFormatException ex) {}

        EventLocationEntity entity = new EventLocationEntity();
        if (id != 0) {
            entity.setId(id);
        }
        if (name != null) {
            entity.setName(Strings.limitStringLen(name, 32));
        }
        if (description != null) {
            entity.setDescription(Strings.limitStringLen(description, 1000));
        }

        if (photo != null) {
            DocumentEntity image = new DocumentEntity();
            // currently we expect only base64 encoded images here
            image.setEncoding(DocumentEntity.ENCODING_BASE64);
            image.updateContent(photo.getBytes());
            image.setType(DocumentEntity.TYPE_IMAGE);
            entity.setPhoto(image);
        }

        return entity;
    }

    /**
     * Given an event location entity, export the necessary fields into a JSON object.
     * 
     * @param entity    Event location entity to export
     * @return          A JSON object containing builder the proper entity fields
     */
    public JsonObjectBuilder exportEventLocationJSON(EventLocationEntity entity) {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("id", (entity.getId() != null) ? entity.getId().toString() : "")
            .add("name", (entity.getName() != null) ? entity.getName() : "")
            .add("description", (entity.getDescription() != null) ? entity.getDescription(): "")
            .add("photoId", (entity.getPhoto() != null) ? entity.getPhoto().getId().toString() : "")
            .add("photoETag", (entity.getPhoto() != null) ? entity.getPhoto().getETag() : "");
        return json;
    }

    /**
     * Export the given event location votes to JSON format.
     * 
     * @param votes    Event location votes
     * @return          A JSON array builder containing the exported votes.
     */
    public JsonObjectBuilder exportLocationVotesJSON(EventLocationVoteEntity votes) {
        JsonObjectBuilder obj = Json.createObjectBuilder();
        obj.add("id", (votes.getId() != null) ? votes.getId().toString() : "")
           .add("eventId", (votes.getEventId() != null) ? votes.getEventId().toString() : "")
           .add("locationId", (votes.getLocationId() != null) ? votes.getLocationId().toString() : "")
           .add("locationName", (votes.getLocationName() != null) ? votes.getLocationName() : "")
           .add("timeBegin", votes.getVoteTimeBegin())
           .add("timeEnd", votes.getVoteTimeEnd())
           .add("creationTime", votes.getCreationTime());

        if (votes.getUserIds() != null) {
            JsonArrayBuilder userids = Json.createArrayBuilder();
            votes.getUserIds().forEach((u)-> {
                userids.add(u);
            });
            obj.add("userIds", userids);
        }
        if (votes.getUserNames() != null) {
            JsonArrayBuilder usernames = Json.createArrayBuilder();
            votes.getUserNames().forEach((u)-> {
                usernames.add(u);
            });
            obj.add("userNames", usernames);
        }

        return obj;
    }

    /**
     * Export a list of event location votes to JSON format.
     * 
     * @param votes     List of location votes
     * @return          A JSON array builder containing the exported votes.
     */
    public JsonArrayBuilder exportLocationVotesJSON(List<EventLocationVoteEntity> votes) {
        JsonArrayBuilder json = Json.createArrayBuilder();
        votes.forEach((v) -> {
            JsonObjectBuilder obj = exportLocationVotesJSON(v);
            json.add(obj);
        });
        return json;
    }
}
