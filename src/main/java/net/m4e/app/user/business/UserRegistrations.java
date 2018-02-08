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
import net.m4e.common.*;
import org.slf4j.*;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.*;


/**
 * This class implements user registration related functionality.
 * 
 * @author boto
 * Date of creation Oct 2, 2017
 */
@ApplicationScoped
public class UserRegistrations {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Entities instance injected during construction
     */
    private final Entities entities;

    /**
     * Users instance injected during construction
     */
    private final Users users;

    /**
     * Event for sending e-mail to user.
     */
    private final Event<SendEmailEvent> sendMailEvent;

    /**
     * User to purge expired user registrations and password resets.
     */
    private final UserResourcePurger userResourcePurger;


    /**
     * Default constructor needed by the container.
     */
    protected UserRegistrations() {
        entities = null;
        users = null;
        sendMailEvent = null;
        userResourcePurger = null;
    }

    /**
     * Create an instance.
     * 
     * @param entities    Entities contains data access operations
     * @param users       Users instance
     */
    @Inject
    public UserRegistrations(Entities entities,
                             Users users,
                             Event<SendEmailEvent> sendMailEvent,
                             UserResourcePurger userResourcePurger) {
        this.entities = entities;
        this.users = users;
        this.sendMailEvent = sendMailEvent;
        this.userResourcePurger = userResourcePurger;
    }

    /**
     * Get the count of pending account registrations. The count includes
     * also expired activations.
     * 
     * @return Count of pending account registrations
     */
    public int getCountPendingAccountActivations() {
        return entities.getCount(UserRegistrationEntity.class);
    }

    /**
     * Get count of pending password reset requests. The count contains also
     * expired requests.
     * 
     * @return Count of pass word reset requests
     */
    public int getCountPendingPasswordResets() {
        return entities.getCount(UserPasswordResetEntity.class);
    }

    /**
     * Purge all expired account registrations and password reset requests.
     * This method may be called periodically (e.g. every 24 hours) by the maintenance module.
     * 
     * @return Return total count of purged resources.
     */
    public int purgeExpiredRequests() {

        int purgedRegistrations = userResourcePurger.purgeAccountRegistrations();

        int purgedPasswordResets = userResourcePurger.purgePasswordResets();

        LOGGER.info("Purged expired account registrations: " + purgedRegistrations);
        LOGGER.info("Purged expired password reset requests: " + purgedPasswordResets);

        return purgedRegistrations + purgedPasswordResets ;
    }

    /**
     * Create a user registration entry and send an e-mail to a user containing an
     * activation token for completing the registration.
     * 
     * @param user          User, mail recipient
     * @param activationURL The base URL used for activating the user account
     * @param bccEmail      Optional email address used for BCC, let it null in order to ignore it.
     */
    public void registerUserAccount(UserEntity user, String activationURL, String bccEmail) {
        UserRegistrationEntity registrationEntity = createUserRegistrationEntity(user);

        String registrationToken = registrationEntity.createActivationToken();

        String body = createUserRegistrationMailBody(user, activationURL, registrationToken);

        sendNotificationMails(user.getEmail(), bccEmail, "Meet4Eat User Activation", body);
    }

    private UserRegistrationEntity createUserRegistrationEntity(UserEntity user) {
        UserRegistrationEntity entity = new UserRegistrationEntity();
        entity.setUser(user);
        entity.setRequestDate((new Date()).getTime());
        entities.create(entity);

        return entity;
    }

