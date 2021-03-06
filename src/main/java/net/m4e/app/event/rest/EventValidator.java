/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.rest;

import net.m4e.app.event.business.*;
import net.m4e.app.event.rest.comm.*;
import net.m4e.common.Strings;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;

/**
 * This class validates event entity related inputs from a client.
 * 
 * @author boto
 * Date of creation Sep 13, 2017
 */
@ApplicationScoped
public class EventValidator {

    /* Min/max string length for user input fields */
    private final int EVENT_INPUT_MIN_LEN_NAME  = 4;
    private final int EVENT_INPUT_MAX_LEN_NAME  = 32;
    private final int EVENT_INPUT_MIN_LEN_DESC  = 0;
    private final int EVENT_INPUT_MAX_LEN_DESC  = 1000;

    private final Events events;

    private final EventLocations eventLocations;


    /**
     * Default constructor needed by container.
     */
    protected EventValidator() {
        this.events = null;
        this.eventLocations = null;
    }

    /**
     * Create an instance of input validator.
     * 
     * @param events            Events instance
     * @param eventLocations    Event locations instance
     */
    @Inject
    public EventValidator(Events events, EventLocations eventLocations) {
        this.events = events;
        this.eventLocations = eventLocations;
    }

   /**
     * Given a JSON string as input containing data for creating a new event, validate 
     * all fields and return an EventEntity, or throw an exception if the validation failed.
     * 
     * @param eventCmd       Data for creating a new event
     * @return               A EventEntity created out of given input
     * @throws Exception     Throws an exception if the validation fails.
     */
    public EventEntity validateNewEntityInput(EventCmd eventCmd) throws Exception {
        EventEntity entity = events.importEvent(eventCmd);
        if (entity == null) {
            throw new Exception("Failed to create event, invalid input.");
        }

        if (!Strings.checkMinMaxLength(entity.getName(), EVENT_INPUT_MIN_LEN_NAME, EVENT_INPUT_MAX_LEN_NAME)) {
            throw new Exception(getLenRangeText("Event name", EVENT_INPUT_MIN_LEN_NAME, EVENT_INPUT_MAX_LEN_NAME));
        }
        if (entity.getDescription() != null) {
            if (!Strings.checkMinMaxLength(entity.getDescription(), EVENT_INPUT_MIN_LEN_DESC, EVENT_INPUT_MAX_LEN_DESC)) {
                throw new Exception(getLenRangeText("Event description", EVENT_INPUT_MIN_LEN_DESC, EVENT_INPUT_MAX_LEN_DESC));
            }            
        }
        return entity;
    }

    /**
     * Given a JSON string as input containing data for updating an existing event, validate 
     * all fields and return an UserEntity, or throw an exception if the validation failed.
     * 
     * @param eventCmd       Data for updating an existing user
     * @return               An EventEntity created out of given input
     * @throws Exception     Throws an exception if the validation fails.
     */
    public EventEntity validateUpdateEntityInput(EventCmd eventCmd) throws Exception {
        EventEntity entity = events.importEvent(eventCmd);
        if (entity == null) {
            throw new Exception("Failed to update event, invalid input.");
        }

        // NOTE: for updating an entity, the some fields may not exist. those fields do not get changed, it is.
        if ((entity.getName() != null) && !Strings.checkMinMaxLength(entity.getName(), EVENT_INPUT_MIN_LEN_NAME, EVENT_INPUT_MAX_LEN_NAME)) {
            throw new Exception(getLenRangeText("Event name", EVENT_INPUT_MIN_LEN_NAME, EVENT_INPUT_MAX_LEN_NAME));
        }
        if ((entity.getDescription() != null) && !Strings.checkMinMaxLength(entity.getDescription(), EVENT_INPUT_MIN_LEN_DESC, EVENT_INPUT_MAX_LEN_DESC)) {
            throw new Exception(getLenRangeText("Event description", EVENT_INPUT_MIN_LEN_DESC, EVENT_INPUT_MAX_LEN_DESC));
        }
        return entity;
    }

   /**
     * Given a JSON string as input containing event location data, validate 
     * all fields and return an EventLocationEntity, or throw an exception if the validation failed.
     * 
     * @param locationCmd    Event location data
     * @param event          Event the location belongs to
     * @return               An EventLocationEntity created out of given input
     * @throws Exception     Throws an exception if the validation fails.
     */
    public EventLocationEntity validateLocationInput(EventLocationCmd locationCmd, EventEntity event) throws Exception {
        EventLocationEntity entity = eventLocations.importLocation(locationCmd);
        if (entity == null) {
            throw new Exception("Failed to validate location input.");
        }

        if (!Strings.checkMinMaxLength(entity.getName(), EVENT_INPUT_MIN_LEN_NAME, EVENT_INPUT_MAX_LEN_NAME)) {
            throw new Exception(getLenRangeText("Event name", EVENT_INPUT_MIN_LEN_NAME, EVENT_INPUT_MAX_LEN_NAME));
        }

        if (entity.getDescription() != null) {
            if (!Strings.checkMinMaxLength(entity.getDescription(), EVENT_INPUT_MIN_LEN_DESC, EVENT_INPUT_MAX_LEN_DESC)) {
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
        if (locs != null) {
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
