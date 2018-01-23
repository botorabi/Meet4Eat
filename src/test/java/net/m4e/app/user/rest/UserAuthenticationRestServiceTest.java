/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.AssertTrue;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.revinate.assertj.json.JsonPathAssert;
import net.m4e.app.auth.AuthorityConfig;
import net.m4e.app.resources.StatusEntity;
import net.m4e.app.user.business.UserEntity;
import net.m4e.app.user.business.Users;
import net.m4e.app.user.rest.UserAuthenticationRestService;
import net.m4e.app.user.rest.comm.LoggedIn;
import net.m4e.app.user.rest.comm.LoginCmd;
import net.m4e.common.GenericResponseResult;
import net.m4e.common.ResponseResults;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

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
    void default_constructor() {
        UserAuthenticationRestService service = new UserAuthenticationRestService();
    }

    @Test
    void state_authenticated() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(42L);
        Mockito.when(session.getAttribute(AuthorityConfig.SESSION_ATTR_USER)).thenReturn(userEntity);

        Assertions.assertThat(userAuthentication.state(request).getCode()).isEqualTo(GenericResponseResult.CODE_OK);
        Assertions.assertThat(userAuthentication.state(request).getData().isAuth()).isTrue();
    }

    @Test
    void state_not_authenticated() {
        Mockito.when(session.getAttribute(AuthorityConfig.SESSION_ATTR_USER)).thenReturn(null);

        Assertions.assertThat(userAuthentication.state(request).getCode()).isEqualTo(GenericResponseResult.CODE_OK);
        Assertions.assertThat(userAuthentication.state(request).getData().isAuth()).isFalse();
    }

    @Test
    void login_rightCredentials() {
        Assertions.assertThat(userAuthentication.login(RIGHT_CREDENTIALS, request).getCode()).isEqualTo(GenericResponseResult.CODE_OK);
    }

    @Test
    void login_wrongCredentials() {
        Assertions.assertThat(userAuthentication.login(WRONG_CREDENTIALS, request).getCode()).isEqualTo(GenericResponseResult.CODE_UNAUTHORIZED);
    }

    @Test
    void login_alreadyLoggedInUser() {
        LoginCmd input = RIGHT_CREDENTIALS;
        Mockito.when(session.getAttribute(AuthorityConfig.SESSION_ATTR_USER)).thenReturn(new Object());

        GenericResponseResult<LoggedIn> response = userAuthentication.login(input, request);


        Assertions.assertThat(response.getCode()).isEqualTo(GenericResponseResult.CODE_NOT_ACCEPTABLE);
    }

    @Test
    void login_nonExistingUser() {
        Assertions.assertThat(userAuthentication.login(NON_EXITING_USER_CREDENTIALS, request).getCode()).isEqualTo(GenericResponseResult.CODE_NOT_FOUND);
    }

    @Test
    void login_nonActiveUser() {
        StatusEntity status = new StatusEntity();
        status.setEnabled( false );
        UserEntity userEntity = new UserEntity();
        userEntity.setStatus(status);
        userEntity.setId(142L);


        Mockito.when(users.findUser(NON_EXISTING_USER)).thenReturn(userEntity);


        Assertions.assertThat(userAuthentication.login(NON_EXITING_USER_CREDENTIALS, request).getCode()).isEqualTo(GenericResponseResult.CODE_NOT_FOUND);
    }

    @Test
    void login_invalidInput() {
        Assertions.assertThat(userAuthentication.login(INVALID_CREDENTIALS1, request).getCode()).isEqualTo(GenericResponseResult.CODE_BAD_REQUEST);
        Assertions.assertThat(userAuthentication.login(INVALID_CREDENTIALS2, request).getCode()).isEqualTo(GenericResponseResult.CODE_BAD_REQUEST);
    }

    @Test
    void login_emptyInput() {
        Assertions.assertThat(userAuthentication.login(EMPTY_CREDENTIALS, request).getCode()).isEqualTo(GenericResponseResult.CODE_BAD_REQUEST);
    }

    @Test
    void logout_loggedIn() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(142L);


        Mockito.when(session.getAttribute(AuthorityConfig.SESSION_ATTR_USER)).thenReturn(userEntity);


        Assertions.assertThat(userAuthentication.logout(request).getCode()).isEqualTo(GenericResponseResult.CODE_OK);
    }

    @Test
    void logout_notLoggedIn() {
        Mockito.when(session.getAttribute(AuthorityConfig.SESSION_ATTR_USER)).thenReturn(null);


        Assertions.assertThat(userAuthentication.logout(request).getCode()).isEqualTo(GenericResponseResult.CODE_NOT_ACCEPTABLE);
    }
}
