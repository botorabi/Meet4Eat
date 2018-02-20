/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.business;

import net.m4e.app.communication.ConnectedClients;
import net.m4e.app.user.business.Users;
import net.m4e.common.*;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.*;
import org.mockito.*;

import javax.json.bind.*;

import static org.mockito.Matchers.anyLong;

/**
 * @author boto
 * Date of creation February 20, 2018
 */
public class EventInfoTest {
    @Mock
    Users users;

    @Mock
    ConnectedClients connections;

    Jsonb jsonb;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        jsonb = JsonbBuilder.create();

        Mockito.doReturn(null).when(connections).getConnectedUser(anyLong());
        Mockito.doReturn(UserEntityCreator.create()).when(users).findUser(anyLong());
    }

    @Test
    void eventInfoSerialization() {
        EventEntity eventEntity  = EventEntityCreator.create();

        EventInfo eventInfo = EventInfo.fromEventEntity(eventEntity, connections, users);

        String json = jsonb.toJson(eventInfo);

        SoftAssertions softy = new SoftAssertions();
        softy.assertThat(json).contains("id");
        softy.assertThat(json).contains("name");
        softy.assertThat(json).contains("description");
        softy.assertThat(json).contains("public");
        softy.assertThat(json).contains("photoId");
        softy.assertThat(json).contains("photoETag");
        softy.assertThat(json).contains("eventStart");
        softy.assertThat(json).contains("repeatWeekDays");
        softy.assertThat(json).contains("repeatDayTime");
        softy.assertThat(json).contains("votingTimeBegin");
        softy.assertThat(json).contains("members");
        softy.assertThat(json).contains("locations");
        softy.assertThat(json).contains("ownerId");
        softy.assertThat(json).contains("ownerName");
        softy.assertThat(json).contains("ownerPhotoId");
        softy.assertThat(json).contains("ownerPhotoETag");
        softy.assertThat(json).contains("ownerStatus");
        softy.assertAll();
    }
}
