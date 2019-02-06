/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.rest;

import net.m4e.app.event.business.*;
import net.m4e.app.event.rest.comm.LocationVote;
import net.m4e.common.*;
import net.m4e.tests.ResponseAssertions;
import org.junit.jupiter.api.*;
import org.mockito.*;

import javax.servlet.http.*;
import java.util.List;

import static org.mockito.Matchers.*;


/**
 * Tests for event location service
 *
 * @author boto
 * Date of creation February 16, 2018
 */
public class EventLocationVoteRestServiceTest {

    private final static Long EXISTING_EVENT_ID = 1000L;
    private final static Long NON_EXISTING_EVENT_ID = 1100L;
    private final static Long INACTIVE_EVENT_ID = 1200L;
    private final static Long ANY_EVENT_LOCATION_ID = 2000L;
    private final static Long EXISTING_EVENT_LOCATION_ID = 2100L;
    private final static Long NON_EXISTING_EVENT_LOCATION_ID = 2200L;
    private final static Long EXISTING_VOTE_ID = 3000L;
    private final static Long NON_EXISTING_VOTE_ID = 3100L;
    private final static Long EXISTING_VOTE_ID_INACTIVE_EVENT = 3200L;
    private final static Long EXISTING_VOTE_ID_NON_EXISTING_EVENT = 3300L;


    private final static String SESSION_ID = "session_id";

    @Mock
    EventNotifications eventNotifications;
    @Mock
    Events events;
    @Mock
    Entities entities;
    @Mock
    EventLocations eventLocations;
    @Mock
    HttpServletRequest request;
    @Mock
    HttpSession session;

    EventLocationVoteRestService voteRestService;

    EventEntityCreator eventEntityCreator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(session.getId()).thenReturn(SESSION_ID);
        Mockito.when(request.getSession()).thenReturn(session);
        voteRestService = new EventLocationVoteRestService(events, entities, eventLocations, eventNotifications);

        eventEntityCreator = new EventEntityCreator();

        EventEntity eventEntity = eventEntityCreator.create();
        EventEntity inactiveEventEntity = eventEntityCreator.create();
        inactiveEventEntity.getStatus().setEnabled(false);

        Mockito.when(entities.find(eq(EventEntity.class), eq(EXISTING_EVENT_ID))).thenReturn(eventEntity);
        Mockito.when(entities.find(eq(EventEntity.class), eq(INACTIVE_EVENT_ID))).thenReturn(inactiveEventEntity);
        Mockito.when(entities.find(eq(EventEntity.class), eq(NON_EXISTING_EVENT_ID))).thenReturn(null);

        EventLocationVoteEntity voteEntity = EventLocationVoteEntityCreator.create();
        voteEntity.setEventId(EXISTING_EVENT_ID);
        EventLocationVoteEntity voteEntityWithInactiveEvent = EventLocationVoteEntityCreator.create();
        voteEntityWithInactiveEvent.setEventId(INACTIVE_EVENT_ID);
        EventLocationVoteEntity voteEntityWithNonExistingEvent = EventLocationVoteEntityCreator.create();
        voteEntityWithNonExistingEvent.setEventId(NON_EXISTING_EVENT_ID);

