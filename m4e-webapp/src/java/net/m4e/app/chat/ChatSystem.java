/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.chat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
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
@Singleton
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
     * Called on post-construction of the instance.
     */
    @PostConstruct
    public void chatSystemInit() {
        Log.info(TAG, "Starting the chat system");
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
     * Send a chat message to all event members. The sender must be a member of the event, otherwise the request is ignored.
     * 
     * @param sender        Message sender
     * @param receiverId    Recipient ID (event ID)
     * @param packet        Chat packet to send
     */
    private void sendMessageEvent(UserEntity sender, Long receiverId, Packet packet) {
        Events events = new Events(entityManager);
        Set<Long> receiverids = events.getMembers(receiverId);
        EventEntity event = events.findEvent(receiverId);
        if ((event == null) || !events.getUserIsEventOwnerOrMember(sender, event)) {
            Log.warning(TAG, "user " + sender.getId() + " tries to send to an event chat without being a member of the event, or the event is invalid!");
            return;
        }
        receiverids.add(sender.getId());
        packet.setSourceId(sender.getId().toString());
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
        packet.setSourceId(sender.getId().toString());
        packet.setSource(sender.getName());
        packet.setTime((new Date()).getTime());
        connections.sendPacket(packet, receiverids);
    }
}
