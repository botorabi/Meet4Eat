/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.tests.entity;

import java.lang.reflect.Field;

/**
 * @author boto
 * Date of creation January 30, 2018
 */
public class EntityHashCodeTester<T> extends EntityTestBase<T> {

    private final Field idField;

    public EntityHashCodeTester(final Class<T> actual) throws NoSuchFieldException {
        super(actual);
        idField = findAnnotatedField(javax.persistence.Id.class);
        idField.setAccessible(true);
    }

    public void verifyAll() throws Exception {
        hashCodeWithId();
        hashCodeWithoutId();
    }

    private void hashCodeWithoutId() {
        T entity = createInstance();

        if (entity.hashCode() != 0) {
            failWithMessage("\nEntities of Class:\n  <%s>\nhas an unexpected hash code with a null ID:\n  <%d != %d>", getActual().getSimpleName(), entity.hashCode(), 0);
        }
    }

    private void hashCodeWithId() throws Exception {
        T entity = createInstance();

        Long id = 42L;
        idField.set(entity, id);
        if (entity.hashCode() != id.hashCode()) {
            failWithMessage("\nEntities of Class:\n  <%s>\nhas an unexpected hash code with a valid ID:\n  <%d != %d>", getActual().getSimpleName(), entity.hashCode(), id.hashCode());
        }
    }
}
