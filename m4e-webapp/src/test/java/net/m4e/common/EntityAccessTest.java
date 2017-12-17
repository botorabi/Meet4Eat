/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.common;

import org.junit.jupiter.api.Test;
import javax.persistence.EntityManager;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test the abstract class EntityAccess
 * 
 * @author boto
 */
public class EntityAccessTest {

    EntityManager mockedEntityManager;

    ConcreteEntityAccess entityAccess;

    private static final Long FIND_ID = 10L;

    /**
     * Concrete class used for actual testing
     */
    class ConcreteEntityAccess extends EntityAccess<ConcreteEntityAccess> {

        public ConcreteEntityAccess() {
            super(ConcreteEntityAccess.class);
        }

        @Override
        protected EntityManager getEntityManager() {
            return mockedEntityManager;
        }
    }

    public EntityAccessTest() {
    }

    @BeforeEach
    void initTest() {
        // create an instance of the testee
        entityAccess = new ConcreteEntityAccess();

        mockedEntityManager = mock(EntityManager.class);

        //! TODO check why anyObject() does not work, we take a constant long for now
        when(mockedEntityManager.find(ConcreteEntityAccess.class, FIND_ID)).thenReturn(new ConcreteEntityAccess());

        //! TODO: futher mock the entity manager
    }

    /**
     * Test of getEntityManager method, of class EntityAccess.
     */
    @Test
    public void testGetEntityManager() {
        System.out.println("getEntityManager");
        assertThat(entityAccess.getEntityManager()).isNotEqualTo(null);
    }

    /**
     * Test of create method, of class EntityAccess.
     */
    @Test
    public void testCreate() {
        System.out.println("create");
        ConcreteEntityAccess newentity = new ConcreteEntityAccess();
        entityAccess.create(newentity);
    }

    /**
     * Test of edit method, of class EntityAccess.
     */
    @Test
    public void testEdit() {
        System.out.println("edit");
        ConcreteEntityAccess newentity = new ConcreteEntityAccess();
        entityAccess.edit(newentity);
    }

    /**
     * Test of remove method, of class EntityAccess.
     */
    @Test
    public void testRemove() {
        System.out.println("remove");
        ConcreteEntityAccess newentity = new ConcreteEntityAccess();
        entityAccess.remove(newentity);
    }

    /**
     * Test of find method, of class EntityAccess.
     */
    @Test
    public void testFind() {
        System.out.println("find");
        entityAccess.find(FIND_ID);
    }

    /**
     * Test of findAll method, of class EntityAccess.
     */
    @Test
    public void testFindAll() {
        System.out.println("findAll");

        fail("The test case is a prototype.");
    }

    /**
     * Test of findRange method, of class EntityAccess.
     */
    @Test
    public void testFindRange() {
        System.out.println("findRange");

        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of count method, of class EntityAccess.
     */
    @Test
    public void testCount() {
        System.out.println("count");

        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
