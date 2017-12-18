/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.common;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Produces;


/**
 * Provide the entity manager. This is the central place to get the entity manager.
 * Here is the place to decide which persistence unit is used for the entity manager
 * depending on execution environment (dev, prod, or testing).
 * 
 * @author boto
 * Date of creation Dec 18, 2017
 */
@ApplicationScoped
public class EntityManagerProvider {

    /**
     * Entity manager needed for entity retrieval and modifications.
     */
    @PersistenceContext(unitName = net.m4e.system.core.AppConfiguration.PERSITENCE_UNIT_NAME)
    private EntityManager entityManager;

    /**
     * Get the entity manager.
     * 
     * @return   Entity manager
     */
    @Produces
    public EntityManager getEntityManager() {
        return entityManager;
    }
}
