/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.communication;

import java.util.Date;

/**
 * This class is used for WebSocket communication.
 *
 * @author boto
 * Date of creation Oct 4, 2017
 */
public class Packet<T> {

    /**
     * Packet channel for system notifications
     */
    public final static String CHANNEL_SYSTEM = "system";

    /**
     * Packet channel for chat messages
     */
    public final static String CHANNEL_NOTIFY = "notify";

    /**
     * Packet channel for chat messages
     */
    public final static String CHANNEL_CHAT = "chat";

    /**
     * Packet channel for meeting event related communication
     */
    public final static String CHANNEL_EVENT = "event";

    private String channel;
    private String sourceId;
    private String source;
    private T data;
    private long time;

    /**
     * Create an empty packet instance.
     */
    public Packet() {
        this("", "", "", null);
    }

    /**
     * Create a packet instance.
     *
     * @param channel  Packet channel, one of CHANNEL_xx strings
     * @param sourceId ID of sender, e.g. user ID
     * @param source   Human readable string, e.g. user name, as far as available
     * @param data     Packet data, it should be in JSON format
     */
    public Packet(String channel, String sourceId, String source, T data) {
        this.channel = channel;
        this.sourceId = sourceId;
        this.source = source;
        this.data = data;
        this.time = new Date().getTime();
    }


    /**
     * Get the packet channel, one of CHANNEL_xxx string.
     *
     * @return Packet channel
     */
    public String getChannel() {
        return channel;
    }

    /**
     * Set the packet channel.
     *
     * @param channel The packet channel
     */
    public void setChannel(String channel) {
        this.channel = channel;
    }

    /**
     * Get packet source, can be also empty.
     *
     * @return Packet source, e.g. a user name
     */
    public String getSource() {
        return source;
    }

    /**
     * Set packet's source
     *
     * @param source Packet source
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Get packet source ID, can also be empty.
     *
     * @return Packet source ID, e.g. a user ID
     */
    public String getSourceId() {
        return sourceId;
    }

    /**
     * Set packet's source ID
     *
     * @param sourceId Packet source ID
     */
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    /**
     * Packet data, this is expected to be a JSON document.
     *
     * @return The packet data
     */
    public T getData() {
        return data;
    }

    /**
     * Set the packet data.
     *
     * @param data Packet data
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * Get packet's timestamp.
     *
     * @return Timestamp
     */
    public long getTime() {
        return time;
    }

    /**
     * Set the packet time, this is a timestamp. Pass a 0L in order to
     * automatically take the current timestamp when building the JSON string.
     *
     * @param time Packet timestamp
     */
    public void setTime(long time) {
        this.time = time;
    }
}
