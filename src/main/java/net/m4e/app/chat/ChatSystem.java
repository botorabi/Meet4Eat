/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.chat;

import java.lang.invoke.MethodHandles;
import java.util.*;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;

import net.m4e.app.communication.*;
import net.m4e.app.event.EventEntity;
import net.m4e.app.event.Events;
import net.m4e.app.user.business.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Central chat functionality providing real-time messaging.
 * 
 * @author boto
 * Date of creation Oct 07, 2017
 */
@Singleton
@ApplicationScoped
public class ChatSystem {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Events events;

    /**
     * Central place to hold all client connections
     */
    @Inject
    ConnectedClients connections;

    
    /**
     * EJB's default constructor
     */
    protected ChatSystem() {
        events = null;
    }

    /**
     * Construct the chat system.
     * 
     * @param events The Events instance
     */
    @Inject
    public ChatSystem(Events events) {
        this.events = events;
    }

    /**
     * Called on post-construction of the instance.
     */
    @PostConstruct
    public void chatSystemInit() {
        LOGGER.info("Starting the chat system");
    }

    /**
     * New chat messages are dispatched asynchronously.
     * 
     * @param event Chat event
     */
    public void dispatchMessage(@ObservesAsync ChannelChatEvent event) {
        Long senderid = event.getSenderId();
        UserEntity user = connections.getConnectedUser(senderid);
        if (user == null) {
            LOGGER.warn("invalid sender id detected: " + senderid);
            return;
        }

        Packet<Map<String, Object>> packet = event.getPacket();
        Map<String, Object> data = packet.getData();
        String receiveuser = (String) data.getOrDefault("receiverUser", "");
        String receiveevent = (String) data.getOrDefault("receiverEvent", "");
        if (receiveuser.isEmpty() && receiveevent.isEmpty()) {
            LOGGER.warn("got invalid receiver from user " + senderid);
            return;
        }
        try {
            if (!receiveevent.isEmpty()) {
                Long eventid = Long.parseLong(receiveevent);
                sendMessageEvent(user, eventid, packet);
            }
            else {
                Long userid = Long.parseLong(receiveuser);
                sendMessageUser(user, userid, packet);
            }
        }
        catch(NumberFormatException ex) {
            LOGGER.warn("could not distribute chat message from sender " + senderid + ", reason: " + ex.getLocalizedMessage());
        }
    }

    /**
     * Send a chat message to all event members. The sender must be a member of the event, otherwise the request is ignored.
     * 
     * @param sender        Message sender
     * @param receiverId    Recipient ID (event ID)
     * @param packet        Chat packet to send
     */
    private void sendMessageEvent(UserEntity sender, Long receiverId, Packet packet) {
        Set<Long> receiverids = events.getMembers(receiverId);
        EventEntity event = events.findEvent(receiverId);
        if ((event == null) || !events.getUserIsEventOwnerOrMember(sender, event)) {
            LOGGER.warn("user " + sender.getId() + " tries to send to an event chat without being a member of the event, or the event is invalid!");
            return;
        }
        receiverids.add(sender.getId());
        packet.setSourceId(sender.getId().toString());
        packet.setSource(sender.getName());
        packet.setTime((new Date()).getTime());
        connections.sendPacket(packet, new ArrayList<>(receiverids));
    }

    /**
     * Send a chat message to given user. This can be used for private messages.
     * 
     * @param sender        Message sender
     * @param receiverId    Recipient ID (user ID)
     * @param packet        Chat packet to send
     */
    private void sendMessageUser(UserEntity sender, Long receiverId, Packet packet) {
        UserEntity recipient = connections.getConnectedUser(receiverId);
        if (recipient == null) {
            return;
        }
        List<Long> receiverids = new ArrayList<>();
        receiverids.add(sender.getId());
        receiverids.add(recipient.getId());
        packet.setSourceId(sender.getId().toString());
        packet.setSource(sender.getName());
        packet.setTime((new Date()).getTime());
        connections.sendPacket(packet, receiverids);
    }
}
