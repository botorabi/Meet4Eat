/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.communication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.websocket.Session;
import net.m4e.app.event.EventNotifications;
import net.m4e.app.notification.NotifyUserRelativesEvent;
import net.m4e.app.user.UserEntity;
import net.m4e.system.core.Log;


/**
 * This resource tracks all connected (via WebSocket) clients. It provides functionality
 * to retrieve connected users and send packets to them.
 * 
 * See 'Connection' class for WebSocket handling.
 * 
 * @author boto
 * Date of creation Oct 4, 2017
 */
@ApplicationScoped
public class ConnectedClients {

    /**
     * Used for logging
     */
    private final static String TAG = "ConnectedClients";

    /**
     * Event used for notifying other users
     */
    @Inject
    Event<NotifyUserRelativesEvent> notifyUserRelativesEvent;

    /**
     * Class used for a user entry
     */
    private class UserEntry {
        public UserEntity user;
        public List<Session /*WebSocket session*/> sessions = new ArrayList();
    }

    /**
     * Map containing user IDs and associated WebSocket sessions
     */
    private final Map<Long /*user ID*/, UserEntry >  connections = new HashMap();

    /**
     * Given an user ID return its user entity if it is currently connected.
     * 
     * @param userId        User ID
     * @return              User entity or null if there is no sessions for given user.
     */
    public UserEntity getConnectedUser(Long userId) {
        UserEntry entry = connections.get(userId);
        if (entry != null) {
            return entry.user;
        }
        return null;
    }

    /**
     * Send a packet to given recipients.
     * 
     * @param packet        Packet to send
     * @param recipientIds  List of recipients containing user IDs
     */
    public void sendPacket(Packet packet, List<Long> recipientIds) {
        String msg = packet.toJSON();
        recipientIds.forEach(id -> {
            UserEntry recentry = connections.get(id);
            if (recentry != null) {
                recentry.sessions.forEach(session -> {
                    try {
                        session.getBasicRemote().sendText(msg);
                    }
                    catch(IOException ex) {
                        Log.warning(TAG, "problem occurred while sending notification to user (" + id + "), reason: " + ex.getLocalizedMessage());
                    }
                });
            }
        });
    }

    /**
     * Given a WebSocket session return its user.
     * 
     * @param session   WebSocket session
     * @return          User entity using this session
     */
    public UserEntity getUser(Session session) {
        UserEntity user = (UserEntity)session.getUserProperties().get("user");
        return user;
    }

    /**
     * Add a new WebSocket connection coming from a user.
     * This method is used by 'Connection' when a WebSocket connection was established.
     * 
     * NOTE: a user can be connected multiple times.
     * 
     * @param user      User
     * @param session   WebSocket session
     * @return          Return false if the session was already added before, otherwise return true.
     */
    protected boolean addConnection(UserEntity user, Session session) {
        UserEntry entry = connections.get(user.getId());
        if (entry == null) {
            entry = new UserEntry();
            entry.user = user;
            connections.put(user.getId(), entry);
        }
        if (entry.sessions.contains(session)) {
            Log.warning(TAG, "session for user " + user.getId() + " already exists!");
            return false;
        }
        // store the user in session, we need it later while handling incoming messages
        session.getUserProperties().put("user", user);
        entry.sessions.add(session);

        // send a notification to user's relatives about going online
        // note that a user can be logged in multiple times, we send this notification only for the first login
        if (entry.sessions.size() == 1) {
            sendNotificationToRelatives(user, true);
        }

        return true;
    }

    /**
     * Remove a session from given user.
     * This method is used by 'Connection' when a WebSocket connection was closed.
     * 
     * @param user      User
     * @param session   The session to remove
     * @return          Return true if successful.
     */
    protected boolean removeConnection(UserEntity user, Session session) {
        UserEntry entry = connections.get(user.getId());
        if (entry == null) {
            return false;
        }
        if (!entry.sessions.remove(session)) {
            return false;
        }
        // if there are no futher connections then remove the user entry
        if (entry.sessions.size() < 1) {
            connections.remove(user.getId());
        }

        // send a notification to user's relatives about going offline
         // note that a user can be logged in multiple times, we send this notification only if the user is completely logged out
        if (entry.sessions.isEmpty()) {
            sendNotificationToRelatives(user, false);
        }

        return true;
    }

    /**
     * Send a notification to user's relatives and let them know about user going on/off.
     * 
     * @param user      The user
     * @param online    Pass true for notifying about going online, otherwise offline
     */
    private void sendNotificationToRelatives(UserEntity user, boolean online) {
        EventNotifications notifications = new EventNotifications(null, notifyUserRelativesEvent);
        notifications.sendNotifyOnlineStatusChanged(user, online);
    }
}