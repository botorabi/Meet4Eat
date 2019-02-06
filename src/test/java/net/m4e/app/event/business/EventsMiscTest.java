/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.business;

import net.m4e.common.*;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.util.Arrays;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.*;


/**
 * Base class for event related tests
 *
 * @author boto
 * Date of creation March 9, 2018
 */
public class EventsMiscTest extends EventsTestBase {

    @Test
    void defaultConstructor() {
        new Events();
    }

    @Test
    void updateEvent() {
        events.updateEvent(new EventEntity());
    }

    @Nested
    class FindEvent {

        private final Long VALID_EVENT_ID = 1000L;
        private final Long INVALID_EVENT_ID = 1100L;
        private final Long INACTIVE_EVENT_ID = 1200L;

        private final Long VALID_LOCATION_ID = 2000L;
        private final Long INVALID_LOCATION_ID = 2100L;
        private final Long INACTIVE_LOCATION_ID = 2200L;

        private EventEntity validEvent = EventEntityCreator.create();
        private EventLocationEntity validEventLocation;

        @BeforeEach
        void setup() {
            validEventLocation = EventLocationEntityCreator.create();
            validEventLocation.setId(VALID_LOCATION_ID);

            EventLocationEntity inactiveEventLocation = EventLocationEntityCreator.create();
            inactiveEventLocation.setId(INACTIVE_LOCATION_ID);
            inactiveEventLocation.getStatus().setEnabled(false);

            validEvent = EventEntityCreator.create();
            validEvent.setId(VALID_EVENT_ID);
            validEvent.setLocations(Arrays.asList(validEventLocation, inactiveEventLocation));

            EventEntity inactiveEvent = EventEntityCreator.create();
            inactiveEvent.setId(INACTIVE_EVENT_ID);
            inactiveEvent.getStatus().setEnabled(false);

            Mockito.doReturn(validEvent).when(entities).find(eq(EventEntity.class), eq(VALID_EVENT_ID));
            Mockito.doReturn(inactiveEvent).when(entities).find(eq(EventEntity.class), eq(INACTIVE_EVENT_ID));
            Mockito.doReturn(null).when(entities).find(eq(EventEntity.class), eq(INVALID_EVENT_ID));
        }

        @Test
        void findEvent() {
            events.findEvent(0L);
        }

        @Test
        void findEventLocationNotExistingEvent() {
            assertThat(events.findEventLocation(INVALID_EVENT_ID, VALID_LOCATION_ID)).isNull();
        }

        @Test
        void findEventLocationNotActiveEvent() {
            assertThat(events.findEventLocation(INACTIVE_EVENT_ID, VALID_LOCATION_ID)).isNull();
        }

        @Test
        void findEventLocationNotExistingLocation() {
            Mockito.doReturn(EventEntityCreator.create()).when(entities).find(eq(EventEntity.class), anyLong());

            assertThat(events.findEventLocation(VALID_EVENT_ID, INVALID_LOCATION_ID)).isNull();
        }

        @Test
        void findEventLocationNotActiveLocation() {
            assertThat(events.findEventLocation(VALID_EVENT_ID, INACTIVE_LOCATION_ID)).isNull();
        }

        @Test
        void findEventLocationSuccess() {
            assertThat(events.findEventLocation(VALID_EVENT_ID, VALID_LOCATION_ID)).isEqualTo(validEventLocation);
        }
    }
}
