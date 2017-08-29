/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.deployment;

/**
 * Central place for registering all available update classes. This is used
 * by update manager. Whenever a new update class is created, use this class for
 * registering it.
 * 
 * @author boto
 * Date of creation Aug 22, 2017
 */
class UpdateRegistry {

    protected UpdateRegistry() {}

    /**
     * Register all available update class instances.
     * 
     * @param um Update manager
     */
    protected void registerAllUpdaters(AppUpdateManager um) {
        um.registerUpdater(new UpdateInit());
        um.registerUpdater(new Update_0_1_0());
    }
}
