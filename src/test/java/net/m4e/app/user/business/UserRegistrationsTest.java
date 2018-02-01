/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.business;

import net.m4e.app.notification.SendEmailEvent;
import net.m4e.common.Entities;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.mockito.stubbing.Answer;

import javax.enterprise.event.Event;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;

/**
 * @author boto
 * Date of creation February 1, 2018
 */
class UserRegistrationsTest {

    final static int PENDING_ACCOUNT_ACTIVATION = 42;
    final static int PENDING_PASSWORD_RESETS = 24;

    @Mock
    Users users;

    @Mock
    Entities entities;

    @Mock
    Event<SendEmailEvent> sendMailEvent;

    UserRegistrations userRegistrations;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(entities.getCount(UserRegistrationEntity.class)).thenReturn(PENDING_ACCOUNT_ACTIVATION);
        Mockito.when(entities.getCount(UserPasswordResetEntity.class)).thenReturn(PENDING_PASSWORD_RESETS);
        userRegistrations = new UserRegistrations(entities, users, sendMailEvent);
    }

    @Test
    void defaultConstructor() {
        new UserRegistrations();
    }

    @Test
    void pendingAccountActivation() {
        assertThat(userRegistrations.getCountPendingAccountActivations()).isEqualTo(PENDING_ACCOUNT_ACTIVATION);
    }

    @Test
    void pendingPasswordResets() {
        assertThat(userRegistrations.getCountPendingPasswordResets()).isEqualTo(PENDING_PASSWORD_RESETS);
    }

    @Test
    void accountRegistration() {
        final Long NEW_USER_ID = 111L;
        final String USER_NAME = "Bob Dillen";
        final String LOGIN = "Bobby";
        final String ACTIVATION_LINK = "https://activate-me.org";
        final String BCC_EMAIL = "bcc@emailbox.com";
        final String RECV_EMAIL = "recv@mailbox.com";

        Mockito.when(entities.create(any())).then((Answer<Boolean>) invocationOnMock -> {
            UserRegistrationEntity userRegistrationEntity = invocationOnMock.getArgumentAt(0, UserRegistrationEntity.class);
            userRegistrationEntity.setId(NEW_USER_ID);
            return true;
        });

        Mockito.when(sendMailEvent.fireAsync(any())).then(invocationOnMock -> {
            SendEmailEvent emailEvent = invocationOnMock.getArgumentAt(0, SendEmailEvent.class);

            assertThat(emailEvent.getBody()).contains(USER_NAME);
            assertThat(emailEvent.getBody()).contains(LOGIN);
            assertThat(emailEvent.getBody()).contains(ACTIVATION_LINK);

            List<String> possibleRecipients = Arrays.asList(RECV_EMAIL, BCC_EMAIL);
            assertThat(possibleRecipients).contains(emailEvent.getRecipients().get(0));

            return null;
        });


        UserEntity user = new UserEntity();
        user.setId(NEW_USER_ID);
        user.setName(USER_NAME);
        user.setLogin(LOGIN);
        user.setEmail(RECV_EMAIL);

        String activationURL = ACTIVATION_LINK;

        String bccEmail = BCC_EMAIL;


        userRegistrations.registerUserAccount(user, activationURL, bccEmail);
        userRegistrations.registerUserAccount(user, activationURL, null);


        assertThat(user.getId()).isEqualTo(NEW_USER_ID);
    }
}
