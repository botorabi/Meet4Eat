/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.auth;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for AuthAccessRuleChecker
 *
 * @author boto
 * Date of creation January 11, 2018
 */
class AuthAccessRuleCheckerTest {

    @Test
    void construct() {
        String path = "/path";
        AuthAccessRuleChecker checker = new AuthAccessRuleChecker(path);
        assertThat(checker.getResourcePath()).isEqualTo(path);
        assertThat(checker.getResourcePathRegexp()).isEqualTo(path);
        assertThat(checker.getAccessRules()).isNotNull();
    }

    @Test
    void construct_fix_path() {
        String path = "/fixpath";
        AuthAccessRuleChecker checker = new AuthAccessRuleChecker(path);
        assertThat(checker.getResourcePathRegexp()).isEqualTo(path);
    }

    @Test
    void construct_complex_path() {
        String path = "/path/{id}/{name}";
        String expectedpath = "/path/(.+)/(.+)";
        AuthAccessRuleChecker checker = new AuthAccessRuleChecker(path);
        assertThat(checker.getResourcePathRegexp()).isEqualTo(expectedpath);
    }

    @Test
    void construct_bad_complex_path() {
        String path1 = "/path/{id";
        AuthAccessRuleChecker checker1 = new AuthAccessRuleChecker(path1);
        assertThat(checker1.getResourcePathRegexp()).isNull();

        String path2 = "/path/id}";
        AuthAccessRuleChecker checker2 = new AuthAccessRuleChecker(path2);
        assertThat(checker2.getResourcePathRegexp()).isNull();

        String path3 = "/path/){id}";
        AuthAccessRuleChecker checker3 = new AuthAccessRuleChecker(path3);
        assertThat(checker3.getResourcePathRegexp()).isNull();
    }

    @Test
    void addAccessRoles() {
        String path = "/fixpath";
        AuthAccessRuleChecker checker = new AuthAccessRuleChecker(path);
        List<String> accessroles = Arrays.asList("ADMIN");
        List<String> moreaccessroles = Arrays.asList("NORMALOS");

        assertThat(checker.addAccessRoles(null, null)).isFalse();
        assertThat(checker.addAccessRoles(null, accessroles)).isFalse();
        assertThat(checker.addAccessRoles(path, null)).isFalse();

        assertThat(checker.addAccessRoles("GET", accessroles)).isTrue();
        assertThat(checker.addAccessRoles("GET", moreaccessroles)).isTrue();
        Map<String /*access*/, List<String /*role*/>> rules = checker.getAccessRules();
        assertThat(rules.containsKey("GET")).isTrue();

        List<String> allroles = new ArrayList<>();
        allroles.addAll(accessroles);
        allroles.addAll(moreaccessroles);
        assertThat(rules.get("GET").size()).isEqualTo(allroles.size());
        assertThat(rules.get("GET").containsAll(allroles)).isTrue();
    }

    @Test
    void checkFixPath_hit() {
        String path = "/fixpath";
        String accessmethod = "GET";
        AuthAccessRuleChecker checker = new AuthAccessRuleChecker(path);
        List<String> accessroles = Arrays.asList("ADMIN", "NORMALOS");
        assertThat(checker.addAccessRoles(accessmethod, accessroles)).isTrue();

        List<String> checkexistingroles = Arrays.asList("ADMIN");
        List<String> checknonexistingroles = Arrays.asList("EVIL");

        assertThat(checker.checkFixPath(null, null, null)).isFalse();
        assertThat(checker.checkFixPath(path, null, null)).isFalse();
        assertThat(checker.checkFixPath(null, accessmethod, null)).isFalse();
        assertThat(checker.checkFixPath(path, accessmethod, null)).isFalse();

        assertThat(checker.checkFixPath(path, accessmethod, checkexistingroles)).isTrue();
        assertThat(checker.checkFixPath(path, accessmethod, checknonexistingroles)).isFalse();
    }

    @Test
    void checkFixPath_nohit() {
        String path = "/fixpath";
        String nohitpath = "/otherfixpath";
        String accessmethod = "GET";
        AuthAccessRuleChecker checker = new AuthAccessRuleChecker(path);
        List<String> accessroles = Arrays.asList("ADMIN", "NORMALOS");
        assertThat(checker.addAccessRoles(accessmethod, accessroles)).isTrue();

        List<String> checkexistingroles = Arrays.asList("ADMIN");
        List<String> checknonexistingroles = Arrays.asList("EVIL");

        assertThat(checker.checkFixPath(null, null, null)).isFalse();
        assertThat(checker.checkFixPath(nohitpath, null, null)).isFalse();
        assertThat(checker.checkFixPath(null, accessmethod, null)).isFalse();
        assertThat(checker.checkFixPath(nohitpath, accessmethod, null)).isFalse();

        assertThat(checker.checkFixPath(nohitpath, accessmethod, checkexistingroles)).isFalse();
        assertThat(checker.checkFixPath(nohitpath, accessmethod, checknonexistingroles)).isFalse();
    }

