/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.business;

import net.m4e.app.auth.AuthorityConfig;
import net.m4e.app.notification.SendEmailEvent;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Matchers.*;

/**
 * @author boto
 * Date of creation February 5, 2018
 */
class UserRegistrationsPasswordResetTest extends UserRegistrationsTest {

    @Nested
    class RequestPasswordReset {

        @Test
        void noExistingUser() {
            Mockito.when(users.findUserByEmail(any())).thenReturn(null);
            try {
                userRegistrations.requestPasswordReset(USER_EMAIL, null, null);
                fail("Non existing user was not properly handled");
            } catch (Exception ex) {
            }
        }

        @Test
        void deletedUser() {
            UserEntity deletedUser = createDeletedUserEntity();

            Mockito.when(users.findUserByEmail(any())).thenReturn(deletedUser);
            try {
                userRegistrations.requestPasswordReset(USER_EMAIL, null, null);
                fail("Deleted user was not properly handled");
            } catch (Exception ex) {
            }
        }

        @Test
        void existingRequest() {
            UserPasswordResetEntity resetEntity = new UserPasswordResetEntity();
            UserEntity user = createEnabledUserEntity();
            resetEntity.setUser(user);

            Mockito.when(users.findUserByEmail(any())).thenReturn(user);
            Mockito.when(entities.findAll(UserPasswordResetEntity.class)).thenReturn(Arrays.asList(resetEntity));

            requestPasswordReset();
        }

        @Test
        void noMatchingRequest() {
            UserPasswordResetEntity resetEntity = new UserPasswordResetEntity();
            resetEntity.setUser(new UserEntity());

            Mockito.when(users.findUserByEmail(any())).thenReturn(createEnabledUserEntity());
            Mockito.when(entities.findAll(UserPasswordResetEntity.class)).thenReturn(Arrays.asList(resetEntity));

            requestPasswordReset();
        }

        @Test
        void nonExistingRequest() {
            UserEntity user = createEnabledUserEntity();

            Mockito.when(users.findUserByEmail(any())).thenReturn(user);
            Mockito.when(entities.findAll(UserPasswordResetEntity.class)).thenReturn(Arrays.asList());

            requestPasswordReset();
        }

        private void requestPasswordReset() {
            Mockito.when(sendMailEvent.fireAsync(any())).then(invocationOnMock -> {
                SendEmailEvent emailEvent = invocationOnMock.getArgumentAt(0, SendEmailEvent.class);

                checkFireEvent(emailEvent, Arrays.asList(USER_EMAIL, BCC_EMAIL), Arrays.asList(USER_NAME, USER_LOGIN, RESET_LINK));

                return null;
            });

            try {
                userRegistrations.requestPasswordReset(USER_EMAIL, RESET_LINK, BCC_EMAIL);
                userRegistrations.requestPasswordReset(USER_EMAIL, RESET_LINK, null);
            } catch (Exception ex) {
                fail("User password reset failed: " + ex.getMessage());
            }
        }
    }

    @Nested
    class ProcessPasswordReset {

        final static String RESET_TOKEN = "MyResetToken";
        final static String RESET_TOKEN_INVALID = "PasswordResetTokenInvalid";
        final static String RESET_TOKEN_EXPIRED = "MyExpiredResetToken";
        final static String RESET_TOKEN_DELETED_USER = "DeletedUserResetToken";
        final static String RESET_TOKEN_NOT_EXISTING_USER = "NotExistingUserResetToken";
        final static String RESET_TOKEN_INTERNAL_ERROR = "ResetTokenInternalError";

        final Instant dateNotExpired = Instant.now();
        final Instant dateExpired = Instant.now().minus(UserResourcePurger.REGISTER_EXPIRATION_HOURS + 1, ChronoUnit.HOURS);

