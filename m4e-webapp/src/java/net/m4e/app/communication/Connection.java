/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.communication;
import java.io.IOException;
import java.util.Objects;
import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import net.m4e.app.auth.AuthorityConfig;
import net.m4e.app.user.UserEntity;
import net.m4e.system.core.Log;

/**
 * This is an WebSocket endpoint for client communication.
 * 
 * @author boto
 * Date of creation Oct 03, 2017
 */    
@ServerEndpoint(value="/ws", configurator = ConnectionConfigurator.class)
public class Connection {

    /**
     * Used for logging
     */
    private final static String TAG = "Connection";

    /**
     * Underlying HTTP session which allows access to authenticated user.
     */
    HttpSession httpSession;

    /**
     * User communicating by this connection.
     */
    UserEntity user;
    
    @OnOpen
    public void open(Session session, EndpointConfig config) throws IOException {
        Log.verbose(TAG, "new client connected");
        httpSession = (HttpSession)config.getUserProperties().get(ConnectionConfigurator.KEY_HTTP_SESSION);
        if (Objects.isNull(httpSession)) {
            // close the connection, no http session exists
            session.getBasicRemote().sendText("No HTTP session exists.");
            CloseReason reason = new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "No HTTP session exists.");
            session.close(reason);
            return;
        }
        user = AuthorityConfig.getInstance().getSessionUser(httpSession);
        if (Objects.isNull(user)) {
            // close the connection, user is not authorized
            session.getBasicRemote().sendText("User is not authenticated.");
            CloseReason reason = new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "User is not authenticated.");
            session.close(reason);
            return;
        }
        String response = "User " + user.getName() + " established a connection";
        session.getBasicRemote().sendText(response);
    }

    @OnClose
    public void close(Session session) {
        Log.verbose(TAG, "client connection closed");
    }

    @OnError
    public void onError(Throwable error) {
        Log.verbose(TAG, "error on client connection: " + error.getLocalizedMessage());
    }

    @OnMessage
    public void handleMessage(String message, Session session) throws IOException {
        Log.verbose(TAG, "client message arrived: " + message);
        session.getBasicRemote().sendText("ECHO: " + message);
    }
}
