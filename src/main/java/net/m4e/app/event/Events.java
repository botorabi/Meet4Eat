/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.event;

import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.util.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.*;

import net.m4e.app.auth.AuthRole;
import net.m4e.app.communication.ConnectedClients;
import net.m4e.app.mailbox.business.MailEntity;
import net.m4e.app.mailbox.business.Mails;
import net.m4e.app.resources.*;
import net.m4e.app.user.business.UserEntity;
import net.m4e.app.user.business.Users;
import net.m4e.common.Entities;
import net.m4e.common.Strings;
import net.m4e.system.core.AppInfoEntity;
import net.m4e.system.core.AppInfos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A collection of event related utilities
 *
 * @author boto
 * Date of creation Sep 4, 2017
 */
@ApplicationScoped
public class Events {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Users users;

    private final Entities entities;

    private final AppInfos appInfos;

    private final Mails mails;

    private final DocumentPool docPool;


    /**
     * Default constructor needed by the container.
     */
    protected Events() {
        entities = null;
        users = null;
        appInfos = null;
        mails = null;
        docPool = null;
    }

    /**
     * Create the Events instance.
     * 
     * @param entities  Entities instance
     * @param users     Users instance
     * @param appInfos  AppInfos instance
     * @param mails     Mails instance
     */
    @Inject
    public Events(Entities entities, Users users, AppInfos appInfos, Mails mails, DocumentPool docPool) {
        this.entities = entities;
        this.users = users;
        this.appInfos = appInfos;
        this.mails = mails;
        this.docPool = docPool;
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
        newevent.setVotingTimeBegin(inputEntity.getVotingTimeBegin());

        if (inputEntity.getPhoto() != null) {
            updateEventImage(newevent, inputEntity.getPhoto());
        }

        // setup the status
        StatusEntity status = new StatusEntity();
        status.setIdCreator(creatorID);
        status.setIdOwner(creatorID);
        Date now = new Date();
        status.setDateCreation(now.getTime());
        status.setDateLastUpdate(now.getTime());
        newevent.setStatus(status);

        createEventEntity(newevent);

        return newevent;
    }

    /**
     * Given an event entity filled with all its fields, create it in database.
     * 
     * @param event         Event entity
     */
    public void createEventEntity(EventEntity event) {
        // photo and members are shared objects, so remove them before event creation
        DocumentEntity photo = event.getPhoto();
        event.setPhoto(null);
        Collection<UserEntity> members = event.getMembers();
        event.setMembers(null);

        entities.create(event);

        // now re-add photo and members to event entity and update it
        event.setPhoto(photo);
        event.setMembers(members);

        entities.update(event);
    }

    /**
     * Delete the given event entity permanently from database.
     * 
     * @param event         Event entity
     */
    public void deleteEvent(EventEntity event) {
        entities.delete(event);
    }

    /**
     * Update event.
     * 
     * @param event       Event entity to update
     */
    public void updateEvent(EventEntity event) {
        entities.update(event);
    }

    /**
     * Try to find an event with given user ID.
     * 
     * @param id Event ID
     * @return Return an entity if found, otherwise return null.
     */
    public EventEntity findEvent(Long id) {
        EventEntity event = entities.find(EventEntity.class, id);
        return event;
    }

    /**
     * Try to find a location in an event. If the location was not found or is not active
     * then return null.
     * 
     * @param eventId       ID of the event to search for locations
     * @param locationId    ID of the location to find
     * @return              Return the location entity if it was found and it is active, otherwise null.
     */
    public EventLocationEntity findEventLocation(Long eventId, Long locationId) {
        EventEntity event = entities.find(EventEntity.class, eventId);
        if ((event == null) || !event.getStatus().getIsActive()){
            return null;
        }
        // go throuh the event locations and check if the given locationId is found among the event locations and is active
        EventLocationEntity loc = null;
        if (event.getLocations() != null) {
            for(EventLocationEntity l: event.getLocations()) {
                if (l.getStatus().getIsActive() && (Objects.equals(l.getId(), locationId))) {
                    loc = l;
                    break;
                }
            }
        }
        return loc;
    }

