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
import javax.json.JsonReader;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import net.m4e.common.EntityUtils;
import net.m4e.app.resources.StatusEntity;
import net.m4e.common.StringUtils;
import net.m4e.system.core.Log;

/**
 * A collection of event related utilities
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

    private final UserTransaction userTransaction;

    /**
     * Create an instance of event utilities.
     * 
     * @param entityManager    Entity manager
     * @param userTransaction  User transaction
     */
    public EventLocationUtils(EntityManager entityManager, UserTransaction userTransaction) {
        this.entityManager = entityManager;
        this.userTransaction = userTransaction;
    }

   /**
     * Given a JSON string as input containing event location data, validate 
     * all fields and return an EventLocationEntity, or throw an exception if the validation failed.
     * 
     * @param locationJson   Event location data in JSON format
     * @return               An EventLocationEntity created out of given input
     * @throws Exception     Throws an exception if the validation fails.
     */
    public EventLocationEntity validateLocationInput(String locationJson) throws Exception {
        EventLocationEntity entity = importLocationJSON(locationJson);
        if (Objects.isNull(entity)) {
            throw new Exception("Failed to validate location input.");
        }
         // perform some checks
        if (Objects.isNull(entity.getName()) || entity.getName().isEmpty()) {
            throw new Exception("Missing location name.");
        }

        return entity;
    }

    /**
     * Create a new event location in database.
     * 
     * @param event
     * @param inputEntity
     * @param creatorID
     * @return 
     */
    public EventLocationEntity createNewLocation(EventEntity event, EventLocationEntity inputEntity, Long creatorID) {
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

        try {
            EntityUtils eutils = new EntityUtils(entityManager, userTransaction);
            eutils.createEntity(newlocation);
            Collection<EventLocationEntity> locs = event.getLocations();
            if (Objects.isNull(locs)) {
                locs = new ArrayList<>();
                event.setLocations(locs);
            }
            event.getLocations().add(newlocation);
            eutils.updateEntity(event);
        }
        catch (Exception ex) {
            Log.warning(TAG, "could not create location entity, reason: " + ex.getLocalizedMessage());
            return null;
        }
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
        EntityUtils entityutils = new EntityUtils(entityManager, userTransaction);
        EventLocationEntity location = entityutils.findEntity(EventLocationEntity.class, inputLocation.getId());
        if (Objects.isNull(location)) {
            throw new Exception("Entity location does not exist.");
        }
        
        if (Objects.isNull(inputLocation.getName())) {
            location.setName(inputLocation.getName());
        }
        if (Objects.isNull(inputLocation.getDescription())) {
            location.setDescription(inputLocation.getDescription());
        }
        //! TODO photo

        entityutils.updateEntity(location);
        return location;
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

        String name, description;
        int id;
        try {
            JsonReader jreader = Json.createReader(new StringReader(jsonString));
            JsonObject jobject = jreader.readObject();
            id             = jobject.getInt("id", 0);
            name           = jobject.getString("name", null);
            description    = jobject.getString("description", null);
            //! TODO import photo, maybe in base64
        }
        catch(Exception ex) {
            Log.warning(TAG, "Could not setup an event loaction given JSON string, reason: " + ex.getLocalizedMessage());
            return null;
        }

        EventLocationEntity entity = new EventLocationEntity();
        if (id != 0) {
            entity.setId(new Long(id));
        }
        if (Objects.nonNull(name)) {
            entity.setName(StringUtils.limitStringLen(name, 32));
        }
        if (Objects.nonNull(description)) {
            entity.setDescription(StringUtils.limitStringLen(description, 1000));
        }
        return entity;
    }
}
