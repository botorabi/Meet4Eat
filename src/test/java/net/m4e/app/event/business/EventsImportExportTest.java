/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.business;

import net.m4e.app.event.rest.comm.*;
import net.m4e.common.*;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.*;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Base class for event related tests
 *
 * @author boto
 * Date of creation March 9, 2018
 */
public class EventsImportExportTest extends EventsTestBase {

    @Nested
    class ExportEvent {
        @Test
        void exportAllFields() {
            EventEntity eventEntity = EventEntityCreator.create();
            eventEntity.setMembers(Arrays.asList(UserEntityCreator.create()));
            eventEntity.setLocations(Arrays.asList(EventLocationEntityCreator.create()));

            EventInfo eventInfo = events.exportEvent(eventEntity);

            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(eventInfo.getId()).isEqualTo("" + eventEntity.getId());
            softly.assertThat(eventInfo.getName()).isEqualTo(eventEntity.getName());
            softly.assertThat(eventInfo.getDescription()).isEqualTo(eventEntity.getDescription());
            softly.assertThat(eventInfo.getPhotoETag()).isEqualTo(eventEntity.getPhoto().getETag());
            softly.assertThat(eventInfo.getPhotoId()).isEqualTo("" + eventEntity.getPhoto().getId());
            softly.assertAll();
        }

        @Test
        void exportPartialFields() {
            EventEntity eventEntity = EventEntityCreator.create();
            eventEntity.setPhoto(null);

            EventInfo eventInfo = events.exportEvent(eventEntity);

            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(eventInfo.getId()).isEqualTo("" + eventEntity.getId());
            softly.assertThat(eventInfo.getName()).isEqualTo(eventEntity.getName());
            softly.assertThat(eventInfo.getDescription()).isEqualTo(eventEntity.getDescription());
            softly.assertThat(eventInfo.getPhotoETag()).isEmpty();
            softly.assertThat(eventInfo.getPhotoId()).isEmpty();
            softly.assertAll();
        }
    }

    @Nested
    class ImportLocation {
        @Test
        void importAllFields() {
            EventCmd eventCmd = new EventCmd(
                    "Name",
                    "Description",
                    false,
                    "Photo Content",
                    1L,
                    2L,
                    3L,
                    4L);

            EventEntity importedEvent = events.importEvent(eventCmd);

            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(importedEvent.getName()).isEqualTo(eventCmd.getName());
            softly.assertThat(importedEvent.getDescription()).isEqualTo(eventCmd.getDescription());
            softly.assertThat(importedEvent.getPhoto()).isNotNull();
            softly.assertThat(importedEvent.getEventStart()).isEqualTo(eventCmd.getEventStart());
            softly.assertThat(importedEvent.getRepeatDayTime()).isEqualTo(eventCmd.getRepeatDayTime());
            softly.assertThat(importedEvent.getRepeatWeekDays()).isEqualTo(eventCmd.getRepeatWeekDays());
            softly.assertThat(importedEvent.getVotingTimeBegin()).isEqualTo(eventCmd.getVotingTimeBegin());
            softly.assertAll();
        }

        @Test
        void importPartialFields() {
            EventCmd eventCmd = new EventCmd(
                    "Name",
                    "Description",
                    false,
                    null,
                    1L,
                    2L,
                    3L,
                    4L);

            EventEntity importedEvent = events.importEvent(eventCmd);

            assertThat(importedEvent.getPhoto()).isNull();
        }
    }
}
