/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.mailbox.rest;

import java.util.List;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.m4e.app.auth.AuthorityConfig;
import net.m4e.app.mailbox.business.*;
import net.m4e.app.mailbox.rest.comm.*;
import net.m4e.app.user.business.UserEntity;
import net.m4e.common.GenericResponseResult;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static net.m4e.tests.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author ybroeker
 */
class MailRestServiceTest {

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
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(AuthorityConfig.SESSION_ATTR_USER)).thenReturn(userEntity);
        when(request.getSession()).thenReturn(session);
        return request;
    }


    /**
     * Check, that {@link MailRestService} is creatable by container.
     */
    @Test
    void checkNoArgsConstructor() {
        new MailRestService();
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
            when(mails.getMails(eq(userEntity), anyInt(), anyInt())).thenReturn(emptyList());

            MailRestService mailRestService = new MailRestService(newMailValidator, mails);


            GenericResponseResult<List<Mail>> retrievedMails = mailRestService.getMails(0, 0, request);


            assertThat(retrievedMails).isOk()
                    .hasDescription()
                    .hasStatusOk()
                    .hasData(emptyList());
        }

        @Test
        void oneMail() {
            when(mails.getMails(eq(userEntity), anyInt(), anyInt())).thenReturn(singletonList(new Mail(new MailEntity(), true, null)));

            MailRestService mailRestService = new MailRestService(newMailValidator, mails);


            GenericResponseResult<List<Mail>> retrievedMails = mailRestService.getMails(0, 0, request);

            assertThat(retrievedMails.getData()).hasSize(1);
            assertThat(retrievedMails).isOk()
                    .hasDescription()
                    .hasStatusOk();
        }

        @Test
        void noUser() {
            MailRestService mailRestService = new MailRestService(newMailValidator, mails);


            GenericResponseResult<List<Mail>> retrievedMails = mailRestService.getMails(0, 0, requestWithSessionUser(null));

            assertThat(retrievedMails).isUnauthorized()
                    .hasStatusNotOk()
                    .hasDescription()
                    .hasNoData();
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
            when(mails.getCountTotalMails(eq(userEntity))).thenReturn(5L);
            when(mails.getCountUnreadMails(eq(userEntity))).thenReturn(2L);

            MailRestService mailRestService = new MailRestService(newMailValidator, mails);


            GenericResponseResult<MailCount> retrievedMails = mailRestService.getCount(request);


            assertThat(retrievedMails).isOk()
                    .hasDescription()
                    .hasStatusOk()
                    .hasDataOfType(MailCount.class)
            ;
            assertThat(retrievedMails.getData().getTotalMails()).isEqualTo(5);
            assertThat(retrievedMails.getData().getUnreadMails()).isEqualTo(2);
        }

        @Test
        void noUser() {

            MailRestService mailRestService = new MailRestService(newMailValidator, mails);


            GenericResponseResult<MailCount> retrievedMails = mailRestService.getCount(requestWithSessionUser(null));

            assertThat(retrievedMails).isUnauthorized()
                    .hasStatusNotOk()
                    .hasDescription()
                    .hasNoData();
        }
    }

    @Nested
    class GetCountUnreadTest {
        @Mock
        NewMailValidator newMailValidator;
        @Mock
        Mails mails;

        @BeforeEach
        void setUp() {
            MockitoAnnotations.initMocks(this);
        }

        @Test
        void getUnread() {
            when(mails.getCountTotalMails(eq(userEntity))).thenReturn(5L);
            when(mails.getCountUnreadMails(eq(userEntity))).thenReturn(2L);

            MailRestService mailRestService = new MailRestService(newMailValidator, mails);

            GenericResponseResult<UnreadMailCount> countUnread = mailRestService.getCountUnread(request);

            assertThat(countUnread).isOk()
                    .hasDescription()
                    .hasStatusOk()
                    .hasDataOfType(UnreadMailCount.class)
            ;
            assertThat(countUnread.getData().getUnreadMails()).isEqualTo(2);
        }


        @Test
        void noUser() {
            MailRestService mailRestService = new MailRestService(newMailValidator, mails);

            GenericResponseResult<UnreadMailCount> countUnread = mailRestService.getCountUnread(requestWithSessionUser(null));

            assertThat(countUnread).isUnauthorized()
                    .hasStatusNotOk()
                    .hasDescription()
                    .hasNoData();
        }
    }

    @Nested
    class SendTest {
        @Mock
        NewMailValidator newMailValidator;
        @Mock
        Mails mails;

        @BeforeEach
        void setUp() {
            MockitoAnnotations.initMocks(this);
        }

        @Test
        void noUser() {
            MailRestService mailRestService = new MailRestService(newMailValidator, mails);

            NewMailCmd newMailCmd = new NewMailCmd("Subject", "Content", 84L);

            GenericResponseResult<Void> response = mailRestService.send(newMailCmd, requestWithSessionUser(null));

            assertThat(response).isUnauthorized()
                    .hasStatusNotOk()
                    .hasDescription()
                    .hasNoData();
        }

        @Test
        void withUser() throws Exception {
            when(newMailValidator.validateNewEntityInput(any(), any())).then(createMailEntity());
            MailRestService mailRestService = new MailRestService(newMailValidator, mails);

            NewMailCmd newMailCmd = new NewMailCmd("Subject", "Content", 84L);


            GenericResponseResult<Void> response = mailRestService.send(newMailCmd, requestWithSessionUser(userEntity));


            verify(mails).createMail(argThat(matchesMail("Subject", "Content", userEntity.getId(), 84L)));

            assertThat(response).isOk()
                    .hasStatusOk()
                    .hasDescription()
                    .hasNoData();
        }

        @Test
        void invalidMail() throws Exception {
            when(newMailValidator.validateNewEntityInput(any(), any())).thenThrow(new Exception("An Error..."));
            MailRestService mailRestService = new MailRestService(newMailValidator, mails);

            NewMailCmd newMailCmd = new NewMailCmd("", "", 84L);


            GenericResponseResult<Void> response = mailRestService.send(newMailCmd, requestWithSessionUser(userEntity));


            verify(mails, times(0)).createMail(argThat(matchesMail("Subject", "Content", userEntity.getId(), 84L)));

            assertThat(response).isClientError()
                    .hasStatusNotOk()
                    .hasDescription("An Error...")
                    .hasNoData();
        }

        @Test
        void withError() throws Exception {
            when(newMailValidator.validateNewEntityInput(any(), any())).then(createMailEntity());
            Mockito.doThrow(new RuntimeException("An Error...")).when(mails).createMail(any());

            MailRestService mailRestService = new MailRestService(newMailValidator, mails);

            NewMailCmd newMailCmd = new NewMailCmd("", "", 84L);

            GenericResponseResult<Void> response = mailRestService.send(newMailCmd, requestWithSessionUser(userEntity));

            assertThat(response).isServerError()
                    .hasStatusNotOk()
                    .hasDescription("Problem occurred while sending mail")
                    .hasNoData();
        }

        Answer<MailEntity> createMailEntity() {
            return new Answer<MailEntity>() {
                @Override
                public MailEntity answer(final InvocationOnMock args) {
                    return new MailEntity(args.getArgumentAt(1, UserEntity.class).getId(),
                            "",
                            args.getArgumentAt(0, NewMailCmd.class).getReceiverId(),
                            "",
                            0L,
                            args.getArgumentAt(0, NewMailCmd.class).getSubject(),
                            args.getArgumentAt(0, NewMailCmd.class).getContent());
                }
            };
        }

        Matcher<MailEntity> matchesMail(final String subject, final String content, final Long senderId, final Long receiverId) {
            return new TypeSafeMatcher<MailEntity>() {
                public boolean matchesSafely(MailEntity item) {
                    return content.equals(item.getContent())
                            && subject.equals(item.getSubject())
                            && senderId.equals(item.getSenderId())
                            && receiverId.equals(item.getReceiverId());
                }

                public void describeTo(Description description) {
                    description.appendText(
                            String.format("a Mail with subject <%s>, content <%s>, from <%s> to <%s>", subject, content, senderId, receiverId));
                }
            };
        }
    }

    @Nested
    class OperateTest {
        @Mock
        NewMailValidator newMailValidator;
        @Mock
        Mails mails;

        @BeforeEach
        void setUp() {
            MockitoAnnotations.initMocks(this);
        }

        @ParameterizedTest
        @ArgumentsSource(OperationProvider.class)
        void withUser(String operationString, MailOperation op) throws Exception {
            when(mails.performMailOperation(Matchers.eq(42L), anyLong(), anyObject()))
                    .then(i -> new ExcecutedMailOperation(
                            i.getArgumentAt(2, MailOperation.class),
                            i.getArgumentAt(1, Long.class).toString()));

            MailRestService mailRestService = new MailRestService(newMailValidator, mails);

            GenericResponseResult<ExcecutedMailOperation> excecutedMailOperation = mailRestService.operate(42L, new MailOperationCmd(op), request);

            verify(mails).performMailOperation(42L, 42L, op);

            assertThat(excecutedMailOperation).is200()
                    .hasStatusOk()
                    .hasDescription();
            assertThat(excecutedMailOperation).hasDataOfType(ExcecutedMailOperation.class);
            assertThat(excecutedMailOperation.getData().getId()).isEqualTo("42");
            assertThat(excecutedMailOperation.getData().getOperation()).isEqualTo(op);
        }

        @ParameterizedTest
        @ArgumentsSource(OperationProvider.class)
        void noUser(String operationString, MailOperation op) {
            MailRestService mailRestService = new MailRestService(newMailValidator, mails);

            GenericResponseResult<ExcecutedMailOperation> excecutedMailOperation = mailRestService.operate(42L, new MailOperationCmd(op), requestWithSessionUser(null));

            assertThat(excecutedMailOperation).hasNoData()
                    .isUnauthorized()
                    .hasStatusNotOk()
                    .hasDescription();
        }

        @ParameterizedTest
        @ArgumentsSource(OperationProvider.class)
        void withError(String operationString, MailOperation op) throws Exception {
            when(mails.performMailOperation(Matchers.eq(42L), anyLong(), anyObject())).thenThrow(new Exception("An Error..."));

            MailRestService mailRestService = new MailRestService(newMailValidator, mails);

            GenericResponseResult<ExcecutedMailOperation> excecutedMailOperation = mailRestService.operate(42L, new MailOperationCmd(op), request);

            assertThat(excecutedMailOperation).isClientError()
                    .hasNoData()
                    .hasStatusNotOk()
                    .hasDescription();
        }
    }

    static class OperationProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) {
            return Stream.of(
                    Arguments.of("trash", MailOperation.TRASH),
                    Arguments.of("read", MailOperation.READ),
                    Arguments.of("unread", MailOperation.UNREAD),
                    Arguments.of("untrash", MailOperation.UNTRASH)

            );
        }
    }
}
