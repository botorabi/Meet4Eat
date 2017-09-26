/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.event;

import java.util.Collection;
import java.util.Objects;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import net.m4e.common.StringUtils;

/**
 * This class validates event entity related inputs from a client.
 * 
 * @author boto
 * Date of creation Sep 13, 2017
 */
public class EventEntityInputValidator {

    /* Min/max string length for user input fields */
    private final int EVENT_INPUT_MIN_LEN_NAME  = 4;
    private final int EVENT_INPUT_MAX_LEN_NAME  = 32;
    private final int EVENT_INPUT_MIN_LEN_DESC  = 0;
    private final int EVENT_INPUT_MAX_LEN_DESC  = 1000;

    private final EntityManager entityManager;

    private final UserTransaction userTransaction;

    /**
     * Create an instance of input validator.
     * 
     * @param entityManager    Entity manager
     * @param userTransaction  User transaction
     */
    public EventEntityInputValidator(EntityManager entityManager, UserTransaction userTransaction) {
        this.entityManager = entityManager;
        this.userTransaction = userTransaction;
    }

   /**
     * Given a JSON string as input containing data for creating a new event, validate 
     * all fields and return an EventEntity, or throw an exception if the validation failed.
     * 
     * @param eventJson      Data for creating a new event in JSON format
     * @return               A EventEntity created out of given input
     * @throws Exception     Throws an exception if the validation fails.
     */
    public EventEntity validateNewEntityInput(String eventJson) throws Exception {
        EventUtils eventutils = new EventUtils(entityManager, userTransaction);
        EventEntity entity = eventutils.importEventJSON(eventJson);
        if (entity == null) {
            throw new Exception("Failed to create event, invalid input.");
        }

        if (!StringUtils.checkMinMaxLength(entity.getName(), EVENT_INPUT_MIN_LEN_NAME, EVENT_INPUT_MAX_LEN_NAME)) {
            throw new Exception(getLenRangeText("Event name", EVENT_INPUT_MIN_LEN_NAME, EVENT_INPUT_MAX_LEN_NAME));
        }
        if (Objects.nonNull(entity.getDescription())) {
            if (!StringUtils.checkMinMaxLength(entity.getDescription(), EVENT_INPUT_MIN_LEN_DESC, EVENT_INPUT_MAX_LEN_DESC)) {
                throw new Exception(getLenRangeText("Event description", EVENT_INPUT_MIN_LEN_DESC, EVENT_INPUT_MAX_LEN_DESC));
            }            
        }
        return entity;
    }

    /**
     * Given a JSON string as input containing data for updating an existing event, validate 
     * all fields and return an UserEntity, or throw an exception if the validation failed.
     * 
     * @param userJson       Data for creating a new user in JSON format
     * @return               An EventEntity created out of given input
     * @throws Exception     Throws an exception if the validation fails.
     */
    public EventEntity validateUpdateEntityInput(String userJson) throws Exception {
        EventUtils  eventutils = new EventUtils(entityManager, userTransaction);
        EventEntity entity = eventutils.importEventJSON(userJson);
        if (Objects.isNull(entity)) {
            throw new Exception("Failed to update event, invalid input.");
        }

        // NOTE: for updating an entity, the some fields may not exist. those fields do not get changed, it is.
        if (Objects.nonNull(entity.getName()) && !StringUtils.checkMinMaxLength(entity.getName(), EVENT_INPUT_MIN_LEN_NAME, EVENT_INPUT_MAX_LEN_NAME)) {
            throw new Exception(getLenRangeText("Event name", EVENT_INPUT_MIN_LEN_NAME, EVENT_INPUT_MAX_LEN_NAME));
        }
        if (Objects.nonNull(entity.getDescription()) && !StringUtils.checkMinMaxLength(entity.getDescription(), EVENT_INPUT_MIN_LEN_DESC, EVENT_INPUT_MAX_LEN_DESC)) {
            throw new Exception(getLenRangeText("Event description", EVENT_INPUT_MIN_LEN_DESC, EVENT_INPUT_MAX_LEN_DESC));
        }
        return entity;
    }

   /**
     * Given a JSON string as input containing event location data, validate 
     * all fields and return an EventLocationEntity, or throw an exception if the validation failed.
     * 
     * @param locationJson   Event location data in JSON format
     * @param event          Event the location belongs to
     * @return               An EventLocationEntity created out of given input
     * @throws Exception     Throws an exception if the validation fails.
     */
    public EventLocationEntity validateLocationInput(String locationJson, EventEntity event) throws Exception {
        EventLocationUtils  locationutils = new EventLocationUtils(entityManager, userTransaction);
        EventLocationEntity entity = locationutils.importLocationJSON(locationJson);
        if (Objects.isNull(entity)) {
            throw new Exception("Failed to validate location input.");
        }

        if (!StringUtils.checkMinMaxLength(entity.getName(), EVENT_INPUT_MIN_LEN_NAME, EVENT_INPUT_MAX_LEN_NAME)) {
            throw new Exception(getLenRangeText("Event name", EVENT_INPUT_MIN_LEN_NAME, EVENT_INPUT_MAX_LEN_NAME));
        }

        if (Objects.nonNull(entity.getDescription())) {
            if (!StringUtils.checkMinMaxLength(entity.getDescription(), EVENT_INPUT_MIN_LEN_DESC, EVENT_INPUT_MAX_LEN_DESC)) {
                throw new Exception(getLenRangeText("Event description", EVENT_INPUT_MIN_LEN_DESC, EVENT_INPUT_MAX_LEN_DESC));
            }            
        }
        return entity;
    }

    /**
     * Check if the given location name is unique considering all event's location.
     * 
     * @param event
     * @param locationName
     * @return 
     */
    public boolean validateUniqueLocationName(EventEntity event, String locationName) {
        Collection<EventLocationEntity> locs = event.getLocations();
        if (Objects.nonNull(locs)) {
            for (EventLocationEntity loc: locs) {
                if (loc.getStatus().getIsActive() && Objects.equals(loc.getName(), locationName)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Generate a text describing the string length range.
     * 
     * @param field    String field name
     * @param minLen   Minimal length
     * @param maxLen   Maximal length
     * @return         Range text
     */
    private String getLenRangeText(String field, int minLen, int maxLen) {
        return field + " must be at least " + minLen + " and not exceed " + maxLen + " characters.";
    }
}
