/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.business;

import net.m4e.app.auth.RoleEntity;
import net.m4e.app.resources.*;
import net.m4e.tests.EntityAssertions;
import org.assertj.core.api.*;
import org.junit.jupiter.api.Test;

import java.util.*;

/**
 * @author boto
 * Date of creation January 25, 2018
 */
class UserEntityTest implements DefaultUserData {

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
        long id = USER_ID;
        StatusEntity status = new StatusEntity();
        DocumentEntity photo = new DocumentEntity();
        photo.setId(100L);
        UserProfileEntity prof = new UserProfileEntity();
        prof.setId(101L);
        List<RoleEntity> roles = Arrays.asList(new RoleEntity());
        String login = USER_LOGIN;
        String name = USER_NAME;
        String email = USER_EMAIL;
        Long dateLastLogin = USER_DATE_LAST_LOGIN;

        UserEntity entity = new UserEntity();

        entity.setId(id);
        entity.setPhoto(photo);
        entity.setStatus(status);
        entity.setProfile(prof);
        entity.setRoles(roles);
        entity.setName(name);
        entity.setLogin(login);
        entity.setEmail(email);
        entity.setDateLastLogin(dateLastLogin);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(entity.getId()).isEqualTo(id);
        softly.assertThat(entity.getStatus()).isEqualTo(status);
        softly.assertThat(entity.getPhoto()).isEqualTo(photo);
        softly.assertThat(entity.getProfile()).isEqualTo(prof);
        softly.assertThat(entity.getRoles()).isEqualTo(roles);
        softly.assertThat(entity.getLogin()).isEqualTo(login);
        softly.assertThat(entity.getName()).isEqualTo(name);
        softly.assertThat(entity.getEmail()).isEqualTo(email);
        softly.assertThat(entity.getDateLastLogin()).isEqualTo(dateLastLogin);
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
