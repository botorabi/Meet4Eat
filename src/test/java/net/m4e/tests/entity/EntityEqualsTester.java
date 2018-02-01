/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.tests.entity;

import org.assertj.core.util.Objects;

import java.lang.reflect.Field;
import java.util.Random;

/**
 * @author ybroeker
 */
public class EntityEqualsTester<T> extends EntityTestBase<T> {

    private final Field idField;

    private final Random random = new Random();

    public EntityEqualsTester(final Class<T> actual) throws NoSuchMethodException {
        super(actual);
        idField = findAnnotatedField(javax.persistence.Id.class);
    }

    public void verifyAll() throws Exception {
        notEqualWithoutIds();
        equalWithSameIds();
        notEqualWithDifferentIds();
        notEqualWithDifferentTypes();
    }

    private void notEqualWithoutIds() {
        T entity1 = createInstance();
        T entity2 = createInstance();

        setIdField(entity1, null);
        setIdField(entity2, null);

        if (Objects.areEqual(entity1, entity2) || Objects.areEqual(entity2, entity1)) {
            failWithMessage("\nEntities of Class:\n  <%s>\n with ID:\n  <null>\nshouldn't be equal", getActual().getSimpleName());
        }
    }

    private void setIdField(T entity, Object value) {
        try {
            idField.set(entity, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void equalWithSameIds() {
        T entity1 = createInstance();
        T entity2 = createInstance();

        Object id1 = createSameID();
        Object id2 = createSameID();

        setIdField(entity1, id1);
        setIdField(entity2, id2);

        if (Objects.areEqual(entity1, entity2) && Objects.areEqual(entity2, entity1)) {
            return;
        }
        failWithMessage("\nEntities of class:\n  <%s>\n with ID:\n  <%s>\nshould be equal", getActual().getSimpleName(), id1);
    }

    @SuppressWarnings("UnnecessaryBoxing")
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
            failWithMessage("\nEntities of class:\n  <%s>\nwith IDs:\n  <%s>\nand:\n  <%s>\n shouldn't be equal", getActual().getSimpleName(), id1, id2);
        }
    }

    @SuppressWarnings("UnnecessaryBoxing")
    private Object createDifferentID() {
        //TODO other types;
        return Long.valueOf(random.nextInt(Integer.MAX_VALUE));
    }

    private void notEqualWithDifferentTypes() throws Exception {
        T entity1 = createInstance();
        idField.set(entity1, createSameID());
        Object o = new Object();

        if (Objects.areEqual(entity1, o) || Objects.areEqual(o, entity1)) {
            failWithMessage("\nEntities of Class:\n  <%s>\nshouldn't be equal to instances of class:\n  <%s>", getActual().getSimpleName(), o.getClass().getSimpleName());
        }
    }
}
