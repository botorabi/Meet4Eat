/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.mailbox;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;

/**
 * @author ybroeker
 */
class MailTest {

    @Nested
    class ToJson {

        Jsonb jsonb;

        @BeforeEach
        void setUp() {
            jsonb = JsonbBuilder.create();


        }

        @Test
        void toJson() {
            MailEntity mailEntity = new MailEntity();
            Mail mail = new Mail(mailEntity, true, Instant.now().minus(5, ChronoUnit.SECONDS));

            String json = jsonb.toJson(mail);

            Assertions.assertThat(json).doesNotContain("\"trashed\"");
        }
    }

}
