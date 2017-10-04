/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.communication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.enterprise.context.ApplicationScoped;
import javax.websocket.Session;
import net.m4e.app.user.UserEntity;
import net.m4e.system.core.Log;

/**
 * This resource tracks all connected clients.
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
    public static class UserEntry {
        public UserEntity user;
        public List<Session /*WebSocket session*/> sessions = new ArrayList();
    }

    /**
     * Map containing user IDs and associated WebSocket sessions
     */
    private final Map<Long /*user ID*/, UserEntry >  connections = new HashMap();

    /**
     * Get the WebSocket session of given user.
     * 
     * @param userId        User ID
     * @return              User's Websocket sessions, or null if there is no sessions for given user.
     */
    public UserEntry getConnection(Long userId) {
        return connections.get(userId);
    }

    /**
     * Add a new WebSocket connection coming from a user.
     * 
     * NOTE: a user can be connected multiple times.
     * 
     * @param user      User
     * @param session   WebSocket session
     * @return          Return false if the session was already added before, otherwise return true.
     */
    public boolean addConnection(UserEntity user, Session session) {
        UserEntry entry = getConnection(user.getId());
        if (Objects.isNull(entry)) {
            entry = new UserEntry();
            entry.user = user;
            connections.put(user.getId(), entry);
        }
        if (entry.sessions.contains(session)) {
            Log.warning(TAG, "session for user " + user.getId() + " already exists!");
            return false;
        }
        entry.sessions.add(session);
        return true;
    }

    /**
     * Remove a session from given user.
     * 
     * @param user      User
     * @param session   The session to remove
     * @return          Return true if successful.
     */
    public boolean removeConnection(UserEntity user, Session session) {
        UserEntry entry = getConnection(user.getId());
        if (Objects.isNull(entry)) {
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
}
