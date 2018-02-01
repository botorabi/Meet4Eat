/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.tests.entity;

import org.assertj.core.api.Assertions;

import java.lang.reflect.Constructor;

/**
 * @author boto
 * Date of creation February 1, 2018
 */
public class EntityToStringTester<T> extends EntityTestBase<T> {

    private final Constructor<T> constructor;

    public EntityToStringTester(final Class<T> actual) throws NoSuchMethodException {
        super(actual);
        constructor = actual.getDeclaredConstructor();
    }

    public void verifyAll() {
        testToString();
    }

    private void testToString() {
        T entity = createInstance();

        String entityToString = entity.toString();
        String classPath = entity.getClass().getPackage().getName();
        classPath += "." + entity.getClass().getSimpleName();
        Assertions.assertThat(entityToString).contains(classPath);
    }
}
