/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.common;

import net.m4e.app.event.business.EventLocationEntity;
import net.m4e.app.resources.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Base class for event related tests
 *
 * @author boto
 * Date of creation February 16, 2018
 */
public class EventLocationEntityCreator {

    public static Long EVENT_LOCATION_ID = 1000L;
    public static String EVENT_LOCATION_NAME = "My Event Location";
    public static String EVENT_LOCATION_DESCRIPTION = "Event location description...";
    public static Long EVENT_LOCATION_STATUS_ID = 3000L;
    public static Long EVENT_LOCATION_PHOTO_ID = 4000L;
    public static String EVENT_LOCATION_PHOTO_ETAG = "PhotoETAG";
    public static Long EVENT_LOCATION_DATE_CREATION = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();

    /**
     * Create an event location entity with default data.
     */
    static public EventLocationEntity create() {
        EventLocationEntity event = new EventLocationEntity();
        event.setId(EVENT_LOCATION_ID);
        event.setName(EVENT_LOCATION_NAME);
        event.setDescription(EVENT_LOCATION_DESCRIPTION);

        StatusEntity status = new StatusEntity();
        status.setId(EVENT_LOCATION_STATUS_ID);
        status.setDateCreation(EVENT_LOCATION_DATE_CREATION);
        status.setIdOwner(event.getId());
        event.setStatus(status);

        DocumentEntity photo = new DocumentEntity();
        photo.setId(EVENT_LOCATION_PHOTO_ID);
        photo.setDocumentETag(EVENT_LOCATION_PHOTO_ETAG);
        event.setPhoto(photo);

        return event;
    }
}
