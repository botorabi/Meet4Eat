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
    private String text;

    /**
     * If needed put some data into the event.
     */
    private String data;


    public NotifyUsersEvent() {}

    /**
     * Get ID of user sending this notification. It can be 0 meaning that the notification
     * comes from system.
     * 
     * @return Sender ID
     */
    public Long getSenderId() {
        return senderId;
    }

    /**
     * Set ID of user sending this notification.
     * 
     * @param senderId User ID
     */
    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    /**
     * Get all users receiving the notification.
     * 
     * @return List of user IDs the notification is sent to
     */
    public List<Long> getRecipientIds() {
        return recipientIds;
    }

    /**
     * Set the user IDs receiving the notification.
     * 
     * @param recipientIds User IDs the notification is sent to
     */
    public void setRecipientIds(List<Long> recipientIds) {
        this.recipientIds = recipientIds;
    }

    /**
     * Get the notification subject.
     * 
     * @return Subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Set the notification subject.
     * 
     * @param subject Notification subject
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Get the notification text.
     * 
     * @return Notification text
     */
    public String getText() {
        return text;
    }

    /**
     * Set the notification text.
     * 
     * @param message Notification text
     */
    public void setText(String message) {
        this.text = message;
    }

    /**
     * Get the notification data. It can be null if no data is needed.
     * 
     * @return Data
     */
    public String getData() {
        return data;
    }

    /**
     * Set the notification data.
     * 
     * @param data Data
     */
    public void setData(String data) {
        this.data = data;
    }
}
