/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
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
public abstract class ChannelEvent<T> {

    /**
     * Sender ID, let it be 0 in the case that the sender is the system.
     */
    private Long senderId = 0L;

    /**
     * The WebSocket session ID.
     */
    private String sessionId = "";

    /**
     * The network packet
     */
    private Packet<T> packet;

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
     * Get the WebSocket session ID.
     * 
     * @return Session ID
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Set the WebSocket session ID.
     * 
     * @param sessionId Session ID
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Get the communication packet.
     * 
     * @return The packet
     */
    public Packet<T> getPacket() {
        return packet;
    }

    /**
     * Set the communication packet.
     * 
     * @param packet The packet
     */
    public void setPacket(Packet<T> packet) {
        this.packet = packet;
    }
}
