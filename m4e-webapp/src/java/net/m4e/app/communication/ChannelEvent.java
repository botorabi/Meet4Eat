/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.communication;

/**
 * Base class for all kinds of communication channel events
 * 
 * @author boto
 * Date of creation Oct 9, 2017
 */
public abstract class ChannelEvent {

    /**
     * Sender ID, let it be 0 in the case that the sender is the system.
     */
    private Long senderId = 0L;

    /**
     * The network packet
     */
    private Packet packet;

    /**
     * Get ID of user sending the channel packet.
     * 
     * @return Sender ID
     */
    public Long getSenderId() {
        return senderId;
    }

    /**
     * Set ID of user sending this packet.
     * 
     * @param senderId User ID
     */
    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    /**
     * Get the communication packet.
     * 
     * @return The packet
     */
    public Packet getPacket() {
        return packet;
    }

    /**
     * Set the communication packet.
     * 
     * @param packet The packet
     */
    public void setPacket(Packet packet) {
        this.packet = packet;
    }
}
