/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.notification;

/**
 * Event used for sending an e-mail to a user.
 * 
 * @author boto
 * Date of creation Oct 1, 2017
 */
public class SendEmailEvent {

    /**
     * Recipient address
     */
    private String email;

    /**
     * E-Mail's title
     */
    private String title;

    /**
     * E-Mails body
     */
    private String message;

    public SendEmailEvent() {        
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
