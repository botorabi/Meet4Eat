/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.business;

import net.m4e.app.user.business.UserEntity;
import net.m4e.common.EventEntityCreator;
import net.m4e.tests.EntityAssertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import java.util.*;

/**
 * @author boto
 * Date of creation February 12, 2018
 */
public class EventEntityTest extends EventEntityCreator {

    @Test
    void commonEntityTests() {
        EntityAssertions.assertThat(UserEntity.class)
                .isSerializable()
                .hasSerialVersionUID()
                .hasEntityAnnotation()
                .hasIdAnnotation()
                .conformsToEqualsContract()
                .hasHashCode()
                .hasProperToString();
    }

    @Test
    void setterGetter() {
        List<UserEntity> members = Arrays.asList(new UserEntity(), new UserEntity());
        List<EventLocationEntity> locations = Arrays.asList(new EventLocationEntity(), new EventLocationEntity());

        EventEntity entity = create();
        entity.setMembers(members);
        entity.setLocations(locations);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(entity.getId()).isEqualTo(EVENT_ID);
        softly.assertThat(entity.getName()).isEqualTo(EVENT_NAME);
        softly.assertThat(entity.getDescription()).isEqualTo(EVENT_DESCRIPTION);
        softly.assertThat(entity.getIsPublic()).isEqualTo(EVENT_IS_PUBLIC);
        softly.assertThat(entity.getStatus()).isNotNull();
        softly.assertThat(entity.getPhoto()).isNotNull();
        softly.assertThat(entity.getMembers()).hasSize(2);
        softly.assertThat(entity.getLocations()).hasSize(2);
        softly.assertThat(entity.getEventStart()).isEqualTo(EVENT_START);
        softly.assertThat(entity.getRepeatDayTime()).isEqualTo(EVENT_REPEAT_DAYTIME);
        softly.assertThat(entity.getRepeatWeekDays()).isEqualTo(EVENT_REPEAT_WEEKDAYS);
        softly.assertThat(entity.getVotingTimeBegin()).isEqualTo(EVENT_VOTING_BEGIN);
        softly.assertAll();
    }
}
