/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.communication;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Map;

import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.annotation.JsonbProperty;
import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

import net.m4e.app.auth.AuthorityConfig;
import net.m4e.app.user.business.UserEntity;
import net.m4e.system.core.AppConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an WebSocket endpoint for client communication.
 *
 * @author boto
 * Date of creation Oct 03, 2017
 */
@ServerEndpoint(value = AppConfiguration.WEBSOCKET_URL, configurator = ConnectionConfigurator.class, decoders = Connection.PacketMapDecoder.class, encoders = Connection.JsonBEncoder.class)
public class Connection {

    /**
     * WebSocket protocol version. The packet header may differ from version
     * to version.
     */
    public final static String PROTOCOL_VERSION = "1.0.0";

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
    public void open(Session session, EndpointConfig config) throws IOException, EncodeException {
        LOGGER.trace("new client connected, id: {}", session.getId());
        HttpSession httpSession = (HttpSession) config.getUserProperties().get(ConnectionConfigurator.KEY_HTTP_SESSION);
        if (httpSession == null) {
            // close the connection, no http session exists
            LOGGER.debug("closing websocket connection, no session was established before");
            CloseReason reason = new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "No HTTP session exists.");
            session.close(reason);
            return;
        }
        user = AuthorityConfig.getInstance().getSessionUser(httpSession);
        if (user == null) {
            // close the connection, user is not authorized
            LOGGER.debug("closing websocket connection, user was not authenticated before");
            CloseReason reason = new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "User is not authenticated.");
            session.close(reason);
            return;
        }

        // store the user connection
        if (!connections.addConnection(user, session)) {
            LOGGER.warn("could not store user's connection");
        }

        Packet<WSConnectionStatus> response = createResponse("ok", "User " + user.getName() + " established a connection");
        session.getBasicRemote().sendObject(response);
    }

    /**
     * Create a connection response packet.
     *
     * @param status      Status ok or nok
     * @param description Response description
     * @return String in JSON format ready to send.
     */
    private Packet<WSConnectionStatus> createResponse(String status, String description) {
        Packet<WSConnectionStatus> packet = new Packet<>();
        packet.setChannel(Packet.CHANNEL_SYSTEM);
        packet.setData(new WSConnectionStatus(PROTOCOL_VERSION, status, description));
        return packet;

    }

    @OnClose
    public void close(Session session) {
        LOGGER.trace("client connection closed, id: {}", session.getId());
        if (!connections.removeConnection(user, session)) {
            LOGGER.warn("could not remove user's connection");
        }
    }

    @OnError
    public void onError(Throwable error) {
        LOGGER.debug("error on client connection", error);
    }

    @OnMessage
    public void handleMessage(Packet<Map<String, Object>> packet, Session session) {
        if (packet == null) {
            LOGGER.debug("invalid message format received from client, ignoring it");
            return;
        }
        msgHandler.dispatchMessage(packet, session);
    }

    /**
     * Used for transferring the web socket connection status to client.
     */
    public static class WSConnectionStatus {
        private final String protocolVersion;
        private final String status;
        private final String description;

        WSConnectionStatus(@JsonbProperty("protocolVersion") final String protocolVersion,
                           @JsonbProperty("status") final String status,
                           @JsonbProperty("description") final String description) {
            this.protocolVersion = protocolVersion;
            this.status = status;
            this.description = description;
        }

        public String getProtocolVersion() {
            return protocolVersion;
        }

        public String getStatus() {
            return status;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Decoder for Text-Messages. Parses Json to Packet.
     */
    public static class PacketMapDecoder implements Decoder.Text<Packet<Map<String, Object>>> {
        @SuppressWarnings("unchecked")
        @Override
        public Packet<Map<String, Object>> decode(final String string) {
            try {
                return JsonbBuilder.create().fromJson(string, Packet.class);
            } catch (Exception ex) {
                LOGGER.debug("Could not read JSON string, reason: {}", ex.getLocalizedMessage(), ex);
            }
            //Todo: null or Exception?
            return null;
        }

        @Override
        public boolean willDecode(final String s) {
            return true;
        }

        @Override
        public void init(final EndpointConfig config) {
        }

        @Override
        public void destroy() {
        }
    }

    /**
     * Encodes the given Object as Json.
     */
    public static class JsonBEncoder implements Encoder.Text<Object> {
        @Override
        public String encode(final Object object) {
            return JsonbBuilder.create().toJson(object);
        }

        @Override
        public void init(final EndpointConfig config) {
        }

        @Override
        public void destroy() {
        }
    }
}
