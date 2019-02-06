/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
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
    void setterGetter() {
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
}
