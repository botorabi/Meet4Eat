/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.event;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import net.m4e.app.auth.AuthRole;
import net.m4e.common.EntityUtils;
import net.m4e.app.resources.ImageEntity;
import net.m4e.app.resources.ImagePool;
import net.m4e.app.resources.StatusEntity;
import net.m4e.common.StringUtils;
import net.m4e.system.core.AppInfoEntity;
import net.m4e.system.core.AppInfoUtils;
import net.m4e.system.core.Log;
import net.m4e.app.user.UserEntity;
import net.m4e.app.user.UserUtils;

/**
 * A collection of event related utilities
 *
 * @author boto
 * Date of creation Sep 4, 2017
 */
public class EventUtils {

    /**
     * Used for logging
     */
    private final static String TAG = "EventUtils";

    private final EntityManager entityManager;

    private final UserTransaction userTransaction;

    /**
     * Create an instance of event utilities.
     * 
     * @param entityManager    Entity manager
     * @param userTransaction  User transaction
     */
    public EventUtils(EntityManager entityManager, UserTransaction userTransaction) {
        this.entityManager = entityManager;
        this.userTransaction = userTransaction;
    }

    /**
     * Create a new event entity basing on data in given input entity.
     * 
     * @param inputEntity   Input data for new entity
     * @param creatorID     ID of creator
     * @return              New created entity
     * @throws Exception    Throws exception if something went wrong.
     */
    public EventEntity createNewEvent(EventEntity inputEntity, Long creatorID) throws Exception {
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
            createEventEntity(newevent);
        }
        catch (Exception ex) {
            throw ex;
        }
        return newevent;
    }

    /**
     * Given an event entity filled with all its fields, create it in database.
     * 
     * @param event         Event entity
     * @throws Exception    Throws exception if any problem occurred.
     */
    public void createEventEntity(EventEntity event) throws Exception {
        EntityUtils eutils = new EntityUtils(entityManager, userTransaction);

        // photo and members are shared objects, so remove them before event creation
        ImageEntity photo = event.getPhoto();
        event.setPhoto(null);
        Collection<UserEntity> members = event.getMembers();
        event.setMembers(null);

        eutils.createEntity(event);

        // now re-add photo and members to event entity and update it
        event.setPhoto(photo);
        event.setMembers(members);

        eutils.updateEntity(event);
    }

    /**
     * Delete the given event entity permanently from database.
     * 
     * @param event         Event entity
     * @throws Exception    Throws exception if any problem occurred.
     */
    public void deleteEvent(EventEntity event) throws Exception {
        EntityUtils eutils = new EntityUtils(entityManager, userTransaction);
        eutils.deleteEntity(event);
    }

    /**
     * Update event.
     * 
     * @param event       Event entity to update
     * @throws Exception  Throws exception if any problem occurred.
     */
    public void updateEvent(EventEntity event) throws Exception {
        EntityUtils eutils = new EntityUtils(entityManager, userTransaction);
        try {
            eutils.updateEntity(event);
        }
        catch (Exception ex) {
            Log.error(TAG, "*** Could not update event '" + event.getName() + "', id: " + event.getId());
            throw ex;
        }
    }

    /**
     * Try to find an event with given user ID.
     * 
     * @param id Event ID
     * @return Return an entity if found, otherwise return null.
     */
    public EventEntity findEvent(Long id) {
        EntityUtils eutils = new EntityUtils(entityManager, userTransaction);
        EventEntity event = eutils.findEntity(EventEntity.class, id);
        return event;
    }

    /**
     * Update the event image with the content of given image.
     * 
     * @param event         Event entity
     * @param image         Image to set to given event
     * @throws Exception    Throws exception if any problem occurred.
     */
    void updateEventImage(EventEntity event, ImageEntity image) throws Exception {
        ImagePool imagepool = new ImagePool(entityManager,userTransaction);
        ImageEntity img = imagepool.getOrCreatePoolImage(image.getImageHash());
        if (!imagepool.compareImageHash(event.getPhoto(), img.getImageHash())) {
            imagepool.releasePoolImage(event.getPhoto());
        }
        img.setContent(image.getContent());
        img.updateImageHash();
        img.setEncoding(image.getEncoding());
        img.setResourceURL("/Event/Image");
        event.setPhoto(img);
    }

    /**
     * Check if the given user is owner or member of an event.
     * 
     * @param user      User to check
     * @param event     Event
     * @return          Return true if the user is owner or member of given event, otherwise return false.
     */
    public boolean getUserIsEventOwnerOrMember(UserEntity user, EventEntity event) {
        boolean owner = Objects.equals(user.getId(), event.getStatus().getIdOwner());
        if (!owner && Objects.nonNull(event.getMembers())) {
            if (event.getMembers().stream().anyMatch((u) -> (Objects.equals(u.getId(), user.getId())))) {
                return true;
            } 
        }
        return owner;
    }

    /**
     * Add an user to given event.
     * 
     * @param event       Event
     * @param userToAdd   User to add
     * @throws Exception  Throws exception if any problem occurred.
     */
    public void addMember(EventEntity event, UserEntity userToAdd) throws Exception {
        Collection<UserEntity> members = event.getMembers();
        if (Objects.isNull(members)) {
            members = new ArrayList<>();
            event.setMembers(members);
        }
        if (members.contains(userToAdd)) {
            throw new Exception("User is already an event member.");
        }
        if (Objects.equals(userToAdd.getId(), event.getStatus().getIdOwner())) {
            throw new Exception("User is event owner.");            
        }
        members.add(userToAdd);
        updateEvent(event);
    }

    /**
     * Remove an user from given event.
     * 
     * @param event        Event
     * @param userToRemove User to remove
     * @throws Exception   Throws exception if any problem occurred.
     */
    public void removeMember(EventEntity event, UserEntity userToRemove) throws Exception {
        Collection<UserEntity> members = event.getMembers();
        if (Objects.isNull(members)) {
            throw new Exception("User is not member of event.");
        }
        if (!members.remove(userToRemove)) {
            throw new Exception("User is not member of event.");            
        }
        updateEvent(event);
    }

    /**
     * Remove any user in given list from an event.
     * 
     * @param event         Event
     * @param usersToRemove Users to remove
     * @throws Exception    Throws exception if any problem occurred.
     */
    public void removeAnyMember(EventEntity event, List<UserEntity> usersToRemove) throws Exception {
        for (UserEntity user: usersToRemove) {
            Collection<UserEntity> members = event.getMembers();
            if (Objects.isNull(members)) {
                continue;
            }
            members.remove(user);
        }
        updateEvent(event);
    }

    /**
     * Mark an event as deleted by setting its status deletion time stamp. This
     * method also updates the system app info entity.
     * 
     * @param event         Event entity
     * @throws Exception    Throws exception if any problem occurred.
     */
    public void markEventAsDeleted(EventEntity event) throws Exception {
        EntityUtils eutils = new EntityUtils(entityManager, userTransaction);
        StatusEntity status = event.getStatus();
        if (Objects.isNull(status)) {
            throw new Exception("Event has no status field!");
        }
        status.setDateDeletion((new Date().getTime()));
        eutils.updateEntity(event);

        // update the app stats
        AppInfoUtils autils = new AppInfoUtils(entityManager, userTransaction);
        AppInfoEntity appinfo = autils.getAppInfoEntity();
        if (Objects.isNull(appinfo)) {
            throw new Exception("Problem occured while retrieving AppInfo entity!");
        }
        appinfo.incrementEventCountPurge(1L);
        eutils.updateEntity(appinfo);
    }

    /**
     * Get all events which are marked as deleted.
     * 
     * @return List of events which are marked as deleted.
     */
    public List<EventEntity> getMarkedAsDeletedEvents() {
        EntityUtils eutils = new EntityUtils(entityManager, userTransaction);
        List<EventEntity> events = eutils.findAllEntities(EventEntity.class);
        List<EventEntity> deletedevents = new ArrayList<>();
        for (EventEntity event: events) {
            if (event.getStatus().getIsDeleted()) {
                deletedevents.add(event);
            }
        }
        return deletedevents;
    }

    /**
     * Get all event locations which are marked as deleted.
     * 
     * @return List of event locations which are marked as deleted.
     */
    public List<EventLocationEntity> getMarkedAsDeletedEventLocations() {
        EntityUtils eutils = new EntityUtils(entityManager, userTransaction);
        List<EventLocationEntity> eventlocs = eutils.findAllEntities(EventLocationEntity.class);
        List<EventLocationEntity> deletedeventlocs = new ArrayList<>();
        for (EventLocationEntity loc: eventlocs) {
            if (loc.getStatus().getIsDeleted()) {
                deletedeventlocs.add(loc);
            }
        }
        return deletedeventlocs;
    }

    /**
     * Given an event entity, export the necessary fields into a JSON object.
     * 
     * @param entity    Event entity to export
     * @return          A JSON object containing builder the proper entity fields
     */
    public JsonObjectBuilder exportEventJSON(EventEntity entity) {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("id", Objects.nonNull(entity.getId()) ? entity.getId() : 0);
        json.add("name", Objects.nonNull(entity.getName()) ? entity.getName() : "");
        json.add("description", Objects.nonNull(entity.getDescription()) ? entity.getDescription(): "");
        json.add("public", entity.getIsPublic());
        json.add("photoId", Objects.nonNull(entity.getPhoto()) ? entity.getPhoto().getId(): 0);
        json.add("photoETag", Objects.nonNull(entity.getPhoto()) ? entity.getPhoto().getImageHash(): "");
        json.add("eventStart", Objects.nonNull(entity.getEventStart()) ? entity.getEventStart(): 0);
        json.add("repeatWeekDays", (Objects.nonNull(entity.getRepeatWeekDays()) ? entity.getRepeatWeekDays(): 0));
        json.add("repeatDayTime", (Objects.nonNull(entity.getRepeatDayTime()) ? entity.getRepeatDayTime(): 0));

        JsonArrayBuilder members = Json.createArrayBuilder();
        if (Objects.nonNull(entity.getMembers())) {
            for (UserEntity m: entity.getMembers()) {
                if (!m.getStatus().getIsActive()) {
                    continue;
                }
                JsonObjectBuilder member = Json.createObjectBuilder();
                member.add("id", m.getId());
                member.add("name", Objects.nonNull(m.getName()) ? m.getName() : "");
                member.add("photoId", Objects.nonNull(m.getPhoto()) ? m.getPhoto().getId(): 0);
                member.add("photoETag", Objects.nonNull(m.getPhoto()) ? m.getPhoto().getImageHash() : "");
                members.add(member);
            }
        }
        json.add("members", members);

        JsonArrayBuilder locations = Json.createArrayBuilder();
        if (Objects.nonNull(entity.getLocations())) {
            for (EventLocationEntity l: entity.getLocations()) {
                JsonObjectBuilder loc = Json.createObjectBuilder();
                loc.add("id", l.getId());
                loc.add("name", Objects.nonNull(l.getName()) ? l.getName() : "");
                loc.add("description", Objects.nonNull(l.getDescription()) ? l.getDescription() : "");
                loc.add("photoId", Objects.nonNull(l.getPhoto()) ? l.getPhoto().getId(): 0);
                loc.add("photoETag", Objects.nonNull(l.getPhoto()) ? l.getPhoto().getImageHash(): "");
                locations.add( loc );
            }
        }
        json.add("locations", locations);

        String     ownername, ownerphotoetag;
        Long       ownerphotoid;
        Long       ownerid   = entity.getStatus().getIdOwner();
        UserUtils  userutils = new UserUtils(entityManager, userTransaction);
        UserEntity owner     = userutils.findUser(ownerid);
        if (Objects.isNull(owner) || !owner.getStatus().getIsActive()) {
            ownerid = 0L;
            ownername = "";
            ownerphotoid = 0L;
            ownerphotoetag = "";
        }
        else {
            ownername = owner.getName();
            ownerphotoid = Objects.nonNull(owner.getPhoto()) ? owner.getPhoto().getId() : 0L;
            ownerphotoetag = Objects.nonNull(owner.getPhoto()) ? owner.getPhoto().getImageHash(): "";
        }
        json.add("ownerId", ownerid);
        json.add("ownerName", ownername);
        json.add("ownerPhotoId", ownerphotoid);
        json.add("ownerPhotoETag", ownerphotoetag);

        return json;
    }

    /**
     * Given a JSON string, import the necessary fields and create an event entity.
     * 
     * NOTE: Event members and locations are not imported by this method.
     * 
     * @param jsonString  JSON string representing an event entity
     * @return            Event entity or null if the JSON string was not appropriate
     */
    public EventEntity importEventJSON(String jsonString) {
        if (Objects.isNull(jsonString)) {
            return null;
        }

        String name, description, photo;
        Long eventstart, repeatweekdays, repeatdaytime;
        boolean ispublic;
        try {
            JsonReader jreader = Json.createReader(new StringReader(jsonString));
            JsonObject jobject = jreader.readObject();

            name           = jobject.getString("name", null);
            description    = jobject.getString("description", null);
            ispublic       = jobject.getBoolean("public", false);
            photo          = jobject.getString("photo", null);
            eventstart     = new Long(jobject.getInt("eventStart", 0));
            repeatweekdays = new Long(jobject.getInt("repeatWeekDays", 0));
            repeatdaytime  = new Long(jobject.getInt("repeatDayTime", 0));
        }
        catch(Exception ex) {
            Log.warning(TAG, "Could not setup user entity out of given JSON string, reason: " + ex.getLocalizedMessage());
            return null;
        }

        EventEntity entity = new EventEntity();
        if (Objects.nonNull(name)) {
            entity.setName(StringUtils.limitStringLen(name, 32));
        }
        if (Objects.nonNull(description)) {
            entity.setDescription(StringUtils.limitStringLen(description, 1000));
        }
        if (eventstart > 0L) {
            entity.setEventStart(eventstart);
        }
        entity.setIsPublic(ispublic);
        entity.setRepeatWeekDays(repeatweekdays);
        entity.setRepeatDayTime(repeatdaytime);

        if (Objects.nonNull(photo)) {
            ImageEntity image = new ImageEntity();
            // currently we expect only base64 encoded images here
            image.setEncoding(ImageEntity.ENCODING_BASE64);
            image.setContent(photo.getBytes());
            image.updateImageHash();
            entity.setPhoto(image);
        }

        return entity;
    }

    /**
     * Export the given event if it is public, or it belongs to given user, or
     * the user is a member of event.
     * If the user has admin role then the event is exported.
     * 
     * @param event    Events used for filtering the user relevant events from
     * @param user     User     
     * @return         All user relevant events in JSON format
     */
    public JsonObjectBuilder exportUserEventJSON(EventEntity event, UserEntity user) {
        UserUtils         userutils = new UserUtils(entityManager, userTransaction);
        boolean           privuser  = userutils.checkUserRoles(user, Arrays.asList(AuthRole.USER_ROLE_ADMIN));
        JsonObjectBuilder json      = Json.createObjectBuilder();
        boolean           doexp     = event.getStatus().getIsActive()&& 
                                      (privuser || event.getIsPublic() || getUserIsEventOwnerOrMember(user, event));

        if (!doexp) {
            return json;
        }
        return exportEventJSON(event);
    }

    /**
     * Export all public events and those accociated (owner or member) to given user to JSON.
     * If the user has admin role then all events are exported.
     * 
     * @param events   Events used for filtering the user relevant events from
     * @param user     User     
     * @return         All user relevant events in JSON format
     */
    public JsonArrayBuilder exportUserEventsJSON(List<EventEntity> events, UserEntity user) {
        //! NOTE: Although we could make use of method exportUserEventJSON here, we don't in the sake of performance!
        UserUtils        userutils = new UserUtils(entityManager, userTransaction);
        boolean          privuser  = userutils.checkUserRoles(user, Arrays.asList(AuthRole.USER_ROLE_ADMIN));
        JsonArrayBuilder allevents = Json.createArrayBuilder();
        events.stream()
            .filter((event) -> (event.getStatus().getIsActive() && (privuser || event.getIsPublic() || getUserIsEventOwnerOrMember(user, event))))
            .forEach((event) -> {
                allevents.add(exportEventJSON(event));
            });

        return allevents;
    }
}
