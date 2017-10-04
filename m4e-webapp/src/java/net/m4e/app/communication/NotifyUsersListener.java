/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.communication;

import java.io.IOException;
import java.util.List;
import javax.ejb.Stateless;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.websocket.Session;
import net.m4e.app.notification.NotifyUsersEvent;
import net.m4e.system.core.Log;

/**
 * Event listener for sending a notification to a group of users
 * 
 * @author boto
 * Date of creation Oct 4, 2017
 */
@Stateless
public class NotifyUsersListener {
    /**
     * Used for logging
     */
    private final static String TAG = "NotifyUsersListener";

    /**
     * Central place to hold all client connections
     */
    @Inject
    ConnectedClients connections;

    /**
     * Event observer
     * 
     * @param event NotifyUser event
     */
    public void notifyUsers(@ObservesAsync NotifyUsersEvent event) {
        Log.debug(TAG, "Sending out user notification");

        //! TODO what should we send back to sender?
        /*
        UserEntity sender = null;
        // is there a user as sender?
        Long senderid = Objects.nonNull(event) ? event.getSenderId() : 0L;
        if (senderid != 0L) {
            ConnectedClients.UserEntry entry = connections.getConnection(senderid);
            sender = entry.user;
        }
        */

        // get all recipient sessions
        List<Long> recipientids = event.getRecipientIds();
        for (Long id: recipientids) {
            ConnectedClients.UserEntry recentry = connections.getConnection(id);
            for (Session session: recentry.sessions) {
                sendNotify(session, event);
            }
        }
    }

    /**
     * Send out the notification
     * 
     * @param session   WebSocket session to send to
     * @param event     The event to send
     */
    private void sendNotify(Session session, NotifyUsersEvent event) {
        try {
            String text = "";
            text += "Subject: " + event.getSubject() + "\n";
            text += "Message: " + event.getMessage();
            session.getBasicRemote().sendText(text);
        }
        catch(IOException ex) {
            Log.warning(TAG, "problem occurred while sending notification to user, reason: " + ex.getLocalizedMessage());
        }
    }
}
