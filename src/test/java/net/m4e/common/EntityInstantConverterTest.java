/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.common;

import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * @author boto
 * Date of creation February 10, 2018
 */
class EntityInstantConverterTest {

    EntityInstantConverter converter;

    Instant instant;

    Long timestamp;


    @BeforeEach
    void setup() {
        converter = new EntityInstantConverter();
        instant = Instant.now();
        timestamp = instant.toEpochMilli();
    }

    @Test
    void convertFromInstant() {
        assertThat(converter.convertToDatabaseColumn(instant)).isNotNull();
        assertThat(converter.convertToDatabaseColumn(instant)).isGreaterThan(0L);
    }

    @Test
    void convertFromNullInstant() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    void convertToInstant() {
        assertThat(converter.convertToEntityAttribute(timestamp)).isNotNull();
        assertThat(converter.convertToEntityAttribute(timestamp).toEpochMilli()).isEqualTo(timestamp);
    }

    @Test
    void convertToNullInstant() {
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }
}
