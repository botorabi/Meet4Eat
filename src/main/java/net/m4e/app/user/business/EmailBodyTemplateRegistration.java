/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.business;

import net.m4e.common.EmailBodyTemplate;

import java.util.*;

/**
 * Mail body template used for User Registration emails.
 *
 * @author boto
 * Date of creation February 2, 2018
 */
class EmailBodyTemplateRegistration extends EmailBodyTemplate {

    public static final String KEY_USER_NAME = "@USER_NAME@";
    public static final String KEY_LOGIN = "@LOGIN@";
    public static final String KEY_ACTIVATION_URL = "@ACTIVATION_URL@";
    public static final String KEY_REGISTRATION_TOKEN = "@REGISTRATION_TOKEN@";
    public static final String KEY_REGISTRATION_EXPIRATION = "@REGISTRATION_EXPIRATION@";

    public EmailBodyTemplateRegistration() {
        super();
    }

    protected List<String> registerPlaceHolders() {
        return Arrays.asList(
                KEY_USER_NAME,
                KEY_LOGIN,
                KEY_ACTIVATION_URL,
                KEY_REGISTRATION_TOKEN,
                KEY_REGISTRATION_EXPIRATION
        );
    }

    public String createTemplate() {
        String body = "";
        body += "Hello Dear " + KEY_USER_NAME + "!";
        body += "\n\n";
        body += "You have registered an account at Meet4Eat with following login name: " + KEY_LOGIN;
        body += "\n\n";
        body += "Please click the following link in order to complete your registration for Meet4Eat by activating your account.";
        body += "\n\n";
        body += " " + KEY_ACTIVATION_URL + "?token=" + KEY_REGISTRATION_TOKEN;
        body += "\n\n";
        body += "Note that the account registration and activation process will expire in " + KEY_REGISTRATION_EXPIRATION + " hours.";
        body += "\n";
        body += "Don't hesitate to contact us if you need any help with registration.";
        body += "\n\n";
        body += "Website: http://m4e.org\n";
        body += "Support: support@m4e.org";
        body += "\n\n";
        body += "Best Regards\n";
        body += "Meet4Eat Team\n";
        return body;
    }
}
