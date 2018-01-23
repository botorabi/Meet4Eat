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
import net.m4e.common.Entities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * This class implements user registration related functionality.
 * 
 * @author boto
 * Date of creation Oct 2, 2017
 */
@ApplicationScoped
public class UserRegistrations {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Amount of hours for expiring a new registration.
     */
    private final int REGISTER_EXPIRATION_HOURS = 24;

    /**
     * Amount of minutes for expiring a password reset request.
     */
    private final int PW_RESET_EXPIRATION_MINUTES = 30;

    /**
     * Entities instance injected during construction
     */
    private final Entities entities;

    /**
     * Users instance injected during construction
     */
    private final Users users;

    /**
     * Default constructor needed by the container.
     */
    protected UserRegistrations() {
        entities = null;
        users = null;
    }

    /**
     * Create an instance.
     * 
     * @param entities    Entities contains data access operations
     * @param users       Users instance
     */
    @Inject
    public UserRegistrations(Entities entities, Users users) {
        this.entities = entities;
        this.users = users;
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
     * This method may be called priodically (e.g. every 24 hours) by the maintenance module.
     * 
     * @return Return total count of purged resources.
     */
    public int purgeExpiredRequests() {
        int purgedregs = 0;
        int purgedpwresets = 0;

        Long now = (new Date()).getTime();

        // purge expired account registrations
        List<UserRegistrationEntity> regs = entities.findAll(UserRegistrationEntity.class);
        for (UserRegistrationEntity reg: regs) {
            Long duration = now - reg.getRequestDate();
            duration /= (1000 * 60 * 60);
            if (duration > REGISTER_EXPIRATION_HOURS) {
                purgedregs++;
                UserEntity user = reg.getUser();
                reg.setUser(null);
                entities.delete(reg);
                if (user != null) {
                    try {
                        users.markUserAsDeleted(user);
                    }
                    catch(Exception ex) {
                        LOGGER.warn("could not mark the user as deleted, id: " + user.getId());
                    }
                }
            }
        }
        // purge expired password reset requests
        List<UserPasswordResetEntity> resets = entities.findAll(UserPasswordResetEntity.class);
        for (UserPasswordResetEntity reset: resets) {
            Long duration = now - reset.getRequestDate();
            duration /= (1000 * 60);
            if (duration > PW_RESET_EXPIRATION_MINUTES) {
                purgedpwresets++;
                entities.delete(reset);
            }
        }
        LOGGER.info("Purged expired account registrations: " + purgedregs);
        LOGGER.info("Purged expired password reset requests: " + purgedpwresets);

        return purgedpwresets + purgedregs;
    }

    /**
     * Create a user registration entry and send an e-mail to a user containing an
     * activation token for completing the registration.
     * 
     * @param user          User, mail recipient
     * @param activationURL The base URL used for activating the user account
     * @param bccEmail      Optional email address used for BCC, let it null in order to ignore it.
     * @param event         Mail event sent out to mail observer.
     */
    public void registerUserAccount(UserEntity user, String activationURL, String bccEmail, Event event) {
        // create a registration entry
        UserRegistrationEntity reg = new UserRegistrationEntity();
        reg.setUser(user);
        reg.setRequestDate((new Date()).getTime());
        String regtoken = reg.createActivationToken();
        entities.create(reg);

        // send an email to user
        String body = "";
        body += "Hello Dear " + user.getName();
        body += "\n\n";
        body += "You have registered an account at Meet4Eat with following login name: " + user.getLogin();
        body += "\n\n";
        body += "Please click the following link in order to complete your registration for Meet4Eat by activating your account.";
        body += "\n\n";
        body += " " + activationURL + "?token=" + regtoken;
        body += "\n\n";
        body += "Note that the account registration and activation process will expire in " + REGISTER_EXPIRATION_HOURS + " hours.";
        body += "\n";
        body += "Don't hesitate to contact us if you need any help with registration.";
        body += "\n\n";
        body += "Website: http://m4e.org\n";
        body += "Support: support@m4e.org";
        body += "\n\n";
        body += "Best Regards\n";
        body += "Meet4Eat Team\n";
        SendEmailEvent sendmail = new SendEmailEvent();
        sendmail.setRecipients(Arrays.asList(user.getEmail()));
        sendmail.setSubject("Meet4Eat User Activation");
        sendmail.setHtmlBody(false);
        sendmail.setBody(body);
        event.fireAsync(sendmail);

        //! NOTE we do not user BCC on the user mail as the mail header may unveil the bccEmail
        if (bccEmail != null) {
            SendEmailEvent sendccmail = new SendEmailEvent();
            sendccmail.setRecipients(Arrays.asList(bccEmail));
            sendccmail.setSubject("Notification - Meet4Eat User Activation");
            sendccmail.setHtmlBody(false);
            body = "Copy of Email to " + user.getEmail() + "\n---\n\n" + body;
            sendccmail.setBody(body);
            event.fireAsync(sendccmail);
        }
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
        List<UserRegistrationEntity> regs = entities.findByField(UserRegistrationEntity.class, "activationToken", token);
        if (regs.size() > 1) {
            LOGGER.error("there are more than one registration entry with same token, count: " + regs.size());
            throw new Exception("Internal Registration Failure!");
        }
        if (regs.size() < 1) {
            throw new Exception("Invalid registration code.");
        }
        UserRegistrationEntity registration = regs.get(0);
        UserEntity user = registration.getUser();
        // delete the registration entry
        entities.delete(registration);
        
        if ((user == null) || (user.getStatus().getIsDeleted())) {
            throw new Exception("Internal error, user no longer exists.");            
        }
        // check the expiration
        Long duration = (new Date()).getTime() - registration.getRequestDate();
        duration /= (1000 * 60 * 60);
        if ( duration > REGISTER_EXPIRATION_HOURS) {
            // delete the entity if it is expired
            entities.delete(user);
            throw new Exception("Activation code was expired.");            
        }
        // activate the user
        user.getStatus().setEnabled(true);
        return user;
    }

    /**
     * Request a user password reset. This is used for the case that the user has
     * forgotten the password. The given user email address is validated and if successful
     * then an email with a reset link is sent to the user.
     * 
     * @param email         Email of the user who requests a password reset
     * @param resetURL      The base URL used for performing the password reset
     * @param bccEmail      Optional email address used for BCC, let it null in order to ignore it.
     * @param event         Mail event sent out to mail observer.
     * @throws Exception    Throws exception if no user with given email address was found.
     */
    public void requestPasswordReset(String email, String resetURL, String bccEmail, Event event) throws Exception {
        UserEntity user = users.findUserByEmail(email);
        if ((user == null) || (user.getStatus().getIsDeleted())) {
            throw new Exception("There is no registered user with given email address!");
        }

        UserPasswordResetEntity reset = null;
        // if there is already a reset password entry then re-use it
        List<UserPasswordResetEntity> regs = entities.findAll(UserPasswordResetEntity.class);
        for (UserPasswordResetEntity reg : regs) {
            if (user.equals(reg.getUser())) {
                reset = reg;
                break;
            }
        }
        // create a 'password reset' entry if no one exists
        if (reset == null) {
            reset = new UserPasswordResetEntity();
            reset.setUser(user);
            entities.create(reset);
        }
        reset.setRequestDate((new Date()).getTime());
        String resettoken = reset.createResetToken();

        // send an email to user
        String body = "";
        body += "Hello Dear " + user.getName();
        body += "\n\n";
        body += "You have requested for a password reset for your account at Meet4Eat with following login name: " + user.getLogin();
        body += "\n";
        body += "If you did not request for a password reset, please contact the Meet4Eat team.";
        body += "\n\n";
        body += "Please click the following link in order to reset your password.";
        body += "\n\n";
        body += " " + resetURL + "?token=" + resettoken;
        body += "\n\n";
        body += "Note that the password reset process will expire in " + PW_RESET_EXPIRATION_MINUTES + " minutes.";
        body += "\n";
        body += "Don't hesitate to contact us if you need any help";
        body += "\n\n";
        body += "Website: http://m4e.org\n";
        body += "Support: support@m4e.org";
        body += "\n\n";
        body += "Best Regards\n";
        body += "Meet4Eat Team\n";
        SendEmailEvent sendmail = new SendEmailEvent();
        sendmail.setRecipients(Arrays.asList(user.getEmail()));
        sendmail.setSubject("Meet4Eat Password Reset");
        sendmail.setHtmlBody(false);
        sendmail.setBody(body);
        event.fireAsync(sendmail);

        //! NOTE we do not user BCC on the user mail as the mail header may unveil the bccEmail
        if (bccEmail != null) {
            SendEmailEvent sendccmail = new SendEmailEvent();
            sendccmail.setRecipients(Arrays.asList(bccEmail));
            sendccmail.setSubject("Notification - Password Reset");
            sendccmail.setHtmlBody(false);
            body = "Copy of Email to " + user.getEmail() + "\n---\n\n" + body;
            sendccmail.setBody(body);
            event.fireAsync(sendccmail);
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
        List<UserPasswordResetEntity> resets = entities.findByField(UserPasswordResetEntity.class, "resetToken", resetToken);
        if (resets.size() > 1) {
            LOGGER.error("there are more than one password reset entry with same token, count: " + resets.size());
            throw new Exception("Internal Password Reset Failure!");
        }
        if (resets.size() < 1) {
            throw new Exception("Invalid password reset code.");
        }
        UserPasswordResetEntity reset = resets.get(0);
        UserEntity user = reset.getUser();
        // delete the reset entry
        entities.delete(reset);
        if ((user == null) || (user.getStatus().getIsDeleted())) {
            throw new Exception("Internal error, user no longer exists.");            
        }
        // check the expiration
        Long duration = (new Date()).getTime() - reset.getRequestDate();
        duration /= (1000 * 60);
        if ( duration > PW_RESET_EXPIRATION_MINUTES) {
            throw new Exception("Reset code was expired.");            
        }

        if (AuthorityConfig.getInstance().createPassword("").equals(newPassword)) {
            throw new Exception("Cannot reset user password, the password must not be empty.");            
        }
        // set the password
        user.setPassword(newPassword);
        return user;
    }
}
