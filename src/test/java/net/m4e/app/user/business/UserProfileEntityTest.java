/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.business;

import net.m4e.app.resources.DocumentEntity;
import net.m4e.tests.EntityAssertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.*;

import java.time.Instant;

/**
 * @author boto
 * Date of creation February 5, 2018
 */
class UserProfileEntityTest {

    private UserProfileEntity userProfileEntity;

    @BeforeEach
    void setUp() {
        userProfileEntity = new UserProfileEntity();
    }

    @Test
    void commonEntityTests() {
        EntityAssertions.assertThat(UserProfileEntity.class)
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
        final long id = 42L;
        final Long BIRTHDAY = 100000L;
        final String BIO = "This is my bio ....";
        final DocumentEntity photo = new DocumentEntity();
        photo.setId(100L);

        userProfileEntity.setId(id);
        userProfileEntity.setBirthday(BIRTHDAY);
        userProfileEntity.setBio(BIO);
        userProfileEntity.setPhoto(photo);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(userProfileEntity.getId()).isEqualTo(id);
        softly.assertThat(userProfileEntity.getBirthday()).isEqualTo(BIRTHDAY);
        softly.assertThat(userProfileEntity.getBio()).isEqualTo(BIO);
        softly.assertThat(userProfileEntity.getPhoto()).isEqualTo(photo);
        softly.assertAll();
    }
}
