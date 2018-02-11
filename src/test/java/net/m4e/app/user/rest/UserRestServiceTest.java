/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.rest;

import net.m4e.app.auth.*;
import net.m4e.app.communication.ConnectedClients;
import net.m4e.app.resources.*;
import net.m4e.app.user.business.*;
import net.m4e.app.user.rest.comm.*;
import net.m4e.common.*;
import net.m4e.system.core.AppInfos;
import net.m4e.tests.ResponseAssertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.mockito.*;

import javax.servlet.http.*;
import java.util.Arrays;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;

/**
 * @author boto
 * Date of creation February 11, 2018
 */
class UserRestServiceTest extends UserEntityCreator {


    private static final String SESSION_ID = "session_id";
    private static final Long USER_ID_ADMIN = 10000L;
    private static final String USER_NAME_ADMIN = "admin";
    private static final Long USER_ID_NON_ADMIN = 20000L;
    private static final String USER_NAME_NON_ADMIN = "nonadmin";
    private static final Long USER_ID_OTHER = 30000L;
    private static final String USER_NAME_OTHER = "otheruser";

    @Mock
    Users users;
    @Mock
    Entities entities;
    @Mock
    UserValidator validator;
    @Mock
    UserRegistrations registration;
    @Mock
    AppInfos appInfos;
    @Mock
    ConnectedClients connections;
    @Mock
    DocumentPool docPool;

    @Mock
    HttpServletRequest request;
    @Mock
    HttpSession session;

    UserRestService restService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(users.importUser(anyObject())).thenCallRealMethod();
        Mockito.when(users.createNewUser(anyObject(), anyLong())).thenReturn(createUser());
        Mockito.when(session.getId()).thenReturn(SESSION_ID);
        Mockito.when(request.getSession()).thenReturn(session);

        restService = new UserRestService(users, entities, validator, registration, appInfos, connections);
    }

    @Nested
    class UserCreationDeletion {

        @Test
        void createUserSuccess() throws Exception {
            mockSessionUser(mockAdminUser());
            mockNewUserValidationSuccess();

            UserCmd userCmd = createUserCmd();

            GenericResponseResult<CreateUser> response = restService.createUser(userCmd, request);

            ResponseAssertions.assertThat(response).hasStatusOk();
        }

        @Test
        void createUserValidationFailed() throws Exception {
            mockAdminUser();
            mockNewUserValidationFailed();

            UserCmd userCmd = createUserCmd();

            GenericResponseResult<CreateUser> response = restService.createUser(userCmd, request);

            ResponseAssertions.assertThat(response).hasStatusNotOk();
        }

        @Test
        void deleteUserSelfDeletion() {
            mockSessionUser(mockUser(USER_ID_ADMIN, USER_NAME_ADMIN));

            GenericResponseResult<DeleteUser> response = restService.remove(USER_ID_ADMIN, request);

            ResponseAssertions.assertThat(response).hasStatusNotOk();
        }

        @Test
        void deleteNonExistingUser() {
            mockUser(USER_ID_NON_ADMIN, USER_NAME_NON_ADMIN);
            mockSessionUser(mockAdminUser());

            GenericResponseResult<DeleteUser> response = restService.remove(USER_ID_OTHER, request);

            ResponseAssertions.assertThat(response).hasStatusNotOk();
        }

        @Test
        void deleteNonActiveUser() {
            mockUser(USER_ID_OTHER, USER_NAME_OTHER).getStatus().setEnabled(false);
            mockSessionUser(mockAdminUser());

            GenericResponseResult<DeleteUser> response = restService.remove(USER_ID_OTHER, request);

            ResponseAssertions.assertThat(response).hasStatusNotOk();
        }

        @Test
        void deletionMarkingFailure() throws Exception {
            mockUser(USER_ID_OTHER, USER_NAME_OTHER).getStatus().setEnabled(true);
            mockSessionUser(mockAdminUser());

            Mockito.doThrow(new Exception("Failure marking user as deleted")).when(users).markUserAsDeleted(anyObject());

            GenericResponseResult<DeleteUser> response = restService.remove(USER_ID_OTHER, request);

            ResponseAssertions.assertThat(response).hasStatusNotOk();
        }

        @Test
        void deleteExistingActiveUser() {
            mockUser(USER_ID_OTHER, USER_NAME_OTHER).getStatus().setEnabled(true);
            mockSessionUser(mockAdminUser());

            GenericResponseResult<DeleteUser> response = restService.remove(USER_ID_OTHER, request);

            ResponseAssertions.assertThat(response).hasStatusOk();
        }

        @NotNull
        private UserCmd createUserCmd() {
            UserCmd userCmd = new UserCmd();
            userCmd.setLogin("login");
            userCmd.setName("name");
            userCmd.setPassword("password");
            return userCmd;
        }
    }

    @Nested
    class FindUsers {

        @BeforeEach
        void setup() {
            mockUser(USER_ID_ADMIN, USER_NAME_ADMIN);
            mockUser(USER_ID_NON_ADMIN, USER_NAME_NON_ADMIN);
            mockUser(USER_ID_OTHER, USER_NAME_OTHER);
        }

        @Test
        void findUserByAdmin() {
            mockSessionUser(mockAdminUser());
            Mockito.when(users.userIsOwnerOrAdmin(anyObject(), anyObject())).thenReturn(true);

            GenericResponseResult<UserInfo> response = restService.find(USER_ID_OTHER, request);

            ResponseAssertions.assertThat(response).hasStatusOk();
        }
    }

    private UserEntity mockAdminUser() {
        UserEntity user = createUserWithRoles(Arrays.asList(AuthRole.USER_ROLE_ADMIN));
        user.setId(USER_ID_ADMIN);
        user.setName(USER_NAME_ADMIN);

        Mockito.when(users.findUser(USER_NAME_ADMIN)).thenReturn(user);

        return user;
    }

    private UserEntity mockUser(final Long id, final String name) {
        UserEntity user = createUser();
        user.setId(id);
        user.setName(name);

        Mockito.when(users.findUser(id)).thenReturn(user);
        Mockito.when(users.findUser(name)).thenReturn(user);

        return user;
    }

    private void mockSessionUser(UserEntity user) {
        Mockito.when(session.getAttribute(AuthorityConfig.SESSION_ATTR_USER)).thenReturn(user);
    }

    private void mockNewUserValidationSuccess() throws Exception {
        Mockito.when(validator.validateNewEntityInput(anyObject(), anyObject())).thenReturn(new UserEntity());
    }

    private void mockNewUserValidationFailed() throws Exception {
        Mockito.when(validator.validateNewEntityInput(anyObject(), anyObject())).thenThrow(new Exception("Validation failed"));
    }
}
