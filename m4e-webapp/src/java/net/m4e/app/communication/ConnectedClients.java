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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.websocket.Session;
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
        if (null != entry) {
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
        String msg = packet.getJSON();
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
        if (null == entry) {
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
        if (null == entry) {
            return false;
        }
        if (!entry.sessions.remove(session)) {
            return false;
        }
        // if there are no futher connections then remove the user entry
        if (entry.sessions.size() < 1) {
            connections.remove(user.getId());
        }
        return true;
    }

    /**
     * Handle incoming message.
     * 
     * @param message   Message
     * @param session   WebSocket session the message was arrived
     */
    protected void onMessage(String message, Session session) {
        UserEntity user = (UserEntity)session.getUserProperties().get("user");

        //! TODO distribute the message to registered channels

        Log.verbose(TAG, "client message arrived from user (" + user.getName() + "): " + message);
        try {
            session.getBasicRemote().sendText("ECHO: " + message);
        }
        catch (IOException ex) {
            Log.warning(TAG, "could not send out message, reason: " + ex.getLocalizedMessage());
        }
    }
}
