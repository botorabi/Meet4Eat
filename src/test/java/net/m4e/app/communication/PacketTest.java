/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.communication;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

/**
 * @author ybroeker
 */
class PacketTest {

    @Test
    void setter_getter() {
        Packet packet = new Packet();
        packet.setSource("source");
        packet.setChannel("channel");
        packet.setSourceId("id");
        packet.setData(null);
        packet.setTime(100L);

        Packet expected = new Packet("channel", "id", "source", null);
        expected.setTime(100L);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(packet.getChannel()).isEqualTo(expected.getChannel());
        softly.assertThat(packet.getSource()).isEqualTo(expected.getSource());
        softly.assertThat(packet.getSourceId()).isEqualTo(expected.getSourceId());
        softly.assertThat(packet.getTime()).isEqualTo(expected.getTime());
        softly.assertThat(packet.getData()).isEqualTo(expected.getData());

        softly.assertAll();
    }

    /*
    @Test
    void toJson() {
        String expected1 = Packet.toJSON("channel", "id", "source", null);
        // we have to ensure that the timestamps are equal
        Packet packetgettime = Packet.fromJSON(expected1);
        Packet<JsonObject> packet1 = new Packet<>("channel", "id", "source", null);
        packet1.setTime(packetgettime.getTime());
        String json1 = packet1.toJSON();
        assertThat(json1).isEqualTo(expected1);

        String expected2 = Packet.toJSON(null, null, null, null);
        packetgettime = Packet.fromJSON(expected2);
        Packet<JsonObject> packet2 = new Packet<>(null, null, null, null);
        packet2.setTime(packetgettime.getTime());

        String json2 = packet2.toJSON();
        assertThat(json2).isEqualTo(expected2);
    }

    @Test
    void toJson_fromJSON() {
        final String PAYLOAD_FIELD = "mypayloadfield";
        final String PAYLOAD_VALUE = "payloaddata";

        JsonObject payload = Json.createObjectBuilder().add(PAYLOAD_FIELD, PAYLOAD_VALUE).build();

        Packet<JsonObject> packet = new Packet<>("channel", "id", "source", payload);

        Assume.assumeTrue(packet.getChannel().equals("channel"));

        String json = packet.toJSON();

        Packet<JsonObject> extracted = Packet.fromJSON(json);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(extracted.getChannel()).isEqualTo(packet.getChannel());
        softly.assertThat(extracted.getSource()).isEqualTo(packet.getSource());
        softly.assertThat(extracted.getSourceId()).isEqualTo(packet.getSourceId());
        softly.assertThat(extracted.getData().getString(PAYLOAD_FIELD)).isEqualTo(PAYLOAD_VALUE);

        softly.assertAll();
    }

    @Test
    void fromJSON_invalidInput() {
        Packet<JsonObject> packet = new Packet<>("channel", "id", "source", null);

        String json = packet.toJSON();
        json = json.substring(0, json.length() - 5);

        Packet extracted = Packet.fromJSON(json);

        assertThat(extracted).isEqualTo(null);
    }

    @Test
    void toJson_fromJson_Map() {
        final String PAYLOAD_FIELD = "mypayloadfield";
        final String PAYLOAD_VALUE = "payloaddata";

        String json = null;
        try {
            Packet<Map<String, String>> packet = new Packet<>("channel", "id", "source", Collections.singletonMap(PAYLOAD_FIELD, PAYLOAD_VALUE));
            json = json = packet.toJSON();
        } catch (Exception ex) {
            fail(ex);
        }

        // @formatter:off
        Type type = new HashMap<String, Object>() {}.getClass().getGenericSuperclass();
        // @formatter:on

        Packet<Map<String, Object>> extracted = Packet.fromJsonWithData(json, type);
        assertThat(extracted.getData().get(PAYLOAD_FIELD)).isEqualTo(PAYLOAD_VALUE);
    }

    @Test
    void fromJsonObjectToMap() {
        final String PAYLOAD_FIELD = "mypayloadfield";
        final String PAYLOAD_VALUE = "payloaddata";

        JsonObject payload = Json.createObjectBuilder().add(PAYLOAD_FIELD, PAYLOAD_VALUE).build();

        String json = null;
        try {
            Packet<JsonObject> packet = new Packet<>("channel", "id", "source", payload);
            json = packet.toJSON();
        } catch (Exception ex) {
            fail(ex);
        }

        // @formatter:off
        Type type = new HashMap<String, Object>() {}.getClass().getGenericSuperclass();
        // @formatter:on

        Packet<Map<String, Object>> extracted = Packet.fromJsonWithData(json, type);
        assertThat(extracted.getData().get(PAYLOAD_FIELD)).isEqualTo(PAYLOAD_VALUE);
    }

    @Test
    void toJson_fromJson_String() {
        final String PAYLOAD_VALUE = "payloaddata";

        Packet<String> packet = new Packet<>("channel", "id", "source", PAYLOAD_VALUE);
        String json = packet.toJSON();

        Packet<String> extracted = Packet.fromJsonWithData(json, String.class);

        Assertions.assertThat(extracted).isEqualToComparingFieldByField(packet);
    }

    @Test
    void toJson_fromJson_Integer() {
        final Integer PAYLOAD_VALUE = 42;

        Packet<Integer> packet = new Packet<>("channel", "id", "source", PAYLOAD_VALUE);
        String json = packet.toJSON();

        Packet<Integer> extracted = Packet.fromJsonWithData(json, Integer.class);

        Assertions.assertThat(extracted).isEqualToComparingFieldByField(packet);
        Assertions.assertThat(extracted.getData()).isEqualToComparingFieldByField(packet.getData());

    }
    */
}
