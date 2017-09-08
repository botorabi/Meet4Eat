/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.system.core;

import java.util.List;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import net.m4e.common.EntityUtils;

/**
 * A collection of app info related utilities.
 * 
 * @author boto
 * Date of creation Aug 22, 2017
 */
public class AppInfoUtils {

    /**
     * Used for logging
     */
    private final static String TAG = "AppInfoUtils";

    private final EntityManager entityManager;

    private final UserTransaction userTransaction;

    /**
     * Create the utils instance for given entity manager and user transaction object.
     * 
     * @param entityManager   Entity manager
     * @param userTransaction User transaction
     */
    public AppInfoUtils(EntityManager entityManager, UserTransaction userTransaction) {
        this.entityManager = entityManager;
        this.userTransaction = userTransaction;
    }

    /**
     * Get the application info entity. There is one single entry in database for this entity.
     * 
     * @return App info entity, or null if there is some problem to retrieve it.
     */
    public AppInfoEntity getAppInfoEntity() {
        //! TODO to speed up the access to this single entity we may consider a global scoped bean or 
        //        at least a session bean or something similar!

        EntityUtils eutils = new EntityUtils(entityManager, userTransaction);
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
        EntityUtils eutils = new EntityUtils(entityManager, userTransaction);
        try {
            eutils.updateEntity(info);
        }
        catch (Exception ex) {
            Log.error(TAG, "*** Problem occured while updating app information in database, reason: " + ex.getLocalizedMessage());
        }
    }
}