        Mockito.when(entities.find(eq(EventLocationVoteEntity.class), eq(EXISTING_VOTE_ID))).thenReturn(voteEntity);
        Mockito.when(entities.find(eq(EventLocationVoteEntity.class), eq(NON_EXISTING_VOTE_ID))).thenReturn(null);
        Mockito.when(entities.find(eq(EventLocationVoteEntity.class), eq(EXISTING_VOTE_ID_INACTIVE_EVENT))).thenReturn(voteEntityWithInactiveEvent);
        Mockito.when(entities.find(eq(EventLocationVoteEntity.class), eq(EXISTING_VOTE_ID_NON_EXISTING_EVENT))).thenReturn(voteEntityWithNonExistingEvent);
    }

    void mockOwnerOrMember(boolean isOwnerOrMember) {
        Mockito.doReturn(isOwnerOrMember).when(events).getUserIsEventOwnerOrMember(anyObject(), anyObject());
    }

    @Test
    void defaultConstruction() {
        new EventLocationVoteRestService();
    }

    @Nested
    class SettingVote {

        @Test
        void setVoteNonExistingEvent() {
            GenericResponseResult<LocationVote> result = voteRestService.setVote(
                    NON_EXISTING_EVENT_ID,
                    ANY_EVENT_LOCATION_ID,
                    true,
                    request);

            ResponseAssertions.assertThat(result).codeIsBadRequest();
        }

        @Test
        void setVoteNonInactiveEvent() {
            GenericResponseResult<LocationVote> result = voteRestService.setVote(
                    INACTIVE_EVENT_ID,
                    ANY_EVENT_LOCATION_ID,
                    true,
                    request);

            ResponseAssertions.assertThat(result).codeIsBadRequest();
        }

        @Test
        void setVoteNoPrivilege() {
            mockOwnerOrMember(false);

            GenericResponseResult<LocationVote> result = voteRestService.setVote(
                    EXISTING_EVENT_ID,
                    ANY_EVENT_LOCATION_ID,
                    true,
                    request);

            ResponseAssertions.assertThat(result).codeIsUnauthorized();
        }

        @Test
        void setVoteInvalidLocation() {
            mockOwnerOrMember(true);

            GenericResponseResult<LocationVote> result = voteRestService.setVote(
                    EXISTING_EVENT_ID,
                    NON_EXISTING_EVENT_LOCATION_ID,
                    true,
                    request);

            ResponseAssertions.assertThat(result).codeIsBadRequest();
        }

        @Test
        void setVoteInVotingWindow() {
            EventLocationVoteEntity locationVote = EventLocationVoteEntityCreator.create();

            GenericResponseResult<LocationVote> result = getVoteResponse(locationVote);

            ResponseAssertions.assertThat(result).codeIsOk();
        }

        @Test
        void setVoteOutOfVotingWindow() {
            GenericResponseResult<LocationVote> result = getVoteResponse(null);

            ResponseAssertions.assertThat(result).codeIsBadRequest();
        }

        private GenericResponseResult<LocationVote> getVoteResponse(EventLocationVoteEntity voteEntity) {
            mockOwnerOrMember(true);

            EventLocationEntity location = EventLocationEntityCreator.create();

            Mockito.doReturn(location).when(events).findEventLocation(anyLong(), anyLong());
            Mockito.doReturn(voteEntity).when(eventLocations).createOrUpdateVote(anyObject(), anyObject(), anyObject(), anyBoolean());

            return voteRestService.setVote(
                    EXISTING_EVENT_ID,
                    EXISTING_EVENT_LOCATION_ID,
                    true,
                    request);
            }
    }

    @Nested
    class VotesByTime {

        @Test
        void getVotesNonExistingEvent() {
            GenericResponseResult<List<LocationVoteInfo>> result = voteRestService.getVotesByTime(
                    NON_EXISTING_EVENT_ID,
                    0L,
                    0L,
                    request);

            ResponseAssertions.assertThat(result).codeIsBadRequest();
        }


        @Test
        void getVotesInactiveEvent() {
            GenericResponseResult<List<LocationVoteInfo>> result = voteRestService.getVotesByTime(
                    INACTIVE_EVENT_ID,
                    0L,
                    0L,
                    request);

            ResponseAssertions.assertThat(result).codeIsBadRequest();
        }

        @Test
        void getVotesNoPrivilege() {
            mockOwnerOrMember(false);

            GenericResponseResult<List<LocationVoteInfo>> result = voteRestService.getVotesByTime(
                    EXISTING_EVENT_ID,
                    0L,
                    0L,
                    request);


            ResponseAssertions.assertThat(result).codeIsUnauthorized();
        }

        @Test
        void getVotes() {
            mockOwnerOrMember(true);

            GenericResponseResult<List<LocationVoteInfo>> result = voteRestService.getVotesByTime(
                    EXISTING_EVENT_ID,
                    0L,
                    0L,
                    request);


            ResponseAssertions.assertThat(result).codeIsOk();
        }
    }

    @Nested
    class VotesById {

        @Test
        void getVotesNonExisting() {
            GenericResponseResult<LocationVoteInfo> result = voteRestService.getVotesById(
                    NON_EXISTING_VOTE_ID,
                    request);

            ResponseAssertions.assertThat(result).codeIsBadRequest();
        }

        @Test
        void getVotesNoPrivilege() {
            mockOwnerOrMember(false);

            GenericResponseResult<LocationVoteInfo> result = voteRestService.getVotesById(
                    EXISTING_VOTE_ID,
                    request);


            ResponseAssertions.assertThat(result).codeIsUnauthorized();
        }

        @Test
        void getVotesInactiveEvent() {
            mockOwnerOrMember(true);

            GenericResponseResult<LocationVoteInfo> result = voteRestService.getVotesById(
                    EXISTING_VOTE_ID_INACTIVE_EVENT,
                    request);


            ResponseAssertions.assertThat(result).codeIsBadRequest();
        }

        @Test
        void getVotesNonExistingEvent() {
            mockOwnerOrMember(true);

            GenericResponseResult<LocationVoteInfo> result = voteRestService.getVotesById(
                    EXISTING_VOTE_ID_NON_EXISTING_EVENT,
                    request);


            ResponseAssertions.assertThat(result).codeIsBadRequest();
        }

        @Test
        void getVotes() {
            mockOwnerOrMember(true);

            GenericResponseResult<LocationVoteInfo> result = voteRestService.getVotesById(
                    EXISTING_VOTE_ID,
                    request);


            ResponseAssertions.assertThat(result).codeIsOk();
        }
    }
}