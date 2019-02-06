/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.business;

import net.m4e.app.resources.DocumentPool;
import net.m4e.common.*;
import net.m4e.system.core.AppInfos;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.*;

import javax.persistence.*;
import java.util.*;

import static org.mockito.Matchers.*;


/**
 * Base class for event location related tests
 *
 * @author boto
 * Date of creation February 14, 2018
 */
public class EventLocationsTestBase {

    @Mock
    EntityManager entityManager;
    @Mock
    Entities entities;
    @Mock
    AppInfos appInfos;
    @Mock
    DocumentPool docPool;

    EventLocations eventLocations;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);

        eventLocations = new EventLocations(entityManager, entities, appInfos, docPool);

        mockNamedQuery(Collections.emptyList());
    }

    protected void mockNamedQuery(List<EventLocationVoteEntity> voteEntities) {
        TypedQuery mockedTypedQuery = Mockito.mock(TypedQuery.class);
        Mockito.when(entityManager.createNamedQuery(anyString(), eq(EventLocationVoteEntity.class))).thenReturn(mockedTypedQuery);
        Mockito.when(mockedTypedQuery.setMaxResults(anyInt())).thenReturn(mockedTypedQuery);
        Mockito.when(mockedTypedQuery.getResultList()).thenReturn(voteEntities);
    }
}
