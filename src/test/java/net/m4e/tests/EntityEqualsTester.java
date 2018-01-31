/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.tests;

import java.lang.reflect.Field;
import java.util.Random;

import org.assertj.core.internal.Failures;
import org.assertj.core.util.Objects;

/**
 * @author ybroeker
 */
public class EntityEqualsTester<T> extends EntityTestBase<T> {

    private final Field idField;

    private final Random random = new Random();

    EntityEqualsTester(final Class<T> actual) throws NoSuchMethodException {
        super(actual);
        idField = findAnnotatedField(javax.persistence.Id.class);
    }

    void verifyAll() throws Exception {
        notEqualWithoutIds();
        equalWithSameIds();
        notEqualWithDifferentIds();
        notEqualWithDifferentTypes();
    }

    private void notEqualWithoutIds() throws Exception {
        T entity1 = createInstance();
        T entity2 = createInstance();

        idField.set(entity1, null);
        idField.set(entity2, null);

        if (Objects.areEqual(entity1, entity2) || Objects.areEqual(entity2, entity1)) {
            throw getFailures().failure(String.format("Entities of Class <%s> with ID <null> shouldn't be equal", getActual().getSimpleName()));
        }
    }

    private void equalWithSameIds() throws Exception {
        T entity1 = createInstance();
        T entity2 = createInstance();

        Object id1 = createSameID();
        Object id2 = createSameID();

        idField.set(entity1, id1);
        idField.set(entity2, id2);

        if (!(Objects.areEqual(entity1, entity2) && Objects.areEqual(entity2, entity1))) {
            throw getFailures().failure(String.format("Entities of class <%s> with ID <%s> should be equal", getActual().getSimpleName(), id1));
        }
    }

    private Object createSameID() {
        //TODO other types;
        return Long.valueOf(1024L);
    }

    private void notEqualWithDifferentIds() throws Exception {
        T entity1 = createInstance();
        T entity2 = createInstance();

        Object id1 = createDifferentID();
        Object id2 = createDifferentID();

        idField.set(entity1, id1);
        idField.set(entity2, id2);

        if (Objects.areEqual(entity1, entity2) || Objects.areEqual(entity2, entity1)) {
            throw getFailures().failure(String.format("Entities of class <%s> with IDs <%s> and <%s> shouldn't be equal", getActual().getSimpleName(), id1, id2));
        }
    }

    private Object createDifferentID() {
        //TODO other types;
        return Long.valueOf(random.nextInt(Integer.MAX_VALUE));
    }

    private void notEqualWithDifferentTypes() throws Exception {
        T entity1 = createInstance();
        idField.set(entity1, createSameID());
        Object o = new Object();

        if (Objects.areEqual(entity1, o) || Objects.areEqual(o, entity1)) {
            throw getFailures().failure(String.format("Entities of Class <%s> shouldn't be equal to instances of class <%s>", getActual().getSimpleName(), o.getClass().getSimpleName()));
        }
    }
}
