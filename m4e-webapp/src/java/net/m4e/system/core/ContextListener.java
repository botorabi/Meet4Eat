/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.system.core;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.transaction.UserTransaction;


/**
 * Web application lifecycle listener.
 *
 * @author boto
 * Date of creation Aug 20, 2017
 */
public class ContextListener implements ServletContextListener {

    /**
     * Used for logging
     */
    private final static String TAG = "ContextListener";

    @PersistenceContext(unitName = net.m4e.system.core.AppConfiguration.PERSITENCE_UNIT_NAME)
    private EntityManager entityManager;

    @Resource
    private UserTransaction userTransaction;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Log.info(TAG, "Starting the servlet container");

        // save the context parameters in app configuration
        ServletContext ctx = sce.getServletContext();
        String appversion = ctx.getInitParameter(AppConfiguration.TOKEN_APP_VERSION);
        AppConfiguration.getInstance().setConfigValue(AppConfiguration.TOKEN_APP_VERSION, appversion);

        // handle a possible deployment update
        AppUpdateManager um = new AppUpdateManager(entityManager, userTransaction);
        um.checkForUpdate(appversion);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Log.info(TAG, "Destroying the servlet container");
    }
}