    /**
     * Update the event image with the content of given image.
     * 
     * @param event         Event entity
     * @param image         Image to set to given event
     * @throws Exception  Throws an exception if something goes wrong.
     */
    public void updateEventImage(EventEntity event, DocumentEntity image) throws Exception {
        // make sure that the resource URL is set
        image.setResourceURL("/Event/Image");
        docPool.updatePhoto(event, image);
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
        if (!owner && (event.getMembers() != null)) {
            if (event.getMembers().stream().anyMatch((u) -> (Objects.equals(u.getId(), user.getId())))) {
                return true;
            } 
        }
        return owner;
    }

    /**
     * Given an event ID return the IDs of all of its members (including the owner). If the event was not
     * found then an empty set is returned.
     * 
     * @param eventId   Event ID
     * @return          A set with member IDs
     */
    public Set<Long> getMembers(Long eventId) {
        Set<Long> memberids = new HashSet();
        EventEntity event = findEvent(eventId);
        if ((event == null) || !event.getStatus().getIsActive()) {
            return memberids;
        }

        Collection<UserEntity> members = event.getMembers();
        // avoid duplicate IDs by using a set (the sender can be also the owner or part of the members)
        memberids.add(event.getStatus().getIdOwner());
        if (members != null) {
            members.forEach((m) -> {
                memberids.add(m.getId());
            });
        }
        return memberids;
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
        if (members == null) {
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
        if (members == null) {
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
    public void removeAnyMember(EventEntity event, List<UserEntity> usersToRemove) {
        for (UserEntity user: usersToRemove) {
            Collection<UserEntity> members = event.getMembers();
            if (members == null) {
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
        StatusEntity status = event.getStatus();
        if (status == null) {
            throw new Exception("Event has no status field!");
        }
        status.setDateDeletion((new Date().getTime()));
        entities.update(event);

        // update the app stats
        AppInfoEntity appinfo = appInfos.getAppInfoEntity();
        if (appinfo == null) {
            throw new Exception("Problem occured while retrieving AppInfo entity!");
        }
        appinfo.incrementEventCountPurge(1L);
        entities.update(appinfo);
    }

    /**
     * Get all events which are marked as deleted.
     * 
     * @return List of events which are marked as deleted.
     */
    public List<EventEntity> getMarkedAsDeletedEvents() {
        List<EventEntity> events = entities.findAll(EventEntity.class);
        List<EventEntity> deletedevents = new ArrayList<>();
        // speed up the task by using parallel processing
        events.stream().parallel()
            .filter((event) -> (event.getStatus().getIsDeleted()))
            .forEach((event) -> {
                deletedevents.add(event);
            });

        return deletedevents;
    }

    /**
     * Get all event locations which are marked as deleted.
     * 
     * @return List of event locations which are marked as deleted.
     */
    public List<EventLocationEntity> getMarkedAsDeletedEventLocations() {
        List<EventLocationEntity> eventlocs = entities.findAll(EventLocationEntity.class);
        List<EventLocationEntity> deletedeventlocs = new ArrayList<>();
        // speed up the task by using parallel processing
        eventlocs.stream().parallel()
            .filter((loc) -> (loc.getStatus().getIsDeleted()))
            .forEach((loc) -> {
                deletedeventlocs.add(loc);
            });

        return deletedeventlocs;
    }

    /**
     * Create a inbox message for a new event member.
     * 
     * @param event     The event
     * @param member    The new member
     */
    void createEventJoiningMail(EventEntity event, UserEntity member) {
        MailEntity mail = new MailEntity();
        mail.setSenderId(0L);
        mail.setReceiverId(member.getId());
        mail.setReceiverName(member.getName());
        mail.setSendDate((new Date()).getTime());
        mail.setSubject("You joined an event");
        mail.setContent("Hi " + member.getName() + ",\n\nwe wanted to let you know that you joined the event '" +
                                event.getName() + "'.\n\nBest Regards\nMeet4Eat Team\n");
        try {
            mails.createMail(mail);
        }
        catch (Exception ex) {
            LOGGER.warn("*** could not create mail, reason: " + ex.getLocalizedMessage());
        }
    }

    /**
     * Create a inbox message for a member who has left an event. The event is sent
     * to the event owner and the member itself.
     * 
     * @param event     The event
     * @param member    Member who left the event
     */
    void createEventLeavingMail(EventEntity event, UserEntity member) {
        MailEntity mailuser = new MailEntity();
        mailuser.setSenderId(0L);
        mailuser.setReceiverId(member.getId());
        mailuser.setReceiverName(member.getName());
        mailuser.setSendDate((new Date()).getTime());
        mailuser.setSubject("You have left an event");
        mailuser.setContent("Hi " + member.getName() + ",\n\nwe wanted to confirm that you have left the event '" +
                                event.getName() + "'.\n\nBest Regards\nMeet4Eat Team\n");

        UserEntity ownerentity = entities.find(UserEntity.class, event.getStatus().getIdOwner());

        MailEntity mailowner = new MailEntity();
        mailowner.setSenderId(0L);
        mailowner.setReceiverId(ownerentity.getId());
        mailowner.setReceiverName(ownerentity.getName());
        mailowner.setSendDate((new Date()).getTime());
        mailowner.setSubject("A member has left your event");
        mailowner.setContent("Hi " + ownerentity.getName() + ",\n\nwe wanted to let you know that member '" + member.getName() + "' has left your event '" +
                                event.getName() + "'.\n\nBest Regards\nMeet4Eat Team\n");
        try {
            mails.createMail(mailuser);
            mails.createMail(mailowner);
        }
        catch (Exception ex) {
            LOGGER.warn("*** could not create mail, reason: " + ex.getLocalizedMessage());
        }
    }

    /**
     * Given an event entity, export the necessary fields into a JSON object.
     * 
     * @param entity        Event entity to export
     * @param connections   Real-time user connections
     * @return              A JSON object containing builder the proper entity fields
     */
    public JsonObjectBuilder exportEventJSON(EventEntity entity, ConnectedClients connections) {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("id", (entity.getId() != null) ? entity.getId().toString() : "")
            .add("name", (entity.getName() != null) ? entity.getName() : "")
            .add("description", (entity.getDescription() != null) ? entity.getDescription(): "")
            .add("public", entity.getIsPublic())
            .add("photoId", (entity.getPhoto() != null) ? entity.getPhoto().getId().toString(): "")
            .add("photoETag", (entity.getPhoto() != null) ? entity.getPhoto().getETag(): "")
            .add("eventStart", (entity.getEventStart() != null) ? entity.getEventStart(): 0)
            .add("repeatWeekDays", (entity.getRepeatWeekDays() != null) ? entity.getRepeatWeekDays(): 0)
            .add("repeatDayTime", (entity.getRepeatDayTime() != null) ? entity.getRepeatDayTime(): 0)
            .add("votingTimeBegin", (entity.getVotingTimeBegin() != null) ? entity.getVotingTimeBegin(): 0);

        JsonArrayBuilder members = Json.createArrayBuilder();
        if (entity.getMembers() != null) {
            entity.getMembers()
                .stream()
                .filter((mem) -> (mem.getStatus().getIsActive()))
                .map((mem) -> {
                    JsonObjectBuilder member = Json.createObjectBuilder();
                    member.add("id", mem.getId().toString())
                          .add("name", (mem.getName() != null) ? mem.getName() : "")
                          .add("photoId", (mem.getPhoto() != null) ? mem.getPhoto().getId().toString(): "")
                          .add("photoETag", (mem.getPhoto() != null) ? mem.getPhoto().getETag() : "");
                    // set the online status
                    boolean online = (connections.getConnectedUser(mem.getId()) != null);
                    member.add("status", online ? "online" : "offline");
                    return member;
                })
                .forEach((memobj) -> {
                    members.add(memobj);
                });
        }
        json.add("members", members);

        JsonArrayBuilder locations = Json.createArrayBuilder();
        if (entity.getLocations() != null) {
            entity.getLocations()
                .stream()
                .filter((location) -> (location.getStatus().getIsActive()))
                .map((location) -> {
                    JsonObjectBuilder loc = Json.createObjectBuilder();
                    loc.add("id", location.getId().toString())
                       .add("name", (location.getName() != null) ? location.getName() : "")
                       .add("description", (location.getDescription() != null) ? location.getDescription() : "")
                       .add("photoId", (location.getPhoto() != null) ? location.getPhoto().getId().toString(): "")
                       .add("photoETag", (location.getPhoto() != null) ? location.getPhoto().getETag(): "");
                    return loc;
                })
                .forEach((loc) -> {
                    locations.add( loc );
                });
        }
        json.add("locations", locations);

        String     ownername, ownerphotoetag;
        Long       ownerphotoid;
        Long       ownerid   = entity.getStatus().getIdOwner();
        UserEntity owner     = users.findUser(ownerid);
        boolean    owneronline;
        if ((owner == null) || !owner.getStatus().getIsActive()) {
            owneronline = false;
            ownerid = 0L;
            ownername = "";
            ownerphotoid = 0L;
            ownerphotoetag = "";
        }
        else {
            ownername = owner.getName();
            ownerphotoid = (owner.getPhoto() != null) ? owner.getPhoto().getId() : 0L;
            ownerphotoetag = (owner.getPhoto() != null) ? owner.getPhoto().getETag(): "";
            owneronline = (connections.getConnectedUser(owner.getId()) != null);
        }
        json.add("ownerId", (ownerid > 0)? ownerid.toString() : "")
            .add("ownerName", ownername)
            .add("ownerPhotoId", (ownerphotoid > 0)? ownerphotoid.toString() : "")
            .add("ownerPhotoETag", ownerphotoetag)
            .add("status", owneronline ? "online" : "offline");
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
        if (jsonString == null) {
            return null;
        }

        String name, description, photo;
        Long eventstart, repeatweekdays, repeatdaytime, votingbegin;
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
            votingbegin    = new Long(jobject.getInt("votingTimeBegin", 0));
        }
        catch(Exception ex) {
            LOGGER.warn("Could not setup user entity out of given JSON string, reason: " + ex.getLocalizedMessage());
            return null;
        }

        EventEntity entity = new EventEntity();
        if (name != null) {
            entity.setName(Strings.limitStringLen(name, 32));
        }
        if (description != null) {
            entity.setDescription(Strings.limitStringLen(description, 1000));
        }
        if (eventstart > 0L) {
            entity.setEventStart(eventstart);
        }
        entity.setIsPublic(ispublic);
        entity.setRepeatWeekDays(repeatweekdays);
        entity.setRepeatDayTime(repeatdaytime);
        entity.setVotingTimeBegin(votingbegin);

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
     * Export the given event if it is public, or it belongs to given user, or
     * the user is a member of event.
     * If the user has admin role then the event is exported.
     * 
     * @param event         Events used for filtering the user relevant events from
     * @param user          User
     * @param connections   Real-time user connections
     * @return              All user relevant events in JSON format
     */
    public JsonObjectBuilder exportUserEventJSON(EventEntity event, UserEntity user, ConnectedClients connections) {
        boolean           privuser  = users.checkUserRoles(user, Arrays.asList(AuthRole.USER_ROLE_ADMIN));
        JsonObjectBuilder json      = Json.createObjectBuilder();
        boolean           doexp     = event.getStatus().getIsActive()&& 
                                      (privuser || event.getIsPublic() || getUserIsEventOwnerOrMember(user, event));

        if (!doexp) {
            return json;
        }
        return exportEventJSON(event, connections);
    }

    /**
     * Export all public events and those associated (owner or member) to given user to JSON.
     * If the user has admin role then all events are exported.
     * 
     * @param events        Events used for filtering the user relevant events from
     * @param user          User
     * @param connections   Real-time user connections
     * @return              All user relevant events in JSON format
     */
    public JsonArrayBuilder exportUserEventsJSON(List<EventEntity> events, UserEntity user, ConnectedClients connections) {
        //! NOTE: Although we could make use of method exportUserEventJSON here, we don't in the sake of performance!
        boolean          privuser  = users.checkUserRoles(user, Arrays.asList(AuthRole.USER_ROLE_ADMIN));
        JsonArrayBuilder allevents = Json.createArrayBuilder();
        events.stream()
            .filter((event) -> (event.getStatus().getIsActive() && (privuser || event.getIsPublic() || getUserIsEventOwnerOrMember(user, event))))
            .forEach((event) -> {
                allevents.add(exportEventJSON(event, connections));
            });

        return allevents;
    }

}
