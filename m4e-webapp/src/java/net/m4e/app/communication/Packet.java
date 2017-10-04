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
     * Packet type for system notifications
     */
    public final static String TYPE_SYSTEM = "system";

    /**
     * Packet type for chat messages
     */
    public final static String TYPE_CHAT = "chat";

    /**
     * Packet type for event related communication
     */
    public final static String TYPE_EVENT = "event";

    private String type;
    private String source;
    private String destination;
    private String data;

    /**
     * Create a packet instance.
     * 
     * @param type
     * @param source
     * @param destination
     * @param data 
     */
    public Packet(String type, String source, String destination, String data) {
        this.type = type;
        this.source = source;
        this.destination = destination;
        this.data = data;
    }

    /**
     * Get the JSON formated string.
     * 
     * @return JSON string representing the packet
     */
    public String getJSON() {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("type", ((type != null) ? type : ""));
        json.add("source", ((source != null) ? source : ""));
        json.add("destination", ((destination != null) ? destination : ""));
        json.add("data", ((data != null) ? data : ""));
        return json.build().toString();
    }

    /**
     * Create a JSON string with given fields.
     * 
     * @param type
     * @param source
     * @param destination
     * @param data
     * @return JSON fromatted string
     */
    public static String buildJSON(String type, String source, String destination, String data) {
        return new Packet(type, source, destination, data).getJSON();
    }

    /**
     * Get the packet type, one of TYPE_xxx stringg.
     * 
     * @return Packet type
     */
    public String getType() {
        return type;
    }

    /**
     * Set the packet type.
     * 
     * @param type The packet type
     */
    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
