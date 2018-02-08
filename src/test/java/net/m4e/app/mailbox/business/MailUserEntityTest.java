/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.mailbox.business;

import net.m4e.tests.EntityAssertions;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * @author ybroeker
 */
class MailUserEntityTest {

    @Test
    void commonEntityTests() {
        EntityAssertions.assertThat(MailUserEntity.class)
                .isSerializable()
                .hasSerialVersionUID()
                .hasMethodIsInstanceOfMe()
                .hasEntityAnnotation()
                .hasIdAnnotation()
                .conformsToEqualsContract()
                .hasHashCode()
                .hasProperToString()
        ;
    }

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
}
