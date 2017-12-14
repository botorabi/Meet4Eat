/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.communication;
import java.io.IOException;
import java.util.Date;
import javax.inject.Inject;
import javax.json.Json;
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
     * WebSocket protocol version. The packet header may differ from version 
     * to version.
     */
    public final static String PROTOCOL_VERSION = "1.0.0";

    /**
     * Underlying HTTP session which allows access to authenticated user.
     */
    HttpSession httpSession;

    /**
     * User communicating by this connection.
     */
    UserEntity user;

    /**
     * Central place to hold all client connections
     */
    @Inject
    ConnectedClients connections;

    /**
     * Distribute the incoming message in proper channels.
     */
    @Inject
    MessageDistribution msgHandler;

    @OnOpen
    public void open(Session session, EndpointConfig config) throws IOException {
        Log.verbose(TAG, "new client connected, id: " + session.getId());
        httpSession = (HttpSession)config.getUserProperties().get(ConnectionConfigurator.KEY_HTTP_SESSION);
        if (httpSession == null) {
            // close the connection, no http session exists
            Log.debug(TAG, "  closing websocket connection, no session was established before");
            CloseReason reason = new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "No HTTP session exists.");
            session.close(reason);
            return;
        }
        user = AuthorityConfig.getInstance().getSessionUser(httpSession);
        if (user == null) {
            // close the connection, user is not authorized
            Log.debug(TAG, "  closing websocket connection, user was not authenticated before");
            CloseReason reason = new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "User is not authenticated.");
            session.close(reason);
            return;
        }

        // store the user connection
        if (!connections.addConnection(user, session)) {
            Log.warning(TAG, "  could not store user's connection");
        }

        String response = createResponse("ok", "User " + user.getName() + " established a connection");
        session.getBasicRemote().sendText(response);
    }

    @OnClose
    public void close(Session session) {
        Log.verbose(TAG, "client connection closed, id: " + session.getId());
        if (!connections.removeConnection(user, session)) {
            Log.warning(TAG, "  could not remove user's connection");
        }
    }

    @OnError
    public void onError(Throwable error) {
        Log.verbose(TAG, "error on client connection");
    }

    @OnMessage
    public void handleMessage(String message, Session session) throws IOException {
        Packet packet = Packet.fromJSON(message);
        if (packet == null) {
            Log.debug(TAG, "invalid message format received from client, ignoring it");
            return;
        }
        msgHandler.dispatchMessage(packet, session);
    }

    /**
     * Create a connection response packet.
     * 
     * @param status        Status ok or nok
     * @param description   Response description   
     * @return              String in JSON format ready to send.
     */
    private String createResponse(String status, String description) {
        Packet packet = new Packet();
        packet.setChannel(Packet.CHANNEL_SYSTEM);
        packet.setSource("");
        packet.setTime( ( new Date() ).getTime() );
        packet.setData(Json.createObjectBuilder().add("protocolVersion", PROTOCOL_VERSION)
                                                 .add("status", status)
                                                 .add("description", description)
                                                 .build());
        return packet.toJSON();
    }
}
