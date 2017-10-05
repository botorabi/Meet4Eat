/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.communication;

import javax.json.Json;
import javax.json.JsonObjectBuilder;


/**
 * This class is used for WebSocket communication.
 * 
 * @author boto
 * Date of creation Oct 4, 2017
 */
public class Packet {

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
    private String source;
    private String data;

    /**
     * Create an empty packet instance.
     */
    public Packet() {}

    /**
     * Create a packet instance.
     * 
     * @param channel   Packet channel, one of CHANNEL_xx strings
     * @param source    Human readable string, e.g. user name, as far as available
     * @param data      Packet data
     */
    public Packet(String channel, String source, String data) {
        this.channel = channel;
        this.source = source;
        this.data = data;
    }

    /**
     * Get the JSON formated string.
     * 
     * @return JSON string representing the packet
     */
    public String getJSON() {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("channel", ((channel != null) ? channel : ""));
        json.add("source", ((source != null) ? source : ""));
        json.add("data", ((data != null) ? data : ""));
        return json.build().toString();
    }

    /**
     * Create a JSON string with given fields.
     * 
     * @param channel   Packet channel
     * @param source    Human readable string, e.g. user name, as far as available
     * @param data      Packet data
     * @return JSON formatted string
     */
    public static String buildJSON(String channel, String source, String data) {
        return new Packet(channel, source, data).getJSON();
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
     * @param source
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Packet data, this can be a JSON document.
     * 
     * @return The packet data
     */
    public String getData() {
        return data;
    }

    /**
     * Set the packet data.
     * 
     * @param data Packet data
     */
    public void setData(String data) {
        this.data = data;
    }
}
