/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
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
import net.m4e.system.core.AppInfos;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.*;

import javax.servlet.http.*;
import java.time.Instant;
import java.util.Arrays;

import static org.mockito.Matchers.*;

/**
 * @author boto
 * Date of creation February 20, 2018
 */
class EventRestServiceTestBase {

    protected static final String SESSION_ID = "session_id";

    protected static final Long VALID_MEMBER_ID = 10L;
    protected static final Long INVALID_MEMBER_ID = 20L;
    protected static final Long INACTIVE_MEMBER_ID = 30L;

    protected static final Long ANY_EVENT_ID = 100L;
    protected static final Long VALID_EVENT_ID = 200L;
    protected static final Long INVALID_EVENT_ID = 300L;
    protected static final Long INACTIVE_EVENT_ID = 400L;

    protected static final Long VALID_EVENT_ID_1 = 1000L;
    protected static final Long VALID_EVENT_ID_2 = 2000L;

    protected static final Long VALID_LOCATION_ID = 10L;
    protected static final Long INVALID_LOCATION_ID = 20L;
    protected static final Long INACTIVE_LOCATION_ID = 30L;

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

    protected void mockSessionUser(UserEntity user) {
        Mockito.when(session.getAttribute(AuthorityConfig.SESSION_ATTR_USER)).thenReturn(user);
    }

    protected void mockPrivilegedUser() {
        mockUserPrivilege(true);
    }

    protected void mockNonPrivilegedUser() {
        mockUserPrivilege(false);
    }

    private void mockUserPrivilege(boolean privileged) {
        Mockito.when(users.userIsOwnerOrAdmin(anyObject(), anyObject())).thenReturn(privileged);
    }

    @NotNull
    protected EventCmd createEventCmd() {
        EventCmd eventCmd = new EventCmd();
        eventCmd.setName("name");
        eventCmd.setDescription("event description");
        eventCmd.setIsPublic(false);
        eventCmd.setPhoto("content");
        eventCmd.setEventStart(Instant.now().toEpochMilli());
        return eventCmd;
    }

    @NotNull
    protected EventLocationCmd createEventLocationCmd() {
        EventLocationCmd locationCmd = new EventLocationCmd();
        locationCmd.setName("name");
        locationCmd.setDescription("event description");
        locationCmd.setPhoto("content");
        return locationCmd;
    }

    protected void setupUsers() {
        UserEntity validMember = userMockUp.mockSomeUser();
        validMember.setId(VALID_MEMBER_ID);

        UserEntity invalidMember = null;

        UserEntity inactiveMember = userMockUp.mockSomeUser();
        inactiveMember.setId(INACTIVE_MEMBER_ID);
        inactiveMember.getStatus().setEnabled(false);

        Mockito.when(users.findUser(eq(VALID_MEMBER_ID))).thenReturn(validMember);
        Mockito.when(users.findUser(eq(INVALID_MEMBER_ID))).thenReturn(invalidMember);
        Mockito.when(users.findUser(eq(INACTIVE_MEMBER_ID))).thenReturn(inactiveMember);
    }

    protected void setupEvents() {
        EventEntity validEvent = EventEntityCreator.create();
        validEvent.setId(VALID_EVENT_ID);

        EventEntity invalidEvent = null;

        EventEntity inactiveEvent = EventEntityCreator.create();
        inactiveEvent.setId(INACTIVE_EVENT_ID);
        inactiveEvent.getStatus().setEnabled(false);

        Mockito.when(events.findEvent(eq(VALID_EVENT_ID))).thenReturn(validEvent);
        Mockito.when(events.findEvent(eq(INVALID_EVENT_ID))).thenReturn(invalidEvent);
        Mockito.when(events.findEvent(eq(INACTIVE_EVENT_ID))).thenReturn(inactiveEvent);
    }

    protected void setupEventList() {
        EventEntity validEvent1 = EventEntityCreator.create();
        validEvent1.setId(VALID_EVENT_ID_1);

        EventEntity validEvent2 = EventEntityCreator.create();
        validEvent2.setId(VALID_EVENT_ID_2);

        Mockito.when(entities.findAll(eq(EventEntity.class))).thenReturn(Arrays.asList(validEvent1, validEvent2));
        Mockito.when(entities.findRange(eq(EventEntity.class), anyInt(), anyInt())).thenReturn(Arrays.asList(validEvent1, validEvent2));
    }

    protected void setupLocations() {
        EventLocationEntity validLocation = EventLocationEntityCreator.create();
        validLocation.setId(VALID_LOCATION_ID);

        EventLocationEntity invalidLocation = null;

        EventLocationEntity inactiveLocation = EventLocationEntityCreator.create();
        inactiveLocation.setId(INACTIVE_LOCATION_ID);
        inactiveLocation.getStatus().setEnabled(false);

        Mockito.when(eventLocations.findLocation(eq(VALID_LOCATION_ID))).thenReturn(validLocation);
        Mockito.when(eventLocations.findLocation(eq(INVALID_LOCATION_ID))).thenReturn(invalidLocation);
        Mockito.when(eventLocations.findLocation(eq(INACTIVE_LOCATION_ID))).thenReturn(inactiveLocation);
    }
}
