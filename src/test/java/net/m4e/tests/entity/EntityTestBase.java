/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.tests.entity;

import net.m4e.tests.AssertionStack;
import org.assertj.core.internal.Failures;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;

/**
 * @author boto
 * Date of creation January 30, 2018
 */
abstract class EntityTestBase<T> {

    private final Constructor<T> constructor;

    private final Class<T> actual;

    private final Failures failures = Failures.instance();

    EntityTestBase(final Class<T> actual) {
        try {
            constructor = actual.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e.getMessage());
        }
        this.actual = actual;
    }

    public abstract void verifyAll() throws Exception;

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

    protected Field findAnnotatedField(Class<? extends Annotation> annotationClass) throws NoSuchFieldException {
        for (final Field field : actual.getDeclaredFields()) {
            if (field.isAnnotationPresent(annotationClass)) {
                return field;
            }
        }
        throw new NoSuchFieldException(String.format("No field with annotation <%s> found on class <%s>",
                annotationClass.getSimpleName(), actual.getSimpleName()));
    }

    protected Method findAnnotatedMethod(Class<? extends Annotation> annotationClass) throws NoSuchMethodException {
        for (final Method method : actual.getDeclaredMethods()) {
            if (method.isAnnotationPresent(annotationClass)) {
                return method;
            }
        }
        throw new NoSuchMethodException(String.format("No method with annotation <%s> found on class <%s>",
                annotationClass, actual.getSimpleName()));
    }

    protected Method findMethodByName(final String name) throws NoSuchMethodException {
        for (final Method method : actual.getDeclaredMethods()) {
            if (method.getName().equals(name)) {
                return method;
            }
        }
        throw new NoSuchMethodException(String.format("No method with name <%s> found on class <%s>",
                name, actual.getSimpleName()));
    }

    protected Failures getFailures() {
        return failures;
    }

    protected void failWithMessage(String msg, Object... args) {
        AssertionError assertionError = getFailures().failure(String.format(msg, args));
        AssertionStack.removeTopEntriesFromStacktrace(assertionError, 1);
        throw assertionError;
    }
}
