/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.system.core;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;

import net.m4e.common.Entities;

/**
 * A collection of app info related utilities.
 * 
 * @author boto
 * Date of creation Aug 22, 2017
 */
@Singleton
public class AppInfos {

    /**
     * Used for logging
     */
    private final static String TAG = "AppInfos";

    private EntityManager entityManager;

    /**
     * Create the instance for given entity manager.
     * 
     * @param entityManager   Entity manager
     */
    @Inject
    public AppInfos(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Get the application info entity. There is one single entry in database for this entity.
     * 
     * @return App info entity, or null if there is some problem to retrieve it.
     */
    public AppInfoEntity getAppInfoEntity() {
        //! TODO to speed up the access to this single entity we may consider a global scoped bean or 
        //        at least a session bean or something similar!

        Entities eutils = new Entities(entityManager);
        List<AppInfoEntity> infos = eutils.findAllEntities(AppInfoEntity.class);
        if (infos.size() != 1) {
            Log.error(TAG, "*** Unexpected count of app info entity detected: " + infos.size());
            return null;
        }
        return infos.get(0);
    }

    /**
     * Update app info entity.
     * 
     * @param info App info entity
     */
    public void updateAppInfoEntity(AppInfoEntity info) {
        Entities eutils = new Entities(entityManager);
        try {
            eutils.updateEntity(info);
        }
        catch (Exception ex) {
            Log.error(TAG, "*** Problem occured while updating app information in database, reason: " + ex.getMessage());
        }
    }
}
