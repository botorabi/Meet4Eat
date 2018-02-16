/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.common;

import net.m4e.app.event.business.EventLocationVoteEntity;
import org.glassfish.grizzly.utils.ArraySet;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Base class for event related tests
 *
 * @author boto
 * Date of creation February 14, 2018
 */
public class EventLocationVoteEntityCreator {

    public static Long VOTE_ID = 1000L;
    public static Long VOTE_EVENT_ID = 1010L;
    public static Long VOTE_LOCATION_ID = 1020L;
    public static String VOTE_LOCATION_NAME = "Location Name";
    public static Long VOTE_CREATION_TIME = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
    public static Long VOTE_TIME_BEGIN = Instant.now().toEpochMilli();
    public static Long VOTE_TIME_END = Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli();
    public static Set<Long> VOTE_USER_IDS = new HashSet<>(Arrays.asList(2000L, 3000L));
    public static Set<String> VOTE_USER_NAMES = new HashSet<>(Arrays.asList("Name 1", "Name 2"));

    static public EventLocationVoteEntity create() {
        EventLocationVoteEntity voteEntity = new EventLocationVoteEntity();
        voteEntity.setId(VOTE_ID);
        voteEntity.setEventId(VOTE_EVENT_ID);
        voteEntity.setLocationId(VOTE_LOCATION_ID);
        voteEntity.setLocationName(VOTE_LOCATION_NAME);
        voteEntity.setCreationTime(VOTE_CREATION_TIME);
        voteEntity.setVoteTimeBegin(VOTE_TIME_BEGIN);
        voteEntity.setVoteTimeEnd(VOTE_TIME_END);
        voteEntity.setUserIds(VOTE_USER_IDS);
        voteEntity.setUserNames(VOTE_USER_NAMES);

        return voteEntity;
    }
}
