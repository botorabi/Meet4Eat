/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.communication;

import net.m4e.app.auth.AuthorityConfig;
import net.m4e.app.user.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.json.Json;
import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Date;

/**
 * This is an WebSocket endpoint for client communication.
 * 
 * @author boto
 * Date of creation Oct 03, 2017
 */    
@ServerEndpoint(value="/ws", configurator = ConnectionConfigurator.class)
public class Connection {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
        LOGGER.trace("new client connected, id: " + session.getId());
        httpSession = (HttpSession)config.getUserProperties().get(ConnectionConfigurator.KEY_HTTP_SESSION);
        if (httpSession == null) {
            // close the connection, no http session exists
            LOGGER.debug("  closing websocket connection, no session was established before");
            CloseReason reason = new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "No HTTP session exists.");
            session.close(reason);
            return;
        }
        user = AuthorityConfig.getInstance().getSessionUser(httpSession);
        if (user == null) {
            // close the connection, user is not authorized
            LOGGER.debug("  closing websocket connection, user was not authenticated before");
            CloseReason reason = new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "User is not authenticated.");
            session.close(reason);
            return;
        }

        // store the user connection
        if (!connections.addConnection(user, session)) {
            LOGGER.warn("  could not store user's connection");
        }

        String response = createResponse("ok", "User " + user.getName() + " established a connection");
        session.getBasicRemote().sendText(response);
    }

    @OnClose
    public void close(Session session) {
        LOGGER.trace("client connection closed, id: " + session.getId());
        if (!connections.removeConnection(user, session)) {
            LOGGER.warn("  could not remove user's connection");
        }
    }

    @OnError
    public void onError(Throwable error) {
        LOGGER.trace("error on client connection");
    }

    @OnMessage
    public void handleMessage(String message, Session session) {
        Packet packet = Packet.fromJSON(message);
        if (packet == null) {
            LOGGER.debug("invalid message format received from client, ignoring it");
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
