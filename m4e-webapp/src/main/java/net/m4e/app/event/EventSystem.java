/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event;

import net.m4e.app.communication.ChannelEventEvent;
import net.m4e.app.communication.ConnectedClients;
import net.m4e.app.communication.Packet;
import net.m4e.app.user.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.json.JsonObject;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;


/**
 * Central place for handling event related real-time actions such as
 * distributing messages or coordinating event member votes for locations.
 * 
 * @author boto
 * Date of creation Oct 29, 2017
 */
@Singleton
@ApplicationScoped
public class EventSystem {

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
     * Default constructor for making the container happy.
     */
    protected EventSystem() {
        this.events = null;
    }

    /**
     * Create the bean and inject the necessary resources.
     * 
     * @param events    The Events instance
     */
    @Inject
    public EventSystem(Events events) {
        this.events = events;
    }

    /**
     * Called on post-construction of the instance.
     */
    @PostConstruct
    public void eventSystemInit() {
        LOGGER.info("Starting the event system");
    }

    /**
     * Event messages are dispatched asynchronously.
     * 
     * @param event Chat event
     */
    public void dispatchMessage(@ObservesAsync ChannelEventEvent event) {
        Long senderid = event.getSenderId();
        UserEntity user = connections.getConnectedUser(senderid);
        if (user == null) {
            LOGGER.warn("invalid sender id detected: " + senderid);
            return;
        }

        //! NOTE we may introduce some sort of package validation in future. 
        //       for instance the type of the event notification could be validated.

        Packet packet = event.getPacket();
        JsonObject eventpkg = packet.getData();
        JsonObject eventdata = eventpkg.getJsonObject("data");
        String receiveevent = eventdata.getString("eventId", "");
        try {
            if (!receiveevent.isEmpty()) {
                Long eventid = Long.parseLong(receiveevent);
                sendMessageEvent(user, eventid, packet);
            }
            else {
                LOGGER.warn("invalid receiver event ID detected, ignoting the event message!");
            }
        }
        catch(NumberFormatException ex) {
            LOGGER.warn("could not distribute event notification from sender " + senderid + ", reason: " + ex.getLocalizedMessage());
        }
    }

    /**
     * Send a message to all event members.
     * 
     * @param sender        Message sender
     * @param eventId       Event ID receiving the message
     * @param packet        Chat packet to send
     */
    private void sendMessageEvent(UserEntity sender, Long eventId, Packet packet) {
        Set<Long> receiverids = events.getMembers(eventId);
        receiverids.add(sender.getId());
        packet.setSourceId(sender.getId().toString());
        packet.setSource(sender.getName());
        packet.setTime((new Date()).getTime());
        connections.sendPacket(packet, new ArrayList(receiverids));
    }
}
