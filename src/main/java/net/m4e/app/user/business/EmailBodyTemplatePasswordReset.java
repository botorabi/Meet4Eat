/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.business;

import net.m4e.common.EmailBodyTemplate;

import java.util.*;

/**
 * Mail body template used for Password Reset emails.
 *
 * @author boto
 * Date of creation February 2, 2018
 */
class EmailBodyTemplatePasswordReset extends EmailBodyTemplate {

    public final static String KEY_USER_NAME = "@USER_NAME@";
    public final static String KEY_LOGIN = "@LOGIN@";
    public final static String KEY_RESET_PASSWORD_URL = "@RESET_PASSWORD_URL@";
    public final static String KEY_RESET_PASSWORD_TOKEN = "@RESET_PASSWORD_TOKEN@";
    public final static String KEY_RESET_EXPIRATION = "@RESET_EXPIRATION@";

    public EmailBodyTemplatePasswordReset() {
        super();
    }

    protected List<String> registerPlaceHolders() {
        return Arrays.asList(
                KEY_USER_NAME,
                KEY_LOGIN,
                KEY_RESET_PASSWORD_URL,
                KEY_RESET_PASSWORD_TOKEN,
                KEY_RESET_EXPIRATION
        );
    }

    public String createTemplate() {
        String body = "";
        body += "Hello Dear " + KEY_USER_NAME + "!";
        body += "\n\n";
        body += "You have requested for a password reset for your account at Meet4Eat with following login name: " + KEY_LOGIN;
        body += "\n";
        body += "If you did not request for a password reset, please contact the Meet4Eat team.";
        body += "\n\n";
        body += "Please click the following link in order to reset your password.";
        body += "\n\n";
        body += " " + KEY_RESET_PASSWORD_URL + "?token=" + KEY_RESET_PASSWORD_TOKEN;
        body += "\n\n";
        body += "Note that the password reset process will expire in " + KEY_RESET_EXPIRATION + " minutes.";
        body += "\n";
        body += "Don't hesitate to contact us if you need any help";
        body += "\n\n";
        body += "Website: http://m4e.org\n";
        body += "Support: support@m4e.org";
        body += "\n\n";
        body += "Best Regards\n";
        body += "Meet4Eat Team\n";
        return body;
    }
}
