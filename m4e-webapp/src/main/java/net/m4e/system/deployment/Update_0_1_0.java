/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.system.deployment;

import net.m4e.common.Entities;
import net.m4e.system.core.AppUdateBaseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.lang.invoke.MethodHandles;

/**
 * Deployment updater for version "0.1.0"
 * 
 * @author boto
 * Date of creation Aug 22, 2017
 */
public class Update_0_1_0 extends AppUdateBaseHandler {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Make sure to increment this number for every new update class.
     */
    private static final int    INC_NUMBER = 1;

    /**
     * App version this update belongs to
     */
    private static final String APP_VERSION = "0.1.0";

    /**
     * Construct the update instance.
     */
    public Update_0_1_0() {
        incUpdateNumber = INC_NUMBER;
        appVersion = APP_VERSION;
    }

    /**
     * Perform the update.
     * 
     * @param entityManager   For the case that any entity structure manipulation is needed
     * @param entities        Entities contains entity related operations
     */
    @Override
    public void performUpdate(EntityManager entityManager, Entities entities) {
        LOGGER.debug("Updating to version: " + appVersion + " (" + incUpdateNumber + ")");
        LOGGER.debug(" Updating to version: " + appVersion + " (" + incUpdateNumber + ") completed");
    }
}
