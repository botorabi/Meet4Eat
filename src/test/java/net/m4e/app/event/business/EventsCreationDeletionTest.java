/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.business;

import net.m4e.common.EventEntityCreator;
import net.m4e.system.core.AppInfoEntity;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;


/**
 * Base class for event related tests
 *
 * @author boto
 * Date of creation March 9, 2018
 */
public class EventsCreationDeletionTest extends EventsTestBase {

    @Test
    void createNewLocation() {
        EventEntity eventEntity = EventEntityCreator.create();
        events.createNewEvent(eventEntity, 0L);
    }

    @Nested
    class EventDeletion {

        private EventEntity eventEntity;

        @BeforeEach
        void setup() {
            eventEntity = EventEntityCreator.create();
        }

        @Test
        void deleteEvent() {
            events.deleteEvent(eventEntity);
        }

        @Test
        void markAsDeletedInvalidStatus() {
            try {
                eventEntity.setStatus(null);
                events.markEventAsDeleted(eventEntity);
                fail("Event with invalid status was not detected!");
            } catch(Exception e){
            }
        }

        @Test
        void markAsDeletedInvalidAppInfos() {
            Mockito.when(appInfos.getAppInfoEntity()).thenReturn(null);
            try {
                events.markEventAsDeleted(eventEntity);
                fail("Internal error (missing AppInfo) was not detected!");
            } catch(Exception e){
            }
        }

        @Test
        void markAsDeleted() throws Exception {
            Mockito.when(appInfos.getAppInfoEntity()).thenReturn(new AppInfoEntity());

            events.markEventAsDeleted(eventEntity);

            assertThat(eventEntity.getStatus().getIsDeleted()).isTrue();
        }

        @Test
        void getEventsMarkedAsDeleted() {
            EventEntity entity = EventEntityCreator.create();

            EventEntity deletedEntity1 = EventEntityCreator.create();
            deletedEntity1.getStatus().setDateDeletion(Instant.now().toEpochMilli());

            EventEntity deletedEntity2 = EventEntityCreator.create();
            deletedEntity2.getStatus().setDateDeletion(Instant.now().toEpochMilli());

            Mockito.doReturn(Arrays.asList(entity, deletedEntity1, deletedEntity2)).when(entities).findAll(eq(EventEntity.class));

            assertThat(events.getEventsMarkedAsDeleted()).hasSize(2);
        }
    }
}
