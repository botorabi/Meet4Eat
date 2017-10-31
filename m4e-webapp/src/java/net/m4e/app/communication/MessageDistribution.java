/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.communication;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.websocket.Session;
import net.m4e.app.user.UserEntity;

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
     * Used for logging
     */
    private final static String TAG = "MessageDistribution";

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
        //! TODO cover all other channel types
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
}
