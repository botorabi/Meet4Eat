/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.business;

import net.m4e.app.auth.AuthRole;
import net.m4e.app.communication.ConnectedClients;
import net.m4e.app.user.rest.comm.UserCmd;
import org.jetbrains.annotations.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Matchers.anyObject;

/**
 * Base test class for Users
 *
 * @author boto
 * Date of creation February 6, 2018
 */
class UsersExportImportTest extends UsersTestBase {

    @Nested
    class ExportUser {

        @Mock
        ConnectedClients connections;

        @BeforeEach
        void setUp() {
            MockitoAnnotations.initMocks(this);
        }

        @Test
        void exportUserOnline() {
            UserEntity user = createUser();
            Mockito.when(connections.getConnectedUser(anyObject())).thenReturn(new UserEntity());

            UserInfo userInfo = users.exportUser(user, connections);

            assertThat(userInfo.getStatus()).isEqualTo(UserInfo.OnlineStatus.ONLINE);
        }

        @Test
        void exportUserOffline() {
            UserEntity user = createUser();
            Mockito.when(connections.getConnectedUser(anyObject())).thenReturn(null);

            UserInfo userInfo = users.exportUser(user, connections);

            assertThat(userInfo.getStatus()).isEqualTo(UserInfo.OnlineStatus.OFFLINE);
        }

        @Test
        void exportUsersAsAdmin() {
            List<UserEntity> allUsers = createUserEntities();

            UserEntity adminUser = createUserWithRoles(Arrays.asList(AuthRole.USER_ROLE_ADMIN));

            Mockito.when(connections.getConnectedUser(anyObject())).thenReturn(null);

            List<UserInfo> userInfo = users.exportUsers(allUsers, adminUser, connections);

            assertThat(userInfo.size()).isEqualTo(2);
        }

        @Test
        void exportUsersAsNonAdmin() {
            final Long NON_ADMIN_USER_ID = 32L;

            List<UserEntity> allUsers = createUserEntities();

            UserEntity nonAdminUser = createUser();
            nonAdminUser.setId(NON_ADMIN_USER_ID);

            Mockito.when(connections.getConnectedUser(anyObject())).thenReturn(null);

            List<UserInfo> userInfo = users.exportUsers(allUsers, nonAdminUser, connections);

            assertThat(userInfo.size()).isEqualTo(1);
            assertThat(userInfo.get(0).getId()).isEqualTo("" + NON_ADMIN_USER_ID);
        }

        @NotNull
        private List<UserEntity> createUserEntities() {
            UserEntity user1 = createUser();
            UserEntity user2 = createUser();
            UserEntity inactiveUser1 = createUser();
            inactiveUser1.getStatus().setEnabled(false);
            UserEntity inactiveUser2 = createUser();
            inactiveUser2.getStatus().setEnabled(false);
            return Arrays.asList(user1, user2, inactiveUser1, inactiveUser2);
        }
    }


    @Nested
    class ImportUser {

       @Test void importWithPhoto() {
            UserCmd cmd = createUserCmd();

            UserEntity importedUser = users.importUser(cmd);

            assertThat(importedUser.getName()).isEqualTo(cmd.getName());
            assertThat(importedUser.getLogin()).isEqualTo(cmd.getLogin());
            assertThat(importedUser.getEmail()).isEqualTo(cmd.getEmail());
            assertThat(importedUser.getPassword()).isEqualTo(cmd.getPassword());
            assertThat(importedUser.getRolesAsString()).containsAll(cmd.getRoles());
        }

        @Test void importWithoutPhoto() {
            UserCmd cmd = createUserCmd();
            cmd.setPhoto(null);

            UserEntity importedUser = users.importUser(cmd);

            assertThat(importedUser.getPhoto()).isNull();
        }

        @Test void importWithoutRoles() {
            UserCmd cmd = createUserCmd();
            cmd.setRoles(null);

            UserEntity importedUser = users.importUser(cmd);

            assertThat(importedUser.getRoles()).isNull();
            assertThat(importedUser.getRolesAsString()).isEmpty();
        }

        @NotNull
        private UserCmd createUserCmd() {
            UserCmd cmd = new UserCmd();
            cmd.setName(USER_NAME);
            cmd.setLogin(USER_LOGIN);
            cmd.setEmail(USER_EMAIL);
            cmd.setPassword("PW");
            cmd.setPhoto("CONTENT");
            cmd.setRoles(Arrays.asList(AuthRole.USER_ROLE_ADMIN));

            return cmd;
        }
    }
}
