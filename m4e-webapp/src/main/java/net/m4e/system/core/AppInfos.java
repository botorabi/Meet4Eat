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
 * @since Aug 22, 2017
 */
@Singleton
public class AppInfos {

    /**
     * Used for logging
     */
    private final static String TAG = "AppInfos";

    private final Entities eutils;

    /**
     * Create the instance for given entity manager.
     *
     * @param entityManager the Entity manager
     * @see this#AppInfos(Entities)
     */
    public AppInfos(EntityManager entityManager) {
        this.eutils = new Entities(entityManager);
    }

    /**
     * Create the instance with the given Entities.
     *
     * @param entities the Entities
     */
    @Inject
    public AppInfos(Entities entities) {
        this.eutils = entities;
    }

    /**
     * Get the application info entity. There is one single entry in database for this entity.
     *
     * @return App info entity, or null if there is some problem to retrieve it.
     */
    public AppInfoEntity getAppInfoEntity() {
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
     * @param info the App info entity
     */
    public void updateAppInfoEntity(AppInfoEntity info) {
        try {
            eutils.updateEntity(info);
        } catch (Exception ex) {
            Log.error(TAG,
                    "*** Problem occured while updating app information in database, reason: " + ex.getMessage());
        }
    }
}
