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
import javax.persistence.criteria.Predicate;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import net.m4e.core.Log;

/**
 * A collection of usual entity related utilities.
 * 
 * @author boto
 * Date of creation Aug 22, 2017
 */
public class EntityUtils {

    /**
     * Used for logging
     */
    private final static String TAG = "EntityUtils";

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
            userTransaction.rollback();
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
            userTransaction.rollback();
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
            userTransaction.rollback();
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
     * Try to find any entity given its ID.
     * 
     * @param <T>           Entity class type
     * @param entityClass   Pass the entity class
     * @param id            Entity's ID
     * @return Instance of found entity, or null if no entity with given ID was found.
     */
    public <T> T findEntity(Class<T> entityClass, Long id) {
        return entityManager.find(entityClass, id);
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

    /**
     * Search the database for an entity type and given keyword in fields. The keyword is
     * used to search for similarities in fields.
     * 
     * @param <T>           Entity class type
     * @param entityClass   Pass the entity class
     * @param keyword       Keyword for similarity check, it must be at least 3 characters.
     * @param searchFields  Given fields of entity are searched for similarity
     * @param maxResults    Maximal count of results.
     * @return              A List of search hits.
     */
    public <T> List<T> search(Class<T> entityClass, String keyword, List<String> searchFields, int maxResults) {
        List<T> results = new ArrayList<>();
        if (searchFields.size() < 1) {
            Log.warning(TAG, "Cannot search for keyword '" + keyword + "', no search fields defined!");
            return results;
        }
        if (keyword.length() < 3) {
            Log.warning(TAG, "Cannot search for keyword '" + keyword + "', need at least 3 characters!");
            return results;
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        javax.persistence.criteria.CriteriaQuery cq = cb.createQuery();
        javax.persistence.criteria.Root<T> rt = cq.from(entityClass);

        Predicate predtotal;
        List<Predicate> predicates = new ArrayList<>();
        for (int i = 0; i < searchFields.size(); i++) {
            predicates.add(cb.like(rt.get(searchFields.get(i)), "%" + keyword + "%"));
        }
        // is predicate chaining necessary?
        if (predicates.size() > 1) {
            predtotal = cb.or(predicates.get(0), predicates.get(1));
            for (int i = 2; i < predicates.size(); i++) {
                predtotal = cb.or(predtotal, predicates.get(i));
            }
        }
        else {
            predtotal = predicates.get(0);
        }

        cq.select(rt).where(predtotal);
        javax.persistence.Query q = entityManager.createQuery(cq);
        List<T> res = q.setMaxResults(maxResults).getResultList();

        return res;
    }
}
