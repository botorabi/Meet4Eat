/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.notification;

import net.m4e.app.communication.ConnectedClients;
import net.m4e.app.communication.Packet;
import net.m4e.app.user.UserEntity;
import net.m4e.app.user.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.lang.invoke.MethodHandles;
import java.util.List;

/**
 * Event listener for sending a notification to a group of users
 * 
 * @author boto
 * Date of creation Oct 4, 2017
 */
@Stateless
public class NotifyUsersListener {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Central place to hold all client connections
     */
    @Inject
    ConnectedClients connections;

    /**
     * The users
     */
    private final Users users;


    /**
     * EJB's default constructor. Make the container happy.
     */
    protected NotifyUsersListener() {
        this.users = null;  
    }

    /**
     * Create the listener.
     *
     * @param users     Users instance
     */
    @Inject
    public NotifyUsersListener(Users users) {
        this.users = users;
    }

    /**
     * Event observer for sending a notification from a user to other users.
     * 
     * @param event NotifyUser event
     */
    public void notifyUsers(@ObservesAsync NotifyUsersEvent event) {
        LOGGER.debug("Sending out user notification");

        if (event == null) {
            LOGGER.warn("  attempt to send an empty event!");
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
        packet.setChannel(Packet.CHANNEL_NOTIFY);
        packet.setSourceId((sender != null) && (sender.getId() != null) ? sender.getId().toString() : "");
        packet.setSource((sender != null) ? sender.getName() : "");

        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("type", event.getType());
        json.add("subject", event.getSubject());
        json.add("text", event.getText());
        if (event.getData() != null) {
            json.add("data", event.getData());
        }
        packet.setData(json.build());

        connections.sendPacket(packet, event.getRecipientIds());
    }

    /**
     * Event observer for sending a notification from a user to all its related users.
     * 
     * @param event NotifyUser event, for this event 'recipient IDs' are not used.
     */
    public void notifyUserRelatives(@ObservesAsync NotifyUserRelativesEvent event) {
        LOGGER.debug("Sending out user notification to user's relatives");

        if (event == null) {
            LOGGER.warn("  attempt to send an empty event!");
            return;
        }

        // is a user sending a notification or the system?
        UserEntity sender;
        Long senderid = event.getSenderId();
        if (senderid == 0L) {
            LOGGER.warn("  attempt to send an event with an invalid sender ID!");
            return;
        }

        sender = users.findUser(senderid);
        if ((sender == null) || !sender.getStatus().getIsActive()) {
            LOGGER.warn("  attempt to send an event with an invalid sender!");
            return;            
        }

        // assemble a packet and send it out
        Packet packet = new Packet();
        packet.setChannel(Packet.CHANNEL_NOTIFY);
        packet.setSourceId(sender.getId().toString());
        packet.setSource(sender.getName());

        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("type", event.getType())
            .add("subject", event.getSubject())
            .add("text", event.getText());
        if (event.getData() != null) {
            json.add("data", event.getData());
        }
        packet.setData(json.build());

        List<Long> recipients = users.getUserRelatives(sender);
        connections.sendPacket(packet, recipients);
    }
}
