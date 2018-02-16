/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.business;

import net.m4e.app.user.business.UserEntity;
import net.m4e.common.*;
import net.m4e.system.core.AppInfoEntity;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyObject;


/**
 * Base class for event related tests
 *
 * @author boto
 * Date of creation February 14, 2018
 */
public class EventLocationsCreationDeletionTest extends EventLocationsTestBase {

    @Test
    void createNewLocation() {
        EventLocationEntity locationEntity = EventLocationEntityCreator.create();
        eventLocations.createNewLocation(new EventEntity(), locationEntity, 0L);
    }

    @Test
    void createOrUpdateVote() {
        UserEntity voter = UserEntityCreator.create();
        EventEntity event = EventEntityCreator.create();
        EventLocationEntity location = new EventLocationEntity();
        boolean vote = true;

        EventLocations partialMockedEventLocations = Mockito.spy(eventLocations);

        Mockito.doReturn(false).when(partialMockedEventLocations).calculateVoteBeginAndEndTime(anyObject());

        EventLocationVoteEntity entity = partialMockedEventLocations.createOrUpdateVote(voter, event, location, vote);

        assertThat(entity).isNull();

        Mockito.doReturn(true).when(partialMockedEventLocations).calculateVoteBeginAndEndTime(anyObject());
        Mockito.doReturn(new EventLocationVoteEntity()).when(partialMockedEventLocations).getOrCreateVoteEntity(anyObject(), anyObject());

        entity = partialMockedEventLocations.createOrUpdateVote(voter, event, location, vote);

        assertThat(entity).isNotNull();
    }

    @Test
    void createOrUpdateUnvote() {
        UserEntity voter = UserEntityCreator.create();
        EventEntity event = EventEntityCreator.create();
        EventLocationEntity location = EventLocationEntityCreator.create();
        boolean vote = false;

        EventLocations partialMockedEventLocations = Mockito.spy(eventLocations);

        Mockito.doReturn(true).when(partialMockedEventLocations).calculateVoteBeginAndEndTime(anyObject());
        Mockito.doReturn(new EventLocationVoteEntity()).when(partialMockedEventLocations).getOrCreateVoteEntity(anyObject(), anyObject());

        EventLocationVoteEntity entity = partialMockedEventLocations.createOrUpdateVote(voter, event, location, vote);

        assertThat(entity).isNotNull();
    }

    @Test
    void getOrCreateVoteEntity() {
        EventEntity event = EventEntityCreator.create();
        EventLocationEntity location = EventLocationEntityCreator.create();
        EventLocationVoteEntity vote = new EventLocationVoteEntity();

        assertThat(eventLocations.getOrCreateVoteEntity(event, location)).isNotNull();

        mockNamedQuery(Arrays.asList(vote));

        assertThat(eventLocations.getOrCreateVoteEntity(event, location)).isNotNull();
    }

    @Nested
    class LocationDeletion {

        private EventEntity eventEntity;
        private EventLocationEntity eventLocationEntity;

        @BeforeEach
        void setup() {
            eventEntity = EventEntityCreator.create();
            eventLocationEntity = EventLocationEntityCreator.create();
        }

        @Test
        void markAsDeletedInvalidLocation() {
            try {
                eventLocations.markLocationAsDeleted(eventEntity, eventLocationEntity);
                fail("Location does not belonging to event was not detected!");
            } catch(Exception e){
            }

            eventEntity.setLocations(Collections.emptyList());
            try {
                eventLocations.markLocationAsDeleted(eventEntity, eventLocationEntity);
                fail("Location does not belonging to event was not detected!");
            } catch(Exception e){
            }
        }

        @Test
        void markAsDeletedLocationAlreadyDeleted() {
            eventLocationEntity.getStatus().setDateDeletion(Instant.now().toEpochMilli());
            try {
                eventLocations.markLocationAsDeleted(eventEntity, eventLocationEntity);
                fail("Already deleted location was not detected!");
            } catch(Exception e){
            }
        }

        @Test
        void markAsDeletedInvalidAppInfos() {
            eventEntity.setLocations(Arrays.asList(eventLocationEntity));
            Mockito.when(appInfos.getAppInfoEntity()).thenReturn(null);
            try {
                eventLocations.markLocationAsDeleted(eventEntity, eventLocationEntity);
                fail("Internal error (missing AppInfo) was not detected!");
            } catch(Exception e){
            }
        }

        @Test
        void markAsDeleted() throws Exception {
            eventEntity.setLocations(Arrays.asList(eventLocationEntity));
            Mockito.when(appInfos.getAppInfoEntity()).thenReturn(new AppInfoEntity());

            eventLocations.markLocationAsDeleted(eventEntity, eventLocationEntity);

            assertThat(eventLocationEntity.getStatus().getIsDeleted()).isTrue();
        }
    }
}
