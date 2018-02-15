/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.business;

import net.m4e.app.resources.*;
import net.m4e.app.user.business.UserEntity;
import net.m4e.common.*;
import net.m4e.system.core.AppInfos;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.*;
import org.mockito.*;

import javax.persistence.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.*;


/**
 * Base class for event related tests
 *
 * @author boto
 * Date of creation February 14, 2018
 */
public class EventLocationsTest {

    @Mock
    EntityManager entityManager;
    @Mock
    Entities entities;
    @Mock
    AppInfos appInfos;
    @Mock
    DocumentPool docPool;

    EventLocations eventLocations;

    UserEntityCreator userEntityCreator;

    EventLocationVoteEntityCreator voteEntityCreator;


    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);

        eventLocations = new EventLocations(entityManager, entities, appInfos, docPool);
        voteEntityCreator = new EventLocationVoteEntityCreator();
        userEntityCreator = new UserEntityCreator();

        mockNamedQuery();
    }

    private void mockNamedQuery() {
        TypedQuery mockedTypedQuery = Mockito.mock(TypedQuery.class);
        Mockito.when(entityManager.createNamedQuery(anyString(), eq(EventLocationVoteEntity.class))).thenReturn(mockedTypedQuery);
        Mockito.when(mockedTypedQuery.setMaxResults(anyInt())).thenReturn(mockedTypedQuery);
    }

    @Test
    void defaultConstructor() {
        new EventLocations();
    }

    @Test
    void createNewLocation() {
        EventLocationEntity locationEntity = createLocationEntity();
        eventLocations.createNewLocation(new EventEntity(), locationEntity, 0L);
    }

    @Nested
    class UpdateLocation {

        private static final String LOCATION_NAME = "Location name";
        private static final String DESCRIPTION = "Description";

        private EventLocationEntity inputLocation;
        private EventLocationEntity eventLocationEntity;

        @BeforeEach
        void setup() {
            inputLocation = createLocationEntity();
            eventLocationEntity = createLocationEntity();
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
        void updateLocation() throws Exception {
            inputLocation.setName(LOCATION_NAME);
            inputLocation.setDescription(DESCRIPTION);
            inputLocation.setPhoto(new DocumentEntity());

            Mockito.when(entities.find(anyObject(), anyLong())).thenReturn(eventLocationEntity);

            EventLocationEntity updatedEntity = eventLocations.updateLocation(inputLocation);

            assertThat(updatedEntity.getName()).isEqualTo(LOCATION_NAME);
            assertThat(updatedEntity.getDescription()).isEqualTo(DESCRIPTION);
        }
    }

    private EventLocationEntity createLocationEntity() {
        EventLocationEntity entity = new EventLocationEntity();
        StatusEntity status = new StatusEntity();
        entity.setStatus(status);
        return entity;
    }

    @Test
    void createOrUpdateVote() {
        UserEntity voter = userEntityCreator.createUser();
        EventEntity event = new EventEntity();
        EventLocationEntity location = new EventLocationEntity();
        boolean vote = true;

        EventLocations partialMockedEventLocations = Mockito.spy(eventLocations);

        Mockito.doReturn(false).when(partialMockedEventLocations).calculateVoteBeginAndEndTime(anyObject());

        EventLocationVoteEntity entity = partialMockedEventLocations.createOrUpdateVote(voter, event, location, vote);

        assertThat(entity).isNull();

        Mockito.doReturn(true).when(partialMockedEventLocations).calculateVoteBeginAndEndTime(anyObject());
        Mockito.doReturn(new EventLocationVoteEntity()).when(partialMockedEventLocations).getOrCreateVoteEntity(anyObject(), anyObject());

        entity = partialMockedEventLocations.createOrUpdateVote(voter, event, location, vote);

        assertThat(entity).isNotNull();
    }

    @Test
    void createOrUpdateUnvote() {
        UserEntity voter = userEntityCreator.createUser();
        EventEntity event = new EventEntity();
        EventLocationEntity location = new EventLocationEntity();
        boolean vote = false;

        EventLocations partialMockedEventLocations = Mockito.spy(eventLocations);

        Mockito.doReturn(true).when(partialMockedEventLocations).calculateVoteBeginAndEndTime(anyObject());
        Mockito.doReturn(new EventLocationVoteEntity()).when(partialMockedEventLocations).getOrCreateVoteEntity(anyObject(), anyObject());

        EventLocationVoteEntity entity = partialMockedEventLocations.createOrUpdateVote(voter, event, location, vote);

        assertThat(entity).isNotNull();
    }

    @Test
    void getVotes() {
        eventLocations.getVotes(new EventEntity(), 0L, 0L);
    }

    @Test
    void findLocation() {
        eventLocations.findLocation(0L);
    }

    @Test
    void updateLocationImage() {
        eventLocations.updateEventLocationImage(new EventLocationEntity(), new DocumentEntity());
    }

    @Test
    void exportVotes() {
        EventLocationVoteEntity voteEntity = voteEntityCreator.createVoteEntity();

        LocationVoteInfo voteInfo = eventLocations.exportLocationVotes(voteEntity);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(voteInfo.getId()).isEqualTo(voteEntityCreator.VOTE_ID.toString());
        softly.assertThat(voteInfo.getEventId()).isEqualTo(voteEntityCreator.VOTE_EVENT_ID.toString());
        softly.assertThat(voteInfo.getLocationId()).isEqualTo(voteEntityCreator.VOTE_LOCATION_ID.toString());
        softly.assertThat(voteInfo.getLocationName()).isEqualTo(voteEntityCreator.VOTE_LOCATION_NAME);
        softly.assertThat(voteInfo.getCreationTime()).isEqualTo(voteEntityCreator.VOTE_CREATION_TIME);
        softly.assertThat(voteInfo.getTimeBegin()).isEqualTo(voteEntityCreator.VOTE_TIME_BEGIN);
        softly.assertThat(voteInfo.getTimeEnd()).isEqualTo(voteEntityCreator.VOTE_TIME_END);
        softly.assertThat(voteInfo.getUserIds()).hasSameElementsAs(voteEntityCreator.VOTE_USER_IDS);
        softly.assertThat(voteInfo.getUserNames()).hasSameElementsAs(voteEntityCreator.VOTE_USER_NAMES);
        softly.assertAll();
    }

    @Test
    void exportVotesList() {
        EventLocationVoteEntity voteEntity1 = voteEntityCreator.createVoteEntity();
        EventLocationVoteEntity voteEntity2 = voteEntityCreator.createVoteEntity();

        List<LocationVoteInfo> votes = eventLocations.exportLocationVotes(Arrays.asList(voteEntity1, voteEntity2));

        assertThat(votes).hasSize(2);
    }
}