    private String createUserRegistrationMailBody(UserEntity user, String activationURL, String registrationToken) {
        EmailBodyTemplateRegistration mailBodyTemplate = new EmailBodyTemplateRegistration();

        mailBodyTemplate.setPlaceHolderValue(EmailBodyTemplateRegistration.KEY_USER_NAME, user.getName());
        mailBodyTemplate.setPlaceHolderValue(EmailBodyTemplateRegistration.KEY_LOGIN, user.getLogin());
        mailBodyTemplate.setPlaceHolderValue(EmailBodyTemplateRegistration.KEY_ACTIVATION_URL, activationURL);
        mailBodyTemplate.setPlaceHolderValue(EmailBodyTemplateRegistration.KEY_REGISTRATION_TOKEN, registrationToken);
        mailBodyTemplate.setPlaceHolderValue(EmailBodyTemplateRegistration.KEY_REGISTRATION_EXPIRATION, "" + UserResourcePurger.REGISTER_EXPIRATION_HOURS);

        String template = mailBodyTemplate.createTemplate();
        return EmailBodyCreator.create(template, mailBodyTemplate.getPlaceHolders());
    }

    /**
     * Try to activate a user after an user account registration. Check if the activation
     * token was expired. If activation was successful then the user gets enabled and
     * the registration entry (created during registration) will be deleted.
     * 
     * @param token         Activation token
     * @return              Activated User
     * @throws Exception    Throws an exception if the activation was not successful.
     */
    public UserEntity activateUserAccount(String token) throws Exception {
        UserRegistrationEntity registrationEntity = getUserRegistrationEntity(token);

        entities.delete(registrationEntity);

        checkRegistrationExpiration(registrationEntity);

        UserEntity user = registrationEntity.getUser();
        user.getStatus().setEnabled(true);
        return user;
    }

    protected void checkRegistrationExpiration(UserRegistrationEntity registrationEntity) throws Exception {
        UserEntity user = registrationEntity.getUser();
        if ((user == null) || (user.getStatus().getIsDeleted())) {
            throw new Exception("Internal error, user no longer exists.");
        }
        Long elapsedTimeSinceRegistration = (new Date()).getTime() - registrationEntity.getRequestDate();
        elapsedTimeSinceRegistration /= (1000 * 60 * 60);
        if (elapsedTimeSinceRegistration > UserResourcePurger.REGISTER_EXPIRATION_HOURS) {
            entities.delete(user);
            throw new Exception("Activation token was expired.");
        }
    }

    protected UserRegistrationEntity getUserRegistrationEntity(String token) throws Exception {
        List<UserRegistrationEntity> regs = entities.findByField(UserRegistrationEntity.class, "activationToken", token);
        if (regs.size() > 1) {
            LOGGER.error("there are more than one registration entry with same token, count: " + regs.size());
            throw new Exception("Internal registration failure");
        }
        if (regs.size() < 1) {
            throw new Exception("Invalid registration token");
        }
        return regs.get(0);
    }

    /**
     * Request a user password reset. This is used for the case that the user has
     * forgotten the password. The given user email address is validated and if successful
     * then an email with a reset link is sent to the user.
     * 
     * @param email         Email of the user who requests a password reset
     * @param resetPasswordURL      The base URL used for performing the password reset
     * @param bccEmail      Optional email address used for BCC, let it null in order to ignore it.
     * @throws Exception    Throws exception if no user with given email address was found.
     */
    public void requestPasswordReset(String email, String resetPasswordURL, String bccEmail) throws Exception {
        UserEntity user = users.findUserByEmail(email);
        if ((user == null) || (user.getStatus().getIsDeleted())) {
            throw new Exception("There is no registered user with given email address!");
        }

        UserPasswordResetEntity resetEntity = getOrCreatePasswordResetEntity(user);

        String resetPasswordToken = resetEntity.createResetToken();

        String body = createResetPasswordMailBody(user, resetPasswordURL, resetPasswordToken);

        sendNotificationMails(user.getEmail(), bccEmail, "Password Reset", body);
    }

