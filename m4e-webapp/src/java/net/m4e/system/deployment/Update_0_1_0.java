/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.system.deployment;

import net.m4e.system.core.AppUdateBaseHandler;
import javax.persistence.EntityManager;
import net.m4e.system.core.Log;

/**
 * Deployment updater for version "0.1.0"
 * 
 * @author boto
 * Date of creation Aug 22, 2017
 */
public class Update_0_1_0 extends AppUdateBaseHandler {

    /**
     * Used for logging
     */
    private final static String TAG = "Update_0_1_0";

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
     * @throws Exception This exception is thrown if something went wrong.
     */
    @Override
    public void performUpdate(EntityManager entityManager) throws Exception {
        Log.debug(TAG, "Updating to version: " + appVersion + " (" + incUpdateNumber + ")");
        Log.debug(TAG, " Updating to version: " + appVersion + " (" + incUpdateNumber + ") completed");
    }
}
