/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.rest;

import net.m4e.app.event.business.EventInfo;
import net.m4e.common.GenericResponseResult;
import net.m4e.tests.ResponseAssertions;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.util.List;

import static org.mockito.Matchers.anyObject;

/**
 * @author boto
 * Date of creation February 20, 2018
 */
class EventRestServiceFindTest extends EventRestServiceTestBase {

    @Nested
    class FindEvents {

        @BeforeEach
        void setup() {
            setupEvents();
            setupEventList();
        }

        @Test
        void find() {
            Mockito.doReturn(true).when(events).getUserIsEventOwnerOrMember(anyObject(), anyObject());

            GenericResponseResult<EventInfo> response = restService.find(VALID_EVENT_ID, request);

            ResponseAssertions.assertThat(response).hasStatusOk();
        }


        @Test
        void findNoPrivilege() {
            Mockito.doReturn(false).when(events).getUserIsEventOwnerOrMember(anyObject(), anyObject());

            GenericResponseResult<EventInfo> response = restService.find(VALID_EVENT_ID, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsUnauthorized();
        }

        @Test
        void findInvalidEvent() {
            Mockito.doReturn(true).when(events).getUserIsEventOwnerOrMember(anyObject(), anyObject());

            GenericResponseResult<EventInfo> response = restService.find(INVALID_EVENT_ID, request);

            ResponseAssertions.assertThat(response)
                .hasStatusNotOk()
                .codeIsNotFound();
        }

        @Test
        void findInactiveEvent() {
            Mockito.doReturn(true).when(events).getUserIsEventOwnerOrMember(anyObject(), anyObject());

            GenericResponseResult<EventInfo> response = restService.find(INACTIVE_EVENT_ID, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsNotFound();
        }

        @Test
        void findAllEvents() {
            Mockito.doReturn(true).when(events).getUserIsEventOwnerOrMember(anyObject(), anyObject());

            GenericResponseResult<List<EventInfo>> response = restService.findAllEvents(request);

            ResponseAssertions.assertThat(response.getData()).hasSize(2);
        }

        @Test
        void findRange() {
            Mockito.doReturn(true).when(events).getUserIsEventOwnerOrMember(anyObject(), anyObject());

            GenericResponseResult<List<EventInfo>> response = restService.findRange(1, 2, request);

            ResponseAssertions.assertThat(response.getData()).hasSize(2);
        }
    }
}
