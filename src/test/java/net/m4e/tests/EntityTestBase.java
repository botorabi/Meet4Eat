/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.tests;

import org.assertj.core.internal.Failures;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * @author boto
 * Date of creation January 30, 2018
 */
public class EntityTestBase<T> {

    private final Constructor<T> constructor;

    private final Class<T> actual;

    private final Failures failures = Failures.instance();

    EntityTestBase(final Class<T> actual) throws NoSuchMethodException {
        constructor = actual.getDeclaredConstructor();
        this.actual = actual;
    }

    protected T createInstance() throws Exception {
        return constructor.newInstance();
    }

    protected Class<T> getActual() {
        return actual;
    }

    protected Field findAnnotatedField(Class<? extends Annotation> fieldType) {
        for (final Field field : actual.getDeclaredFields()) {
            if (field.isAnnotationPresent(fieldType)) {
                field.setAccessible(true);
                return field;
            }
        }
        throw new IllegalArgumentException(String.format("No field with proper annotation found on <%s>", actual.getSimpleName()));
    }

    protected Failures getFailures() {
        return failures;
    }
}
