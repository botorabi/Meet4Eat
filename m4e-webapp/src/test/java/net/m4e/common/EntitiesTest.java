/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.common;

import net.m4e.system.core.AppInfoEntity;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;


/**
 * Test the class Entities
 * 
 * @author boto
 */
@RunWith(Arquillian.class)
public class EntitiesTest {

    /**
     * Create the test package which will be deployed by Arquillian.
     *
     * @return  Test package
     */
    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
        addEntities(archive);
        addClasses(archive);
        archive.addAsResource("META-INF/persistence.xml")
               .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return archive;
    }

    /**
     * Add all entities relevant for this test.
     *
     * @param archive   The jar archive
     * @return          Jar archive with added entities
     */
    private static JavaArchive addEntities(JavaArchive archive) {
        archive
                /*
                .addClass(net.m4e.app.auth.PermissionEntity.class)
                .addClass(net.m4e.app.auth.RoleEntity.class)
                .addClass(net.m4e.app.event.EventEntity.class)
                .addClass(net.m4e.app.event.EventLocationEntity.class)
                .addClass(net.m4e.app.event.EventLocationVoteEntity.class)
                .addClass(net.m4e.app.mailbox.MailEntity.class)
                .addClass(net.m4e.app.mailbox.MailUserEntity.class)
                .addClass(net.m4e.app.resources.DocumentEntity.class)
                .addClass(net.m4e.app.resources.StatusEntity.class)
                .addClass(net.m4e.app.user.UserEntity.class)
                .addClass(net.m4e.app.user.UserPasswordResetEntity.class)
                .addClass(net.m4e.app.user.UserProfileEntity.class)
                .addClass(net.m4e.app.user.UserRegistrationEntity.class)
                .addClass(net.m4e.update.UpdateCheckEntity.class)
                .addClass(net.m4e.common.EntityWithPhoto.class); // this is needed by some entities
                */
                .addClass(net.m4e.system.core.AppInfoEntity.class);

        return archive;
    }

    /**
     * Add all classes relevant for this test.
     *
     * @param archive   The jar archive
     * @return          Jar archive with added classes
     */
    private static JavaArchive addClasses(JavaArchive archive) {
        archive
                .addClass(net.m4e.common.Entities.class)
                .addClass(net.m4e.system.core.Log.class)
                .addClass(net.m4e.common.EntityManagerProvider.class) // this contains the entity manager producer
                .addClass(net.m4e.system.core.AppInfoEntity.class); // we use this entity for testing the Entities class

        return archive;
    }

    @PersistenceContext
    EntityManager entityManager;

    @Inject
    UserTransaction userTransaction;

    @Inject
    Entities entities;


    /**
     * Interface used for performing various entity operations.
     */
    interface EntityOperation {
        void perform();
    }


    /**
     * This is executed before any test.
     */
    @Before
    public void initTest() {
        assertNotEquals("Invalid entity manager!", entityManager, null);
        assertNotEquals("Invalid user transaction!", userTransaction, null);
        assertNotEquals("Invalid entities!", entities, null);
    }


    /**
     * Perform an entity operation.
     *
     * @param operation  The operation which is performed.
     *
     * @throws Exception
     */
    private void performOp(EntityOperation operation) throws Exception {
        try {

            userTransaction.begin();
            operation.perform();
            userTransaction.commit();

        } catch (Exception e) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                fail("Could not rollback the transaction!");
            }
            throw e;
        }
    }

    /**
     * Find an entity with given ID.
     *
     * @param entityClass       Entity class
     * @param id                ID to find
     * @param <T>               Entity class type
     * @return                  An entity instance if found, otherwise null.
     */
    private <T> T findEntity(Class<T> entityClass, Long id) {
        return entities.find(entityClass, id);
    }

    /**
     * Create the given entity in database.
     *
     * @param entity    Entity to persist in database
     * @param <T>       Entity type to persist
     */
    private <T> void persistEntity(T entity) {
        try {
            performOp(() -> entities.create(entity));
        } catch (Exception e) {
            fail("Could not create entity: " + e.getLocalizedMessage());
        }
    }

    /**
     * Delete the given entity from database.
     *
     * @param entity    Entity to delete from database
     * @param <T>       Entity type
     */
    private <T> void deleteEntity(T entity) {
        try {
            performOp(() -> entities.delete(entity));
        } catch (Exception e) {
            fail("Could not delete entity: " + e.getLocalizedMessage());
        }
    }

    /**
     * Test of create method, of class Entities.
     */
    @Test
    public void testCreate() {
        AppInfoEntity entity = new AppInfoEntity();
        entity.setVersion("1.0.0");
        entity.setDateLastUpdate(42L);
        entity.setDateLastMaintenance(43L);
        entity.incrementUserCountPurge(1L);
        persistEntity(entity);
        assertNotEquals("Invalid entity", entity.getId(), null);

        AppInfoEntity expected = findEntity(AppInfoEntity.class, entity.getId());
        assertEquals(entity.getVersion(), expected.getVersion());
        assertEquals(entity.getDateLastUpdate(),expected.getDateLastUpdate());
        assertEquals(entity.getDateLastMaintenance(), expected.getDateLastMaintenance());
        assertEquals(entity.getUserCountPurge(), expected.getUserCountPurge());

        deleteEntity(entity);
    }

    /**
     * Test of delete method, of class Entities.
     */
    @Test
    public void testDelete() {
        AppInfoEntity entity = new AppInfoEntity();
        persistEntity(entity);
        assertNotEquals("Invalid entity", entity.getId(), null);

        Long entityid = entity.getId();

        deleteEntity(entity);

        AppInfoEntity expected = findEntity(AppInfoEntity.class, entityid);
        assertEquals("Could not delete created entity", expected, null);
    }

    /**
     * Test of update method, of class Entities.
     */
    @Test
    public void testUpdate() {
        AppInfoEntity entity = new AppInfoEntity();
        entity.setVersion("1.0.0");
        persistEntity(entity);
        assertNotEquals("Invalid entity", entity.getId(), null);

        assertEquals(entity.getVersion(), "1.0.0");

        entity.setVersion("1.1.1");
        try {
            performOp(() -> entities.update(entity));
        } catch (Exception e) {
            fail("Could not update entity: " + e.getLocalizedMessage());
        }

        AppInfoEntity modified = findEntity(AppInfoEntity.class, entity.getId());
        assertEquals(modified.getVersion(), "1.1.1");

        deleteEntity(modified);

    }

    /**
     * Test of getCount method, of class Entities.
     */
    @Test
    public void testGetCount() {
        AppInfoEntity entity = new AppInfoEntity();
        persistEntity(entity);
        assertNotEquals("Invalid entity", entity.getId(), null);

        int count = entities.getCount(AppInfoEntity.class);
        assertTrue("The expected count is > 0", count > 0);

        deleteEntity(entity);
    }

    /**
     * Test of findAll method, of class Entities.
     */
    @Test
    public void testFindAll() {
        AppInfoEntity entity = new AppInfoEntity();
        persistEntity(entity);
        assertNotEquals("Invalid entity", entity.getId(), null);

        List result = entities.findAll(AppInfoEntity.class);
        assertTrue("Could not find any entities", result.size() > 0);

        deleteEntity(entity);
    }

    /**
     * Test of findRange method, of class Entities.
     */
    @Test
    public void testFindRange() {
        AppInfoEntity entity1 = new AppInfoEntity();
        persistEntity(entity1);
        assertNotEquals("Invalid entity", entity1.getId(), null);
        AppInfoEntity entity2 = new AppInfoEntity();
        persistEntity(entity2);
        assertNotEquals("Invalid entity", entity2.getId(), null);

        List result = entities.findRange(AppInfoEntity.class, 0, 1);
        assertTrue("Could not find entities in range 0 to 1", result.size() == 2);

        deleteEntity(entity1);
        deleteEntity(entity2);
    }

    /**
     * Test of find method, of class Entities.
     */
    @Test
    public void testFind() {
        AppInfoEntity entity = new AppInfoEntity();
        persistEntity(entity);
        assertNotEquals("Invalid entity", entity.getId(), null);

        AppInfoEntity foundentity = findEntity(AppInfoEntity.class, entity.getId());
        assertNotEquals("Entity not found", foundentity, null);

        deleteEntity(entity);
    }

    /**
     * Test of findByField method, of class Entities.
     */
    @Test
    public void testFindByField() {
        final String FIELD_VALUE = "VERSION";
        final String FIELD_VALUE_WRONG = "VERZ";

        AppInfoEntity entity = new AppInfoEntity();
        entity.setVersion(FIELD_VALUE);
        persistEntity(entity);
        assertNotEquals("Invalid entity", entity.getId(), null);

        List result = entities.findByField(AppInfoEntity.class, "version", FIELD_VALUE_WRONG);
        assertTrue("Wrong entity was found: " + result.size(), result.size() == 0);
        result = entities.findByField(AppInfoEntity.class, "version", FIELD_VALUE);
        assertTrue("Entity not found", result.size() > 0);

        deleteEntity(entity);
    }

    /**
     * Test of search method, of class Entities.
     */
    @Test
    public void testSearch() {
        final String FIELD_VALUE = "$$$TestTheSearch";
        final String KEYWORD = "$$$Test";
        final String KEYWORD_NO_HIT = "this leads to no hit";

        AppInfoEntity entity = new AppInfoEntity();
        entity.setVersion(FIELD_VALUE);
        persistEntity(entity);
        assertNotEquals("Invalid entity", entity.getId(), null);

        List<String> searchFields = Arrays.asList("version");
        List result = entities.search(AppInfoEntity.class, KEYWORD, searchFields, 10);
        assertTrue("Search failed: " + result.size(), result.size() > 0);

        result = entities.search(AppInfoEntity.class, KEYWORD_NO_HIT, searchFields, 10);
        assertTrue("Search found wrong entities", result.size() == 0);

        deleteEntity(entity);
    }
}
