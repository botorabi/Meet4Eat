/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.common;

import net.m4e.app.event.business.EventEntity;
import net.m4e.app.resources.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Base class for event related tests
 *
 * @author boto
 * Date of creation February 14, 2018
 */
public class EventEntityCreator {

    public static Long EVENT_ID = 1000L;
    public static String EVENT_NAME = "My Event";
    public static String EVENT_DESCRIPTION = "Event description...";
    public static boolean EVENT_IS_PUBLIC = false;
    public static Long EVENT_STATUS_ID = 3000L;
    public static Long EVENT_PHOTO_ID = 4000L;
    public static String EVENT_PHOTO_ETAG = "PhotoETAG";
    public static Long EVENT_DATE_CREATION = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
    public static Long EVENT_START = 10L;
    public static Long EVENT_REPEAT_WEEKDAYS = 7L;
    public static Long EVENT_REPEAT_DAYTIME = 20L;
    public static Long EVENT_VOTING_BEGIN = 30L;

    /**
     * Create an event entity with default data.
     */
    static public EventEntity create() {
        EventEntity event = new EventEntity();
        event.setId(EVENT_ID);
        event.setName(EVENT_NAME);
        event.setDescription(EVENT_DESCRIPTION);
        event.setIsPublic(EVENT_IS_PUBLIC);
        event.setEventStart(EVENT_START);
        event.setRepeatDayTime(EVENT_REPEAT_DAYTIME);
        event.setRepeatWeekDays(EVENT_REPEAT_WEEKDAYS);
        event.setVotingTimeBegin(EVENT_VOTING_BEGIN);

        StatusEntity status = new StatusEntity();
        status.setId(EVENT_STATUS_ID);
        status.setDateCreation(EVENT_DATE_CREATION);
        status.setIdOwner(event.getId());
        event.setStatus(status);

        DocumentEntity photo = new DocumentEntity();
        photo.setId(EVENT_PHOTO_ID);
        photo.setDocumentETag(EVENT_PHOTO_ETAG);
        event.setPhoto(photo);

        return event;
    }
}
