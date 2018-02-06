/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.business;

import net.m4e.app.communication.ConnectedClients;
import org.junit.jupiter.api.*;
import org.mockito.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;

/**
 * Base test class for Users
 *
 * @author boto
 * Date of creation February 6, 2018
 */
class UsersExportTest extends UsersTestBase {

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
    }
}
