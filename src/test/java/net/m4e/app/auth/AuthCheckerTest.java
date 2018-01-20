/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Test class for AuthChecker
 *
 * @author boto
 * Date of creation January 11, 2018
 */
class AuthCheckerTest {

    final String REQ_SCHEME = "https://mydomain.com";
    final String REQ_SCHEME_BAD = "htt//mydomain.com";
    final String REQ_BASE_PATH = "/root";
    final String REQ_BASE_PATH_INVALID = "/somebasepath";
    final String BEAN_BASE_PATH1 = "/bean1";
    final String BEAN_BASE_PATH2 = "/bean2";

    @Path(BEAN_BASE_PATH1)
    class Bean1 {
        @GET
        @Path("m1")
        @AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
        public void m1() {}

        @POST
        @Path("m2")
        @AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
        public void m2() {}

        @PUT
        @Path("m3/{id}")
        @AuthRole(grantRoles={AuthRole.USER_ROLE_ADMIN})
        public void m3() {}

        @PUT
        //! NOTE no path annotation here!
        @AuthRole(grantRoles={AuthRole.USER_ROLE_ADMIN})
        public void noPath() {}
    }

    @Path(BEAN_BASE_PATH2)
    class Bean2 {
        @PUT
        @Path("m1")
        @AuthRole(grantRoles={AuthRole.USER_ROLE_ADMIN})
        public void m1() {}

        @POST
        @Path("m2")
        @AuthRole(grantRoles={AuthRole.VIRT_ENDPOINT_CHECK})
        public void m2() {}
    }

    AuthChecker authChecker;

    private HttpServletRequest mockHttpRequest(String beanPath, String accessMethod) {
        StringBuffer accesspath = new StringBuffer(REQ_SCHEME + REQ_BASE_PATH + beanPath);
        HttpServletRequest request = mock(HttpServletRequest.class);
        Mockito.when(request.getRequestURL()).thenReturn(accesspath);
        Mockito.when(request.getMethod()).thenReturn(accessMethod);
        return request;
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);

        authChecker = new AuthChecker();
        List<Class<?>> beans = Arrays.asList(Bean1.class, Bean2.class);
        authChecker.initialize(beans);
    }

    @Test
    void initialize_no_beans() {
        AuthChecker checker = new AuthChecker();
        checker.initialize(null);
    }

    @Test
    void checkAccess_bad_url() {
        StringBuffer accesspath = new StringBuffer(REQ_SCHEME_BAD + REQ_BASE_PATH + BEAN_BASE_PATH1 + "/m3/myID");
        HttpServletRequest request = mock(HttpServletRequest.class);
        Mockito.when(request.getRequestURL()).thenReturn(accesspath);
        Mockito.when(request.getMethod()).thenReturn("PUT");

        List<String> hitroles = Arrays.asList(AuthRole.VIRT_ROLE_USER);
        assertThat(authChecker.checkAccess(REQ_BASE_PATH, request, hitroles)).isFalse();
    }

    @Test
    void checkAccess_without_grant_always_roles() {
        HttpServletRequest request = mockHttpRequest(BEAN_BASE_PATH1 + "/m1", "GET");

        List<String> hitroles = Arrays.asList(AuthRole.VIRT_ROLE_USER);
        assertThat(authChecker.checkAccess(REQ_BASE_PATH, request, hitroles)).isTrue();

        List<String> nohitroles = Arrays.asList(AuthRole.USER_ROLE_ADMIN);
        assertThat(authChecker.checkAccess(REQ_BASE_PATH, request, nohitroles)).isFalse();
    }

    @Test
    void checkAccess_with_grant_always_roles() {
        HttpServletRequest request = mockHttpRequest(BEAN_BASE_PATH1 + "/m2", "POST");

        List<String> adminroles = Arrays.asList(AuthRole.USER_ROLE_ADMIN);
        authChecker.setGrantAlwaysRoles(adminroles);
        assertThat(authChecker.getGrantAlwaysRoles().size()).isEqualTo(1);
        assertThat(authChecker.getGrantAlwaysRoles().containsAll(adminroles)).isTrue();

        List<String> hitroles = Arrays.asList(AuthRole.USER_ROLE_ADMIN);
        assertThat(authChecker.checkAccess(REQ_BASE_PATH, request, hitroles)).isTrue();

        List<String> nohitroles = Arrays.asList(AuthRole.VIRT_ROLE_GUEST);
        assertThat(authChecker.checkAccess(REQ_BASE_PATH, request, nohitroles)).isFalse();
    }

    @Test
    void checkAccess_no_path_hit() {
        HttpServletRequest request = mockHttpRequest(BEAN_BASE_PATH2 + "/m1", "PUT");

        List<String> nohitroles = Arrays.asList(AuthRole.USER_ROLE_ADMIN);
        assertThat(authChecker.checkAccess(REQ_BASE_PATH_INVALID, request, nohitroles)).isFalse();
    }

    @Test
    void checkAccess_complex_path() {
        HttpServletRequest request = mockHttpRequest(BEAN_BASE_PATH1 + "/m3/myID", "PUT");

        List<String> hitroles = Arrays.asList(AuthRole.VIRT_ROLE_USER);
        assertThat(authChecker.checkAccess(REQ_BASE_PATH, request, hitroles)).isFalse();

        List<String> nohitroles = Arrays.asList(AuthRole.USER_ROLE_ADMIN);
        assertThat(authChecker.checkAccess(REQ_BASE_PATH, request, nohitroles)).isTrue();
    }
}
