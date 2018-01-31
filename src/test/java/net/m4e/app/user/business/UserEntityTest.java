/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.business;

import java.util.Arrays;
import java.util.List;

import net.m4e.app.auth.RoleEntity;
import net.m4e.app.resources.DocumentEntity;
import net.m4e.app.resources.StatusEntity;
import net.m4e.tests.*;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

/**
 * @author boto
 * Date of creation January 25, 2018
 */
class UserEntityTest {

    @Test
    void commonTests() {
        CommonEntityTests<UserEntityCommonTests> commonTests = new CommonEntityTests<>(UserEntityCommonTests.class);
        commonTests.performTests();
    }

    @Test
    void entityTest() {
        EntityAssertions.assertThat(UserEntity.class)
                .isSerializable()
                .hasEntityAnnotation()
                .hasIdAnnotation()
                .conformsToEqualsContract();
    }

    @Test
    void setterGetter() {
        long id = 42L;
        StatusEntity status = new StatusEntity();
        DocumentEntity photo = new DocumentEntity();
        UserProfileEntity prof = new UserProfileEntity();
        List<RoleEntity> roles = Arrays.asList(new RoleEntity());
        String login = "login";
        String name = "name";
        String email = "email";
        Long dateLastLogin = 124L;

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

    /**
     * Used for testing common functionality of the entity.
     */
    public static class UserEntityCommonTests extends UserEntity implements BaseTestEntity {
    }
}
