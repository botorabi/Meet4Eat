/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.communication;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.websocket.Session;

import net.m4e.app.user.business.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    protected void dispatchMessage(Packet<Map<String,Object>> packet, Session session) {
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
    private void distributeToChannelChat(Packet<Map<String,Object>> packet, Session session) {
        //TODO: Merge with distributeToChannelEvent, distributeToChannelSystem
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
    private void distributeToChannelEvent(Packet<Map<String,Object>> packet, Session session) {
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
    private void distributeToChannelSystem(Packet<Map<String,Object>> packet, Session session) {
        UserEntity user = connections.getUser(session);
        ChannelEventSystem ev = new ChannelEventSystem();
        ev.setSenderId(user.getId());
        ev.setPacket(packet);
        //TODO: Session only needed in this case?
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

        Packet<Map<String, Object>> packet = event.getPacket();
        Map<String, Object> data = packet.getData();
        if (data == null) {
            LOGGER.warn("invalid system command received from user: " + senderid);
            return;
        }

        //! NOTE currently we support only the ping command.
        String cmd = (String) data.getOrDefault("cmd", "");
        if ("ping".equals(cmd)) {
            Packet<PingResponse> response = new Packet<>();
            response.setChannel(Packet.CHANNEL_EVENT);
            response.setData(new PingResponse("ping", packet.getTime()));
            connections.sendPacket(response, senderid, event.getSessionId());
        }
        else {
            LOGGER.warn("unsupported system command '" + cmd + "' received from user: " + senderid);
        }
    }

    public static class PingResponse {
        private final String cmd;
        private final long pong;

        public PingResponse(final String cmd, final long pong) {
            this.cmd = cmd;
            this.pong = pong;
        }

        public String getCmd() {
            return cmd;
        }

        public long getPong() {
            return pong;
        }
    }
}
