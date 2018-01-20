/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.system.core;

import net.m4e.system.deployment.UpdateInit;
import net.m4e.system.deployment.Update_0_1_0;

/**
 * Central place for registering all available update classes. This is used
 * by update manager. Whenever a new update class is created, use this class for
 * registering it.
 * 
 * @author boto
 * Date of creation Aug 22, 2017
 */
public class AppUpdateRegistry {

    protected AppUpdateRegistry() {}

    /**
     * Register all available update class instances.
     * TODO: This ugly dependency on deployment package should be solved by evaluating
     *       the app config (in e.g. web.xml) and extracting the update handler class names.
     * 
     * @param um Update manager
     */
    public void registerAllUpdaters(AppUpdateManager um) {
        um.registerUpdater(new UpdateInit());
        um.registerUpdater(new Update_0_1_0());
    }
}
