/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.business;

import net.m4e.app.auth.RoleEntity;
import net.m4e.app.resources.*;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

interface DefaultUserData {

    Long   USER_ID = 1000L;
    String USER_EMAIL = "user@mailbox.com";
    String USER_NAME = "Bob Dillon";
    Long   USER_STATUS_ID = 3000L;
    Long   USER_PHOTO_ID = 4000L;
    String USER_PHOTO_ETAG = "PhotoETAG";
    Long   USER_PROFILE_ID = 5000L;
    String USER_LOGIN = "MyLogin";
    Long   USER_DATE_CREATION = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
    Long   USER_DATE_LAST_LOGIN = Instant.now().toEpochMilli();

    /**
     * Create a user entity with default data.
     */
    default UserEntity createUser() {
        UserEntity user = new UserEntity();
        user.setId(USER_ID);
        user.setName(USER_NAME);
        user.setLogin(USER_LOGIN);
        user.setEmail(USER_EMAIL);
        user.setDateLastLogin(USER_DATE_LAST_LOGIN);

        StatusEntity status = new StatusEntity();
        status.setId(USER_STATUS_ID);
        status.setDateCreation(USER_DATE_CREATION);
        status.setIdOwner(user.getId());
        user.setStatus(status);

        DocumentEntity photo = new DocumentEntity();
        photo.setId(USER_PHOTO_ID);
        photo.setDocumentETag(USER_PHOTO_ETAG);
        user.setPhoto(photo);

        UserProfileEntity profile = new UserProfileEntity();
        profile.setId(USER_PROFILE_ID);
        user.setProfile(profile);

        return user;
    }

    /**
     * Create a user entity with default data and given roles.
     */
    default UserEntity createUserWithRoles(@NotNull final List<String> roles) {
        UserEntity user = createUser();
        user.setRoles(createRoleEntities(roles));

        return user;
    }

    /**
     * Create role entities out of given roles strings.
     */
    default List<RoleEntity> createRoleEntities(@NotNull final List<String> roles) {
        final Long id = 20000L;
        List<RoleEntity> roleEntities = new ArrayList<>();
        roles.stream().forEach(roleName -> {
            RoleEntity role = new RoleEntity();
            role.setName(roleName);
            role.setId(id + roleEntities.size());
            roleEntities.add(role);
        });
        return roleEntities;
    }
}
