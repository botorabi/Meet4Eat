/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.rest;

import net.m4e.app.event.business.*;
import net.m4e.app.event.rest.comm.*;
import net.m4e.app.user.business.UserEntity;
import net.m4e.common.*;
import net.m4e.tests.ResponseAssertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import static org.mockito.Matchers.*;

/**
 * @author boto
 * Date of creation February 22, 2018
 */
class EventRestServiceLocationsTest extends EventRestServiceTestBase {

    private EventLocationCmd someEventLocationCmd;

    @BeforeEach
    void setup() {
        setupLocations();

        setupEvents();

        someEventLocationCmd = createEventLocationCmd();
    }

    @Nested
    class LocationAddRemove {

        @Test
        void addLocationInvalidInput() {
            GenericResponseResult<AddRemoveEventLocation> response = restService.putLocation(null, null, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsNotAcceptable();

            response = restService.putLocation(VALID_EVENT_ID, null, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsNotAcceptable();

            response = restService.putLocation(null, someEventLocationCmd, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsNotAcceptable();
        }

        @Test
        void addLocationInvalidEvent() {
            GenericResponseResult<AddRemoveEventLocation> response = restService.putLocation(INVALID_EVENT_ID, someEventLocationCmd, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsNotFound();
        }

        @Test
        void addLocationInactiveEvent() {
            GenericResponseResult<AddRemoveEventLocation> response = restService.putLocation(INACTIVE_EVENT_ID, someEventLocationCmd, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsNotFound();
        }

        @Test
        void addLocationNonPrivileged() {
            mockNonPrivilegedUser();

            GenericResponseResult<AddRemoveEventLocation> response = restService.putLocation(VALID_EVENT_ID, someEventLocationCmd, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsUnauthorized();
        }

        @Test
        void addLocationValidationFailed() throws Exception {
            mockPrivilegedUser();

            mockLocationValidationFailed();

            GenericResponseResult<AddRemoveEventLocation> response = restService.putLocation(VALID_EVENT_ID, someEventLocationCmd, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsBadRequest();
        }

        @Test
        void addLocationSuccess() {
            mockPrivilegedUser();

            EventRestService partialRestService = Mockito.spy(restService);
            Mockito.doReturn(GenericResponseResult.ok("")).when(partialRestService).checkAndAddLocation(anyObject(), anyObject(), anyObject());

            GenericResponseResult<AddRemoveEventLocation> response = partialRestService.putLocation(VALID_EVENT_ID, someEventLocationCmd, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusOk();
        }
    }

    @Nested
    class CheckAndAddLocation {

        private UserEntity sessionUser;
        private EventLocationEntity inputLocation;

        @BeforeEach
        void setup() {
            sessionUser = UserEntityCreator.create();
            inputLocation = EventLocationEntityCreator.create();
        }

        @Test
        void addNewLocationUniqueLocationNameValidationFailed() {
            inputLocation.setId(0L);

            GenericResponseResult<AddRemoveEventLocation> response = checkAndNewLocation(false, inputLocation);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsNotAcceptable();
        }

        @Test
        void addNewLocation() {
            inputLocation.setId(null);

            GenericResponseResult<AddRemoveEventLocation> response = checkAndNewLocation(true, inputLocation);

            ResponseAssertions.assertThat(response)
                    .hasStatusOk();
        }

        @Test
        void updateLocation() throws Exception {
            Mockito.doReturn(inputLocation).when(eventLocations).updateLocation(anyObject());

            GenericResponseResult<AddRemoveEventLocation> response = checkAndNewLocation(true, inputLocation);

            ResponseAssertions.assertThat(response)
                    .hasStatusOk();
        }

        @NotNull
        private GenericResponseResult<AddRemoveEventLocation> checkAndNewLocation(boolean passNameValidation, final EventLocationEntity inputLocation) {
            EventEntity eventEntity = EventEntityCreator.create();

            Mockito.when(eventLocations.createNewLocation(anyObject(), anyLong(), anyObject())).thenReturn(EventLocationEntityCreator.create());

            mockLocationNameValidation(passNameValidation);

            return restService.checkAndAddLocation(inputLocation, sessionUser, eventEntity);
        }
    }

    protected void mockLocationNameValidation(boolean pass) {
        Mockito.when(validator.validateUniqueLocationName(anyObject(), anyObject())).thenReturn(pass);
    }

    protected void mockLocationValidationFailed() throws Exception {
        Mockito.when(validator.validateLocationInput(anyObject(), anyObject())).thenThrow(new Exception("Validation failed"));
    }
}
