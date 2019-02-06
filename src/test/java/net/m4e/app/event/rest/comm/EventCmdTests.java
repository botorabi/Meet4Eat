/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.rest.comm;


import com.jayway.jsonpath.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;

import javax.json.bind.*;

import static com.revinate.assertj.json.JsonPathAssert.assertThat;

/**
 * @author boto
 * Date of creation February 21, 2018
 */
public class EventCmdTests {

    Jsonb jsonb;

    @BeforeEach
    void setup() {
        this.jsonb = JsonbBuilder.create();
    }

    @Nested
    class EventLocationCmdTests {

        private static final String ID = "id";
        private static final String NAME = "name";
        private static final String DESCRIPTION = "description";
        private static final String PHOTO_CONTENT = "photo-content";

        @Test
        void deserialize() {
            EventLocationCmd locationCmd = new EventLocationCmd(ID, NAME, DESCRIPTION, PHOTO_CONTENT);

            String json = jsonb.toJson(locationCmd);

            EventLocationCmd newLocationCmd = jsonb.fromJson(json, EventLocationCmd.class);

            Assertions.assertThat(newLocationCmd.getId()).isEqualTo(ID);
            Assertions.assertThat(newLocationCmd.getName()).isEqualTo(NAME);
            Assertions.assertThat(newLocationCmd.getDescription()).isEqualTo(DESCRIPTION);
            Assertions.assertThat(newLocationCmd.getPhoto()).isEqualTo(PHOTO_CONTENT);
        }

        @Test
        void serializeByConstructor() {
            EventLocationCmd locationCmd = new EventLocationCmd(ID, NAME, DESCRIPTION, PHOTO_CONTENT);

            String json = jsonb.toJson(locationCmd);

            serialize(json);
        }

        @Test
        void serializeBySetters() {
            EventLocationCmd locationCmd = new EventLocationCmd();

            locationCmd.setId(ID);
            locationCmd.setName(NAME);
            locationCmd.setDescription(DESCRIPTION);
            locationCmd.setPhoto(PHOTO_CONTENT);

            String json = jsonb.toJson(locationCmd);

            serialize(json);
        }

        private void serialize(final String json) {
            DocumentContext ctx = JsonPath.parse(json);

            assertThat(ctx).jsonPathAsString("$.id").isEqualTo(ID);
            assertThat(ctx).jsonPathAsString("$.name").isEqualTo(NAME);
            assertThat(ctx).jsonPathAsString("$.description").isEqualTo(DESCRIPTION);
            assertThat(ctx).jsonPathAsString("$.photo").isEqualTo(PHOTO_CONTENT);
        }
    }
}
