/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.business;

import net.m4e.app.auth.*;
import net.m4e.common.UserEntityCreator;
import net.m4e.tests.EntityAssertions;
import org.assertj.core.api.*;
import org.junit.jupiter.api.Test;

import java.util.*;

/**
 * @author boto
 * Date of creation January 25, 2018
 */
class UserEntityTest extends UserEntityCreator {

    @Test
    void commonEntityTests() {
        EntityAssertions.assertThat(UserEntity.class)
                .isSerializable()
                .hasSerialVersionUID()
                .hasEntityAnnotation()
                .hasIdAnnotation()
                .conformsToEqualsContract()
                .hasHashCode()
                .hasProperToString();
    }

    @Test
    void setterGetter() {
        List<String> roles = Arrays.asList(AuthRole.USER_ROLE_ADMIN);

        UserEntity entity = createWithRoles(roles);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(entity.getId()).isEqualTo(USER_ID);
        softly.assertThat(entity.getStatus()).isNotNull();
        softly.assertThat(entity.getPhoto()).isNotNull();
        softly.assertThat(entity.getProfile()).isNotNull();
        softly.assertThat(entity.getRoles()).isNotEmpty();
        softly.assertThat(entity.getRolesAsString()).containsAll(roles);
        softly.assertThat(entity.getLogin()).isEqualTo(USER_LOGIN);
        softly.assertThat(entity.getName()).isEqualTo(USER_NAME);
        softly.assertThat(entity.getEmail()).isEqualTo(USER_EMAIL);
        softly.assertThat(entity.getDateLastLogin()).isEqualTo(USER_DATE_LAST_LOGIN);
        softly.assertAll();
    }

    @Test
    void getRolesAsString() {
        String ROLE1 = "ROLE1";
        String ROLE2 = "ROLE2";

        RoleEntity role1 = new RoleEntity();
        role1.setName("ROLE1");
        RoleEntity role2 = new RoleEntity();
        role2.setName("ROLE2");

        UserEntity entity = new UserEntity();
        List<RoleEntity> roles = Arrays.asList(role1, role2);

        entity.setRoles(roles);

        Assertions.assertThat(entity.getRolesAsString()).containsExactly(ROLE1, ROLE2);
    }

    @Test
    void getRolesAsString_noRoles() {
        UserEntity entity = new UserEntity();
        entity.setRoles(null);

        Assertions.assertThat(entity.getRolesAsString()).isEmpty();
    }
}
