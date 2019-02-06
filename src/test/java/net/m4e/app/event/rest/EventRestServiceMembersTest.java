/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.rest;

import net.m4e.app.event.rest.comm.AddRemoveEventMember;
import net.m4e.app.user.business.UserEntity;
import net.m4e.common.GenericResponseResult;
import net.m4e.tests.ResponseAssertions;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import static org.mockito.Matchers.anyObject;

/**
 * @author boto
 * Date of creation February 22, 2018
 */
class EventRestServiceMembersTest extends EventRestServiceTestBase {

    @Nested
    class MemberAddRemove {

        @BeforeEach
        void setup() {
            setupUsers();
            setupEvents();
        }

        @Test
        void addMemberInvalidInput() {
            GenericResponseResult<AddRemoveEventMember> response = restService.addMember(null, null, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsNotAcceptable();

            response = restService.addMember(VALID_EVENT_ID, null, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsNotAcceptable();

            response = restService.addMember(null, VALID_MEMBER_ID, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsNotAcceptable();
        }

        @Test
        void addMemberInvalidMember() {
            GenericResponseResult<AddRemoveEventMember> response = restService.addMember(VALID_EVENT_ID, INVALID_MEMBER_ID, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsNotFound();
        }

        @Test
        void addMemberInactiveMember() {
            GenericResponseResult<AddRemoveEventMember> response = restService.addMember(VALID_EVENT_ID, INACTIVE_MEMBER_ID, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsNotFound();
        }


        @Test
        void addMemberInvalidEvent() {
            GenericResponseResult<AddRemoveEventMember> response = restService.addMember(INVALID_EVENT_ID, VALID_MEMBER_ID, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsNotFound();
        }

        @Test
        void addMemberInactiveEvent() {
            GenericResponseResult<AddRemoveEventMember> response = restService.addMember(INACTIVE_EVENT_ID, VALID_MEMBER_ID, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsNotFound();
        }

        @Test
        void addMemberNonPrivileged() {
            mockNonPrivilegedUser();

            GenericResponseResult<AddRemoveEventMember> response = restService.addMember(VALID_EVENT_ID, VALID_MEMBER_ID, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsUnauthorized();
        }

        @Test
        void problemAddingMember() throws Exception {
            mockPrivilegedUser();

            Mockito.doThrow(new Exception("Cannot add member!")).when(events).addMember(anyObject(), anyObject());

            GenericResponseResult<AddRemoveEventMember> response = restService.addMember(VALID_EVENT_ID, VALID_MEMBER_ID, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsNotAcceptable();
        }

        @Test
        void addMemberSuccess() {
            mockPrivilegedUser();

            GenericResponseResult<AddRemoveEventMember> response = restService.addMember(VALID_EVENT_ID, VALID_MEMBER_ID, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusOk()
                    .hasData();

            ResponseAssertions.assertThat(response.getData().getEventId()).isEqualTo(VALID_EVENT_ID.toString());

            ResponseAssertions.assertThat(response.getData().getMemberId()).isEqualTo(VALID_MEMBER_ID.toString());
        }

        @Test
        void removeMemberInvalidInput() {
            GenericResponseResult<AddRemoveEventMember> response = restService.removeMember(null, null, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsNotAcceptable();

            response = restService.removeMember(VALID_EVENT_ID, null, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsNotAcceptable();

            response = restService.removeMember(null, VALID_MEMBER_ID, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsNotAcceptable();
        }

        @Test
        void removeMemberInvalidMember() {
            GenericResponseResult<AddRemoveEventMember> response = restService.removeMember(VALID_EVENT_ID, INVALID_MEMBER_ID, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsNotFound();
        }

        @Test
        void removeMemberInactiveMember() {
            GenericResponseResult<AddRemoveEventMember> response = restService.removeMember(VALID_EVENT_ID, INACTIVE_MEMBER_ID, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsNotFound();
        }


        @Test
        void removeMemberInvalidEvent() {
            GenericResponseResult<AddRemoveEventMember> response = restService.removeMember(INVALID_EVENT_ID, VALID_MEMBER_ID, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsNotFound();
        }

        @Test
        void removeMemberInactiveEvent() {
            GenericResponseResult<AddRemoveEventMember> response = restService.removeMember(INACTIVE_EVENT_ID, VALID_MEMBER_ID, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsNotFound();
        }

        @Test
        void removeMemberNonPrivileged() {
            mockSessionUser(userMockUp.mockSomeUser());

            mockNonPrivilegedUser();

            GenericResponseResult<AddRemoveEventMember> response = restService.removeMember(VALID_EVENT_ID, VALID_MEMBER_ID, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsUnauthorized();
        }

        @Test
        void problemRemovingMember() throws Exception {
            mockSessionUser(userMockUp.mockAdminUser());

            mockPrivilegedUser();

            Mockito.doThrow(new Exception("Cannot remove member!")).when(events).removeMember(anyObject(), anyObject());

            GenericResponseResult<AddRemoveEventMember> response = restService.removeMember(VALID_EVENT_ID, VALID_MEMBER_ID, request);

            ResponseAssertions.assertThat(response)
                    .hasStatusNotOk()
                    .codeIsNotAcceptable();
        }

        @Test
        void selfRemovingMemberSuccess() {
            UserEntity sessionUser = userMockUp.mockSomeUser();
            sessionUser.setId(VALID_MEMBER_ID);
            mockSessionUser(sessionUser);

            mockNonPrivilegedUser();

            GenericResponseResult<AddRemoveEventMember> response = restService.removeMember(VALID_EVENT_ID, VALID_MEMBER_ID, request);

            ResponseAssertions.assertThat(response).hasStatusOk();
        }

        @Test
        void removeMemberSuccess() {
            mockSessionUser(userMockUp.mockAdminUser());

            mockPrivilegedUser();

            GenericResponseResult<AddRemoveEventMember> response = restService.removeMember(VALID_EVENT_ID, VALID_MEMBER_ID, request);

            ResponseAssertions.assertThat(response).hasStatusOk();
        }
    }
}
