/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.notification;

import javax.ejb.Stateless;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import net.m4e.app.communication.ConnectedClients;
import net.m4e.app.communication.Packet;
import net.m4e.app.user.UserEntity;
import net.m4e.system.core.Log;

/**
 * Event listener for sending a notification to a group of users
 * 
 * @author boto
 * Date of creation Oct 4, 2017
 */
@Stateless
public class NotifyUsersListener {
    /**
     * Used for logging
     */
    private final static String TAG = "NotifyUsersListener";

    /**
     * Central place to hold all client connections
     */
    @Inject
    ConnectedClients connections;

    /**
     * Event observer
     * 
     * @param event NotifyUser event
     */
    public void notifyUsers(@ObservesAsync NotifyUsersEvent event) {
        Log.debug(TAG, "Sending out user notification");

        if (null == event) {
            Log.warning(TAG, "attempt to send an empty event!");
            return;
        }

        // is a user sending a notification or the system?
        UserEntity sender = null;
        Long senderid = event.getSenderId();
        if (senderid != 0L) {
            sender = connections.getConnectedUser(senderid);
        }

        // assemble a packet and send it out
        Packet packet = new Packet();
        packet.setChannel((null != sender) ? Packet.CHANNEL_NOTIFY : Packet.CHANNEL_SYSTEM);
        packet.setSource((null != sender) ? sender.getName() : "");

        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("subject", (null != event.getSubject()) ? event.getSubject() : "");
        json.add("text", (null != event.getText()) ? event.getText() : "");
        json.add("data", (null != event.getData()) ? event.getData() : "");
        packet.setData(json.build());

        connections.sendPacket(packet, event.getRecipientIds());
    }
}