    private String createResetPasswordMailBody(UserEntity user, String resetPasswordURL, String resetToken) {
        EmailBodyTemplatePasswordReset mailBodyTemplate = new EmailBodyTemplatePasswordReset();

        mailBodyTemplate.setPlaceHolderValue(EmailBodyTemplatePasswordReset.KEY_USER_NAME, user.getName());
        mailBodyTemplate.setPlaceHolderValue(EmailBodyTemplatePasswordReset.KEY_LOGIN, user.getLogin());
        mailBodyTemplate.setPlaceHolderValue(EmailBodyTemplatePasswordReset.KEY_RESET_PW_URL, resetPasswordURL);
        mailBodyTemplate.setPlaceHolderValue(EmailBodyTemplatePasswordReset.KEY_RESET_PW_TOKEN, resetToken);
        mailBodyTemplate.setPlaceHolderValue(EmailBodyTemplatePasswordReset.KEY_RESET_EXPIRATION, "" + UserResourcePurger.PASSWORD_RESET_EXPIRATION_MINUTES);

        String template = mailBodyTemplate.createTemplate();
        return EmailBodyCreator.create(template, mailBodyTemplate.getPlaceHolders());
    }

    private UserPasswordResetEntity getOrCreatePasswordResetEntity(UserEntity user) {
        List<UserPasswordResetEntity> allEntities = this.entities.findAll(UserPasswordResetEntity.class);
        for (UserPasswordResetEntity entity : allEntities) {
            if (user.equals(entity.getUser())) {
                return entity;
            }
        }

        UserPasswordResetEntity entity = new UserPasswordResetEntity();
        entity.setUser(user);
        entities.create(entity);
        entity.setRequestDate((new Date()).getTime());
        return entity;
    }

    private void sendNotificationMails(String recipient, String bccRecipient, String subject, String body) {
        SendEmailEvent mailEvent = EmailEventCreator.plainTextMail(Arrays.asList(recipient), subject, body);
        sendMailEvent.fireAsync(mailEvent);

        //! NOTE we do not use BCC on the user mail as the mail header may unveil the bccRecipient
        if (bccRecipient != null) {
            SendEmailEvent sendBccMailEvent = EmailEventCreator.plainTextMail(Arrays.asList(bccRecipient), "Notification - " + subject, "");
            body = "Copy of Email to " + recipient + "\n---\n\n" + body;
            sendBccMailEvent.setBody(body);
            sendMailEvent.fireAsync(sendBccMailEvent);
        }
    }

    /**
     * Try to reset a user password. Check if the reset token was expired.
     * 
     * @param resetToken    Password reset token
     * @param newPassword   The new password for the user
     * @return              Return the user whos password was reset.
     * @throws Exception    Throws an exception if the password reset was not successful.
     */
    public UserEntity processPasswordReset(String resetToken, String newPassword) throws Exception {
        UserPasswordResetEntity passwordResetEntity = getUserPasswordResetEntity(resetToken);

        entities.delete(passwordResetEntity);

        checkPasswordResetExpiration(passwordResetEntity);

        if (AuthorityConfig.getInstance().createPassword("").equals(newPassword)) {
            throw new Exception("Cannot reset user password, the password must not be empty.");            
        }

        UserEntity user = passwordResetEntity.getUser();
        user.setPassword(newPassword);
        return user;
    }

    protected void checkPasswordResetExpiration(UserPasswordResetEntity reset) throws Exception {
        UserEntity user = reset.getUser();
        if ((user == null) || (user.getStatus().getIsDeleted())) {
            throw new Exception("Internal error, user no longer exists.");
        }

        Long elapsedTimeSinceResetRequest = (new Date()).getTime() - reset.getRequestDate();
        elapsedTimeSinceResetRequest /= (1000 * 60);
        if ( elapsedTimeSinceResetRequest > UserResourcePurger.PASSWORD_RESET_EXPIRATION_MINUTES) {
            throw new Exception("Reset token was expired.");
        }
    }

    protected UserPasswordResetEntity getUserPasswordResetEntity(String resetToken) throws Exception {
        List<UserPasswordResetEntity> resets = entities.findByField(UserPasswordResetEntity.class, "resetToken", resetToken);
        if (resets.size() > 1) {
            LOGGER.error("there are more than one password reset entry with same token, count: " + resets.size());
            throw new Exception("Internal password reset failure");
        }
        if (resets.size() < 1) {
            throw new Exception("Invalid password reset token");
        }
        return resets.get(0);
    }
}
