/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.common;

import java.util.List;
import javax.persistence.EntityManager;

/**
 * Facade class used for all classes providing entity manipulation functionality.
 * 
 * @author boto
 * Date of creation Aug 18, 2017
 */
public abstract class AbstractFacade<T> {

    private Class<T> entityClass;

    public AbstractFacade(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected abstract EntityManager getEntityManager();

    /**
     * Create a new entity.
     * 
     * @param entity 
     */
    public void create(T entity) {
        getEntityManager().persist(entity);
    }

    /**
     * Modify an existing entity.
     * 
     * @param entity 
     */
    public void edit(T entity) {
        getEntityManager().merge(entity);
    }

    /**
     * Delete an existing entity.
     * 
     * @param entity 
     */
    public void remove(T entity) {
        getEntityManager().remove(getEntityManager().merge(entity));
    }

    /**
     * Find an entity with given ID.
     * 
     * @param id
     * @return      Entity with given ID or null if the ID was not found.
     */
    public T find(Object id) {
        return getEntityManager().find(entityClass, id);
    }

    /**
     * Get all available entities.
     * 
     * @return  List of all existing entities
     */
    public List<T> findAll() {
        javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        cq.select(cq.from(entityClass));
        return getEntityManager().createQuery(cq).getResultList();
    }

    /**
     * Find entities in given range, used usually for pagination.
     * 
     * @param range An integer array with two elements [low range, high range]
     * @return List of entities in given range.
     */
    public List<T> findRange(int[] range) {
        javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        cq.select(cq.from(entityClass));
        javax.persistence.Query q = getEntityManager().createQuery(cq);
        q.setMaxResults(range[1] - range[0] + 1);
        q.setFirstResult(range[0]);
        return q.getResultList();
    }

    /**
     * Get the total count of existing entities.
     * 
     * @return Total count of entities
     */
    public int count() {
        javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        javax.persistence.criteria.Root<T> rt = cq.from(entityClass);
        cq.select(getEntityManager().getCriteriaBuilder().count(rt));
        javax.persistence.Query q = getEntityManager().createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }
}
