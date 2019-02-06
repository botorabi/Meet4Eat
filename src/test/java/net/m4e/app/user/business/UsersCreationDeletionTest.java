/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.business;

import net.m4e.app.auth.AuthRole;
import net.m4e.common.UserEntityCreator;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Matchers.anyObject;

/**
 * @author boto
 * Date of creation February 6, 2018
 */
class UsersCreationDeletionTest extends UsersTestBase {

    @Nested
    class CreateNewUser {

        @Test
        void noCreatorID() {
            Long NEW_USER_ID = 10L;
            UserEntity inputEntity = UserEntityCreator.create();

            Mockito.doAnswer(invocationOnMock -> {
                UserEntity user = invocationOnMock.getArgumentAt(0, UserEntity.class);
                user.setId(NEW_USER_ID);
                return null;
            }).when(entities).create(anyObject());

            UserEntity newUser = users.createNewUser(inputEntity, null);

            assertThat(newUser.getStatus().getIdOwner()).isEqualTo(NEW_USER_ID);
            assertThat(newUser.getStatus().getIdCreator()).isEqualTo(NEW_USER_ID);
        }

        @Test
        void withCreatorID() {
            Long NEW_USER_ID = 10L;
            Long CREATOR_ID = 300L;
            UserEntity inputEntity = UserEntityCreator.create();

            Mockito.doAnswer(invocationOnMock -> {
                UserEntity user = invocationOnMock.getArgumentAt(0, UserEntity.class);
                user.setId(NEW_USER_ID);
                return null;
            }).when(entities).create(anyObject());

            UserEntity newUser = users.createNewUser(inputEntity, CREATOR_ID);

            assertThat(newUser.getStatus().getIdOwner()).isEqualTo(NEW_USER_ID);
            assertThat(newUser.getStatus().getIdCreator()).isEqualTo(CREATOR_ID);
        }

        @Test
        void withRoles() {
            UserEntity inputEntity = createWithRoles(Arrays.asList(AuthRole.USER_ROLE_ADMIN, AuthRole.USER_ROLE_MODERATOR));

            UserEntity newUser = users.createNewUser(inputEntity, null);

            assertThat(newUser.getRolesAsString()).contains(AuthRole.USER_ROLE_ADMIN, AuthRole.USER_ROLE_MODERATOR);
        }
    }

    @Nested
    class UserDeletion {

        @Test
        void markWithInvalidStatus() {
            UserEntity user = UserEntityCreator.create();
            user.setStatus(null);

            try {
                users.markUserAsDeleted(user);
                fail("User deletion marking did not detect an invalid user status");
            } catch (Exception ex) {
            }
        }

        @Test
        void markWithInternalError() {
            UserEntity user = UserEntityCreator.create();

            Mockito.when(appInfos.getAppInfoEntity()).thenReturn(null);

            try {
                users.markUserAsDeleted(user);
                fail("User deletion marking did not detect an internal error");
            } catch (Exception ex) {
            }
        }

        @Test
        void markAsDeleted() {
            UserEntity user = UserEntityCreator.create();

            try {
                users.markUserAsDeleted(user);

                assertThat(user.getStatus().getIsDeleted()).isTrue();

            } catch (Exception ex) {
                fail("Could not mark user as deleted, reason: " + ex.getMessage());
            }
        }

        @Test
        void getDeletedUsers() {
            UserEntity userNotDeleted = UserEntityCreator.create();
            UserEntity userDeleted = UserEntityCreator.create();

            try {
                users.markUserAsDeleted(userDeleted);
            } catch (Exception ex) {
                fail("Could not mark user as deleted, reason: " + ex.getMessage());
            }

            Mockito.when(entities.findAll(UserEntity.class)).thenReturn(Arrays.asList(userNotDeleted, userDeleted));

            List<UserEntity> deletedUsers = users.getMarkedAsDeletedUsers();

            assertThat(deletedUsers.size()).isEqualTo(1);
        }

        @Test
        void deleteUser() {
            UserEntity user = UserEntityCreator.create();
            users.deleteUser(user);
        }
    }
}
