/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.update.rest.comm;

import org.junit.jupiter.api.*;

import javax.json.bind.*;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author boto
 * Date of creation March 12, 2018
 */
public class UpdateCheckMiscTest {

    private Jsonb json;

    @BeforeEach
    void setup() {
        json = JsonbBuilder.create();
    }

    @Test
    void checkCount() {
        UpdateCheckCount result = new UpdateCheckCount(100L);

        String jsonString = json.toJson(result);

        assertThat(jsonString).contains("count");
    }

    @Test
    void checkId() {
        UpdateCheckId result = new UpdateCheckId("100");

        String jsonString = json.toJson(result);

        assertThat(jsonString).contains("id");
    }

    @Nested
    class UpdateCheckCmdTests {

        private final String NAME = "App";
        private final String OS = "Win";
        private final String FLAVOR = "https://update.org";
        private final String CLIENT_VERSION = "1.0.0";

        @Test
        void serialize() {
            UpdateCheckCmd checkCmd = new UpdateCheckCmd();
            checkCmd.setName(NAME);
            checkCmd.setOs(OS);
            checkCmd.setFlavor(FLAVOR);
            checkCmd.setClientVersion(CLIENT_VERSION);

            String jsonString = json.toJson(checkCmd);

            assertThat(jsonString).contains("name");
            assertThat(jsonString).contains("os");
            assertThat(jsonString).contains("flavor");
            assertThat(jsonString).contains("clientVersion");
        }

        @Test
        void deserialize() {
            String jsonString = json.toJson(new UpdateCheckCmd(NAME, OS, FLAVOR, CLIENT_VERSION));
            UpdateCheckCmd checkCmd = json.fromJson(jsonString, UpdateCheckCmd.class);

            assertThat(checkCmd.getName()).isEqualTo(NAME);
            assertThat(checkCmd.getOs()).isEqualTo(OS);
            assertThat(checkCmd.getFlavor()).isEqualTo(FLAVOR);
            assertThat(checkCmd.getClientVersion()).isEqualTo(CLIENT_VERSION);
        }
    }
}
