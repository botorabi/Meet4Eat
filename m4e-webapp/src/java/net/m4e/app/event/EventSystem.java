/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event;

import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import net.m4e.app.communication.ChannelEventEvent;
import net.m4e.app.communication.ConnectedClients;
import net.m4e.app.communication.Packet;
import net.m4e.app.user.UserEntity;
import net.m4e.system.core.Log;


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
     * Used for logging
     */
    private final static String TAG = "EventSystem";

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
    public void eventSystemInit() {
        Log.info(TAG, "Starting the event system");
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
            Log.warning(TAG, "invalid sender id detected: " + senderid);
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
                Log.warning(TAG, "invalid receiver event ID detected, ignoting the event message!");
            }
        }
        catch(NumberFormatException ex) {
            Log.warning(TAG, "could not distribute event notification from sender " + senderid + ", reason: " + ex.getLocalizedMessage());
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
        Events events = new Events(entityManager);
        Set<Long> receiverids = events.getMembers(eventId);
        receiverids.add(sender.getId());
        packet.setSourceId(sender.getId().toString());
        packet.setSource(sender.getName());
        packet.setTime((new Date()).getTime());
        connections.sendPacket(packet, new ArrayList(receiverids));
    }
}
