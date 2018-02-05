/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.business;

import net.m4e.app.auth.*;
import net.m4e.app.resources.*;
import org.assertj.core.api.SoftAssertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author boto
 * Date of creation February 1, 2018
 */
class UserInfoTest {

    final static Long USER_ID = 42L;
    final static String EMAIL = "user@mailbox.com";
    final static String NAME = "Bob Dillen";
    final static String PHOTO_ID = "PhotoId";
    final static String PHOTO_ETAG = "PhotoETAG";
    final static String LOGIN = "MyLogin";
    final static Long DATE_CREATION = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
    final static Long DATE_LAST_LOGIN = Instant.now().toEpochMilli();
    final static UserInfo.OnlineStatus STATUS_ONLINE = UserInfo.OnlineStatus.ONLINE;
    final static List<String> ROLES_AS_STRINGS = Arrays.asList("ROLE1", "ROLE2");


    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);


    }

    @Test
    void setterGetter() {
        UserInfo userInfo = new UserInfo();

        userInfo.setId(idAsString(USER_ID));
        userInfo.setLogin(LOGIN);
        userInfo.setName(NAME);
        userInfo.setEmail(EMAIL);
        userInfo.setPhotoId(PHOTO_ID);
        userInfo.setPhotoETag(PHOTO_ETAG);
        userInfo.setDateCreation(DATE_CREATION);
        userInfo.setDateLastLogin(DATE_LAST_LOGIN);
        userInfo.setStatus(STATUS_ONLINE);
        userInfo.setRoles(ROLES_AS_STRINGS);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(userInfo.getId()).isEqualTo(idAsString(USER_ID));
        softly.assertThat(userInfo.getLogin()).isEqualTo(LOGIN);
        softly.assertThat(userInfo.getName()).isEqualTo(NAME);
        softly.assertThat(userInfo.getEmail()).isEqualTo(EMAIL);
        softly.assertThat(userInfo.getPhotoId()).isEqualTo(PHOTO_ID);
        softly.assertThat(userInfo.getPhotoETag()).isEqualTo(PHOTO_ETAG);
        softly.assertThat(userInfo.getDateCreation()).isEqualTo(DATE_CREATION);
        softly.assertThat(userInfo.getDateLastLogin()).isEqualTo(DATE_LAST_LOGIN);
        softly.assertThat(userInfo.getStatus()).isEqualTo(STATUS_ONLINE);
        softly.assertThat(userInfo.getRoles()).containsAll(ROLES_AS_STRINGS);
        softly.assertAll();
    }

    @Test
    void fromUserEntity() {
        Long PHOTO_DOC_ID = 101L;
        DocumentEntity photo = new DocumentEntity();
        photo.setId(PHOTO_DOC_ID);
        photo.setDocumentETag(PHOTO_ETAG);

        StatusEntity status = new StatusEntity();
        status.setDateCreation(DATE_CREATION);

        UserEntity user = createUserEntity(photo, status);

        UserInfo userInfoFromEntity = UserInfo.fromUserEntity(user, UserInfo.OnlineStatus.ONLINE);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(userInfoFromEntity.getId()).isEqualTo(idAsString(USER_ID));
        softly.assertThat(userInfoFromEntity.getLogin()).isEqualTo(LOGIN);
        softly.assertThat(userInfoFromEntity.getName()).isEqualTo(NAME);
        softly.assertThat(userInfoFromEntity.getEmail()).isEqualTo(EMAIL);
        softly.assertThat(userInfoFromEntity.getPhotoId()).isEqualTo("" + PHOTO_DOC_ID);
        softly.assertThat(userInfoFromEntity.getPhotoETag()).isEqualTo(PHOTO_ETAG);
        softly.assertThat(userInfoFromEntity.getDateCreation()).isEqualTo(DATE_CREATION);
        softly.assertThat(userInfoFromEntity.getDateLastLogin()).isEqualTo(DATE_LAST_LOGIN);
        softly.assertThat(userInfoFromEntity.getStatus()).isEqualTo(STATUS_ONLINE);
        softly.assertThat(userInfoFromEntity.getRoles()).containsAll(ROLES_AS_STRINGS);
        softly.assertAll();
    }

    @Test
    void fromUserNoPhoto() {
        StatusEntity status = new StatusEntity();
        status.setDateCreation(DATE_CREATION);

        UserEntity user = createUserEntity(null, status);

        UserInfo userInfoFromEntity = UserInfo.fromUserEntity(user, UserInfo.OnlineStatus.ONLINE);

        assertThat(userInfoFromEntity.getPhotoId()).isEqualTo("");
        assertThat(userInfoFromEntity.getPhotoETag()).isEqualTo("");
    }

    @Test
    void fromUserNoStatus() {
        UserEntity user = createUserEntity(null, null);

        UserInfo userInfoFromEntity = UserInfo.fromUserEntity(user, UserInfo.OnlineStatus.ONLINE);

        assertThat(userInfoFromEntity.getDateCreation()).isEqualTo(0L);
    }

    @NotNull
    private UserEntity createUserEntity(DocumentEntity photo, StatusEntity status) {
        UserEntity user = new UserEntity();
        user.setId(USER_ID);
        user.setLogin(LOGIN);
        user.setName(NAME);
        user.setEmail(EMAIL);
        user.setPhoto(photo);
        user.setStatus(status);
        user.setDateLastLogin(DATE_LAST_LOGIN);
        user.setRoles(rolesAsEntities(ROLES_AS_STRINGS));
        return user;
    }

    private String idAsString(Long id) {
        return "" + id;
    }

    private List<RoleEntity> rolesAsEntities(List<String> rolesAsStrings) {
        List<RoleEntity> roles = new ArrayList<>();
        for(String roleName: rolesAsStrings) {
            RoleEntity roleEntity = new RoleEntity();
            roleEntity.setName(roleName);
            roles.add(roleEntity);
        }
        return roles;
    }
}
