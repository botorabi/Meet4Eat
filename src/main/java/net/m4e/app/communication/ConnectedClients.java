/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.communication;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.*;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.websocket.EncodeException;
import javax.websocket.Session;

import net.m4e.app.event.business.EventNotifications;
import net.m4e.app.notification.NotifyUserRelativesEvent;
import net.m4e.app.user.business.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Event used for notifying other users
     */
    @Inject
    Event<NotifyUserRelativesEvent> notifyUserRelativesEvent;

    /**
     * Class used for a user entry
     */
    private class UserEntry {
        private UserEntity user;
        private List<Session /*WebSocket session*/> sessions = new ArrayList<>();

        public UserEntity getUser() {
            return user;
        }

        public void setUser(UserEntity user) {
            this.user = user;
        }

        public List<Session> getSessions() {
            return sessions;
        }
    }

    /**
     * Map containing user IDs and associated WebSocket sessions
     */
    private final Map<Long /*user ID*/, UserEntry >  connections = new HashMap<>();

    /**
     * Given an user ID return its user entity if it is currently connected.
     * 
     * @param userId        User ID
     * @return              User entity or null if there is no sessions for given user.
     */
    public UserEntity getConnectedUser(Long userId) {
        UserEntry entry = connections.get(userId);
        if (entry != null) {
            return entry.getUser();
        }
        return null;
    }

    /**
     * Send a packet to given recipients. The packet is sent to all connections of
     * recipients.
     * 
     * @param packet        Packet to send
     * @param recipientIds  List of recipients containing user IDs
     */
    public void sendPacket(Packet<?> packet, List<Long> recipientIds) {
        recipientIds.forEach(id -> {
            UserEntry receiverEntry = connections.get(id);
            if (receiverEntry != null) {
                receiverEntry.getSessions().forEach(session -> {
                    try {
                        session.getBasicRemote().sendObject(packet);
                    }
                    catch(IOException | EncodeException ex) {
                        LOGGER.warn("problem occurred while sending notification to user ({}), reason: {}" , id, ex.getLocalizedMessage());
                    }
                });
            }
        });
    }

    /**
     * Send a packet to a WebSocket connection of a user with given session ID.
     * NOTE: a user can be logged in multiple times from different devices.
     * 
     * @param packet        Packet to send
     * @param userId        User ID
     * @param sessionId     Session ID of a WebSocket connection
     */
    public void sendPacket(Packet<?> packet, Long userId, String sessionId) {
        UserEntry receiverEntry = connections.get(userId);
        if (receiverEntry != null) {
            receiverEntry.getSessions().stream().
                    filter(session -> (session.getId().equals(sessionId))).
                    forEach(session -> {
                        try {
                            session.getBasicRemote().sendObject(packet);
                        }
                        catch(IOException | EncodeException ex) {
                            LOGGER.warn("problem occurred while sending notification to user ({}/{}), reason: {}",
                                    userId, sessionId, ex.getLocalizedMessage());
                        }
                    });
        }
    }

    /**
     * Given a WebSocket session return its user.
     * 
     * @param session   WebSocket session
     * @return          User entity using this session
     */
    public UserEntity getUser(Session session) {
        return (UserEntity)session.getUserProperties().get("user");
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
            entry.setUser(user);
            connections.put(user.getId(), entry);
        }
        if (entry.getSessions().contains(session)) {
            LOGGER.warn("session for user {} already exists!", user.getId());
            return false;
        }
        // store the user in session, we need it later while handling incoming messages
        session.getUserProperties().put("user", user);
        entry.getSessions().add(session);

        // send a notification to user's relatives about going online
        // note that a user can be logged in multiple times, we send this notification only for the first login
        if (entry.getSessions().size() == 1) {
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
        if (!entry.getSessions().remove(session)) {
            return false;
        }
        // If there are no further connections then remove the user entry.
        // Send also a notification to user's relatives about going offline.
        // Note that a user can be logged in multiple times, we send this notification only if the user is completely logged out.
        if (entry.getSessions().isEmpty()) {
            connections.remove(user.getId());
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
