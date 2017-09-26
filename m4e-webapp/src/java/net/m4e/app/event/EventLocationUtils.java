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
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.persistence.EntityManager;
import net.m4e.app.resources.DocumentEntity;
import net.m4e.app.resources.DocumentPool;
import net.m4e.common.EntityUtils;
import net.m4e.app.resources.StatusEntity;
import net.m4e.common.StringUtils;
import net.m4e.system.core.AppInfoEntity;
import net.m4e.system.core.AppInfoUtils;
import net.m4e.system.core.Log;

/**
 * A collection of event location related utilities
 *
 * @author boto
 * Date of creation Sep 13, 2017
 */
public class EventLocationUtils {

    /**
     * Used for logging
     */
    private final static String TAG = "EventLocationUtils";

    private final EntityManager entityManager;

    /**
     * Create an instance of event utilities.
     * 
     * @param entityManager    Entity manager
     */
    public EventLocationUtils(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Create a new event location in database.
     * 
     * @param event        The event getting the new location
     * @param inputEntity  Entity containing the new location data
     * @param creatorID    Creator ID
     * @return             A new created event location entity if successfully, otherwise null.
     */
    public EventLocationEntity createNewLocation(EventEntity event, EventLocationEntity inputEntity, Long creatorID) throws Exception {
        // setup the new entity
        EventLocationEntity newlocation = new EventLocationEntity();
        newlocation.setName(inputEntity.getName());
        newlocation.setDescription(inputEntity.getDescription());

        // setup the status
        StatusEntity status = new StatusEntity();
        status.setIdCreator(creatorID);
        status.setIdOwner(creatorID);
        Date now = new Date();
        status.setDateCreation(now.getTime());
        status.setDateLastUpdate(now.getTime());
        newlocation.setStatus(status);

        EntityUtils eutils = new EntityUtils(entityManager);
        eutils.createEntity(newlocation);
        Collection<EventLocationEntity> locs = event.getLocations();
        if (Objects.isNull(locs)) {
            locs = new ArrayList<>();
            event.setLocations(locs);
        }
        event.getLocations().add(newlocation);
        eutils.updateEntity(event);
        return newlocation;
    }

    /**
     * Update the given entity in database.
     * 
     * @param inputLocation  The location containing the updates
     * @return               Updated location
     * @throws Exception     Throws an exception if something went wrong.
     */
    EventLocationEntity updateLocation(EventLocationEntity inputLocation) throws Exception {
        EntityUtils entityutils = new EntityUtils(entityManager);
        EventLocationEntity location = entityutils.findEntity(EventLocationEntity.class, inputLocation.getId());
        if (Objects.isNull(location) || !location.getStatus().getIsActive()) {
            throw new Exception("Entity location does not exist.");
        }

        if (Objects.nonNull(inputLocation.getName())) {
            location.setName(inputLocation.getName());
        }
        if (Objects.nonNull(inputLocation.getDescription())) {
            location.setDescription(inputLocation.getDescription());
        }
        if (Objects.nonNull(inputLocation.getPhoto())) {
            updateEventLocationImage(location, inputLocation.getPhoto());
        }

        entityutils.updateEntity(location);
        return location;
    }

    /**
     * Update the event location image with the content of given image.
     * 
     * @param location      Event location entity
     * @param image         Image to set to given event
     * @throws Exception    Throws exception if any problem occurred.
     */
    void updateEventLocationImage(EventLocationEntity location, DocumentEntity image) throws Exception {
        DocumentPool imagepool = new DocumentPool(entityManager);
        DocumentEntity img = imagepool.getOrCreatePoolDocument(image.getETag());
        if (!imagepool.compareETag(location.getPhoto(), img.getETag())) {
            imagepool.releasePoolDocument(location.getPhoto());
        }
        img.setContent(image.getContent());
        img.updateETag();
        img.setType(DocumentEntity.TYPE_IMAGE);
        img.setEncoding(image.getEncoding());
        img.setResourceURL("/EventLoction/Image");
        location.setPhoto(img);
    }

    /**
     * Try to find an event location with given user ID.
     * 
     * @param id Event location ID
     * @return Return an entity if found, otherwise return null.
     */
    public EventLocationEntity findLocation(Long id) {
        EntityUtils eutils = new EntityUtils(entityManager);
        EventLocationEntity event = eutils.findEntity(EventLocationEntity.class, id);
        return event;
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
        if (Objects.isNull(locations) || !locations.contains(locationToRemove)) {
            throw new Exception("Location is not part of event.");
        }
        // mark the location entity as deleted
        locationToRemove.getStatus().setDateDeletion((new Date()).getTime());
        EntityUtils eutils = new EntityUtils(entityManager);
        eutils.updateEntity(locationToRemove);

        // update the app stats
        AppInfoUtils autils = new AppInfoUtils(entityManager);
        AppInfoEntity appinfo = autils.getAppInfoEntity();
        if (Objects.isNull(appinfo)) {
            throw new Exception("Problem occured while retrieving AppInfo entity!");
        }
        appinfo.incrementEventLocationCountPurge(1L);
        eutils.updateEntity(appinfo);
    }

    /**
     * Given a JSON string, import the necessary fields and create an event location entity.
     * 
     * @param jsonString  JSON string representing an event location entity
     * @return            Event location entity or null if the JSON string was not appropriate
     */
    public EventLocationEntity importLocationJSON(String jsonString) {
        if (Objects.isNull(jsonString)) {
            return null;
        }

        String name, idstring, description, photo;
        try {
            JsonReader jreader = Json.createReader(new StringReader(jsonString));
            JsonObject jobject = jreader.readObject();
            idstring    = jobject.getString("id", "0");
            name        = jobject.getString("name", null);
            description = jobject.getString("description", null);
            photo       = jobject.getString("photo", null);
        }
        catch(Exception ex) {
            Log.warning(TAG, "Could not setup an event loaction given JSON string, reason: " + ex.getLocalizedMessage());
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
        if (Objects.nonNull(name)) {
            entity.setName(StringUtils.limitStringLen(name, 32));
        }
        if (Objects.nonNull(description)) {
            entity.setDescription(StringUtils.limitStringLen(description, 1000));
        }

        if (Objects.nonNull(photo)) {
            DocumentEntity image = new DocumentEntity();
            // currently we expect only base64 encoded images here
            image.setEncoding(DocumentEntity.ENCODING_BASE64);
            image.setContent(photo.getBytes());
            image.updateETag();
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
        json.add("id", Objects.nonNull(entity.getId()) ? entity.getId() : 0);
        json.add("name", Objects.nonNull(entity.getName()) ? entity.getName() : "");
        json.add("description", Objects.nonNull(entity.getDescription()) ? entity.getDescription(): "");
        json.add("photoId", Objects.nonNull(entity.getPhoto()) ? entity.getPhoto().getId(): 0);
        json.add("photoETag", Objects.nonNull(entity.getPhoto()) ? entity.getPhoto().getETag() : "");
        return json;
    }
}
