package net.m4e.tests;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Random;
import java.util.logging.Logger;

import org.assertj.core.internal.Failures;
import org.assertj.core.util.Objects;

/**
 * @author ybroeker
 */
public class EntityEqualsTester<T> {
    private static final Logger LOG = Logger.getLogger(EntityEqualsTester.class.getName());

    private final Class<T> actual;

    private final Constructor<T> constructor;

    private final Field idField;

    private final Random random = new Random();

    //WritableAssertionInfo writableAssertionInfo = new WritableAssertionInfo();

    private final Failures failures = Failures.instance();

    EntityEqualsTester(final Class<T> actual) throws NoSuchMethodException {
        this.actual = actual;
        this.constructor = actual.getDeclaredConstructor();
        idField = findIdField(actual);
    }

    private Field findIdField(Class<T> clazz) {
        for (final Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(javax.persistence.Id.class)) {
                field.setAccessible(true);
                return field;
            }
        }
        throw new IllegalArgumentException(String.format("No field with @ID found on <%s>", clazz.getSimpleName()));
    }

    void verifyAll() throws ReflectiveOperationException {
        notEqualWithoutIds();
        equalWithSameIds();
        notEqualWithDifferentIds();

    }

    private void notEqualWithoutIds() throws ReflectiveOperationException {
        T entity1 = createInstance();
        T entity2 = createInstance();

        idField.set(entity1, null);
        idField.set(entity2, null);

        if (Objects.areEqual(entity1, entity2) || Objects.areEqual(entity2, entity1)) {
            throw failures.failure(String.format("Entitys of Class <%s> with ID <null> shouldn't be equal!", actual.getSimpleName()));
        }
    }

    private T createInstance() throws ReflectiveOperationException {
        return constructor.newInstance();
    }

    private void equalWithSameIds() throws ReflectiveOperationException {
        T entity1 = createInstance();
        T entity2 = createInstance();

        Object id1 = createSameID();
        Object id2 = createSameID();

        idField.set(entity1, id1);
        idField.set(entity2, id2);

        if (Objects.areEqual(entity1, entity2) || Objects.areEqual(entity2, entity1)) {
            throw failures.failure(String.format("Entitys of class <%s> with ID <%s> should be equal!", actual.getSimpleName(), id1));
        }
    }

    private Object createSameID() {
        //TODO other types;
        return Long.valueOf(1024L);
    }

    private void notEqualWithDifferentIds() throws ReflectiveOperationException {
        T entity1 = createInstance();
        T entity2 = createInstance();

        Object id1 = createDifferentID();
        Object id2 = createDifferentID();

        idField.set(entity1, id1);
        idField.set(entity2, id2);

        if (Objects.areEqual(entity1, entity2) || Objects.areEqual(entity2, entity1)) {
            throw failures.failure(String.format("Entitys of class <%s> with IDs <%s> and <%s> shouldn't be equal!", actual.getSimpleName(), id1, id2));
        }
    }

    private Object createDifferentID() {
        //TODO other types;
        return Long.valueOf(random.nextInt(Integer.MAX_VALUE));
    }

    private void notEqualWithDifferentTypes() throws ReflectiveOperationException {
        T entity1 = createInstance();
        idField.set(entity1, createSameID());
        Object o = new Object();

        if (Objects.areEqual(entity1, o) || Objects.areEqual(o, entity1)) {
            throw failures.failure(String.format("Entitys of Class <%s> shouldn't be equal to instances of class <%s>!", actual.getSimpleName(), o.getClass().getSimpleName()));
        }
    }

}
