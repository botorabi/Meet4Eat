/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.communication;

import net.m4e.app.user.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.websocket.Session;
import java.lang.invoke.MethodHandles;

/**
 * All incoming messages are dispatched and distributed via proper events.
 * The actual handling of messages is up to event listener.
 * 
 * @author boto
 * Date of creation Oct 7, 2017
 */
@ApplicationScoped
public class MessageDistribution {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Used for firing chat channel events
     */
    @Inject
    Event<ChannelChatEvent> channelChatEvent;

    /**
     * Used for firing event channel events
     */
    @Inject
    Event<ChannelEventEvent> channelEventEvent;

    /**
     * Used for firing event channel events
     */
    @Inject
    Event<ChannelEventSystem> channelEventSystem;

    @Inject
    ConnectedClients connections;

    /**
     * Construct the resource.
     */
    public MessageDistribution() {}

    /**
     * Handle incoming message.
     * 
     * @param packet        Incoming network packet
     * @param session       WebSocket session the message was arrived
     */
    protected void dispatchMessage(Packet packet, Session session) {
        if (packet.getChannel().equals(Packet.CHANNEL_CHAT)) {
            distributeToChannelChat(packet, session);
        }
        else if (packet.getChannel().equals(Packet.CHANNEL_EVENT)) {
            distributeToChannelEvent(packet, session);
        }
        else if (packet.getChannel().equals(Packet.CHANNEL_SYSTEM)) {
            distributeToChannelSystem(packet, session);
        }
    }

    /**
     * Send an asynchronous event to listeners of communication channel 'Chat'.
     * 
     * @param packet    Incoming chat packet
     * @param session   WebSocket session receiving the packet
     */
    private void distributeToChannelChat(Packet packet, Session session) {
        UserEntity user = connections.getUser(session);
        ChannelChatEvent ev = new ChannelChatEvent();
        ev.setSenderId(user.getId());
        ev.setPacket(packet);
        channelChatEvent.fireAsync(ev);
    }

    /**
     * Send an asynchronous event to listeners of communication channel 'Event'.
     * 
     * @param packet    Incoming event packet
     * @param session   WebSocket session receiving the packet
     */
    private void distributeToChannelEvent(Packet packet, Session session) {
        UserEntity user = connections.getUser(session);
        ChannelEventEvent ev = new ChannelEventEvent();
        ev.setSenderId(user.getId());
        ev.setPacket(packet);
        channelEventEvent.fireAsync(ev);
    }

    /**
     * Send an asynchronous event to listeners of communication channel 'System'.
     * 
     * @param packet    Incoming event packet
     * @param session   WebSocket session receiving the packet
     */
    private void distributeToChannelSystem(Packet packet, Session session) {
        UserEntity user = connections.getUser(session);
        ChannelEventSystem ev = new ChannelEventSystem();
        ev.setSenderId(user.getId());
        ev.setPacket(packet);
        ev.setSessionId(session.getId());
        channelEventSystem.fireAsync(ev);
    }

    /**
     * Dispatcher for system channel messages.
     * 
     * @param event System event
     */
    public void dispatchMessage(@ObservesAsync ChannelEventSystem event) {
        Long senderid = event.getSenderId();
        UserEntity user = connections.getConnectedUser(senderid);
        if (user == null) {
            LOGGER.warn("invalid sender id detected: " + senderid);
            return;
        }

        Packet packet = event.getPacket();
        JsonObject data = packet.getData();
        if (data == null) {
            LOGGER.warn("invalid system command received from user: " + senderid);
            return;
        }

        //! NOTE currently we support only the ping command.

        String cmd = data.getString("cmd", "");
        if ("ping".equals(cmd)) {
            JsonObjectBuilder json = Json.createObjectBuilder();
            json.add("cmd", "ping");
            json.add("pong", packet.getTime()); // pong contains the timestamp of ping requester
            packet.setSource("");
            packet.setSourceId("");
            packet.setData(json.build());
            connections.sendPacket(packet, senderid, event.getSessionId());
        }
        else {
            LOGGER.warn("unsupported system command '" + cmd + "' received from user: " + senderid);
        }
    }
}
