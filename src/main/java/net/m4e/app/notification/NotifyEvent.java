/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.notification;

import java.util.Map;

/**
 * Base event used for sending a notification
 * 
 * @author boto
 * Date of creation Oct 12, 2017
 */
public abstract class NotifyEvent {

    /**
     * Sender ID, let it be 0 in the case that the sender is the system.
     */
    private Long senderId = 0L;

    /**
     * Notification type
     */
    private String type = "";

    /**
     * Notification subject
     */
    private String subject = "";

    /**
     * Notification text
     */
    private String text = "";

    /**
     * If needed put some data into the event.
     */
    private Map<String, Object> data;


    public NotifyEvent() {}

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
     * Get the notification type
     * 
     * @return Type
     */
    public String getType() {
        return type;
    }

    /**
     * Set the notification type
     * 
     * @param type Notification type
     */
    public void setType(String type) {
        this.type = type;
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
    public Map<String, Object> getData() {
        return data;
    }

    /**
     * Set the notification data.
     *
     * @param data Data
     */
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
