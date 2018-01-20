/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.communication;

import java.util.Collections;
import java.util.Map;

import javax.websocket.DecodeException;
import javax.websocket.EncodeException;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.m4e.app.communication.Connection.JsonBEncoder;
import net.m4e.app.communication.Connection.PacketMapDecoder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.revinate.assertj.json.JsonPathAssert.assertThat;

/**
 * @author ybroeker
 */
class ConnectionTest {

    @Nested
    class PacketMapDecoderTest {

        @Test
        void testEmptyPayloadDecoding() throws DecodeException {
            PacketMapDecoder packetMapDecoder = new PacketMapDecoder();

            String json = "{\"channel\":\"lennahc\", \"sourceid\":\"1\", \"source\":\"ecruos\"}";

            Packet<Map<String, Object>> packet = packetMapDecoder.decode(json);

            Assertions.assertThat(packet.getChannel()).isEqualTo("lennahc");
            Assertions.assertThat(packet.getSourceId()).isEqualTo("1");
            Assertions.assertThat(packet.getSource()).isEqualTo("ecruos");
            Assertions.assertThat(packet.getData()).isNull();

        }

        @Test
        void testValidPayloadDecoding() throws DecodeException {
            PacketMapDecoder packetMapDecoder = new PacketMapDecoder();

            String json = "{\"channel\":\"lennahc\", \"sourceid\":\"1\", \"source\":\"ecruos\", \"data\":{\"test\":\"tset\"}}";

            Packet<Map<String, Object>> packet = packetMapDecoder.decode(json);

            Assertions.assertThat(packet.getChannel()).isEqualTo("lennahc");
            Assertions.assertThat(packet.getSourceId()).isEqualTo("1");
            Assertions.assertThat(packet.getSource()).isEqualTo("ecruos");
            Assertions.assertThat(packet.getData().get("test")).isEqualTo("tset");
        }
    }

    @Nested
    class JsonBEncoderTest {

        @Test
        void testEmptyPayloadDecoding() throws EncodeException {
            JsonBEncoder jsonBEncoder = new JsonBEncoder();
            Packet<Map> packet = new Packet<>("lennahc", "1", "ecruos", null);

            String json = jsonBEncoder.encode(packet);

            DocumentContext ctx = JsonPath.parse(json);
            assertThat(ctx).jsonPathAsString("$.channel").isEqualTo("lennahc");
            assertThat(ctx).jsonPathAsString("$.sourceId").isEqualTo("1");
            assertThat(ctx).jsonPathAsString("$.source").isEqualTo("ecruos");
        }

        @Test
        void testDecoding() throws EncodeException {
            JsonBEncoder jsonBEncoder = new JsonBEncoder();
            Packet<Map> packet = new Packet<>("lennahc", "1", "ecruos", Collections.singletonMap("payload", "content"));

            String json = jsonBEncoder.encode(packet);

            DocumentContext ctx = JsonPath.parse(json);
            assertThat(ctx).jsonPathAsString("$.channel").isEqualTo("lennahc");
            assertThat(ctx).jsonPathAsString("$.sourceId").isEqualTo("1");
            assertThat(ctx).jsonPathAsString("$.source").isEqualTo("ecruos");
            assertThat(ctx).jsonPathAsString("$.data.payload").isEqualTo("content");
        }
    }
}
