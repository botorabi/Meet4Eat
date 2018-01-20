/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.system.core;

import java.lang.invoke.MethodHandles;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import net.m4e.common.Entities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A collection of app info related utilities.
 *
 * @author boto
 * @since Aug 22, 2017
 */
@ApplicationScoped
public class AppInfos {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Entities entities;


    /**
     * Default constructor needed by the container.
     */
    protected AppInfos() {
        entities = null;
    }

    /**
     * Create an AppInfos instance.
     * 
     * @param entities  Entities instance
     */
    @Inject
    public AppInfos(Entities entities) {
        this.entities = entities;
    }

    /**
     * Get the application info entity. There is one single entry in database for this entity.
     *
     * @return App info entity, or null if there is some problem to retrieve it.
     */
    public AppInfoEntity getAppInfoEntity() {
        List<AppInfoEntity> infos = entities.findAll(AppInfoEntity.class);
        if (infos.size() != 1) {
            LOGGER.error("*** Unexpected count of app info entity detected: " + infos.size());
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
            entities.update(info);
        }
        catch (Exception ex) {
            LOGGER.error("*** Problem occured while updating app information in database, reason: " + ex.getMessage());
        }
    }
}
