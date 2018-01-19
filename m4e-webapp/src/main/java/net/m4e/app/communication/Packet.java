/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.communication;

import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.util.Date;

import javax.json.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used for WebSocket communication.
 *
 * @author boto Date of creation Oct 4, 2017
 */
public class Packet {
    //TODO: (https://github.com/botorabi/Meet4Eat/issues/7) Generify? Enables to use explicit classes instead of JSON

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
    private JsonObject data;
    private Long   time = 0L;

    /**
     * Create an empty packet instance.
     */
    public Packet() {}

    /**
     * Create a packet instance.
     *
     * @param channel   Packet channel, one of CHANNEL_xx strings
     * @param sourceId  ID of sender, e.g. user ID
     * @param source    Human readable string, e.g. user name, as far as available
     * @param data      Packet data, it should be in JSON format
     */
    public Packet(String channel, String sourceId, String source, JsonObject data) {
        this.channel = channel;
        this.sourceId = sourceId;
        this.source = source;
        this.data = data;
    }

    /**
     * Get the JSON formated string.
     *
     * @return JSON string representing the packet
     */
    public String toJSON() {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("channel", ((channel != null) ? channel : ""))
            .add("sourceId", ((sourceId != null) ? sourceId : ""))
            .add("source", ((source != null) ? source : ""))
            .add("time", (time == 0L) ? (new Date()).getTime() : time);
        if (data != null) {
            json.add("data", data);
        }
        return json.build().toString();
    }

    /**
     * Create a JSON string with given fields.
     *
     * @param channel   Packet channel
     * @param sourceId  Source ID
     * @param source    Human readable string of the source, e.g. user name, as far as available
     * @param data      Packet data, it should be in JSON format
     * @return          JSON formatted string
     */
    public static String toJSON(String channel, String sourceId, String source, JsonObject data) {
        return new Packet(channel, sourceId, source, data).toJSON();
    }

    /**
     * Create a packet out of given JSON string. If an invalid JSON format is given, then
     * null will be returned.
     * 
     * @param input     Packet in JSON format
     * @return          A packet representing the JSON format, or null if an invalid JSON input was given.
     */
    public static Packet fromJSON(String input) {
        Packet packet = null;
        try {
            JsonReader jreader = Json.createReader(new StringReader(input));
            JsonObject jobject = jreader.readObject();
            String channel = jobject.getString("channel", "");
            String sourceId = jobject.getString("soourceId", "");
            String source = jobject.getString("soource", "");
            JsonObject data = jobject.getJsonObject("data");
            long time = jobject.getJsonNumber("time").longValue();
            packet = new Packet(channel, sourceId, source, data);
            packet.setTime(time);
        }
        catch (Exception ex) {
            LOGGER.debug("Could not read JSON string, reason: " + ex.getLocalizedMessage());
        }
        return packet;
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
    public JsonObject getData() {
        return data;
    }

    /**
     * Set the packet data.
     *
     * @param data Packet data
     */
    public void setData(JsonObject data) {
        this.data = data;
    }

    /**
     * Get packet's timestamp.
     *
     * @return Timestamp
     */
    public Long getTime() {
        return time;
    }

    /**
     * Set the packet time, this is a timestamp. Pass a 0L in order to
     * automatically take the current timestamp when building the JSON string.
     *
     * @param time Packet timestamp
     */
    public void setTime(Long time) {
        this.time = time;
    }
}
