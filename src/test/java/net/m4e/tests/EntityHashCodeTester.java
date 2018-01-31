/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.tests;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class EntityHashCodeTester<T> extends EntityTestBase<T> {

    private final Constructor<T> constructor;

    private final Field idField;

    EntityHashCodeTester(final Class<T> actual) throws NoSuchMethodException {
        super(actual);
        constructor = actual.getDeclaredConstructor();
        idField = findAnnotatedField(javax.persistence.Id.class);
    }

    void verifyAll() throws Exception {
        hashCodeWithId();
        hashCodeWithoutId();
    }

    private void hashCodeWithoutId() throws Exception {
        T entity = createInstance();

        Assertions.assertThat(entity.hashCode()).isEqualTo(0);
    }

    private void hashCodeWithId() throws Exception {
        T entity = createInstance();

        Long id = 42L;
        idField.set(entity, id);

        Assertions.assertThat(entity.hashCode()).isEqualTo(id.hashCode());
    }
}
