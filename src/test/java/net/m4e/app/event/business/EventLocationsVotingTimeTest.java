/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.business;

import net.m4e.common.EventEntityCreator;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Base class for event related tests
 *
 * @author boto
 * Date of creation February 14, 2018
 */
public class EventLocationsVotingTimeTest extends EventLocationsTestBase {

    @Nested
    class VotingTimeCalculationOneShotEvent {

        @Test
        void voteBeginAndEndTimeInVotingWindow() {
            EventEntity event = createEventEntityForVotingWindow(1, 2);

            assertThat(eventLocations.calculateVoteBeginAndEndTime(event)).isTrue();
        }

        @Test
        void voteBeginAndEndTimeOutOfVotingWindow() {
            EventEntity event = createEventEntityForVotingWindow(-2, 1);

            assertThat(eventLocations.calculateVoteBeginAndEndTime(event)).isFalse();
        }

        @NotNull
        private EventEntity createEventEntityForVotingWindow(int timeOffset, int beginTimeHoursOffset) {
            EventEntity event = EventEntityCreator.create();
            event.setRepeatWeekDays(0L);
            Long eventStart = Instant.now().plus(timeOffset, ChronoUnit.HOURS).getEpochSecond();
            Long votingTimeBegin = beginTimeHoursOffset * 60L * 60L;
            event.setEventStart(eventStart);
            event.setVotingTimeBegin(votingTimeBegin);

            return event;
        }
    }

    @Nested
    class VotingTimeCalculationRepeatedEvent {

        @Test
        void voteBeginAndEndTimeInVotingWindow() {
            EventEntity event = createRepeatedEventEntityForVotingWindow(1, 2, 0);

            assertThat(eventLocations.calculateVoteBeginAndEndTime(event)).isTrue();
        }

        @Test
        void voteBeginAndEndTimeOutOfVotingWindow() {
            EventEntity event = createRepeatedEventEntityForVotingWindow(1, 2, 1);

            assertThat(eventLocations.calculateVoteBeginAndEndTime(event)).isFalse();
        }

        @NotNull
        private EventEntity createRepeatedEventEntityForVotingWindow(int timeOffset, int beginTimeHoursOffset, int dayOfWeekOffset) {
            EventEntity event = EventEntityCreator.create();

            int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + dayOfWeekOffset;
            currentDay = (1 << ((currentDay + 5) % 7));
            event.setRepeatWeekDays((long)currentDay);

            Long eventStart = Instant.now().plus(timeOffset, ChronoUnit.HOURS).getEpochSecond();
            eventStart = (eventStart % (60L * 60L * 24));
            Long votingTimeBegin = beginTimeHoursOffset * 60L * 60L;

            event.setRepeatDayTime(eventStart);
            event.setVotingTimeBegin(votingTimeBegin);

            return event;
        }
    }
}
