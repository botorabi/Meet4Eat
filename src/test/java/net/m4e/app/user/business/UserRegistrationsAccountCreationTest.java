/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.business;

import net.m4e.app.notification.SendEmailEvent;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Matchers.*;

/**
 * @author boto
 * Date of creation February 1, 2018
 */
class UserRegistrationsAccountCreationTest extends UserRegistrationsTest {

    @Nested
    class AccountCreation {

        @Test
        void registerUserAccountValidateInputs() {
            String noBccEmail = null;

            String invalidActivationURL = null;
            String validActivationURL = ACTIVATION_LINK;

            UserEntity invalidUserEntity = createInvalidRegistrationUser();
            UserEntity validUserEntity = createValidRegistrationUser();

            assertThat(isUserRegistrationExceptionThrown(invalidUserEntity, validActivationURL, noBccEmail)).isTrue();
            assertThat(isUserRegistrationExceptionThrown(invalidUserEntity, invalidActivationURL, noBccEmail)).isTrue();
            assertThat(isUserRegistrationExceptionThrown(validUserEntity, invalidActivationURL, noBccEmail)).isTrue();

            assertThat(isUserRegistrationExceptionThrown(validUserEntity, validActivationURL, noBccEmail)).isFalse();
        }

        @Test
        void registerUserAccountWithBccEmail() {
            UserEntity validUserEntity = createValidRegistrationUser();

            assertThat(isUserRegistrationExceptionThrown(validUserEntity, ACTIVATION_LINK, BCC_EMAIL)).isFalse();
        }

        @Test
        void registerUserAccount() {

            UserEntity user = createEnabledUserEntity();

            Mockito.when(entities.create(any())).then((Answer<Boolean>) invocationOnMock -> {
                UserRegistrationEntity userRegistrationEntity = invocationOnMock.getArgumentAt(0, UserRegistrationEntity.class);
                userRegistrationEntity.setId(USER_ID);
                return true;
            });

            Mockito.when(sendMailEvent.fireAsync(any())).then(invocationOnMock -> {
                SendEmailEvent emailEvent = invocationOnMock.getArgumentAt(0, SendEmailEvent.class);

                checkFireEvent(emailEvent, Arrays.asList(USER_EMAIL, BCC_EMAIL), Arrays.asList(USER_NAME, USER_LOGIN, ACTIVATION_LINK));

                return null;
            });

            userRegistrations.registerUserAccount(user, ACTIVATION_LINK, BCC_EMAIL);
            userRegistrations.registerUserAccount(user, ACTIVATION_LINK, null);

            assertThat(user.getId()).isEqualTo(USER_ID);
        }

        private UserEntity createValidRegistrationUser() {
            UserEntity entity = createDisabledUserEntity();
            return entity;
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

    @Nested
    class AccountActivation {

        final static String ACTIVATION_TOKEN = "MyActivationToken";
        final static String ACTIVATION_TOKEN_EXPIRED = "MyExpiredActivationToken";
        final static String ACTIVATION_TOKEN_DELETED_USER = "DeletedUser";
        final static String ACTIVATION_TOKEN_NOT_EXISTING_USER = "NotExistingUser";
        final static String ACTIVATION_TOKEN_INTERNAL_ERROR = "UserActivationInternalError";
        final static String ACTIVATION_TOKEN_INVALID = "UserActivationTokenInvalid";

        final Instant dateNotExpired = Instant.now();
        final Instant dateExpired = Instant.now().minus(UserResourcePurger.REGISTER_EXPIRATION_HOURS + 1, ChronoUnit.HOURS);

        @BeforeEach
        void setUp() {

            mockRegistrationEntry(createDisabledUserEntity(), ACTIVATION_TOKEN, dateNotExpired);

            mockRegistrationEntry(createDisabledUserEntity(), ACTIVATION_TOKEN_EXPIRED, dateExpired);

            mockRegistrationEntry(createDeletedUserEntity(), ACTIVATION_TOKEN_DELETED_USER, dateNotExpired);

            mockRegistrationEntry(null, ACTIVATION_TOKEN_NOT_EXISTING_USER, dateNotExpired);

            mockInvalidRegistrationToken(ACTIVATION_TOKEN_INVALID);

            mockRegistrationInternalError();
        }

        private void mockRegistrationEntry(UserEntity user, String activationToken, Instant requestDate) {
            UserRegistrationEntity registrationEntity = new UserRegistrationEntity();
            registrationEntity.setUser(user);
            registrationEntity.setActivationToken(activationToken);
            registrationEntity.setRequestDate(requestDate.toEpochMilli());
            Mockito.when(entities.findByField(any(), eq("activationToken"), eq(activationToken)))
                    .thenReturn(Collections.singletonList(registrationEntity));

        }

        private void mockInvalidRegistrationToken(String activationToken) {
            Mockito.when(entities.findByField(any(), eq("activationToken"), eq(activationToken)))
                    .thenReturn(Collections.emptyList());

        }

        private void mockRegistrationInternalError() {
            UserRegistrationEntity internalErrorRegistration1 = new UserRegistrationEntity();
            UserRegistrationEntity internalErrorRegistration2 = new UserRegistrationEntity();
            Mockito.when(entities.findByField(any(), eq("activationToken"), eq(ACTIVATION_TOKEN_INTERNAL_ERROR)))
                    .thenReturn(Arrays.asList(internalErrorRegistration1, internalErrorRegistration2));
        }

        @Test
        void successfullyActivate() {
            try {
                userRegistrations.activateUserAccount(ACTIVATION_TOKEN);
                checkIfUserWasActivated();
            } catch (Exception ex) {
                fail("Account activation failed: " + ex.getMessage());
            }
        }

        private void checkIfUserWasActivated() throws Exception {
            UserRegistrationEntity registration = userRegistrations.getUserRegistrationEntity(ACTIVATION_TOKEN);
            UserEntity user = registration.getUser();

            assertThat(user.getStatus().isEnabled()).isTrue();
        }

        @Test
        void expiredUserAccount() {
            try {
                userRegistrations.activateUserAccount(ACTIVATION_TOKEN_EXPIRED);
                fail("Account activation did not detect expiration");
            } catch (Exception ex) {
            }
        }

        @Test
        void deletedUserAccount() {
            try {
                userRegistrations.activateUserAccount(ACTIVATION_TOKEN_DELETED_USER);
                fail("Account activation did not detect deleted user");
            } catch (Exception ex) {
            }
        }

        @Test
        void notExistingUserAccount() {
            try {
                userRegistrations.activateUserAccount(ACTIVATION_TOKEN_NOT_EXISTING_USER);
                fail("Account activation did not detect non-existing user");
            } catch (Exception ex) {
                assertThat(ex.getMessage()).contains("Internal error, user no longer exists");
            }
        }

        @Test
        void invalidRegistrationToken() {
            try {
                userRegistrations.activateUserAccount(ACTIVATION_TOKEN_INVALID);
                fail("Account activation did not detect invalid registration token");
            } catch (Exception ex) {
                assertThat(ex.getMessage()).contains("Invalid registration token");
            }
        }

        @Test
        void detectInternalError() {
            try {
                userRegistrations.activateUserAccount(ACTIVATION_TOKEN_INTERNAL_ERROR);
                fail("Account activation did not detect internal error: there more than one registration entries with the same token");
            } catch (Exception ex) {
                assertThat(ex.getMessage()).contains("Internal registration failure");
            }
        }
    }
}
