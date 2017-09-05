/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.event;

import java.io.StringReader;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import net.m4e.common.EntityUtils;
import net.m4e.common.ImageEntity;
import net.m4e.common.StatusEntity;
import net.m4e.common.StringUtils;
import net.m4e.core.AppInfoEntity;
import net.m4e.core.AppInfoUtils;
import net.m4e.core.Log;
import net.m4e.user.UserEntity;

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
     * Given an event entity filled with all its fields, create it in database.
     * 
     * @param event         Event entity
     * @throws Exception    Throws exception if any problem occurred.
     */
    public void createEvent(EventEntity event) throws Exception {
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
     * Update event.
     * 
     * @param event Event entity to update
     */
    public void updateEvent(EventEntity event) {
        EntityUtils eutils = new EntityUtils(entityManager, userTransaction);
        try {
            eutils.updateEntity(event);
        }
        catch (Exception ex) {
            Log.error(TAG, "*** Could not update event '" + event.getName() + "', id: " + event.getId());
        }
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
     * Delete the given event entity in database.
     * 
     * @param event         Event entity
     * @throws Exception    Throws exception if any problem occurred.
     */
    public void deleteEvent(EventEntity event) throws Exception {
        EntityUtils eutils = new EntityUtils(entityManager, userTransaction);
        eutils.deleteEntity(event);
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
        json.add("photoId", Objects.nonNull(entity.getPhoto()) ? entity.getPhoto().getId(): 0);
        json.add("eventStart", Objects.nonNull(entity.getEventStart()) ? entity.getEventStart(): 0);
        json.add("repeatWeekDays", (Objects.nonNull(entity.getRepeatWeekDays()) ? entity.getRepeatWeekDays(): 0));
        json.add("repeatDayTime", (Objects.nonNull(entity.getRepeatDayTime()) ? entity.getRepeatDayTime(): 0));

        JsonObjectBuilder members = Json.createObjectBuilder();
        if (Objects.nonNull(entity.getMembers())) {
            for (UserEntity m: entity.getMembers()) {
                members.add("id", m.getId());
                members.add("name", Objects.nonNull(m.getName()) ? m.getName() : "");
            }
        }
        json.add("members", members);

        JsonObjectBuilder locations = Json.createObjectBuilder();
        if (Objects.nonNull(entity.getLocations())) {
            for (EventLocationEntity l: entity.getLocations()) {
                locations.add("id", l.getId());
                locations.add("name", Objects.nonNull(l.getName()) ? l.getName() : "");
            }
        }
        json.add("locations", locations);

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

        String name, description;
        Long eventstart, repeatweekdays, repeatdaytime, photoid;
        try {
            JsonReader jreader = Json.createReader(new StringReader(jsonString));
            JsonObject jobject = jreader.readObject();

            name           = jobject.getString("name", null);
            description    = jobject.getString("description", null);
            photoid        = new Long(jobject.getInt("photoId", 0));
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
        entity.setRepeatWeekDays(repeatweekdays);
        entity.setRepeatDayTime(repeatdaytime);

        if (photoid > 0L) {
            EntityUtils eutils = new EntityUtils(entityManager, userTransaction);
            ImageEntity image = eutils.findEntity(ImageEntity.class, photoid);
            entity.setPhoto(image);
        }

        return entity;
    }
}
