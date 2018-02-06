/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.business;

import net.m4e.app.auth.*;
import net.m4e.app.resources.StatusEntity;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;

/**
 * @author boto
 * Date of creation February 6, 2018
 */
class UsersRolesTest extends UsersTestBase {

    @Test
    void availableUserRoles() {
        List<String> roles = users.getAvailableUserRoles();
        assertThat(roles).contains(AuthRole.USER_ROLE_ADMIN, AuthRole.USER_ROLE_MODERATOR);
    }

    @Nested
    class UserAccessPrivilege {

        @Test
        void invalidInputs() {
            UserEntity user = createUserWithRoles(Arrays.asList());
            StatusEntity resourceStatus = new StatusEntity();

            assertThatIllegalArgumentException().isThrownBy(() ->users.userIsOwnerOrAdmin(user, null));
            assertThatIllegalArgumentException().isThrownBy(() ->users.userIsOwnerOrAdmin(null, resourceStatus));
            assertThatIllegalArgumentException().isThrownBy(() ->users.userIsOwnerOrAdmin(null, null));
        }

        @Test
        void addEmptyRoleName() {
            UserEntity user = createUser();

            users.addUserRoles(user, Arrays.asList(AuthRole.USER_ROLE_ADMIN, ""));

            assertThat(user.getRoles().size()).isEqualTo(1);
        }

        @Test
        void addRoleInternalError() {
            UserEntity user = createUser();

            Mockito.when(entities.findByField(eq(RoleEntity.class), eq("name"), anyString())).thenReturn(Arrays.asList());

            users.addUserRoles(user, Arrays.asList(AuthRole.USER_ROLE_ADMIN));

            assertThat(user.getRoles()).isNull();
        }

        @Test
        void asAdmin() {
            UserEntity admin = createUserWithRoles(Arrays.asList(AuthRole.USER_ROLE_ADMIN));
            StatusEntity resourceStatus = new StatusEntity();

            assertThat(users.userIsOwnerOrAdmin(admin, resourceStatus)).isTrue();
        }

        @Test
        void asModerator() {
            UserEntity moderator = createUserWithRoles(Arrays.asList(AuthRole.USER_ROLE_MODERATOR));
            StatusEntity resourceStatus = new StatusEntity();

            assertThat(users.userIsOwnerOrAdmin(moderator, resourceStatus)).isFalse();
        }

        @Test
        void asOwner() {
            final Long ID_OWNER = 100L;

            UserEntity owner = createUserWithRoles(Collections.emptyList());
            owner.setId(ID_OWNER);

            StatusEntity resourceStatus = new StatusEntity();
            resourceStatus.setIdOwner(ID_OWNER);

            assertThat(users.userIsOwnerOrAdmin(owner, resourceStatus)).isTrue();
        }

        @Test
        void asOther() {
            UserEntity otherUser = createUserWithRoles(Collections.emptyList());
            StatusEntity resourceStatus = new StatusEntity();

            assertThat(users.userIsOwnerOrAdmin(otherUser, resourceStatus)).isFalse();
        }
    }

    @Nested
    class CheckUserRoles {
        @Test
        void noRoles() {
            UserEntity user = createUserWithRoles(Collections.emptyList());

            assertThat(users.checkUserRoles(user, Collections.emptyList())).isFalse();
            assertThat(users.checkUserRoles(user, Arrays.asList(AuthRole.USER_ROLE_ADMIN))).isFalse();

            user = createUserWithRoles(Arrays.asList(AuthRole.USER_ROLE_ADMIN));
            assertThat(users.checkUserRoles(user, Collections.emptyList())).isFalse();
        }

        @Test
        void withRoles() {
            UserEntity user = createUserWithRoles(Arrays.asList(AuthRole.USER_ROLE_ADMIN, AuthRole.USER_ROLE_MODERATOR));

            assertThat(users.checkUserRoles(user, Arrays.asList(AuthRole.USER_ROLE_ADMIN))).isTrue();
        }
    }

    @Nested
    class AdaptUserRoles {

        @Test
        void noUserPrivilege() {
            List<RoleEntity> requestedRoles = createRoleEntities(Collections.emptyList());
            UserEntity user = createUserWithRoles(Collections.emptyList());

            Collection<RoleEntity> adaptedRoles = users.adaptRequestedRoles(user, requestedRoles);

            assertThat(adaptedRoles).isEmpty();

            requestedRoles = createRoleEntities(Arrays.asList(AuthRole.USER_ROLE_ADMIN));
            adaptedRoles = users.adaptRequestedRoles(user, requestedRoles);

            assertThat(adaptedRoles).isEmpty();
        }

        @Test
        void noRequestedRoles() {
            List<RoleEntity> requestedRoles = null;
            UserEntity user = createUserWithRoles(Collections.emptyList());

            Collection<RoleEntity> adaptedRoles = users.adaptRequestedRoles(user, requestedRoles);

            assertThat(adaptedRoles).isEmpty();
        }

        @Test
        void invalidRoleName() {
            List<RoleEntity> requestedRoles = createRoleEntities(Arrays.asList("INVALID_ROLE"));
            UserEntity user = createUserWithRoles(Collections.emptyList());

            Collection<RoleEntity> adaptedRoles = users.adaptRequestedRoles(user, requestedRoles);

            assertThat(adaptedRoles).isEmpty();
        }

        @Test
        void invalidRoleId() {
            RoleEntity invalidRole = new RoleEntity();
            invalidRole.setName(AuthRole.USER_ROLE_ADMIN);
            invalidRole.setId(null);

            List<RoleEntity> requestedRoles = Arrays.asList(invalidRole);
            UserEntity user = createUserWithRoles(Collections.emptyList());

            Collection<RoleEntity> adaptedRoles = users.adaptRequestedRoles(user, requestedRoles);

            assertThat(adaptedRoles).isEmpty();
        }

        @Test
        void removeDuplicates() {
            List<RoleEntity> requestedRoles = createRoleEntities(Arrays.asList(AuthRole.USER_ROLE_ADMIN, AuthRole.USER_ROLE_ADMIN, AuthRole.USER_ROLE_MODERATOR));
            UserEntity user = createUserWithRoles(Arrays.asList(AuthRole.USER_ROLE_ADMIN));

            Collection<RoleEntity> adaptedRoles = users.adaptRequestedRoles(user, requestedRoles);

            assertThat(adaptedRoles.size()).isEqualTo(2);
        }

        @Test
        void adaptUserRoles() {
            List<RoleEntity> requestedRoles = createRoleEntities(Arrays.asList(AuthRole.USER_ROLE_ADMIN, AuthRole.USER_ROLE_MODERATOR));
            UserEntity user = createUserWithRoles(Arrays.asList(AuthRole.USER_ROLE_ADMIN));

            Collection<RoleEntity> adaptedRoles = users.adaptRequestedRoles(user, requestedRoles);

            assertThat(adaptedRoles.size()).isEqualTo(2);
        }
    }
}
