/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.business;

import net.m4e.common.Entities;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Matchers.any;

/**
 * @author boto
 * Date of creation February 5, 2018
 */
class UserResourcePurgerTest {

    @Mock
    Users users;

    @Mock
    Entities entities;

    UserResourcePurger userResourcePurger;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        userResourcePurger = new UserResourcePurger(entities, users);
    }

    @Test
    void defaultConstructor() {
        new UserResourcePurger();
    }

    @Test
    void purgeAccountRegistrations() {
        createRegistrationEntries(5, 2);

        assertThat(userResourcePurger.purgeAccountRegistrations()).isEqualTo(5);
    }

    @Test
    void purgeAccountRegistrationInvalidEntry() {
        createRegistrationEntries(1, 0);

        UserRegistrationEntity registrationEntity = entities.findAll(UserRegistrationEntity.class).get(0);
        registrationEntity.setUser(null);

        assertThat(userResourcePurger.purgeAccountRegistrations()).isEqualTo(1);
    }

    private void createRegistrationEntries(int countExpiredRegistrations, int countNotExpiredRegistrations) {
        final Instant dateNotExpired = Instant.now();
        final Instant dateExpired = Instant.now().minus(UserResourcePurger.REGISTER_EXPIRATION_HOURS + 1, ChronoUnit.HOURS);

        List<UserRegistrationEntity> entries = new ArrayList<>();

        for (int i = 0; i < countNotExpiredRegistrations; ++i) {
            entries.add(createRegistrationEntry(dateNotExpired));
        }

        for (int i = 0; i < countExpiredRegistrations; ++i) {
            entries.add(createRegistrationEntry(dateExpired));
        }

        Mockito.when(entities.findAll(UserRegistrationEntity.class)).thenReturn(entries);
    }

    private UserRegistrationEntity createRegistrationEntry(Instant requestDate) {
        UserRegistrationEntity entry = new UserRegistrationEntity();
        entry.setRequestDate(requestDate.toEpochMilli());
        entry.setUser(new UserEntity());
        return entry;
    }

    @Test
    void purgePasswordResets() {
        createPasswordResetEntries(3, 1);

        assertThat(userResourcePurger.purgePasswordResets()).isEqualTo(3);
    }

    private void createPasswordResetEntries(int countExpiredRegistrations, int countNotExpiredRegistrations) {
        final Instant dateNotExpired = Instant.now();
        final Instant dateExpired = Instant.now().minus(UserResourcePurger.PASSWORD_RESET_EXPIRATION_MINUTES + 1, ChronoUnit.MINUTES);

        List<UserPasswordResetEntity> entries = new ArrayList<>();

        for (int i = 0; i < countNotExpiredRegistrations; ++i) {
            entries.add(createPasswordResetEntry(dateNotExpired));
        }

        for (int i = 0; i < countExpiredRegistrations; ++i) {
            entries.add(createPasswordResetEntry(dateExpired));
        }

        Mockito.when(entities.findAll(UserPasswordResetEntity.class)).thenReturn(entries);
    }

    private UserPasswordResetEntity createPasswordResetEntry(Instant requestDate) {
        UserPasswordResetEntity entry = new UserPasswordResetEntity();
        entry.setRequestDate(requestDate.toEpochMilli());
        return entry;
    }
}
