/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.system.maintenance;

import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import net.m4e.system.core.Log;


/**
 * All periodic tasks for maintenance are implemented in this bean.
 * 
 * @author boto
 * Date of creation Sep 8, 2017
 */
@Stateless
public class MaintenanceCronJobs {

    /**
     * Used for logging
     */
    private final static String TAG = "MaintenanceCronJobs";

    /**
     * Entity manager needed for entity retrieval and modifications.
     */
    @PersistenceContext(unitName = net.m4e.system.core.AppConfiguration.PERSITENCE_UNIT_NAME)
    private EntityManager entityManager;

    /**
     * Create the bean.
     */
    public MaintenanceCronJobs() {
    }

    /**
     * Here all jobs for midnight are done.
     */
    @Schedule(hour="0", persistent=false)
    public void nightlyJobs(){
        Log.info(TAG, "starting midnight maintenance tasks");

        Maintenance maintenance = new Maintenance(entityManager);
        int countpurges = maintenance.purgeExpiredResources();

        Log.info(TAG, " count of purged expired resource: " + countpurges);
    }
}
