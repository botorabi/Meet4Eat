/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.common;

import java.util.List;
import javax.persistence.EntityManager;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

/**
 * A collection of usual entity related utilities.
 * 
 * @author boto
 * Date of creation Aug 22, 2017
 */
public class EntityUtils {

    private final EntityManager entityManager;
    private final UserTransaction userTransaction;

    /**
     * Create the utils instance for given entity manager and user transaction object.
     * 
     * @param entityManager   Entity manager
     * @param userTransaction User transaction
     */
    public EntityUtils(EntityManager entityManager, UserTransaction userTransaction) {
        this.entityManager = entityManager;
        this.userTransaction = userTransaction;
    }

    /**
     * Create the entity in persistence layer.
     * 
     * @param <T>
     * @param entity        Entity instance which is created in database
     * @throws Exception    Throws exception if any problem occurred.
     */
    public <T> void createEntity(T entity) throws Exception {
        try {
            userTransaction.begin();
            entityManager.persist(entity);        
            userTransaction.commit();
        }
        catch(NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            throw ex;
        }
    }

    /**
     * Delete the entity from persistence layer.
     * 
     * @param <T>
     * @param entity        Entity instance which is deleted in database
     * @throws Exception    Throws exception if any problem occurs.
     */
    public <T> void deleteEntity(T entity) throws Exception {
        try {
            userTransaction.begin();
            entityManager.remove(entityManager.merge(entity));        
            userTransaction.commit();
        }
        catch(NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            throw ex;
        }
    }

    /**
     * Update the entity in persistence layer.
     * 
     * @param <T>
     * @param entity        Entity instance which is updated in database
     * @throws Exception    Throws exception if any problem occurs.
     */
    public <T> void updateEntity(T entity) throws Exception {
        try {
            userTransaction.begin();
            entityManager.merge(entity);        
            userTransaction.commit();
        }
        catch(NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            throw ex;
        }
    }

   /**
     * Get the total count of existing entities of given class.
     * 
     * @param <T>           Entity class type
     * @param entityClass   Pass the entity class
     * @return Total count of entities of given class
     */
    public <T> int getEntityCount(Class<T> entityClass) {
        javax.persistence.criteria.CriteriaQuery cq = entityManager.getCriteriaBuilder().createQuery();
        javax.persistence.criteria.Root<T> rt = cq.from(entityClass);
        cq.select(entityManager.getCriteriaBuilder().count(rt));
        javax.persistence.Query q = entityManager.createQuery(cq);
        return ((Long)q.getSingleResult()).intValue();
    }

    /**
     * Find all entities of given type.
     * 
     * @param <T>           Entity class type
     * @param entityClass   Pass the entity class
     * @return List of found entities.
     */
    public <T> List<T> findAllEntities(Class<T> entityClass) {
        javax.persistence.criteria.CriteriaQuery cq = entityManager.getCriteriaBuilder().createQuery();
        cq.select(cq.from(entityClass));
        javax.persistence.Query q = entityManager.createQuery(cq);
        List<T> res = q.getResultList();
        return res;
    }

    /**
     * Try to find all entities which has the value 'matchName' in their field 'fieldValue'.
     * 
     * @param <T>           Entity class type
     * @param entityClass   Pass the entity class
     * @param fieldName     Name of field which is checked for value
     * @param fieldValue    Value to check in given field
     * @return List of found entities.
     */
    public <T> List<T> findEntityByField(Class<T> entityClass, String fieldName, String fieldValue) {
        javax.persistence.criteria.CriteriaQuery cq = entityManager.getCriteriaBuilder().createQuery();
        javax.persistence.criteria.Root<T> rt = cq.from(entityClass);
        cq.select(rt).where(entityManager.getCriteriaBuilder().equal(rt.get(fieldName), fieldValue));
        javax.persistence.Query q = entityManager.createQuery(cq);
        List<T> res = q.getResultList();
        return res;
    }
}
