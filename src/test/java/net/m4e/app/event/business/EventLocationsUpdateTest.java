/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.business;

import net.m4e.app.resources.DocumentEntity;
import net.m4e.common.EventLocationEntityCreator;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.*;


/**
 * Base class for event related tests
 *
 * @author boto
 * Date of creation March 9, 2018
 */
public class EventLocationsUpdateTest extends EventLocationsTestBase {

    @Nested
    class UpdateLocation {

        private static final String LOCATION_NAME = "Location name";
        private static final String DESCRIPTION = "Description";

        private EventLocationEntity inputLocation;
        private EventLocationEntity eventLocationEntity;

        @BeforeEach
        void setup() {
            inputLocation = EventLocationEntityCreator.create();
            eventLocationEntity = EventLocationEntityCreator.create();
        }

        @Test
        void updateLocationInvalidInput() {
            Mockito.when(entities.find(anyObject(), anyLong())).thenReturn(null);

            try {
                eventLocations.updateLocation(inputLocation);
                fail("Not-existing entity was not detected");
            } catch (Exception e) {
            }
        }

        @Test
        void updateLocationInactiveInput() {
            eventLocationEntity.getStatus().setEnabled(false);

            Mockito.when(entities.find(anyObject(), anyLong())).thenReturn(eventLocationEntity);

            try {
                eventLocations.updateLocation(inputLocation);
                fail("Inactive entity was not detected");
            } catch (Exception e) {
            }
        }

        @Test
        void updateLocationPartialFields() throws Exception {
            inputLocation.setName(null);
            inputLocation.setDescription(null);
            inputLocation.setPhoto(null);

            Mockito.when(entities.find(anyObject(), anyLong())).thenReturn(eventLocationEntity);

            EventLocationEntity updatedEntity = eventLocations.updateLocation(inputLocation);

            assertThat(updatedEntity.getName()).isEqualTo(eventLocationEntity.getName());
            assertThat(updatedEntity.getDescription()).isEqualTo(eventLocationEntity.getDescription());
            assertThat(updatedEntity.getPhoto()).isEqualTo(eventLocationEntity.getPhoto());
        }

        @Test
        void updateLocation() throws Exception {
            inputLocation.setName(LOCATION_NAME);
            inputLocation.setDescription(DESCRIPTION);
            inputLocation.setPhoto(new DocumentEntity());

            Mockito.when(entities.find(anyObject(), anyLong())).thenReturn(eventLocationEntity);

            EventLocationEntity updatedEntity = eventLocations.updateLocation(inputLocation);

            assertThat(updatedEntity.getName()).isEqualTo(LOCATION_NAME);
            assertThat(updatedEntity.getDescription()).isEqualTo(DESCRIPTION);
            assertThat(updatedEntity.getPhoto()).isNotNull();
        }
    }
}
