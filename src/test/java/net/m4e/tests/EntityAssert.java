/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.tests;

import net.m4e.tests.entity.*;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.description.Description;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * @author ybroeker
 */
public class EntityAssert<T> extends AbstractObjectAssert<EntityAssert<T>, Class<T>> {

    public EntityAssert(final Class<T> actual) {
        super(actual, EntityAssert.class);
    }

    public EntityAssert<T> isSerializable() {
        String failMessage = "\nExpecting class:\n  <%s>\nto implement:\n  Serializable\nbut doesn't";
        if (!Serializable.class.isAssignableFrom(actual)) {
            failWithMessage(failMessage, actual.getName());
        }
        return this;
    }

    public EntityAssert<T> hasSerialVersionUID() {
        String failMessage = "\nExpecting class:\n  <%s>\nto contain:\n  <static long serialVersionUID>\nbut doesn't";
        try {
            Field field = actual.getDeclaredField("serialVersionUID");
            if (!(field.getType().equals(long.class) && java.lang.reflect.Modifier.isStatic(field.getModifiers()))) {
                failWithMessage(failMessage, actual.getName());
            }
        } catch (NoSuchFieldException e) {
            failWithMessage(failMessage, actual.getName());
        }
        return this;
    }

    public EntityAssert<T> hasEntityAnnotation() {
        String failMessage = "\nExpecting class:\n  <%s>\nto be annotated with:\n  <@Entity>\nbut isn't";
        if (!actual.isAnnotationPresent(javax.persistence.Entity.class)) {
            failWithMessage(failMessage, actual.getName());
        }
        return this;
    }

    public EntityAssert<T> hasIdAnnotation() {
        String failMessage = "\nExpecting class:\n  <%s>\nto have field annotated with:\n  <@Id>\nbut hasn't";
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
            EntityEqualsTester<T> tester = new EntityEqualsTester<>(actual);
            tester.verifyAll();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    public EntityAssert<T> hasHashCode() {
        try {
            EntityHashCodeTester<T> tester = new EntityHashCodeTester<>(actual);
            tester.verifyAll();
        } catch (Exception e) {
            throw new RuntimeException();
        }

        return this;
    }

    public EntityAssert<T> hasProperToString() {
        try {
            EntityToStringTester<T> tester = new EntityToStringTester<>(actual);
            tester.verifyAll();
        } catch (Exception e) {
            throw new RuntimeException();
        }

        return this;
    }
}
