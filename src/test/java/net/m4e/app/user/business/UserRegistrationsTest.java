/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.business;

import net.m4e.app.notification.SendEmailEvent;
import net.m4e.app.resources.StatusEntity;
import net.m4e.common.Entities;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.mockito.*;

import javax.enterprise.event.Event;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author boto
 * Date of creation February 1, 2018
 */
class UserRegistrationsTest {

    final static Long USER_ID = 111L;
    final static String USER_EMAIL = "user@mailbox.com";
    final static String USER_NAME = "Bob Dillen";
    final static String USER_LOGIN = "Bobby";
    final static String BCC_EMAIL = "bcc@emailbox.com";
    final static String ACTIVATION_LINK = "https://activate-me.org";
    final static String RESET_LINK = "https://reset-me.org";

    @Mock
    Users users;

    @Mock
    Entities entities;

    @Mock
    Event<SendEmailEvent> sendMailEvent;

    @Mock
    UserResourcePurger userResourcePurger;

    UserRegistrations userRegistrations;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        userRegistrations = new UserRegistrations(entities, users, sendMailEvent, userResourcePurger);
    }

    @Test
    void defaultConstructor() {
        new UserRegistrations();
    }

    @Nested
    class Resources {

        final static int PENDING_ACCOUNT_ACTIVATION = 42;
        final static int PENDING_PASSWORD_RESETS = 24;

        @BeforeEach
        void setUp() {
            Mockito.when(entities.getCount(UserRegistrationEntity.class)).thenReturn(PENDING_ACCOUNT_ACTIVATION);
            Mockito.when(entities.getCount(UserPasswordResetEntity.class)).thenReturn(PENDING_PASSWORD_RESETS);
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
        void purgeExpiredRequests() {
            userRegistrations.purgeExpiredRequests();
        }
    }

    @NotNull
    protected UserEntity createEnabledUserEntity() {
        StatusEntity status = new StatusEntity();
        status.setEnabled(true);
        UserEntity user = new UserEntity();
        user.setId(USER_ID);
        user.setName(USER_NAME);
        user.setLogin(USER_LOGIN);
        user.setEmail(USER_EMAIL);
        user.setStatus(status);
        return user;
    }

    @NotNull
    protected UserEntity createDisabledUserEntity() {
        UserEntity user = createEnabledUserEntity();
        user.getStatus().setEnabled(false);
        return user;
    }

    @NotNull
    protected UserEntity createDeletedUserEntity() {
        StatusEntity status = new StatusEntity();
        status.setDateDeletion((new Date()).getTime());
        UserEntity deletedUser = new UserEntity();
        deletedUser.setStatus(status);
        return deletedUser;
    }

    protected void checkFireEvent(SendEmailEvent emailEvent, List<String> mustContainOneOfRecipients, List<String> mustContainAllInBody) {

        checkMailBody(emailEvent.getBody(), mustContainAllInBody);

        checkMailMailRecipients(mustContainOneOfRecipients, emailEvent.getRecipients().get(0));
    }

    protected void checkMailBody(String body, List<String> mustContain) {
        assertThat(body).contains(mustContain);
    }

    protected void checkMailMailRecipients(List<String> possibleRecipients, String mustContain) {
        assertThat(possibleRecipients).contains(mustContain);
    }
}
