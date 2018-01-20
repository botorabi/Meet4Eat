/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.common;


import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.Instant;

/**
 * Converter class for handling timestamps in entities using the class Instant.
 * An Instant is stored in database as a long value containing the milliseconds since epoch.
 *
 * @author boto
 * Date of Creation February 15, 2018
 */
@Converter(autoApply = true)
public class EntityInstantConverter implements AttributeConverter<Instant, Long> {

    @Override
    public Long convertToDatabaseColumn(Instant date) {
        return date == null ? null : date.getEpochSecond();
    }

    @Override
    public Instant convertToEntityAttribute(Long value) {
        return value == null ? null : Instant.ofEpochMilli(value);
    }
}
