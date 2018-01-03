/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.system.core;

import net.m4e.common.Entities;

import javax.persistence.EntityManager;

/**
 * Base class for all update classes. A concrete update class is automatically
 * selected by AppUpdateManager using the incremental update number and 
 * application version. During deploying a new application version, it is
 * responsible for performing necessary adaptations and cleanups in data
 * structures and other application resources.
 * 
 * @author boto
 * Date of creation Aug 22, 2017
 */
public abstract class AppUdateBaseHandler {

    /**
     * Incremental update number, this must be set properly in a concrete class.
     */
    protected int incUpdateNumber = 0;

    /**
     * Application version belonging to the update class. This must be set
     * properly in a concrete class.
     */
    protected String appVersion = "";

    /**
     * Construct the update instance.
     */
    public AppUdateBaseHandler() {}

    /**
     * Get the incremental update number. Every concrete update class
     * will have a unique incremental number. This is used to perform several
     * updates in right order.
     * 
     * @return Incremental update number
     */
    public int getIncUpdateNumber() {
        return incUpdateNumber;
    }

    /**
     * Get the application version this update class belongs to. This will
     * be matched with the fresh deployed application version.
     * 
     * @return Application version the updater belongs to.
     */
    public String getAppVersion() {
        return appVersion;
    }

    /**
     * Perform the update.
     * 
     * @param entityManager   For the case that any entity structure manipulation is needed
     * @param entities        Entities contains entity related operations
     * @throws Exception This exception is thrown if something went wrong.
     */
    public abstract void performUpdate(EntityManager entityManager, Entities entities) throws Exception;
}
