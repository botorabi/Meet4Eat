/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.auth;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for Annotations
 *
 * @author boto
 * Date of creation January 10, 2018
 */
class AnnotationsTest {

    /**
     * Class used for testing Annotations class.
     */
    @Path("/testpath")
    static class AnnotatedClass {

        @GET
        @Path("user")
        @AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
        public void methodUser() {}

        @POST
        @Path("guest")
        @AuthRole(grantRoles={AuthRole.VIRT_ROLE_GUEST})
        public void methodGuest() {}

        @PUT
        @Path("severalRoles")
        @AuthRole(grantRoles={AuthRole.VIRT_ROLE_GUEST, AuthRole.VIRT_ROLE_USER, AuthRole.USER_ROLE_ADMIN})
        public void methodSeveralRoles() {}

        @POST
        @Path("severalPerms")
        @AuthRole(grantPermissions={"READ_SERVER_STATUS", "MODIFY_EVENT"})
        public void severalPerms() {}
    }

    Annotations annotations;

    @BeforeEach
    void setup() {
        annotations = new Annotations();
    }

    @Test
    void getClassPath() {
        String expectedpath = "/testpath";
        assertThat(annotations.getClassPath(AnnotatedClass.class)).isEqualTo(expectedpath);
    }

    @Test
    void getMethodsPath() {

        Map<String /*method name*/, String /*path*/> paths = annotations.getMethodsPath(AnnotatedClass.class);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(paths.containsKey("methodUser")).isTrue();
        softly.assertThat(paths.getOrDefault("methodUser", "")).isEqualTo("user");
        softly.assertThat(paths.containsKey("methodGuest")).isTrue();
        softly.assertThat(paths.getOrDefault("methodGuest", "")).isEqualTo("guest");
        softly.assertThat(paths.containsKey("methodSeveralRoles")).isTrue();
        softly.assertThat(paths.getOrDefault("methodSeveralRoles", "")).isEqualTo("severalRoles");
        softly.assertAll();
    }

    @Test
    void getMethodsAuthRoles_existing() {
        Map<String /*path*/, Map<String /*access method*/, List<String /*roles*/>>> roles;
        Map<String /*access method*/, List<String /*roles*/>> access;

        roles = annotations.getMethodsAuthRoles(AnnotatedClass.class);
        assertThat(roles).isNotEqualTo(null);

        assertThat(roles.containsKey("user")).isTrue();
        access = roles.get("user");
        assertThat(access.containsKey("GET")).isTrue();
        assertThat(access.get("GET").contains(AuthRole.VIRT_ROLE_USER)).isTrue();

        assertThat(roles.containsKey("guest")).isTrue();
        access = roles.get("guest");
        assertThat(access.containsKey("POST")).isTrue();
        assertThat(access.get("POST").contains(AuthRole.VIRT_ROLE_GUEST)).isTrue();

        assertThat(roles.containsKey("severalRoles")).isTrue();
        access = roles.get("severalRoles");
        assertThat(access.containsKey("PUT")).isTrue();
        List expectedroles = Arrays.asList(AuthRole.VIRT_ROLE_GUEST, AuthRole.VIRT_ROLE_USER, AuthRole.USER_ROLE_ADMIN);
        List<String /*roles*/> accessroles = access.get("PUT");
        assertThat(accessroles.size()).isEqualTo(expectedroles.size());
        assertThat(accessroles.containsAll(expectedroles)).isTrue();
    }

    @Test
    void getMethodsAuthRoles_nonexisting() {
        Map<String /*path*/, Map<String /*access method*/, List<String /*roles*/>>> roles;

        roles = annotations.getMethodsAuthRoles(AnnotatedClass.class);
        assertThat(roles).isNotEqualTo(null);
        assertThat(roles.containsKey("severalPerms")).isFalse();
    }

    @Test
    void getMethodsAuthPermissions_existing() {
        Map<String /*path*/, Map<String /*access method*/, List<String /*perms*/>>> perms;
        Map<String /*access method*/, List<String /*roles*/>> access;

        perms = annotations.getMethodsAuthPermissions(AnnotatedClass.class);
        assertThat(perms).isNotEqualTo(null);

        assertThat(perms.containsKey("severalPerms")).isTrue();
        access = perms.get("severalPerms");
        assertThat(access.containsKey("POST")).isTrue();
        List expectedroles = Arrays.asList("READ_SERVER_STATUS", "MODIFY_EVENT");
        List<String /*roles*/> accessperms = access.get("POST");
        assertThat(accessperms.size()).isEqualTo(expectedroles.size());
        assertThat(accessperms.containsAll(expectedroles)).isTrue();
    }

    @Test
    void getMethodsAuthPermissions_nonexisting() {
        Map<String /*path*/, Map<String /*access method*/, List<String /*perms*/>>> perms;

        perms = annotations.getMethodsAuthPermissions(AnnotatedClass.class);
        assertThat(perms).isNotEqualTo(null);

        assertThat(perms.containsKey("user")).isFalse();
        assertThat(perms.containsKey("guest")).isFalse();
        assertThat(perms.containsKey("severalRoles")).isFalse();
    }
}
