/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.mailbox.rest.cmds;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;

import static com.revinate.assertj.json.JsonPathAssert.assertThat;

/**
 * @author ybroeker
 */
class NewMailCmdTest {

    @Nested
    class JsonTest {
        Jsonb jsonb;

        @BeforeEach
        void setUp() {
            this.jsonb = JsonbBuilder.create();
        }

        @Test
        void deserialize() {
//            String json = "{\"subject\":\"Betreff\", \"content\":\"ValidContent\", \"receiverId\":52}";
            NewMailCmd newMailCmd = new NewMailCmd("Betreff", "ValidContent", 52L);

            String json = jsonb.toJson(newMailCmd);



            NewMailCmd newMail = jsonb.fromJson(json, NewMailCmd.class);

            Assertions.assertThat(newMail.getSubject()).isEqualTo("Betreff");
            Assertions.assertThat(newMail.getContent()).isEqualTo("ValidContent");
            Assertions.assertThat(newMail.getReceiverId()).isEqualTo(52);
        }

        @Test
        void serialize() {
            NewMailCmd newMailCmd = new NewMailCmd("Betreff", "ValidContent", 52L);

            String json = jsonb.toJson(newMailCmd);

            DocumentContext ctx = JsonPath.parse(json);
            assertThat(ctx).jsonPathAsString("$.subject").isEqualTo("Betreff");
            assertThat(ctx).jsonPathAsString("$.content").isEqualTo("ValidContent");
            assertThat(ctx).jsonPathAsInteger("$.receiverId").isEqualTo(52);
        }
    }


}
