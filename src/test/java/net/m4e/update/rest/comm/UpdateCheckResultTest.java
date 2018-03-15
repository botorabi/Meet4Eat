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
public class UpdateCheckResultTest {

    private final String UPDATE_VERSION = "1.0.0";
    private final String OS = "Win";
    private final String URL = "https://update.org";
    private final Long RELEASE_DATE = Instant.now().toEpochMilli();

    private Jsonb json;

    @BeforeEach
    void setup() {
        json = JsonbBuilder.create();
    }

    @Test
    void serialize() {
        UpdateCheckResult result = new UpdateCheckResult();
        result.setUpdateVersion(UPDATE_VERSION);
        result.setOs(OS);
        result.setUrl(URL);
        result.setReleaseDate(RELEASE_DATE);

        String jsonString = json.toJson(result);

        assertThat(jsonString).contains("updateVersion");
        assertThat(jsonString).contains("os");
        assertThat(jsonString).contains("url");
        assertThat(jsonString).contains("releaseDate");
    }

    @Test
    void deserialize() {
        String jsonString = json.toJson(new UpdateCheckResult(UPDATE_VERSION, OS, URL, RELEASE_DATE));
        UpdateCheckResult result = json.fromJson(jsonString, UpdateCheckResult.class);

        assertThat(result.getUpdateVersion()).isEqualTo(UPDATE_VERSION);
        assertThat(result.getOs()).isEqualTo(OS);
        assertThat(result.getUrl()).isEqualTo(URL);
        assertThat(result.getReleaseDate()).isEqualTo(RELEASE_DATE);
    }
}
