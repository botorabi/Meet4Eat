/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.user;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.enterprise.event.Event;
import javax.persistence.EntityManager;
import net.m4e.app.notification.SendEmailEvent;
import net.m4e.common.Entities;
import net.m4e.system.core.Log;


/**
 * This class implements user registration related functionality.
 * 
 * @author boto
 * Date of creation Oct 2, 2017
 */
public class UserRegistrations {

    /**
     * Used for logging
     */
    private final static String TAG = "UserRegistrations";

    /**
     * Amount of hours for expiring a new registration.
     */
    private final int REGISTER_EXPIRATION_HOURS = 24;

    private final EntityManager entityManager;

    /**
     * Create an instance.
     * 
     * @param entityManager    Entity manager
     */
    public UserRegistrations(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Create a user registration entry and send an e-mail to a user containing an
     * activation token for completing the registration.
     * 
     * @param user          User, mail recipient
     * @param activationURL The base URL used for activating the user account
     * @param event         Mail event sent out to mail observer.
     */
    public void registerUserAccount(UserEntity user, String activationURL, Event event) {
        // create a registration entry
        UserRegistrationEntity reg = new UserRegistrationEntity();
        reg.setUser(user);
        String regtoken = reg.createActivationToken();
        Entities entities = new Entities(entityManager);
        entities.createEntity(reg);

        // send an email to user
        String body = "";
        body += "Hello Dear " + user.getName();
        body += "\n\n";
        body += "You have registered an account at Meet4Eat with following login name: " + user.getLogin();
        body += "\n\n";
        body += "Please click the following link in order to complete your registration for Meet4Eat by activating your account.";
        body += "\n\n";
        body += " " + activationURL + "?id=" + user.getId() + "&token=" + regtoken;
        body += "\n\n";
        body += "Note that the account registration and activation process will expire in " + REGISTER_EXPIRATION_HOURS + " hours.";
        body += "\n";
        body += "For the case that you need help with registration, then don't hesitate to contact us.";
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
    }

    /**
     * Try to activate a user after an user account registration. Check if the activation
     * token was expired. If activation was successful then the user gets enabled and
     * the registration entry (created during registration) will be deleted.
     * 
     * @param id            User ID
     * @param token         Activation token
     * @return              Activated User
     * @throws Exception    Throws an exception if the activation was not successful.
     */
    public UserEntity activateUserAccount(Long id, String token) throws Exception {
        Entities entities = new Entities(entityManager);
        List<UserRegistrationEntity> regs = entities.findEntityByField(UserRegistrationEntity.class, "activationToken", token);
        if (regs.size() > 1) {
            Log.error(TAG, "there are more than one registration entry with same token, count: " + regs.size());
            throw new Exception("Internal Registration Failure!");
        }
        if (regs.size() < 1) {
            throw new Exception("Cannot activate user account, invalid registration token.");
        }
        UserRegistrationEntity registration = regs.get(0);
        UserEntity user = registration.getUser();
        if (null == user) {
            throw new Exception("Internal error, user no longer exists.");            
        }
        // check the expiration
        Long duration = (new Date()).getTime() - user.getStatus().getDateCreation();
        duration /= (1000 * 60 * 60);
        if ( duration > REGISTER_EXPIRATION_HOURS) {
            throw new Exception("Cannot activate user account, activation code was expired.");            
        }
        // activate the user
        user.getStatus().setEnabled(true);
        // delete the registration entry
        entities.deleteEntity(registration);
        
        return user;
    }
}
