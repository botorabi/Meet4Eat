/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.rest.comm;

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
class LoginCmdTest {

    Jsonb jsonb;

    @BeforeEach
    void setUp() {
        this.jsonb = JsonbBuilder.create();
    }

    @Test
    void deserialize() {
        LoginCmd loginCmd = new LoginCmd("login", "password");

        String json = jsonb.toJson(loginCmd);

        LoginCmd newLoginCmd = jsonb.fromJson(json, LoginCmd.class);

        Assertions.assertThat(newLoginCmd.getLogin()).isEqualTo("login");
        Assertions.assertThat(newLoginCmd.getPassword()).isEqualTo("password");
    }

    @Test
    void serialize() {
        LoginCmd loginCmd = new LoginCmd("login", "password");

        String json = jsonb.toJson(loginCmd);

        DocumentContext ctx = JsonPath.parse(json);
        assertThat(ctx).jsonPathAsString("$.login").isEqualTo("login");
        assertThat(ctx).jsonPathAsString("$.password").isEqualTo("password");
    }
}
