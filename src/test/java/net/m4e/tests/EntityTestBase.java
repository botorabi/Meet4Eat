/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.tests;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;

import org.assertj.core.internal.Failures;

/**
 * @author boto
 * Date of creation January 30, 2018
 */
class EntityTestBase<T> {

    private final Constructor<T> constructor;

    private final Class<T> actual;

    private final Failures failures = Failures.instance();

    EntityTestBase(final Class<T> actual) throws NoSuchMethodException {
        constructor = actual.getDeclaredConstructor();
        this.actual = actual;
    }

    protected T createInstance() {
        try {
            return constructor.newInstance();
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new RuntimeException(e);
        }
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

    protected void failWithMessage(String msg, Object... args) {
        AssertionError assertionError = failures.failure(String.format(msg, args));
        Throwables.removeFromStacktrace(assertionError, "net.m4e.tests");
        throw assertionError;
    }
}
