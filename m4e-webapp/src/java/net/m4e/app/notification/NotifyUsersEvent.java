/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.notification;

import java.util.ArrayList;
import java.util.List;

/**
 * Event used for sending a notification to a group of users
 * 
 * @author boto
 * Date of creation Oct 4, 2017
 */
public class NotifyUsersEvent {

    /**
     * Sender ID, let it be 0 in the case that the sender is the system.
     */
    private Long senderId = 0L;

    /**
     * Recipient (user) IDs, multiple users can get the notification.
     */
    private List<Long> recipientIds = new ArrayList();

    /**
     * Notification subject
     */
    private String subject;

    /**
     * Notification text
     */
    private String message;

    /**
     * If needed put some data into the event.
     */
    private byte[] data;


    public NotifyUsersEvent() {}

    
    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public List<Long> getRecipientIds() {
        return recipientIds;
    }

    public void setRecipientIds(List<Long> recipientIds) {
        this.recipientIds = recipientIds;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
