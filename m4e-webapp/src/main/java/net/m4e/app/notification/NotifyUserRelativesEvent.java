/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
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
public class NotifyUserRelativesEvent extends NotifyEvent {

    /**
     * Recipient (user) IDs, multiple users can get the notification.
     */
    private List<Long> recipientIds = new ArrayList();

    public NotifyUserRelativesEvent() {}

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
}
