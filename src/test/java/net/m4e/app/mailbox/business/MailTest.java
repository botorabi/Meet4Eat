/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.mailbox.business;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;

import javax.json.bind.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

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
        void toJson_not_trashed() {
            MailEntity mailEntity = new MailEntity();
            Mail mail = new Mail(mailEntity, true, null);

            String json = jsonb.toJson(mail);

            Assertions.assertThat(json).doesNotContain("\"trashDate\"");
        }

        @Test
        void toJson_trashed() {
            MailEntity mailEntity = new MailEntity();
            Mail mail = new Mail(mailEntity, true, Instant.now().minus(5, ChronoUnit.SECONDS));

            String json = jsonb.toJson(mail);

            Assertions.assertThat(json).contains("\"trashDate\"");
        }
    }

}
