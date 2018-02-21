/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.rest;

import net.m4e.app.auth.AuthorityConfig;
import net.m4e.app.event.business.*;
import net.m4e.app.event.rest.comm.*;
import net.m4e.app.user.business.*;
import net.m4e.common.*;
import net.m4e.system.core.*;
import net.m4e.tests.ResponseAssertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.mockito.*;

import javax.servlet.http.*;
import java.time.Instant;
import java.util.*;

import static org.mockito.Matchers.*;

/**
 * @author boto
 * Date of creation February 20, 2018
 */
class EventRestServiceTest {

    private static final String SESSION_ID = "session_id";
    private static final Long ANY_EVENT_ID = 100L;

    @Mock
    Users users;
    @Mock
    Entities entities;
    @Mock
    Events events;
    @Mock
    EventValidator validator;
    @Mock
    EventLocations eventLocations;
    @Mock
    AppInfos appInfos;
    @Mock
    EventNotifications eventNotifications;

    @Mock
    HttpServletRequest request;
    @Mock
    HttpSession session;

    UserMockUp userMockUp;

    EventRestService restService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(session.getId()).thenReturn(SESSION_ID);
        Mockito.when(request.getSession()).thenReturn(session);

        restService = new EventRestService(entities, events, users, validator, eventLocations, eventNotifications, appInfos);
        userMockUp = new UserMockUp(users);
    }

    @Nested
    class EventCreationDeletion {

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
        void createEventValidationFailed() throws Exception {
            userMockUp.mockSomeUser();
            mockNewEventValidationFailed();

            EventCmd eventCmd = createEventCmd();

            GenericResponseResult<EventId> response = restService.createEvent(eventCmd, request);

            ResponseAssertions.assertThat(response).hasStatusNotOk();
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

            Mockito.doReturn(EventEntityCreator.create()).when(entities).find(eq(EventEntity.class), anyLong());
            Mockito.doReturn(false).when(users).userIsOwnerOrAdmin(anyObject(), anyObject());

            GenericResponseResult<EventId> response = restService.remove(ANY_EVENT_ID, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsForbidden();
        }

        @Test
        void deletionMarkingFailure() throws Exception {
            userMockUp.mockSomeUser();
            mockSessionUser(userMockUp.mockAdminUser());

            Mockito.doThrow(new Exception("Failure marking user as deleted")).when(events).markEventAsDeleted(anyObject());
            Mockito.doReturn(EventEntityCreator.create()).when(entities).find(eq(EventEntity.class), anyLong());
            Mockito.doReturn(true).when(users).userIsOwnerOrAdmin(anyObject(), anyObject());

            GenericResponseResult<EventId> response = restService.remove(ANY_EVENT_ID, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsInternalError();
        }

        @NotNull
        private EventCmd createEventCmd() {
            EventCmd eventCmd = new EventCmd();
            eventCmd.setName("name");
            eventCmd.setDescription("event description");
            eventCmd.setIsPublic(false);
            eventCmd.setPhoto("content");
            eventCmd.setEventStart(Instant.now().toEpochMilli());
            return eventCmd;
        }
    }

    @Nested
    class FindEvents {

        @BeforeEach
        void setup() {
            EventEntity event1 = EventEntityCreator.create();
            event1.setId(1000L);
            EventEntity event2 = EventEntityCreator.create();
            event2.setId(2000L);

            Mockito.when(entities.findAll(eq(EventEntity.class))).thenReturn(Arrays.asList(event1, event2));
            Mockito.when(entities.findRange(eq(EventEntity.class), anyInt(), anyInt())).thenReturn(Arrays.asList(event1, event2));
        }

        @Test
        void find() {
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

    @Test
    void countEvents() {
        Mockito.when(appInfos.getAppInfoEntity()).thenReturn(new AppInfoEntity());

        GenericResponseResult<EventCount> response = restService.count();

        ResponseAssertions.assertThat(response)
                .hasStatusOk()
                .hasData();
    }

    private void mockSessionUser(UserEntity user) {
        Mockito.when(session.getAttribute(AuthorityConfig.SESSION_ATTR_USER)).thenReturn(user);
    }

    private void mockNewEventValidationSuccess() throws Exception {
        Mockito.when(validator.validateNewEntityInput(anyObject())).thenReturn(new EventEntity());
    }

    private void mockNewEventValidationFailed() throws Exception {
        Mockito.when(validator.validateNewEntityInput(anyObject())).thenThrow(new Exception("Validation failed"));
    }
}
