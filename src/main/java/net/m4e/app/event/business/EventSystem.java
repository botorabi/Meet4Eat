/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.business;

import net.m4e.app.communication.*;
import net.m4e.app.user.business.UserEntity;
import org.jetbrains.annotations.NotNull;
import org.slf4j.*;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.*;


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

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Events events;

    private final ConnectedClients connections;

    /**
     * Default constructor for making the container happy.
     */
    protected EventSystem() {
        events = null;
        connections = null;
    }

    /**
     * Create the bean and inject the necessary resources.
     * 
     * @param events    The Events instance
     */
    @Inject
    public EventSystem(@NotNull Events events, @NotNull ConnectedClients connections) {
        this.events = events;
        this.connections = connections;
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
        Long senderId = event.getSenderId();
        UserEntity user = connections.getConnectedUser(senderId);
        if (user == null) {
            LOGGER.warn("invalid sender id detected: " + senderId);
            return;
        }

        //TODO: we may introduce some sort of package validation in future.
        //       for instance the type of the event notification could be validated.

        Packet<Map<String, Object>> packet = event.getPacket();
        Map<String, Object> packetData = packet.getData();
        Map<String, Object> eventData = (Map<String, Object>) packetData.get("data");
        //TODO does this really work this way? {"data":{"data":{"eventId":"42"}}}
        String maybeEventId = (String) eventData.getOrDefault("eventId", "");

        try {
            if (!maybeEventId.isEmpty()) {
                Long eventId = Long.parseLong(maybeEventId);
                sendMessageEvent(user, eventId, packet);
            }
            else {
                LOGGER.warn("invalid receiver event ID detected, ignoring the event message!");
            }
        }
        catch(NumberFormatException ex) {
            LOGGER.warn("could not distribute event notification from sender " + senderId + ", reason: " + ex.getLocalizedMessage());
        }
    }

    /**
     * Send a message to all event members.
     * 
     * @param sender        Message sender
     * @param eventId       Event ID receiving the message
     * @param packet        Chat packet to send
     */
    private void sendMessageEvent(UserEntity sender, Long eventId, Packet<Map<String, Object>> packet) {
        Set<Long> receiverIds = events.getMembers(eventId);
        receiverIds.add(sender.getId());
        packet.setSourceId(sender.getId().toString());
        packet.setSource(sender.getName());
        packet.setTime((new Date()).getTime());
        connections.sendPacket(packet, new ArrayList<>(receiverIds));
    }
}
