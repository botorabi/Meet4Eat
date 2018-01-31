/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.tests;

import java.io.Serializable;
import java.lang.reflect.Field;

import org.assertj.core.api.AbstractObjectAssert;

/**
 * @author ybroeker
 */
public class EntityAssert<T> extends AbstractObjectAssert<EntityAssert<T>, Class<T>> {

    public EntityAssert(final Class<T> actual) {
        super(actual, EntityAssert.class);
    }

    public EntityAssert<T> isSerializable() {
        String failMessage = "\nExpecting class:\n  <%s>\nto implement Serializable\nbut doesn't";
        if (!Serializable.class.isAssignableFrom(actual)) {
            failWithMessage(failMessage, actual.getName());
        }
        return this;
    }

    public EntityAssert<T> hasEntityAnnotation() {
        String failMessage = "\nExpecting class:\n  <%s>\nto be annotated with @Entity\nbut isn't";
        if (!actual.isAnnotationPresent(javax.persistence.Entity.class)) {
            failWithMessage(failMessage, actual.getName());
        }
        return this;
    }

    public EntityAssert<T> hasIdAnnotation() {
        String failMessage = "\nExpecting class:\n  <%s>\nto have field annotated with @Id\nbut hasn't";
        for (final Field field : actual.getDeclaredFields()) {
            if (field.isAnnotationPresent(javax.persistence.Id.class)) {
                return this;
            }
        }

        failWithMessage(failMessage, actual.getName());
        return this;
    }

    public EntityAssert<T> conformsToEqualsContract() {
        try {
            EntityEqualsTester<T> entityEqualsTester = new EntityEqualsTester<>(actual);
            entityEqualsTester.verifyAll();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    public EntityAssert<T> hasHashCode() {
        try {
            EntityHashCodeTester<T> entityHashCodeTester = new EntityHashCodeTester<>(actual);
            entityHashCodeTester.verifyAll();
        } catch (Exception e) {
            throw new RuntimeException();
        }

        return this;
    }

    public EntityAssert<T> hasProperToString() {
        //! TODO
        return this;
    }
}
