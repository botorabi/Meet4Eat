/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.rest;

import net.m4e.app.event.business.EventEntity;
import net.m4e.app.event.rest.comm.EventId;
import net.m4e.common.*;
import net.m4e.tests.ResponseAssertions;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import static org.mockito.Matchers.anyObject;

/**
 * @author boto
 * Date of creation February 20, 2018
 */
class EventRestServiceModifyTest extends EventRestServiceTestBase {

    @Nested
    class ModifyEvent {
        @BeforeEach
        void setup() {
            setupEvents();
        }

        @Test
        void modifyEventValidationFailed() throws Exception {
            mockUpdateEventValidationFailed();

            GenericResponseResult<EventId> response = restService.edit(ANY_EVENT_ID, null, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsBadRequest();
        }

        @Test
        void modifyInvalidEvent() {
            GenericResponseResult<EventId> response = restService.edit(INVALID_EVENT_ID, null, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsNotFound();
        }

        @Test
        void modifyNoPrivilege() {
            Mockito.doReturn(false).when(users).userIsOwnerOrAdmin(anyObject(), anyObject());

            GenericResponseResult<EventId> response = restService.edit(VALID_EVENT_ID, createEventCmd(), request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsUnauthorized();
        }

        @Test
        void modifyEventSuccess() throws Exception {
            userMockUp.mockSomeUser();
            mockUpdateEventValidationSuccess();

            Mockito.doReturn(true).when(users).userIsOwnerOrAdmin(anyObject(), anyObject());

            EventRestService partialMockedService = Mockito.spy(restService);
            Mockito.doNothing().when(partialMockedService).performEventUpdate(anyObject(), anyObject());

            GenericResponseResult<EventId> response = partialMockedService.edit(VALID_EVENT_ID, createEventCmd(), request);

            ResponseAssertions.assertThat(response).hasStatusOk();
        }

        @Test
        void performEventUpdate() {
            EventEntity updateEvent = EventEntityCreator.create();
            EventEntity existingEvent = EventEntityCreator.create();

            restService.performEventUpdate(updateEvent, existingEvent);
        }
    }

    protected void mockUpdateEventValidationSuccess() throws Exception {
        Mockito.when(validator.validateUpdateEntityInput(anyObject())).thenReturn(new EventEntity());
    }

    protected void mockUpdateEventValidationFailed() throws Exception {
        Mockito.when(validator.validateUpdateEntityInput(anyObject())).thenThrow(new Exception("Validation failed"));
    }
}
