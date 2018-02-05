/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.business;

import net.m4e.tests.EntityAssertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.*;

import java.time.Instant;

/**
 * @author boto
 * Date of creation February 5, 2018
 */
class UserPasswordResetEntityTest {

    private UserPasswordResetEntity userPasswordResetEntity;

    @BeforeEach
    void setUp() {
        userPasswordResetEntity = new UserPasswordResetEntity();
    }

    @Test
    void commonEntityTests() {
        EntityAssertions.assertThat(UserPasswordResetEntity.class)
                .isSerializable()
                .hasSerialVersionUID()
                .hasEntityAnnotation()
                .hasIdAnnotation()
                .conformsToEqualsContract()
                .hasHashCode()
                .hasProperToString()
                ;
    }

    @Test
    void setterGetter() {
        final String RESET_TOKEN = "TheToken";
        final long id = 42L;
        final long requestDate = Instant.now().toEpochMilli();
        UserEntity user = new UserEntity();
        user.setId(142L);

        userPasswordResetEntity.setId(id);
        userPasswordResetEntity.setUser(user);
        userPasswordResetEntity.setResetToken(RESET_TOKEN);
        userPasswordResetEntity.setRequestDate(requestDate);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(userPasswordResetEntity.getId()).isEqualTo(id);
        softly.assertThat(userPasswordResetEntity.getUser()).isEqualTo(user);
        softly.assertThat(userPasswordResetEntity.getResetToken()).isEqualTo(RESET_TOKEN);
        softly.assertThat(userPasswordResetEntity.getRequestDate()).isEqualTo(requestDate);
        softly.assertAll();
    }
}
