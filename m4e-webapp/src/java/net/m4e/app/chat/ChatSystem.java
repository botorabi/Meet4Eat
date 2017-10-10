/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.chat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.m4e.app.communication.ChannelChatEvent;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import net.m4e.app.communication.ConnectedClients;
import net.m4e.app.communication.Packet;
import net.m4e.app.event.EventEntity;
import net.m4e.app.event.Events;
import net.m4e.app.user.UserEntity;
import net.m4e.system.core.Log;


/**
 * Central chat functionality providing real-time messaging.
 * 
 * @author boto
 * Date of creation Oct 07, 2017
 */
@ApplicationScoped
public class ChatSystem {

    /**
     * Used for logging
     */
    private final static String TAG = "ChatSystem";

    /**
     * Central place to hold all client connections
     */
    @Inject
    ConnectedClients connections;

    /**
     * Entity manager needed for entity retrieval and modifications.
     */
    @PersistenceContext(unitName = net.m4e.system.core.AppConfiguration.PERSITENCE_UNIT_NAME)
    private EntityManager entityManager;

    /**
     * New chat messages are dispatched asynchronously.
     * 
     * @param event Chat event
     */
    public void dispatchMessge(@ObservesAsync ChannelChatEvent event) {
        Long senderid = event.getSenderId();
        UserEntity user = connections.getConnectedUser(senderid);
        if (user == null) {
            Log.warning(TAG, "invalid sender id detected: " + senderid);
            return;
        }

        Packet packet = event.getPacket();
        JsonObject data = packet.getData();
        String receiveuser = data.getString("receiverUser", "");
        String receiveevent = data.getString("receiverEvent", "");
        if (receiveuser.isEmpty() && receiveevent.isEmpty()) {
            Log.warning(TAG, "got invalid receiver from user " + senderid);
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
            Log.warning(TAG, "could not distribute chat message from sender " + senderid + ", reason: " + ex.getLocalizedMessage());
        }
    }

    /**
     * Send a chat message to all event members.
     * 
     * @param sender        Message sender
     * @param receiverId    Recipient ID (event ID)
     * @param packet        Chat packet to send
     */
    private void sendMessageEvent(UserEntity sender, Long receiverId, Packet packet) {
        Events events = new Events(entityManager);
        EventEntity event = events.findEvent(receiverId);
        if (event == null) {
            Log.warning(TAG, "cannot distribute event message, invalid event id " + receiverId);
            return;
        }

        Collection<UserEntity> members = event.getMembers();
        // avoid duplicate IDs by using a set (the sender can be also the owner or part of the members)
        Set<Long> receiverids = new HashSet();
        receiverids.add(sender.getId());
        receiverids.add(sender.getStatus().getIdOwner());
        if (members != null) {
            members.forEach((m) -> {
                receiverids.add(m.getId());
            });
        }
        packet.setSource(sender.getName());
        packet.setTime((new Date()).getTime());
        connections.sendPacket(packet, new ArrayList(receiverids));
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
        List<Long> receiverids = new ArrayList();
        receiverids.add(sender.getId());
        receiverids.add(recipient.getId());
        packet.setSource(sender.getName());
        packet.setTime((new Date()).getTime());
        connections.sendPacket(packet, receiverids);
    }
}
