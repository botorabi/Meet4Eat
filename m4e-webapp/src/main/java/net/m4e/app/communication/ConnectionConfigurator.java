/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.communication;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

/**
 * This configurator is used for making the HTTP session available to the WebSocket
 * endpoint Connection.
 * 
 * @author boto
 * Date of creation Oct 3, 2017
 */
public class ConnectionConfigurator extends ServerEndpointConfig.Configurator {

    /**
     * User property key for storing WebSocket's HTTP session
     */
    public final static String KEY_HTTP_SESSION = "httpSession";

    /**
     * Intercept in handshaking and store the HTTP session in user properties.
     * 
     * @param endpointConfig
     * @param request
     * @param response 
     */
    @Override
    public void modifyHandshake(ServerEndpointConfig endpointConfig, HandshakeRequest request, HandshakeResponse response) {
        endpointConfig.getUserProperties().put(KEY_HTTP_SESSION, request.getHttpSession());
    }
}
