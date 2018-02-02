/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.system.core;

import net.m4e.common.Entities;
import org.slf4j.*;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.lang.invoke.MethodHandles;
import java.util.*;

/**
 * This class cares about application updates. Whenever a new app version
 * is deployed this class deals with necessary steps such as updating data structures.
 * It is used during application startup.
 * 
 * NOTE: Every deployed application version must have an own registered updater
 *       in order to handle incremental updates properly. See AppUpdateRegistry class.
 * 
 * @author boto
 * Date of creation Aug 22, 2017
 */
public class AppUpdater {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final EntityManager entityManager;

    private final Entities entities;

    private final AppInfos appInfos;

    private final List<AppUpdateBaseHandler> updateRegistry = new ArrayList();


    @Inject
    public AppUpdater(EntityManager entityManager, Entities entities, AppInfos appInfos) {
        this.entityManager = entityManager;
        this.entities = entities;
        this.appInfos = appInfos;
    }

    /**
     * Register an instance of an update class. The manager selects a proper update class basing on current deployment.
     * The registration of all update classes happens when needed in method updateIfNeeded.
     * 
     * @param updater   Updater instance
     */
    protected void registerUpdater(AppUpdateBaseHandler updater) {
        updateRegistry.add(updater);
    }

    /**
     * Check if a new version of the application was deployed. If so then take the
     * necessary steps for performing the update.
     */
    public void updateIfNeeded() throws Exception {
        String appVersion = AppConfiguration.getInstance().getConfigValue(AppConfiguration.TOKEN_APP_VERSION);

        registerAllUpdaters();

        AppInfoEntity currentAppInfo = tryToFindAppInfo();

        if (isInitialDeployment(currentAppInfo)) {
            LOGGER.info("Initial version of application is being deployed.");
            performInitialUpdate();
            createAppVersionEntry(appVersion);
        }
        else {
            if (isNewAppVersion(currentAppInfo, appVersion)) {
                performUpdate(currentAppInfo.getVersion(), appVersion);
                updateAppVersionEntry(currentAppInfo, appVersion);
            }
        }
    }

    protected void registerAllUpdaters() {
        AppUpdateRegistry ur = new AppUpdateRegistry();
        ur.registerAllUpdaters(this);

        sortUpdateRegistry();
    }

    protected AppInfoEntity tryToFindAppInfo() throws Exception {
        List<AppInfoEntity> res = entities.findAll(AppInfoEntity.class);
        if (res.size() > 0) {
            if (res.size() > 1) {
                LOGGER.error("*** Error while checking the app info. More than one app info entry detected in database!");
                throw new Exception("More than one AppInfo entry was found in database!");
            }
            return res.get(0);
        }
        // this seems to be a very first deployment, there is no app info entry in database
        return null;
    }

    protected void sortUpdateRegistry() {
        updateRegistry.sort((Object left, Object right) -> {
            int incNumLeft  = ((AppUpdateBaseHandler)left).getIncUpdateNumber();
            int incNumRight = ((AppUpdateBaseHandler)right).getIncUpdateNumber();
            return incNumLeft - incNumRight;
        });
    }

    protected boolean isInitialDeployment(AppInfoEntity appInfo) {
        return appInfo == null;
    }

    protected boolean isNewAppVersion(AppInfoEntity appInfo, String appVersion) {
        if (appInfo == null || appVersion == null) {
            throw new IllegalArgumentException("Invalid appInfo argument");
        }
        return !appInfo.getVersion().equals(appVersion);
    }

    protected void createAppVersionEntry(String appVersion) {
        AppInfoEntity info = new AppInfoEntity();
        info.setVersion(appVersion);
        info.setDateLastUpdate((new Date().getTime()));
        try {
            entities.create(info);
        }
        catch (Exception ex) {
            LOGGER.error("*** Problem occurred while creating app information in database, reason: " + ex.getLocalizedMessage());
        }
    }

    protected void updateAppVersionEntry(AppInfoEntity info, String appVersion) {
        info.setVersion(appVersion);
        info.setDateLastUpdate((new Date().getTime()));
        appInfos.updateAppInfoEntity(info);
    }

    protected void performUpdate(String currentVersion, String newVersion) throws Exception {
        LOGGER.info("Start updating deployment...");
        int indexcurrent = updateRegistry.indexOf(findUpdater(currentVersion));
        int indexnew = updateRegistry.indexOf(findUpdater(newVersion));
        // check if there is an updater for this version
        if (indexnew < 0) {
            LOGGER.debug("There is no need for update migration for this version: " + currentVersion);
            LOGGER.info("Deployment updating successfully completed");
            return;
        }

        if (indexcurrent < 0) {
            LOGGER.warn("   Current version had no updater, update to new version skipping potential versions in between!");
            indexcurrent = indexnew;
        }

        // do some deployment consistency checks
        indexcurrent++;
        if (indexcurrent > indexnew) {
            LOGGER.error("*** New updater is older than the current updater (" + indexcurrent + " > " + indexnew + "). Check the deployed package, it may contain errors.");
            throw new Exception("Invalid updater configuration detected!");
        }
        // go through every incremental update up to the current version
        for (int i = indexcurrent; i <= indexnew; i++) {
            LOGGER.info("  Start updating to version " + updateRegistry.get(i).getAppVersion());
            try {
                updateRegistry.get(i).performUpdate(entityManager, entities);
                LOGGER.info("  Successfully updated to version " + updateRegistry.get(i).getAppVersion());
            }
            catch (Exception ex) {
                LOGGER.error("***  Failed to perform the deployment update, reason: " + ex.getMessage());
                throw new Exception("Failed to perform the deployment update, reason: " + ex.getMessage());
            }
        }
        LOGGER.info("Deployment updating successfully completed");
    }

    protected void performInitialUpdate() throws Exception {
        // NOTE updater version "0.0.0" is a special updater for initial setup, see class 'UpdateInit'.
        AppUpdateBaseHandler updater = findUpdater("0.0.0");
        if (updater == null) {
            throw new Exception("Could not find the initial updater.");
        }
        try {
            updater.performUpdate(entityManager, entities);
            LOGGER.info("  Successfully updated to initial version " + updater.getAppVersion());
        }
        catch (Exception ex) {
            String msg = "Failed to perform the initial deployment update, reason: " + ex.getMessage();
            LOGGER.error(msg);
            throw new Exception(msg);
        }
    }

    protected AppUpdateBaseHandler findUpdater(String appVersion) {
        for (AppUpdateBaseHandler u: updateRegistry) {
            if (u.getAppVersion().equals(appVersion)) {
                return u;
            }
        }
        return null;
    }
}
