/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.mailbox.rest.comm;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.m4e.app.mailbox.MailOperation;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.revinate.assertj.json.JsonPathAssert.assertThat;


/**
 * @author ybroeker
 */
class MailOperationInTest {


    @Test
    void deserialize() {
        String json = "{\"operation\":\"trash\"}";

        Jsonb jsonb = JsonbBuilder.create();

        MailOperationIn mailOperationIn = jsonb.fromJson(json, MailOperationIn.class);
        Assertions.assertThat(mailOperationIn.getOperation()).isEqualTo(MailOperation.TRASH);
    }

    @Test
    void serialize() {
        MailOperationIn mailOperationIn = new MailOperationIn(MailOperation.TRASH);


        Jsonb jsonb = JsonbBuilder.create();
        String json = jsonb.toJson(mailOperationIn);

        DocumentContext ctx = JsonPath.parse(json);
        assertThat(ctx).jsonPathAsString("$.operation").isEqualTo("trash");
    }


}
