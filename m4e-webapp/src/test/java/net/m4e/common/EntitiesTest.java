/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.common;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import net.m4e.app.resources.DocumentEntity;
import net.m4e.app.resources.DocumentPool;
import net.m4e.app.user.UserEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test the abstract class EntityAccess
 * 
 * @author boto
 */
public class EntitiesTest {

    EntityManager mockedEntityManager;
    DocumentPool mockedDocumentPool;
    Entities entities;

    public EntitiesTest() {
    }

    @BeforeEach
    void initTest() {
        //! TODO properly mock the entity manager
        CriteriaBuilder mockCriteriaBuilder = mock(CriteriaBuilder.class);
        CriteriaQuery mockCriteriaQuery = mock(CriteriaQuery.class);
        Root mockRoot = mock(Root.class);
        mockedEntityManager = mock(EntityManager.class);
        when(mockedEntityManager.getCriteriaBuilder()).thenReturn(mockCriteriaBuilder);
        when(mockCriteriaBuilder.createQuery()).thenReturn(mockCriteriaQuery);
        when(mockCriteriaQuery.from(Class.class)).thenReturn(mockRoot);
        //Query mockQuery = mock(Query.class);
        //when(mockedEntityManager.createQuery(CriteriaQuery.class)).thenReturn(mockQuery);

        mockedDocumentPool = mock(DocumentPool.class);
        entities = new Entities(mockedEntityManager, mockedDocumentPool);
    }

    /**
     * Test of create method, of class Entities.
     */
    @Test
    public void testCreate() {
        System.out.println("create");
        UserEntity entity = new UserEntity();
        entities.create(entity);
    }

    /**
     * Test of delete method, of class Entities.
     */
    @Test
    public void testDelete() {
        System.out.println("delete");
        UserEntity entity = new UserEntity();
        entities.delete(entity);
    }

    /**
     * Test of update method, of class Entities.
     */
    @Test
    public void testUpdate() {
        System.out.println("update");
        UserEntity entity = new UserEntity();
        entities.update(entity);
    }

    /**
     * Test of getCount method, of class Entities.
     */
    @Test
    public void testGetCount() {
        System.out.println("getCount");
        int result = entities.getCount(UserEntity.class);
    }

    /**
     * Test of findAll method, of class Entities.
     */
    @Test
    public void testFindAll() {
        System.out.println("findAll");
        List result = entities.findAll(UserEntity.class);
    }

    /**
     * Test of findRange method, of class Entities.
     */
    @Test
    public void testFindRange() {
        System.out.println("findRange");
        List result = entities.findRange(UserEntity.class, 0, 1);
    }

    /**
     * Test of find method, of class Entities.
     */
    @Test
    public void testFind() {
        System.out.println("find");
        Object result = entities.find(UserEntity.class, 0L);
    }

    /**
     * Test of findByField method, of class Entities.
     */
    @Test
    public void testFindByField() {
        System.out.println("findByField");
        List result = entities.findByField(UserEntity.class, "", "");
    }

    /**
     * Test of search method, of class Entities.
     */
    @Test
    public void testSearch() {
        System.out.println("search");
        List<String> searchFields = new ArrayList();
        List result = entities.search(UserEntity.class, "", searchFields, 10);
    }

    /**
     * Test of updatePhoto method, of class Entities.
     */
    @Test
    public void testUpdatePhoto() {
        System.out.println("updatePhoto");
        UserEntity entity = new UserEntity();
        DocumentEntity newPhoto = new DocumentEntity();
        try {
            entities.updatePhoto(entity, newPhoto);
        }
        catch (Exception ex) {
            fail("Failed to update entity: " + ex.getLocalizedMessage());
        }
    }
}
