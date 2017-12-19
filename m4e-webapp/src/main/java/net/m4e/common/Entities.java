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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;

import net.m4e.app.resources.DocumentEntity;
import net.m4e.app.resources.DocumentPool;
import net.m4e.system.core.Log;



/**
 * A collection of entity related utilities.
 *
 * @author boto
 * Date of creation Aug 22, 2017
 */
@ApplicationScoped
public class Entities {

    /**
     * Used for logging
     */
    private final static String TAG = "Entities";

    private final EntityManager entityManager;

    private final DocumentPool docPool;


    /**
     * Default constructor needed by the container.
     */
    protected Entities() {
        entityManager = null;
        docPool = null;
    }

    /**
     * Create an Entities instance by injection.
     * 
     * @param provider  Entity manager provider
     * @param docPool   Document pool
     */
    @Inject
    public Entities(EntityManagerProvider provider, DocumentPool docPool) {
        this.entityManager = provider.getEntityManager();
        this.docPool = docPool;
    }

    /**
     * Create the entity in persistence layer.
     * 
     * @param <T>           Entity class type
     * @param entity        Entity instance which is created in database
     */
    public <T> void create(T entity) {
        entityManager.persist(entity);
    }

    /**
     * Delete the entity from persistence layer.
     * 
     * @param <T>           Entity class type
     * @param entity        Entity instance which is deleted in database
     */
    public <T> void delete(T entity) {
        entityManager.remove(entityManager.merge(entity));        
    }

    /**
     * Update the entity in persistence layer.
     * 
     * @param <T>           Entity class type
     * @param entity        Entity instance which is updated in database
     */
    public <T> void update(T entity)  {
        entityManager.merge(entity);        
    }

   /**
     * Get the total count of existing entities of given class.
     * 
     * @param <T>           Entity class type
     * @param entityClass   Pass the entity class
     * @return Total count of entities of given class
     */
    public <T> int getCount(Class<T> entityClass) {
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
    public <T> List<T> findAll(Class<T> entityClass) {
        javax.persistence.criteria.CriteriaQuery cq = entityManager.getCriteriaBuilder().createQuery();
        cq.select(cq.from(entityClass));
        javax.persistence.Query q = entityManager.createQuery(cq);
        List<T> res = q.getResultList();
        return res;
    }

    /** 
     * Find entities in given range, used usually for pagination. 
     * 
     * @param <T>           Entity class type
     * @param entityClass   Pass the entity class
     * @param from          Range begin
     * @param to            Range end
     * @return List of entities in given range. 
     */ 
    public <T> List<T> findRange(Class<T> entityClass, int from, int to) {
        javax.persistence.criteria.CriteriaQuery cq = entityManager.getCriteriaBuilder().createQuery(); 
        cq.select(cq.from(entityClass)); 
        javax.persistence.Query q = entityManager.createQuery(cq); 
        q.setMaxResults(to - from + 1); 
        q.setFirstResult(from); 
        return q.getResultList(); 
    } 

    /**
     * Try to find any entity given its ID.
     * 
     * @param <T>           Entity class type
     * @param entityClass   Pass the entity class
     * @param id            Entity's ID
     * @return Instance of found entity, or null if no entity with given ID was found.
     */
    public <T> T find(Class<T> entityClass, Long id) {
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
    public <T> List<T> findByField(Class<T> entityClass, String fieldName, String fieldValue) {
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

    /**
     * This method updates the photo of an entity by using the document pool.
     * The given entity must implement the EntityWithPhoto interface.
     * If the new photo is the same as the old photo (ETags are compared) then the call is ignored.
     * If the new photo does not exist in the document pool then a new document entity is created and
     * added to the pool and set in given entity. Make sure that 'newPhoto' provides the following information:
     * 
     *   Document content: in this case it will be an image
     *   Encoding
     *   Resource URL
     * 
     * @param <T>           The entity type
     * @param entity        The entity which must implement the EntityWithPhoto interface
     * @param newPhoto      New photo
     * @throws Exception    Throws an exception if something goes wrong
     */
    public <T extends EntityWithPhoto> void updatePhoto(T entity, DocumentEntity newPhoto) throws Exception {
        DocumentEntity img = docPool.getOrCreatePoolDocument(newPhoto.getETag());
        // is the old photo the same as the new one?
        if (!docPool.compareETag(entity.getPhoto(), img.getETag())) {
            // release the old photo
            docPool.releasePoolDocument(entity.getPhoto());
            // was the document an existing one?
            if (img.getIsEmpty()) {
                img.updateContent(newPhoto.getContent());
                img.setType(DocumentEntity.TYPE_IMAGE);
                img.setEncoding(newPhoto.getEncoding());
                img.setResourceURL(newPhoto.getResourceURL());
            }
            entity.setPhoto(img);
        }
    }
}
