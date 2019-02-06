/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.rest;

import net.m4e.app.auth.AuthorityConfig;
import net.m4e.app.communication.ConnectedClients;
import net.m4e.app.user.business.*;
import net.m4e.app.user.rest.comm.*;
import net.m4e.common.*;
import net.m4e.system.core.*;
import net.m4e.tests.ResponseAssertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.mockito.*;

import javax.servlet.http.*;
import java.util.*;

import static org.mockito.Matchers.*;

/**
 * @author boto
 * Date of creation February 11, 2018
 */
class UserRestServiceTest {

    private static final String SESSION_ID = "session_id";

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
    HttpServletRequest request;
    @Mock
    HttpSession session;

    UserMockUp userMockUp;

    UserRestService restService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(users.importUser(anyObject())).thenCallRealMethod();
        Mockito.when(users.createNewUser(anyObject(), anyLong())).thenReturn(UserEntityCreator.create());
        Mockito.when(session.getId()).thenReturn(SESSION_ID);
        Mockito.when(request.getSession()).thenReturn(session);

        restService = new UserRestService(users, entities, validator, registration, appInfos, connections);
        userMockUp = new UserMockUp(users);
    }

    @Nested
    class UserCreationDeletion {

        @Test
        void createUserSuccess() throws Exception {
            mockSessionUser(userMockUp.mockAdminUser());
            mockNewUserValidationSuccess();

            UserCmd userCmd = createUserCmd();

            GenericResponseResult<UserId> response = restService.createUser(userCmd, request);

            ResponseAssertions.assertThat(response).hasStatusOk();
        }

        @Test
        void createUserValidationFailed() throws Exception {
            userMockUp.mockSomeUser();
            mockNewUserValidationFailed();

            UserCmd userCmd = createUserCmd();

            GenericResponseResult<UserId> response = restService.createUser(userCmd, request);

            ResponseAssertions.assertThat(response).hasStatusNotOk();
        }

        @Test
        void deleteUserSelfDeletion() {
            mockSessionUser(userMockUp.mockUser(UserMockUp.USER_ID_ADMIN, UserMockUp.USER_NAME_ADMIN));

            GenericResponseResult<UserId> response = restService.remove(UserMockUp.USER_ID_ADMIN, request);

            ResponseAssertions.assertThat(response).hasStatusNotOk();
        }

        @Test
        void deleteNonExistingUser() {
            userMockUp.mockUser(UserMockUp.USER_ID_NON_ADMIN, UserMockUp.USER_NAME_NON_ADMIN);
            mockSessionUser(userMockUp.mockAdminUser());

            GenericResponseResult<UserId> response = restService.remove(UserMockUp.USER_ID_OTHER, request);

            ResponseAssertions.assertThat(response).hasStatusNotOk();
        }

        @Test
        void deleteNonActiveUser() {
            userMockUp.mockSomeUser().getStatus().setEnabled(false);
            mockSessionUser(userMockUp.mockAdminUser());

            GenericResponseResult<UserId> response = restService.remove(UserMockUp.USER_ID_OTHER, request);

            ResponseAssertions.assertThat(response).hasStatusNotOk();
        }

        @Test
        void deletionMarkingFailure() throws Exception {
            userMockUp.mockSomeUser();
            mockSessionUser(userMockUp.mockAdminUser());

            Mockito.doThrow(new Exception("Failure marking user as deleted")).when(users).markUserAsDeleted(anyObject());

            GenericResponseResult<UserId> response = restService.remove(UserMockUp.USER_ID_OTHER, request);

            ResponseAssertions.assertThat(response).hasStatusNotOk();
        }

        @Test
        void deleteExistingActiveUser() {
            userMockUp.mockSomeUser();
            mockSessionUser(userMockUp.mockAdminUser());

            GenericResponseResult<UserId> response = restService.remove(UserMockUp.USER_ID_OTHER, request);

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
            userMockUp.mockUser(UserMockUp.USER_ID_ADMIN, UserMockUp.USER_NAME_ADMIN);
            userMockUp.mockUser(UserMockUp.USER_ID_NON_ADMIN, UserMockUp.USER_NAME_NON_ADMIN);
            userMockUp.mockSomeUser();

            UserEntity user1 = UserEntityCreator.create();
            UserEntity user2 = UserEntityCreator.create();
            Mockito.when(entities.findAll(UserEntity.class)).thenReturn(Arrays.asList(user1, user2));
        }

        @Test
        void findUserFound() {
            mockSessionUser(userMockUp.mockSomeUser());
            Mockito.when(users.userIsOwnerOrAdmin(anyObject(), anyObject())).thenReturn(true);

            GenericResponseResult<UserInfo> response = restService.find(UserMockUp.USER_ID_OTHER, request);

            ResponseAssertions.assertThat(response).hasStatusOk();
        }

        @Test
        void findUserNotFound() {
            mockSessionUser(userMockUp.mockSomeUser());
            Mockito.when(users.findUser(anyLong())).thenReturn(null);

            GenericResponseResult<UserInfo> response = restService.find(UserMockUp.USER_ID_OTHER, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsNotFound();
        }

        @Test
        void findUserNoPrivilege() {
            mockSessionUser(userMockUp.mockSomeUser());
            Mockito.when(users.userIsOwnerOrAdmin(anyObject(), anyObject())).thenReturn(false);

            GenericResponseResult<UserInfo> response = restService.find(UserMockUp.USER_ID_OTHER, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsUnauthorized();
        }

        @Test
        void findAllUsers() {
            mockSessionUser(userMockUp.mockSomeUser());

            GenericResponseResult<List<UserInfo>> response = restService.findAllUsers(request);

            ResponseAssertions.assertThat(response)
                    .hasStatusOk()
                    .hasData();
        }

        @Test
        void findAllUsersWithRange() {
            mockSessionUser(userMockUp.mockSomeUser());

            GenericResponseResult<List<UserInfo>> response = restService.findRange(1, 10, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusOk()
                    .hasData();
        }
    }

    @Nested
    class SearchForUser {
        @BeforeEach
        void setup() {
            UserEntity user1 = UserEntityCreator.create();
            UserEntity user2 = UserEntityCreator.create();
            UserEntity inactiveUser = UserEntityCreator.create();
            inactiveUser.getStatus().setEnabled(false);

            Mockito.when(entities.searchForString(anyObject(), anyString(), anyListOf(String.class), anyInt()))
                    .thenReturn(Arrays.asList(user1, user2, inactiveUser));
        }

        @Test
        void keywordNull() {
            GenericResponseResult<List<SearchHitUser>> response = restService.search(null);

            checkNoHits(response);
        }

        @Test
        void keywordEmpty() {
            GenericResponseResult<List<SearchHitUser>> response = restService.search("");

            checkNoHits(response);
        }

        @Test
        void keywordTooShort() {
            GenericResponseResult<List<SearchHitUser>> response = restService.search("123");

            checkNoHits(response);
        }

        @Test
        void keywordWithoutEmail() {
            GenericResponseResult<List<SearchHitUser>> response = restService.search("theusername");

            checkHasHits(response, 2);
        }

        @Test
        void keywordWithEmail() {
            GenericResponseResult<List<SearchHitUser>> response = restService.search("theuser@email.com");

            checkHasHits(response, 2);
        }

        @Test
        void noHitsOfAdmins() {
            Mockito.when(users.checkUserRoles(anyObject(), anyListOf(String.class))).thenReturn(true);

            GenericResponseResult<List<SearchHitUser>> response = restService.search("theusername");

            checkNoHits(response);
        }

        private void checkNoHits(GenericResponseResult<List<SearchHitUser>> response) {
            ResponseAssertions.assertThat(response)
                    .hasStatusOk()
                    .hasData();

            ResponseAssertions.assertThat(response.getData()).hasSize(0);
        }

        private void checkHasHits(GenericResponseResult<List<SearchHitUser>> response, int countHits) {
            ResponseAssertions.assertThat(response)
                    .hasStatusOk()
                    .hasData();

            ResponseAssertions.assertThat(response.getData()).hasSize(countHits);
        }
    }

    @Test
    void countUsers() {
        Mockito.when(appInfos.getAppInfoEntity()).thenReturn(new AppInfoEntity());

        GenericResponseResult<UserCount> response = restService.count();

        ResponseAssertions.assertThat(response)
                .hasStatusOk()
                .hasData();
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
