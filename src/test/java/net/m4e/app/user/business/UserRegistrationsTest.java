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
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.mockito.stubbing.Answer;

import javax.enterprise.event.Event;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
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

    @Mock
    UserResourcePurger userResourcePurger;

    UserRegistrations userRegistrations;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(entities.getCount(UserRegistrationEntity.class)).thenReturn(PENDING_ACCOUNT_ACTIVATION);
        Mockito.when(entities.getCount(UserPasswordResetEntity.class)).thenReturn(PENDING_PASSWORD_RESETS);
        userRegistrations = new UserRegistrations(entities, users, sendMailEvent, userResourcePurger);
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

            checkFireEvent(emailEvent, Arrays.asList(RECV_EMAIL, BCC_EMAIL), Arrays.asList(USER_NAME, LOGIN, ACTIVATION_LINK));

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

    @Nested
    class PasswordReset {

        final Long USER_ID = 111L;
        final String USER_EMAIL = "user@mailbox.com";
        final String USER_NAME = "Bob Dillen";
        final String LOGIN = "Bobby";
        final String RESET_LINK = "https://reset-me.org";
        final String BCC_EMAIL = "bcc@emailbox.com";

        @Test
        void requestPasswordResetNoExistingUser() {
            final String USER_EMAIL = "user@mailbox.com";

            Mockito.when(users.findUserByEmail(any())).thenReturn(null);
            try {
                userRegistrations.requestPasswordReset(USER_EMAIL, null, null);
                fail("Non existing user was not properly handled");
            } catch (Exception ex) {
            }
        }


        @Test
        void requestPasswordResetDeletedUser() {
            final String USER_EMAIL = "user@mailbox.com";
            StatusEntity status = new StatusEntity();
            status.setDateDeletion((new Date()).getTime());
            UserEntity deletedUser = new UserEntity();
            deletedUser.setStatus(status);

            Mockito.when(users.findUserByEmail(any())).thenReturn(deletedUser);
            try {
                userRegistrations.requestPasswordReset(USER_EMAIL, null, null);
                fail("Deleted user was not properly handled");
            } catch (Exception ex) {
            }
        }

        @Test
        void requestPasswordResetExistingRequest() {

            UserPasswordResetEntity resetEntity = new UserPasswordResetEntity();
            UserEntity user = createUserEntity();
            resetEntity.setUser(user);

            Mockito.when(users.findUserByEmail(any())).thenReturn(user);
            Mockito.when(entities.findAll(any())).thenReturn(Arrays.asList(resetEntity));

            requestPasswordReset(resetEntity);
        }

        @Test
        void requestPasswordResetNoMatchingRequest() {

            UserPasswordResetEntity resetEntity = new UserPasswordResetEntity();
            resetEntity.setUser(new UserEntity());

            Mockito.when(users.findUserByEmail(any())).thenReturn(createUserEntity());
            Mockito.when(entities.findAll(any())).thenReturn(Arrays.asList(resetEntity));

            requestPasswordReset(resetEntity);
        }

        @Test
        void requestPasswordResetNonExistingRequest() {

            UserEntity user = createUserEntity();

            Mockito.when(users.findUserByEmail(any())).thenReturn(user);
            Mockito.when(entities.findAll(any())).thenReturn(Arrays.asList());

            requestPasswordReset(null);
        }

        private void requestPasswordReset(UserPasswordResetEntity resetEntity) {
            Mockito.when(sendMailEvent.fireAsync(any())).then(invocationOnMock -> {
                SendEmailEvent emailEvent = invocationOnMock.getArgumentAt(0, SendEmailEvent.class);

                checkFireEvent(emailEvent, Arrays.asList(USER_EMAIL, BCC_EMAIL), Arrays.asList(USER_NAME, LOGIN, RESET_LINK));

                return null;
            });

            try {
                userRegistrations.requestPasswordReset(USER_EMAIL, RESET_LINK, BCC_EMAIL);
                userRegistrations.requestPasswordReset(USER_EMAIL, RESET_LINK, null);
            } catch (Exception ex) {
                fail("User password reset failed");
            }
        }

        private UserEntity createUserEntity() {
            StatusEntity status = new StatusEntity();
            status.setEnabled(true);
            UserEntity user = new UserEntity();
            user.setId(USER_ID);
            user.setName(USER_NAME);
            user.setLogin(LOGIN);
            user.setEmail(USER_EMAIL);
            user.setStatus(status);
            return user;
        }
    }

    private void checkFireEvent(SendEmailEvent emailEvent, List<String> mustContainOneOfRecipients, List<String> mustContainAllInBody) {

        checkMailBody(emailEvent.getBody(), mustContainAllInBody);

        checkMailMailRecipients(mustContainOneOfRecipients, emailEvent.getRecipients().get(0));
    }

    private void checkMailBody(String body, List<String> mustContain) {
        assertThat(body).contains(mustContain);
    }

    private void checkMailMailRecipients(List<String> possibleRecipients, String mustContain) {
        assertThat(possibleRecipients).contains(mustContain);
    }

    @Test
    void purgeExpiredRequests() {
        userRegistrations.purgeExpiredRequests();
    }

    @Test
    void registerUserAccountInputs() {
        String noBccEmail = null;

        String invalidActivationURL = null;
        String validActivationURL = "https://activate.com";

        UserEntity invalidUserEntity = createInvalidRegistrationUser();
        UserEntity validUserEntity = createValidRegistrationUser();

        assertThat(isUserRegistrationExceptionThrown(invalidUserEntity, validActivationURL, noBccEmail)).isTrue();
        assertThat(isUserRegistrationExceptionThrown(invalidUserEntity, invalidActivationURL, noBccEmail)).isTrue();
        assertThat(isUserRegistrationExceptionThrown(validUserEntity, invalidActivationURL, noBccEmail)).isTrue();

        assertThat(isUserRegistrationExceptionThrown(validUserEntity, validActivationURL, noBccEmail)).isFalse();
    }

    @Test
    void registerUserAccountWithBccEmail() {
        String bccEmail = "bcc@mailbox.com";
        String validActivationURL = "https://activate.com";
        UserEntity validUserEntity = createValidRegistrationUser();

        assertThat(isUserRegistrationExceptionThrown(validUserEntity, validActivationURL, bccEmail)).isFalse();
    }

    private UserEntity createValidRegistrationUser() {
        UserEntity validUserEntity = new UserEntity();
        validUserEntity.setName("My Name");
        validUserEntity.setEmail("me@mailbox.com");
        validUserEntity.setLogin("MyLogin");
        return validUserEntity;
    }

    private UserEntity createInvalidRegistrationUser() {
        return new UserEntity();
    }

    private boolean isUserRegistrationExceptionThrown(UserEntity user, String activationURL, String bccEmail) {
        try {
            userRegistrations.registerUserAccount(user, activationURL, bccEmail);
            return false;
        } catch (IllegalArgumentException ex) {
        }
        return true;
    }

}
