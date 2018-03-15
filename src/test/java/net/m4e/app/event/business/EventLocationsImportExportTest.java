/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
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

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Base class for event related tests
 *
 * @author boto
 * Date of creation February 14, 2018
 */
public class EventLocationsImportExportTest extends EventLocationsTestBase {

    @Nested
    class ExportLocation {
        @Test
        void exportLocationAllFields() {
            EventLocationEntity locationEntity = EventLocationEntityCreator.create();

            EventLocation exportedLocation = eventLocations.exportEventLocation(locationEntity);

            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(exportedLocation.getId()).isEqualTo("" + locationEntity.getId());
            softly.assertThat(exportedLocation.getName()).isEqualTo(locationEntity.getName());
            softly.assertThat(exportedLocation.getDescription()).isEqualTo(locationEntity.getDescription());
            softly.assertThat(exportedLocation.getPhotoETag()).isEqualTo(locationEntity.getPhoto().getETag());
            softly.assertThat(exportedLocation.getPhotoId()).isEqualTo("" + locationEntity.getPhoto().getId());
            softly.assertAll();
        }

        @Test
        void exportLocationPartialFields() {
            EventLocationEntity locationEntity = EventLocationEntityCreator.create();
            locationEntity.setId(null);
            locationEntity.setPhoto(null);

            EventLocation exportedLocation = eventLocations.exportEventLocation(locationEntity);

            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(exportedLocation.getId()).isNull();
            softly.assertThat(exportedLocation.getName()).isEqualTo(locationEntity.getName());
            softly.assertThat(exportedLocation.getDescription()).isEqualTo(locationEntity.getDescription());
            softly.assertThat(exportedLocation.getPhotoETag()).isNull();
            softly.assertThat(exportedLocation.getPhotoId()).isNull();
            softly.assertAll();
        }
    }

    @Nested
    class ImportLocation {
        @Test
        void importLocationAllFields() {
            EventLocationCmd locationCmd = new EventLocationCmd(
                    "100",
                    "Name",
                    "Description",
                    "Photo Content");

            EventLocationEntity importedLocation = eventLocations.importLocation(locationCmd);

            SoftAssertions softly = new SoftAssertions();
            softly.assertThat("" + importedLocation.getId()).isEqualTo(locationCmd.getId());
            softly.assertThat(importedLocation.getName()).isEqualTo(locationCmd.getName());
            softly.assertThat(importedLocation.getDescription()).isEqualTo(locationCmd.getDescription());
            softly.assertThat(importedLocation.getPhoto()).isNotNull();
            softly.assertAll();
        }

        @Test
        void importLocationPartialFields() {
            EventLocationCmd locationCmd = new EventLocationCmd(
                    null,
                    "Name",
                    "Description",
                    null);

            EventLocationEntity importedLocation = eventLocations.importLocation(locationCmd);

            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(importedLocation.getId()).isNull();
            softly.assertThat(importedLocation.getName()).isEqualTo(locationCmd.getName());
            softly.assertThat(importedLocation.getDescription()).isEqualTo(locationCmd.getDescription());
            softly.assertThat(importedLocation.getPhoto()).isNull();
            softly.assertAll();
        }
    }

    @Nested
    class ExportVotes {
        @Test
        void exportVotes() {
            EventLocationVoteEntity voteEntity = EventLocationVoteEntityCreator.create();

            LocationVoteInfo voteInfo = eventLocations.exportLocationVotes(voteEntity);

            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(voteInfo.getId()).isEqualTo(EventLocationVoteEntityCreator.VOTE_ID.toString());
            softly.assertThat(voteInfo.getEventId()).isEqualTo(EventLocationVoteEntityCreator.VOTE_EVENT_ID.toString());
            softly.assertThat(voteInfo.getLocationId()).isEqualTo(EventLocationVoteEntityCreator.VOTE_LOCATION_ID.toString());
            softly.assertThat(voteInfo.getLocationName()).isEqualTo(EventLocationVoteEntityCreator.VOTE_LOCATION_NAME);
            softly.assertThat(voteInfo.getCreationTime()).isEqualTo(EventLocationVoteEntityCreator.VOTE_CREATION_TIME);
            softly.assertThat(voteInfo.getTimeBegin()).isEqualTo(EventLocationVoteEntityCreator.VOTE_TIME_BEGIN);
            softly.assertThat(voteInfo.getTimeEnd()).isEqualTo(EventLocationVoteEntityCreator.VOTE_TIME_END);
            softly.assertThat(voteInfo.getUserIds()).hasSameElementsAs(EventLocationVoteEntityCreator.VOTE_USER_IDS);
            softly.assertThat(voteInfo.getUserNames()).hasSameElementsAs(EventLocationVoteEntityCreator.VOTE_USER_NAMES);
            softly.assertAll();
        }

        @Test
        void exportVotesList() {
            EventLocationVoteEntity voteEntity1 = EventLocationVoteEntityCreator.create();
            EventLocationVoteEntity voteEntity2 = EventLocationVoteEntityCreator.create();

            List<LocationVoteInfo> votes = eventLocations.exportLocationVotes(Arrays.asList(voteEntity1, voteEntity2));

            assertThat(votes).hasSize(2);
        }
    }
}
