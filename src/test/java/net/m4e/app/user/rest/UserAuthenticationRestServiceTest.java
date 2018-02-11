/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.rest;

import net.m4e.app.auth.AuthorityConfig;
import net.m4e.app.resources.StatusEntity;
import net.m4e.app.user.business.*;
import net.m4e.app.user.rest.comm.*;
import net.m4e.common.GenericResponseResult;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.mockito.*;

import javax.servlet.http.*;

/**
 * @author ybroeker
 */
class UserAuthenticationRestServiceTest {

    private final static String EXISTING_USER = "testuser";
    private final static String NON_EXISTING_USER = "nonexisting";
    private final static String PASSWORD = "password";

    private final static String SESSION_ID = "session_id";

    private final static LoginCmd  RIGHT_CREDENTIALS = new LoginCmd(EXISTING_USER, clientSideHash(PASSWORD, SESSION_ID));
    private final static LoginCmd  WRONG_CREDENTIALS = new LoginCmd(EXISTING_USER, clientSideHash("wrong", "salt"));
    private final static LoginCmd  NON_EXITING_USER_CREDENTIALS = new LoginCmd(NON_EXISTING_USER, clientSideHash("wrong", "salt"));
    private final static LoginCmd  INVALID_CREDENTIALS1 = new LoginCmd("", "pw");
    private final static LoginCmd  INVALID_CREDENTIALS2 = new LoginCmd("login", "");
    private final static LoginCmd  EMPTY_CREDENTIALS = new LoginCmd("", "");

    @Mock
    Users users;
    @Mock
    HttpServletRequest request;
    @Mock
    HttpSession session;

    UserAuthenticationRestService userAuthentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(session.getId()).thenReturn(SESSION_ID);
        Mockito.when(request.getSession()).thenReturn(session);
        userAuthentication = new UserAuthenticationRestService(users);

        UserEntity userEntity = new UserEntity();
        userEntity.setStatus(new StatusEntity());
        userEntity.setPassword(AuthorityConfig.getInstance().createPassword(PASSWORD));
        userEntity.setLogin(EXISTING_USER);
        userEntity.setId(1L);
        Mockito.when(users.findUser(EXISTING_USER)).thenReturn(userEntity);
        Mockito.when(users.findUser(NON_EXISTING_USER)).thenReturn(null);
    }

    private static String clientSideHash(String plainPassword, String salt) {
        return AuthorityConfig.getInstance().createPassword(
                AuthorityConfig.getInstance().createPassword(plainPassword) + salt);
    }

    @Test
    void stateAuthenticated() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(42L);
        Mockito.when(session.getAttribute(AuthorityConfig.SESSION_ATTR_USER)).thenReturn(userEntity);

        Assertions.assertThat(userAuthentication.state(request).getCode()).isEqualTo(GenericResponseResult.CODE_OK);
        Assertions.assertThat(userAuthentication.state(request).getData().isAuth()).isTrue();
    }

    @Test
    void stateNotAuthenticated() {
        Mockito.when(session.getAttribute(AuthorityConfig.SESSION_ATTR_USER)).thenReturn(null);

        Assertions.assertThat(userAuthentication.state(request).getCode()).isEqualTo(GenericResponseResult.CODE_OK);
        Assertions.assertThat(userAuthentication.state(request).getData().isAuth()).isFalse();
    }

    @Test
    void loginRightCredentials() {
        Assertions.assertThat(userAuthentication.login(RIGHT_CREDENTIALS, request).getCode()).isEqualTo(GenericResponseResult.CODE_OK);
    }

    @Test
    void loginWrongCredentials() {
        Assertions.assertThat(userAuthentication.login(WRONG_CREDENTIALS, request).getCode()).isEqualTo(GenericResponseResult.CODE_UNAUTHORIZED);
    }

    @Test
    void loginAlreadyLoggedInUser() {
        LoginCmd input = RIGHT_CREDENTIALS;
        Mockito.when(session.getAttribute(AuthorityConfig.SESSION_ATTR_USER)).thenReturn(new Object());

        GenericResponseResult<LoggedIn> response = userAuthentication.login(input, request);


        Assertions.assertThat(response.getCode()).isEqualTo(GenericResponseResult.CODE_NOT_ACCEPTABLE);
    }

    @Test
    void loginNonExistingUser() {
        Assertions.assertThat(userAuthentication.login(NON_EXITING_USER_CREDENTIALS, request).getCode()).isEqualTo(GenericResponseResult.CODE_NOT_FOUND);
    }

    @Test
    void loginNonActiveUser() {
        StatusEntity status = new StatusEntity();
        status.setEnabled(false);
        UserEntity userEntity = new UserEntity();
        userEntity.setStatus(status);
        userEntity.setId(142L);


        Mockito.when(users.findUser(NON_EXISTING_USER)).thenReturn(userEntity);


        Assertions.assertThat(userAuthentication.login(NON_EXITING_USER_CREDENTIALS, request).getCode()).isEqualTo(GenericResponseResult.CODE_NOT_FOUND);
    }

    @Test
    void loginInvalidInput() {
        Assertions.assertThat(userAuthentication.login(INVALID_CREDENTIALS1, request).getCode()).isEqualTo(GenericResponseResult.CODE_BAD_REQUEST);
        Assertions.assertThat(userAuthentication.login(INVALID_CREDENTIALS2, request).getCode()).isEqualTo(GenericResponseResult.CODE_BAD_REQUEST);
    }

    @Test
    void loginEmptyInput() {
        Assertions.assertThat(userAuthentication.login(EMPTY_CREDENTIALS, request).getCode()).isEqualTo(GenericResponseResult.CODE_BAD_REQUEST);
    }

    @Test
    void logoutLoggedIn() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(142L);


        Mockito.when(session.getAttribute(AuthorityConfig.SESSION_ATTR_USER)).thenReturn(userEntity);


        Assertions.assertThat(userAuthentication.logout(request).getCode()).isEqualTo(GenericResponseResult.CODE_OK);
    }

    @Test
    void logoutNotLoggedIn() {
        Mockito.when(session.getAttribute(AuthorityConfig.SESSION_ATTR_USER)).thenReturn(null);


        Assertions.assertThat(userAuthentication.logout(request).getCode()).isEqualTo(GenericResponseResult.CODE_NOT_ACCEPTABLE);
    }
}