    @Test
    void checkComplexPath_hit() {
        String path = "/path/{id}";
        String complexpath = "/path/(.+)";
        String accessmethod = "POST";
        AuthAccessRuleChecker checker = new AuthAccessRuleChecker(path);
        List<String> accessroles = Arrays.asList("ADMIN", "NORMALOS");
        assertThat(checker.addAccessRoles(accessmethod, accessroles)).isTrue();

        List<String> checkexistingroles = Arrays.asList("ADMIN");
        List<String> checknonexistingroles = Arrays.asList("EVIL");

        assertThat(checker.checkComplexPath(null, null, null)).isFalse();
        assertThat(checker.checkComplexPath(complexpath, null, null)).isFalse();
        assertThat(checker.checkComplexPath(null, accessmethod, null)).isFalse();
        assertThat(checker.checkComplexPath(complexpath, accessmethod, null)).isFalse();

        assertThat(checker.checkComplexPath(complexpath, accessmethod, checkexistingroles)).isTrue();
        assertThat(checker.checkComplexPath(complexpath, accessmethod, checknonexistingroles)).isFalse();
    }

    @Test
    void checkComplexPath_nohit() {
        String path = "/path/{id}";
        String complexpath = "/otherpath/(.+)";
        String accessmethod = "POST";
        AuthAccessRuleChecker checker = new AuthAccessRuleChecker(path);
        List<String> accessroles = Arrays.asList("ADMIN", "NORMALOS");
        assertThat(checker.addAccessRoles(accessmethod, accessroles)).isTrue();

        List<String> checkexistingroles = Arrays.asList("ADMIN");
        List<String> checknonexistingroles = Arrays.asList("EVIL");

        assertThat(checker.checkComplexPath(complexpath, accessmethod, checkexistingroles)).isFalse();
        assertThat(checker.checkComplexPath(complexpath, accessmethod, checknonexistingroles)).isFalse();
    }

    @Test
    void checkRoles_nocheck_guest() {
        List<String> accessrole_nocheck = Arrays.asList(AuthRole.VIRT_ENDPOINT_CHECK);
        List<String> accessrole_guest = Arrays.asList(AuthRole.VIRT_ROLE_GUEST);
        String path = "/path";
        String accessmethod = "PUT";

        List<String> checkrole = Arrays.asList("SOME_ROLE");

        AuthAccessRuleChecker checker = new AuthAccessRuleChecker(path);

        assertThat(checker.checkRoles(null, null)).isFalse();
        assertThat(checker.checkRoles(accessmethod, null)).isFalse();
        assertThat(checker.checkRoles(null, accessrole_guest)).isFalse();

        AuthAccessRuleChecker checker1 = new AuthAccessRuleChecker(path);
        assertThat(checker1.addAccessRoles(accessmethod, accessrole_nocheck)).isTrue();
        assertThat(checker1.checkRoles(accessmethod, checkrole)).isTrue();

        AuthAccessRuleChecker checker2 = new AuthAccessRuleChecker(path);
        assertThat(checker2.addAccessRoles(accessmethod, accessrole_guest)).isTrue();
        assertThat(checker2.checkRoles(accessmethod, checkrole)).isTrue();

        AuthAccessRuleChecker checker3 = new AuthAccessRuleChecker(path);
        List<String> accesroles_both = new ArrayList<>();
        accesroles_both.addAll(accessrole_guest);
        accesroles_both.addAll(accessrole_nocheck);
        assertThat(checker3.addAccessRoles(accessmethod, accesroles_both)).isTrue();
        assertThat(checker3.checkRoles(accessmethod, checkrole)).isTrue();
    }

    @Test
    void checkRoles_no_method_hit() {
        List<String> accesroles = Arrays.asList(AuthRole.VIRT_ROLE_USER);
        String path = "/path";
        String accessmethod = "DELETE";
        String accessmethod_nohit = "PUT";
        AuthAccessRuleChecker checker = new AuthAccessRuleChecker(path);

        assertThat(checker.addAccessRoles(accessmethod, accesroles)).isTrue();
        assertThat(checker.checkRoles(accessmethod_nohit, accesroles)).isFalse();
    }

    @Test
    void toString_fix_path() {
        List<String> accesroles = Arrays.asList(AuthRole.VIRT_ROLE_USER);
        String path = "/path";
        String accessmethod = "GET";
        AuthAccessRuleChecker checker = new AuthAccessRuleChecker(path);
        assertThat(checker.addAccessRoles(accessmethod, accesroles)).isTrue();

        String str = checker.toString();
        assertThat(str).isNotNull();
        assertThat(str).isNotEmpty();
    }

    @Test
    void toString_complex_path() {
        List<String> accesroles = Arrays.asList(AuthRole.VIRT_ROLE_USER);
        String path = "/path/{myID}";
        String accessmethod = "POST";
        AuthAccessRuleChecker checker = new AuthAccessRuleChecker(path);
        assertThat(checker.addAccessRoles(accessmethod, accesroles)).isTrue();

        String str = checker.toString();
        assertThat(str).isNotNull();
        assertThat(str).isNotEmpty();
    }
}
