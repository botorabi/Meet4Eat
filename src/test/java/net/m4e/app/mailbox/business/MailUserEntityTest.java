/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.mailbox.business;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import net.m4e.tests.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author ybroeker
 */
class MailUserEntityTest {

    @Test
    void isTrashed() {
        MailUserEntity entity = new MailUserEntity();
        entity.setTrashDate(Instant.now().minus(30, ChronoUnit.DAYS));

        Assertions.assertThat(entity.isTrashed()).isTrue();
    }

    @Test
    void notTrashedWithoutTimestamp() {
        MailUserEntity entity = new MailUserEntity();
        entity.setTrashDate(null);

        Assertions.assertThat(entity.isTrashed()).isFalse();
    }

    @Test
    void isTrashedWithZeroTimestamp() {
        MailUserEntity entity = new MailUserEntity();
        entity.setTrashDate(Instant.ofEpochMilli(0));

        Assertions.assertThat(entity.isTrashed()).isFalse();
    }

    @Test
    void equalsWithoutIds() {
        MailUserEntity entity1 = new MailUserEntity();
        MailUserEntity entity2 = new MailUserEntity();

        Assertions.assertThat(entity1).isEqualTo(entity2);
        Assertions.assertThat(entity2).isEqualTo(entity1);
    }

    @Test
    void notEquals() {
        MailUserEntity entity1 = new MailUserEntity();
        entity1.setId(1L);
        MailUserEntity entity2 = new MailUserEntity();

        Assertions.assertThat(entity1).isNotEqualTo(entity2);
        Assertions.assertThat(entity2).isNotEqualTo(entity1);

    }

    @Test
    void notEqualsDifferentIds() {
        MailUserEntity entity1 = new MailUserEntity();
        entity1.setId(1L);
        MailUserEntity entity2 = new MailUserEntity();
        entity2.setId(2L);

        Assertions.assertThat(entity1).isNotEqualTo(entity2);
        Assertions.assertThat(entity2).isNotEqualTo(entity1);

    }

    @Test
    void equalsSameIds() {
        MailUserEntity entity1 = new MailUserEntity();
        entity1.setId(1L);
        MailUserEntity entity2 = new MailUserEntity();
        entity2.setId(1L);

        Assertions.assertThat(entity1).isEqualTo(entity2);
        Assertions.assertThat(entity2).isEqualTo(entity1);

    }
}
