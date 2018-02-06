/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.business;

import net.m4e.tests.EntityAssertions;
import org.assertj.core.api.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;

import java.time.Instant;

/**
 * @author boto
 * Date of creation February 5, 2018
 */
class UserRegistrationEntityTest {

    private UserRegistrationEntity userRegistrationEntity;

    @BeforeEach
    void setUp() {
        userRegistrationEntity = new UserRegistrationEntity();
    }

    @Test
    void commonEntityTests() {
        EntityAssertions.assertThat(UserRegistrationEntity.class)
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
        final String ACTIVATION_TOKEN = "TheToken";
        final long id = 42L;
        final long requestDate = Instant.now().toEpochMilli();
        UserEntity user = new UserEntity();
        user.setId(142L);

        userRegistrationEntity.setId(id);
        userRegistrationEntity.setUser(user);
        userRegistrationEntity.setActivationToken(ACTIVATION_TOKEN);
        userRegistrationEntity.setRequestDate(requestDate);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(userRegistrationEntity.getId()).isEqualTo(id);
        softly.assertThat(userRegistrationEntity.getUser()).isEqualTo(user);
        softly.assertThat(userRegistrationEntity.getActivationToken()).isEqualTo(ACTIVATION_TOKEN);
        softly.assertThat(userRegistrationEntity.getRequestDate()).isEqualTo(requestDate);
        softly.assertAll();
    }

    @Test
    void createActivationToken() {
        Assertions.assertThat(userRegistrationEntity.createActivationToken()).isNotEmpty();
    }

    @Test
    void getActivationToken() {
        String token = userRegistrationEntity.createActivationToken();
        Assertions.assertThat(userRegistrationEntity.getActivationToken()).isEqualTo(token);
    }
}