        @BeforeEach
        void setUp() {

            mockPasswordResetEntry(createEnabledUserEntity(), RESET_TOKEN, dateNotExpired);
            mockPasswordResetEntry(createEnabledUserEntity(), RESET_TOKEN_EXPIRED, dateExpired);
            mockPasswordResetEntry(createDeletedUserEntity(), RESET_TOKEN_DELETED_USER, dateNotExpired);
            mockPasswordResetEntry(null, RESET_TOKEN_NOT_EXISTING_USER, dateNotExpired);
            mockInvalidResetToken(RESET_TOKEN_INVALID);
            mockResetPasswordInternalError();
        }

        private void mockPasswordResetEntry(UserEntity user, String resetToken, Instant requestDate) {
            UserPasswordResetEntity passwordResetEntity = new UserPasswordResetEntity();
            passwordResetEntity.setUser(user);
            passwordResetEntity.setResetToken(resetToken);
            passwordResetEntity.setRequestDate(requestDate.toEpochMilli());
            Mockito.when(entities.findByField(any(), eq("resetToken"), eq(resetToken)))
                    .thenReturn(Collections.singletonList(passwordResetEntity));
        }

        private void mockInvalidResetToken(String resetToken) {
            Mockito.when(entities.findByField(any(), eq("resetToken"), eq(resetToken)))
                    .thenReturn(Collections.emptyList());

        }

        private void mockResetPasswordInternalError() {
            UserPasswordResetEntity internalErrorRegistration1 = new UserPasswordResetEntity();
            UserPasswordResetEntity internalErrorRegistration2 = new UserPasswordResetEntity();
            Mockito.when(entities.findByField(any(), eq("resetToken"), eq(RESET_TOKEN_INTERNAL_ERROR)))
                    .thenReturn(Arrays.asList(internalErrorRegistration1, internalErrorRegistration2));
        }

        @Test
        void successfulPasswordReset() {
            try {
                final String NEW_PASSWORD = "NewPassword";
                userRegistrations.processPasswordReset(RESET_TOKEN, NEW_PASSWORD);

                checkIfPasswordWasReset(NEW_PASSWORD);

            } catch (Exception ex) {
                fail("User password reset failed: " + ex.getMessage());
            }
        }

        private void checkIfPasswordWasReset(String newPassword) throws Exception {
            UserPasswordResetEntity resetEntity = userRegistrations.getUserPasswordResetEntity(RESET_TOKEN);
            UserEntity user = resetEntity.getUser();

            String passwordHash = AuthorityConfig.getInstance().createPassword(user.getPassword());
            String newPasswordHash = AuthorityConfig.getInstance().createPassword(newPassword);

            assertThat(passwordHash).isEqualTo(newPasswordHash);
        }

        @Test
        void expiredRequest() {
            try {
                userRegistrations.processPasswordReset(RESET_TOKEN_EXPIRED, null);
                fail("Password reset did not detect expiration");
            } catch (Exception ex) {
            }
        }

        @Test
        void deletedUserAccount() {
            try {
                userRegistrations.processPasswordReset(RESET_TOKEN_DELETED_USER, null);
                fail("Password reset did not detect deleted user");
            } catch (Exception ex) {
            }
        }

        @Test
        void notExistingUserAccount() {
            try {
                userRegistrations.processPasswordReset(RESET_TOKEN_NOT_EXISTING_USER, null);
                fail("Password reset did not detect non-existing user");
            } catch (Exception ex) {
                assertThat(ex.getMessage()).contains("Internal error, user no longer exists");
            }
        }

        @Test
        void invalidResetToken() {
            try {
                userRegistrations.processPasswordReset(RESET_TOKEN_INVALID, null);
                fail("Password reset did not detect invalid reset token");
            } catch (Exception ex) {
                assertThat(ex.getMessage()).contains("Invalid password reset token");
            }
        }

        @Test
        void detectInternalError() {
            try {
                userRegistrations.processPasswordReset(RESET_TOKEN_INTERNAL_ERROR, null);
                fail("Password reset did not detect internal error: there more than one reset entries with the same token");
            } catch (Exception ex) {
                assertThat(ex.getMessage()).contains("Internal password reset failure");
            }
        }
    }
}
