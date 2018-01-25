/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.tests;

/**
 * A collection of tests common to all entities
 *
 * @param <T>   Entity type (i.e. YourEntity.class)
 *
 * @author boto
 * Date of creation January 25, 2018
 */
public class CommonEntityTests<T extends BaseTestEntity> {

    Class<T> cls;

    public CommonEntityTests(Class<T> entityClass) {
        this.cls = entityClass;
    }

    /**
     * Perform the common tests on entity class.
     */
    public void performTests() {
        equalsSameIds();
        equalsWithoutIds();
        notEquals();
        notEqualsDifferentIds();
        notEqualDifferentTypes();
        test_hashCode();
        test_toString();
    }

    /**
     * Create an instance of the entity which is going to get tested.
     */
    private T createInstance() {
        try {
            return cls.newInstance();
        }
        catch (Exception ex) {
            Assertions.fail("Could not create entity instance: " + ex.getMessage());
        }
        return null;
    }

    private void equalsWithoutIds() {
        T entity1 = createInstance();
        T entity2 = createInstance();

        Assertions.assertThat(entity1).isEqualTo(entity2);
        Assertions.assertThat(entity2).isEqualTo(entity1);
    }

    private void notEquals() {
        T entity1 = createInstance();
        T entity2 = createInstance();
        entity1.setId(1L);

        Assertions.assertThat(entity1).isNotEqualTo(entity2);
        Assertions.assertThat(entity2).isNotEqualTo(entity1);
    }

    private void notEqualsDifferentIds() {
        T entity1 = createInstance();
        T entity2 = createInstance();
        entity1.setId(1L);
        entity2.setId(2L);

        Assertions.assertThat(entity1).isNotEqualTo(entity2);
        Assertions.assertThat(entity2).isNotEqualTo(entity1);
    }

    private void equalsSameIds() {
        T entity1 = createInstance();
        T entity2 = createInstance();
        entity1.setId(1L);
        entity2.setId(1L);

        Assertions.assertThat(entity1).isEqualTo(entity2);
        Assertions.assertThat(entity2).isEqualTo(entity1);
    }

    private void notEqualDifferentTypes() {
        String otherType = "";
        T entity = createInstance();

        Assertions.assertThat(entity.equals(otherType)).isFalse();
    }

    private void test_hashCode() {
        T entity = createInstance();

        Assertions.assertThat(entity.hashCode()).isEqualTo(0);

        Long id = 42L;
        entity.setId(id);

        Assertions.assertThat(entity.hashCode()).isEqualTo(id.hashCode());
    }

    private void test_toString() {
        T entity = createInstance();

        Assertions.assertThat(entity.toString()).isNotEmpty();
    }
}
