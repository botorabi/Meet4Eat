/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.rest;

import net.m4e.app.event.business.EventEntity;
import net.m4e.app.event.rest.comm.*;
import net.m4e.common.*;
import net.m4e.tests.ResponseAssertions;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import static org.mockito.Matchers.*;

/**
 * @author boto
 * Date of creation February 20, 2018
 */
class EventRestServiceCreationDeletionTest extends EventRestServiceTestBase {

    @BeforeEach
    void setup() {
        setupEvents();
    }

    @Nested
    class EventCreationDeletion {

        @Test
        void createEventValidationFailed() throws Exception {
            userMockUp.mockSomeUser();
            mockNewEventValidationFailed();

            EventCmd eventCmd = createEventCmd();

            GenericResponseResult<EventId> response = restService.createEvent(eventCmd, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsBadRequest();
        }

        @Test
        void eventCreationFailure() throws Exception {
            userMockUp.mockSomeUser();
            mockSessionUser(userMockUp.mockAdminUser());

            Mockito.doThrow(new Exception("Failure creating a new event")).when(events).createNewEvent(anyObject(), anyLong());

            GenericResponseResult<EventId> response = restService.createEvent(createEventCmd(), request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsInternalError();
        }

        @Test
        void createEventSuccess() throws Exception {
            mockSessionUser(userMockUp.mockSomeUser());
            mockNewEventValidationSuccess();

            Mockito.doReturn(EventEntityCreator.create()).when(events).createNewEvent(anyObject(), anyLong());

            EventCmd eventCmd = createEventCmd();

            GenericResponseResult<EventId> response = restService.createEvent(eventCmd, request);

            ResponseAssertions.assertThat(response).hasStatusOk();
        }

        @Test
        void deleteNonExistingEvent() {
            userMockUp.mockSomeUser();

            Mockito.doReturn(null).when(entities).find(eq(EventEntity.class), anyLong());

            GenericResponseResult<EventId> response = restService.remove(ANY_EVENT_ID, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsNotFound();
        }

        @Test
        void deleteNoPrivilege() {
            userMockUp.mockSomeUser();

            mockNonPrivilegedUser();

            GenericResponseResult<EventId> response = restService.remove(VALID_EVENT_ID, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsUnauthorized();
        }

        @Test
        void deletionMarkingFailure() throws Exception {
            userMockUp.mockSomeUser();
            mockSessionUser(userMockUp.mockAdminUser());

            Mockito.doThrow(new Exception("Failure marking user as deleted")).when(events).markEventAsDeleted(anyObject());

            mockPrivilegedUser();

            GenericResponseResult<EventId> response = restService.remove(VALID_EVENT_ID, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsInternalError();
        }

        @Test
        void deletionSuccess() {
            userMockUp.mockSomeUser();
            mockSessionUser(userMockUp.mockAdminUser());

            mockPrivilegedUser();

            GenericResponseResult<EventId> response = restService.remove(VALID_EVENT_ID, request);

            ResponseAssertions.assertThat(response).hasStatusOk();
        }
    }

    protected void mockNewEventValidationSuccess() throws Exception {
        Mockito.when(validator.validateNewEntityInput(anyObject())).thenReturn(new EventEntity());
    }

    protected void mockNewEventValidationFailed() throws Exception {
        Mockito.when(validator.validateNewEntityInput(anyObject())).thenThrow(new Exception("Validation failed"));
    }
}
