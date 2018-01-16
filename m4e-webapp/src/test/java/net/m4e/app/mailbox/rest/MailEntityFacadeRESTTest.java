/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.mailbox.rest;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.m4e.app.auth.AuthorityConfig;
import net.m4e.app.mailbox.*;
import net.m4e.app.mailbox.rest.comm.MailCount;
import net.m4e.app.user.UserEntity;
import net.m4e.common.GenericResponseResult;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.mockito.*;

/**
 * @author ybroeker
 */
class MailEntityFacadeRESTTest {

    UserEntity userEntity;

    HttpServletRequest request;

    @BeforeEach
    void setUp() {
        userEntity = new UserEntity();
        userEntity.setId(42L);
        userEntity.setName("Uncle Bob");

        request = requestWithSessionUser(userEntity);
    }

    private HttpServletRequest requestWithSessionUser(UserEntity userEntity) {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpSession session = Mockito.mock(HttpSession.class);
        Mockito.when(session.getAttribute(AuthorityConfig.SESSION_ATTR_USER)).thenReturn(userEntity);
        Mockito.when(request.getSession()).thenReturn(session);
        return request;
    }


    @Nested
    class GetMailsTest {
        @Mock
        NewMailValidator newMailValidator;
        @Mock
        Mails mails;

        @BeforeEach
        void setUp() {
            MockitoAnnotations.initMocks(this);
        }

        @Test
        void noMails() {
            Mockito.when(mails.getMails(Mockito.eq(userEntity), Mockito.anyInt(), Mockito.anyInt())).thenReturn(Collections.emptyList());

            MailEntityFacadeREST mailEntityFacadeREST = new MailEntityFacadeREST(newMailValidator, mails);


            GenericResponseResult<List<Mail>> retrievedMails = mailEntityFacadeREST.getMails(0, 0, request);

            Assertions.assertThat(retrievedMails.getData()).isEmpty();
            Assertions.assertThat(retrievedMails.getCode()).isEqualTo(200);
            Assertions.assertThat(retrievedMails.getStatus()).isEqualTo(GenericResponseResult.STATUS_OK);
            Assertions.assertThat(retrievedMails.getDescription()).isNotEmpty();
        }

        @Test
        void oneMail() {
            Mockito.when(mails.getMails(Mockito.eq(userEntity), Mockito.anyInt(), Mockito.anyInt())).thenReturn(Collections.singletonList(new Mail(new MailEntity(),true, null)));

            MailEntityFacadeREST mailEntityFacadeREST = new MailEntityFacadeREST(newMailValidator, mails);


            GenericResponseResult<List<Mail>> retrievedMails = mailEntityFacadeREST.getMails(0, 0, request);

            Assertions.assertThat(retrievedMails.getData()).hasSize(1);
            Assertions.assertThat(retrievedMails.getCode()).isEqualTo(200);
            Assertions.assertThat(retrievedMails.getStatus()).isEqualTo(GenericResponseResult.STATUS_OK);
            Assertions.assertThat(retrievedMails.getDescription()).isNotEmpty();
        }

        @Test
        void noUser() {
            MailEntityFacadeREST mailEntityFacadeREST = new MailEntityFacadeREST(newMailValidator, mails);


            GenericResponseResult<List<Mail>> retrievedMails = mailEntityFacadeREST.getMails(0, 0, requestWithSessionUser(null));

            Assertions.assertThat(retrievedMails.getData()).isNull();
            Assertions.assertThat(retrievedMails.getCode()).isEqualTo(401);
            Assertions.assertThat(retrievedMails.getStatus()).isEqualTo(GenericResponseResult.STATUS_NOT_OK);
            Assertions.assertThat(retrievedMails.getDescription()).isNotEmpty();
        }

    }

    @Nested
    class GetCountTest {

        @Mock
        NewMailValidator newMailValidator;
        @Mock
        Mails mails;

        @BeforeEach
        void setUp() {
            MockitoAnnotations.initMocks(this);
        }

        @Test
        void getCount() {
            Mockito.when(mails.getCountTotalMails(Mockito.eq(userEntity))).thenReturn(5L);
            Mockito.when(mails.getCountUnreadMails(Mockito.eq(userEntity))).thenReturn(2L);

            MailEntityFacadeREST mailEntityFacadeREST = new MailEntityFacadeREST(newMailValidator, mails);


            GenericResponseResult<MailCount> retrievedMails = mailEntityFacadeREST.getCount(request);

            Assertions.assertThat(retrievedMails.getData()).isInstanceOf(MailCount.class);
            Assertions.assertThat(retrievedMails.getData().totalMails).isEqualTo(5);
            Assertions.assertThat(retrievedMails.getData().unreadMails).isEqualTo(2);
            Assertions.assertThat(retrievedMails.getCode()).isEqualTo(200);
            Assertions.assertThat(retrievedMails.getStatus()).isEqualTo(GenericResponseResult.STATUS_OK);
            Assertions.assertThat(retrievedMails.getDescription()).isNotEmpty();
        }

        @Test
        void noUser() {

            MailEntityFacadeREST mailEntityFacadeREST = new MailEntityFacadeREST(newMailValidator, mails);


            GenericResponseResult<MailCount> retrievedMails = mailEntityFacadeREST.getCount(requestWithSessionUser(null));

            Assertions.assertThat(retrievedMails.getData()).isNull();
            Assertions.assertThat(retrievedMails.getCode()).isEqualTo(GenericResponseResult.CODE_UNAUTHORIZED);
            Assertions.assertThat(retrievedMails.getStatus()).isEqualTo(GenericResponseResult.STATUS_NOT_OK);
            Assertions.assertThat(retrievedMails.getDescription()).isNotEmpty();
        }
    }

    @Nested
    class GetCountUnreadTest {
    }

    @Nested
    class SendTest {
    }

    @Nested
    class OperateTest {
    }
}
