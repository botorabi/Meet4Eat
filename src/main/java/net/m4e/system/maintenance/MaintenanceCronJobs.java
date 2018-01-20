/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.system.maintenance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.lang.invoke.MethodHandles;


/**
 * All periodic tasks for maintenance are implemented in this bean.
 * 
 * @author boto
 * Date of creation Sep 8, 2017
 */
@Stateless
public class MaintenanceCronJobs {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Maintenance maintenance;

    /**
     * EJB's default constructor.
     */
    public MaintenanceCronJobs() {
        this.maintenance = null;
    }

    /**
     * Create the bean.
     * 
     * @param maintenance   The maintenance instance
     */
    @Inject
    public MaintenanceCronJobs(Maintenance maintenance) {
        this.maintenance = maintenance;
    }

    /**
     * Here all jobs for midnight are done.
     */
    @Schedule(hour="0", persistent=false)
    public void nightlyJobs(){
        LOGGER.info("starting midnight maintenance tasks");
        int countpurges = maintenance.purgeExpiredResources();
        LOGGER.info(" count of purged expired resource: " + countpurges);
    }
}
