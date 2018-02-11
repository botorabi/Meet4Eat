/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.business;

import net.m4e.app.resources.*;
import net.m4e.common.UserEntityCreator;
import org.assertj.core.api.SoftAssertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author boto
 * Date of creation February 1, 2018
 */
class UserInfoTest extends UserEntityCreator {

    final static UserInfo.OnlineStatus STATUS_ONLINE = UserInfo.OnlineStatus.ONLINE;
    final static List<String> ROLES_AS_STRINGS = Arrays.asList("ROLE1", "ROLE2");


    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void setterGetter() {
        UserInfo userInfo = new UserInfo();
        String photoIdAsString = "" + USER_PHOTO_ID;

        userInfo.setId(idAsString(USER_ID));
        userInfo.setLogin(USER_LOGIN);
        userInfo.setName(USER_NAME);
        userInfo.setEmail(USER_EMAIL);
        userInfo.setPhotoId(photoIdAsString);
        userInfo.setPhotoETag(USER_PHOTO_ETAG);
        userInfo.setDateCreation(USER_DATE_CREATION);
        userInfo.setDateLastLogin(USER_DATE_LAST_LOGIN);
        userInfo.setStatus(STATUS_ONLINE);
        userInfo.setRoles(ROLES_AS_STRINGS);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(userInfo.getId()).isEqualTo(idAsString(USER_ID));
        softly.assertThat(userInfo.getLogin()).isEqualTo(USER_LOGIN);
        softly.assertThat(userInfo.getName()).isEqualTo(USER_NAME);
        softly.assertThat(userInfo.getEmail()).isEqualTo(USER_EMAIL);
        softly.assertThat(userInfo.getPhotoId()).isEqualTo(photoIdAsString);
        softly.assertThat(userInfo.getPhotoETag()).isEqualTo(USER_PHOTO_ETAG);
        softly.assertThat(userInfo.getDateCreation()).isEqualTo(USER_DATE_CREATION);
        softly.assertThat(userInfo.getDateLastLogin()).isEqualTo(USER_DATE_LAST_LOGIN);
        softly.assertThat(userInfo.getStatus()).isEqualTo(STATUS_ONLINE);
        softly.assertThat(userInfo.getRoles()).containsAll(ROLES_AS_STRINGS);
        softly.assertAll();
    }

    @Test
    void fromUserEntity() {
        UserEntity user = createUserWithRoles(ROLES_AS_STRINGS);

        UserInfo userInfoFromEntity = UserInfo.fromUserEntity(user, UserInfo.OnlineStatus.ONLINE);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(userInfoFromEntity.getId()).isEqualTo(idAsString(USER_ID));
        softly.assertThat(userInfoFromEntity.getLogin()).isEqualTo(USER_LOGIN);
        softly.assertThat(userInfoFromEntity.getName()).isEqualTo(USER_NAME);
        softly.assertThat(userInfoFromEntity.getEmail()).isEqualTo(USER_EMAIL);
        softly.assertThat(userInfoFromEntity.getPhotoId()).isEqualTo(idAsString(USER_PHOTO_ID));
        softly.assertThat(userInfoFromEntity.getPhotoETag()).isEqualTo(USER_PHOTO_ETAG);
        softly.assertThat(userInfoFromEntity.getDateCreation()).isEqualTo(USER_DATE_CREATION);
        softly.assertThat(userInfoFromEntity.getDateLastLogin()).isEqualTo(USER_DATE_LAST_LOGIN);
        softly.assertThat(userInfoFromEntity.getStatus()).isEqualTo(STATUS_ONLINE);
        softly.assertThat(userInfoFromEntity.getRoles()).containsAll(ROLES_AS_STRINGS);
        softly.assertAll();
    }

    @Test
    void fromUserNoPhoto() {
        StatusEntity status = new StatusEntity();
        status.setDateCreation(USER_DATE_CREATION);

        UserEntity user = createUserEntityWithDocumentAndStatus(null, status);

        UserInfo userInfoFromEntity = UserInfo.fromUserEntity(user, UserInfo.OnlineStatus.ONLINE);

        assertThat(userInfoFromEntity.getPhotoId()).isEqualTo("");
        assertThat(userInfoFromEntity.getPhotoETag()).isEqualTo("");
    }

    @Test
    void fromUserNoStatus() {
        UserEntity user = createUserEntityWithDocumentAndStatus(null, null);

        UserInfo userInfoFromEntity = UserInfo.fromUserEntity(user, UserInfo.OnlineStatus.ONLINE);

        assertThat(userInfoFromEntity.getDateCreation()).isEqualTo(0L);
    }

    @NotNull
    private UserEntity createUserEntityWithDocumentAndStatus(DocumentEntity photo, StatusEntity status) {
        UserEntity user = createUserWithRoles(ROLES_AS_STRINGS);
        user.setPhoto(photo);
        user.setStatus(status);
        return user;
    }

    private String idAsString(Long id) {
        return "" + id;
    }
}
