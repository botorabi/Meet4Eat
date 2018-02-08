/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.rest.comm;

import com.jayway.jsonpath.*;
import net.m4e.app.auth.AuthRole;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;

import javax.json.bind.*;

import java.util.*;

import static com.revinate.assertj.json.JsonPathAssert.assertThat;

/**
 * @author ybroeker, boto
 */
class CmdTests {

    Jsonb jsonb;

    @BeforeEach
    void setUp() {
        this.jsonb = JsonbBuilder.create();
    }

    @Nested
    class LoginCmdTests {
        @Test
        void deserialize() {
            LoginCmd loginCmd = new LoginCmd("login", "password");

            String json = jsonb.toJson(loginCmd);

            LoginCmd newLoginCmd = jsonb.fromJson(json, LoginCmd.class);

            Assertions.assertThat(newLoginCmd.getLogin()).isEqualTo("login");
            Assertions.assertThat(newLoginCmd.getPassword()).isEqualTo("password");
        }

        @Test
        void serializeByConstructor() {
            LoginCmd loginCmd = new LoginCmd("login", "password");

            String json = jsonb.toJson(loginCmd);

            serialize(json);
        }

        @Test
        void serializeBySetters() {
            LoginCmd loginCmd = new LoginCmd();
            loginCmd.setLogin("login");
            loginCmd.setPassword("password");

            String json = jsonb.toJson(loginCmd);

            serialize(json);
        }

        private void serialize(final String json) {
            DocumentContext ctx = JsonPath.parse(json);

            assertThat(ctx).jsonPathAsString("$.login").isEqualTo("login");
            assertThat(ctx).jsonPathAsString("$.password").isEqualTo("password");
        }
    }

    @Nested
    class PerformPasswordResetCmdTest {
        @Test
        void deserialize() {
            PerformPasswordResetCmd passwordResetCmd = new PerformPasswordResetCmd("password");

            String json = jsonb.toJson(passwordResetCmd);

            PerformPasswordResetCmd newCmd = jsonb.fromJson(json, PerformPasswordResetCmd.class);

            Assertions.assertThat(newCmd.getPassword()).isEqualTo("password");
        }

        @Test
        void serializeByConstructor() {
            PerformPasswordResetCmd passwordResetCmd = new PerformPasswordResetCmd("password");

            String json = jsonb.toJson(passwordResetCmd);

            serialize(json);
        }

        @Test
        void serializeBySetters() {
            PerformPasswordResetCmd passwordResetCmd = new PerformPasswordResetCmd();
            passwordResetCmd.setPassword("password");

            String json = jsonb.toJson(passwordResetCmd);

            serialize(json);
        }

        private void serialize(final String json) {
            DocumentContext ctx = JsonPath.parse(json);

            assertThat(ctx).jsonPathAsString("$.password").isEqualTo("password");
        }
    }

    @Nested
    class RequestPasswordResetCmdTest {
        @Test
        void deserialize() {
            RequestPasswordResetCmd passwordResetCmd = new RequestPasswordResetCmd("email");

            String json = jsonb.toJson(passwordResetCmd);

            RequestPasswordResetCmd newCmd = jsonb.fromJson(json, RequestPasswordResetCmd.class);

            Assertions.assertThat(newCmd.getEmail()).isEqualTo("email");
        }

        @Test
        void serializeByConstructor() {
            RequestPasswordResetCmd passwordResetCmd = new RequestPasswordResetCmd("email");

            String json = jsonb.toJson(passwordResetCmd);

            serialize(json);
        }

        @Test
        void serializeBySetters() {
            RequestPasswordResetCmd passwordResetCmd = new RequestPasswordResetCmd();
            passwordResetCmd.setEmail("email");

            String json = jsonb.toJson(passwordResetCmd);

            serialize(json);
        }

        private void serialize(final String json) {
            DocumentContext ctx = JsonPath.parse(json);

            assertThat(ctx).jsonPathAsString("$.email").isEqualTo("email");
        }
    }

    @Nested
    class UserCmdTest {

        @Test
        void deserialize() {
            UserCmd userCmd = createEntityByConstruction();

            String json = jsonb.toJson(userCmd);

            RequestPasswordResetCmd newCmd = jsonb.fromJson(json, RequestPasswordResetCmd.class);

            Assertions.assertThat(newCmd.getEmail()).isEqualTo("email");
        }

        @Test
        void serializeUsingConstructor() {
            UserCmd userCmd = createEntityByConstruction();

            String json = jsonb.toJson(userCmd);

            serialize(json);
        }

        @Test
        void serializeUsingSetters() {
            UserCmd userCmd = createEntityBySetters();

            String json = jsonb.toJson(userCmd);

            serialize(json);
        }

        private void serialize(final String json) {
            DocumentContext ctx = JsonPath.parse(json);

            assertThat(ctx).jsonPathAsString("$.login").isEqualTo("login");
            assertThat(ctx).jsonPathAsString("$.password").isEqualTo("password");
            assertThat(ctx).jsonPathAsString("$.name").isEqualTo("name");
            assertThat(ctx).jsonPathAsString("$.email").isEqualTo("email");
            assertThat(ctx).jsonPathAsString("$.photo").isEqualTo("photo");
            //! TODO make this work!
            //assertThat(ctx).jsonPathAsListOf("$.roles[*]", String.class).contains(AuthRole.USER_ROLE_ADMIN);
        }

        @NotNull
        private UserCmd createEntityByConstruction() {
            List<String> roles = Arrays.asList(AuthRole.USER_ROLE_ADMIN);
            return new UserCmd("login",
                    "password",
                    "name",
                    "email",
                    "photo",
                    roles);
        }

        @NotNull
        private UserCmd createEntityBySetters() {
            UserCmd userCmd = new UserCmd();
            userCmd.setLogin("login");
            userCmd.setPassword("password");
            userCmd.setName("name");
            userCmd.setEmail("email");
            userCmd.setPhoto("photo");
            userCmd.setRoles(Arrays.asList(AuthRole.USER_ROLE_ADMIN));
            return userCmd;
        }
    }
}
