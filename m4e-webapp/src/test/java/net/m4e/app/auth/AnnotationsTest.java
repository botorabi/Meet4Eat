/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.auth;

import java.util.*;

import javax.ws.rs.*;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    class AnnotatedClass {

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

        @DELETE
        @Path("severalPerms")
        @AuthRole(grantPermissions={"READ_SERVER_STATUS", "MODIFY_EVENT"})
        public void severalPerms() {}

        @DELETE
        @Path("onePerms")
        @AuthRole(grantPermissions={"READ_SERVER_STATUS"})
        public void onePerms() {}

        public void nonAnnotated() {}
    }

    /**
     * Class used for testing Annotations class.
     */
    class AnnotatedClassNoPath {
    }

    Annotations annotations;

    @BeforeEach
    void setup() {
        annotations = new Annotations();
    }

    @Test
    void getClassPath_existing_path() {
        String expectedpath = "/testpath";
        assertThat(annotations.getClassPath(AnnotatedClass.class)).isEqualTo(expectedpath);
    }

    @Test
    void getClassPath_no_path() {
        String expectedpath = "";
        assertThat(annotations.getClassPath(AnnotatedClassNoPath.class)).isEqualTo(expectedpath);
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
        assertThat(access.containsKey("DELETE")).isTrue();
        List expectedroles = Arrays.asList("READ_SERVER_STATUS", "MODIFY_EVENT");
        List<String /*roles*/> accessperms = access.get("DELETE");
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

    @Test
    void addGrantAccess_with_no_rules() {
        Map<String, Map<String, List<String>>> rules = new HashMap<>();
        String path = "";
        String accessMethod = "";
        String[] grants = new String[] {""};

        annotations.addGrantAccess(null, null, null, null);

        annotations.addGrantAccess(rules, null, accessMethod, grants);
        assertThat(rules.size()).isZero();
        annotations.addGrantAccess(rules, path, null, grants);
        assertThat(rules.size()).isZero();
        annotations.addGrantAccess(rules, null, null, grants);
        assertThat(rules.size()).isZero();
        annotations.addGrantAccess(rules, path, accessMethod, null);
        assertThat(rules.size()).isZero();
        annotations.addGrantAccess(rules, path, accessMethod, grants);
        assertThat(rules.size()).isEqualTo(1);
    }

    @Test
    void addGrantAccess_with_a_rule() {
        Map<String, Map<String, List<String>>> rules = new HashMap<>();
        String path = "path";
        String accessMethod = "GET";
        String[] grants = new String[3];
        grants[0] = "SUPER_HERO";
        grants[1] = "SUPER_EVIL";
        grants[2] = "FREE4All";

        annotations.addGrantAccess(rules, path, accessMethod, grants);
        assertThat(rules.containsKey("path")).isTrue();

        Map<String, List<String>> roles = rules.get("path");
        assertThat(roles.containsKey("GET")).isTrue();
        List expectedroles = Arrays.asList("SUPER_HERO", "SUPER_EVIL", "FREE4All");
        assertThat(roles.get("GET").size()).isEqualTo(expectedroles.size());
        assertThat(roles.get("GET").containsAll(expectedroles)).isTrue();
    }

    @Test
    void addGrantAccess_add_a_role() {
        Map<String, Map<String, List<String>>> rules = new HashMap<>();
        String path = "path";
        String accessMethod = "GET";
        String[] grants = new String[1];
        grants[0] = "ROLE1";

        annotations.addGrantAccess(rules, path, accessMethod, grants);
        assertThat(rules.containsKey("path")).isTrue();

        String[] addgrant = new String[1];
        addgrant[0] = "ROLE2";

        annotations.addGrantAccess(rules, path, accessMethod, addgrant);
        assertThat(rules.containsKey("path")).isTrue();

        Map<String, List<String>> roles = rules.get("path");
        assertThat(roles.containsKey("GET")).isTrue();
        List expectedroles = Arrays.asList("ROLE1", "ROLE2");
        assertThat(roles.get("GET").size()).isEqualTo(expectedroles.size());
        assertThat(roles.get("GET").containsAll(expectedroles)).isTrue();
    }
}
