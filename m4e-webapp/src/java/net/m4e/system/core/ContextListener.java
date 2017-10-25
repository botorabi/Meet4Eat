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
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
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
        String mailercfg = ctx.getInitParameter(AppConfiguration.TOKEN_MAILER_CONFIG_FILE);
        AppConfiguration.getInstance().setConfigValue(AppConfiguration.TOKEN_MAILER_CONFIG_FILE, mailercfg);
        String userregcfg = ctx.getInitParameter(AppConfiguration.TOKEN_USER_REGISTRATION_CONFIG_FILE);
        AppConfiguration.getInstance().setConfigValue(AppConfiguration.TOKEN_USER_REGISTRATION_CONFIG_FILE, userregcfg);

        // handle a possible deployment update
        AppUpdateManager um = new AppUpdateManager(entityManager);
        Throwable problem = null;
        try {
            // embed the update tasks in user transaction
            userTransaction.begin();
            um.checkForUpdate(appversion);
            userTransaction.commit();
        }
        catch(NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            problem = ex;
        }
        if (null != problem) {
            Log.error( TAG, "problem occurred while update checking, reason: " + problem.getLocalizedMessage());
            try {
                userTransaction.rollback();
            }
            catch(SystemException ex) {
                Log.error( TAG, "problem occurred while rolling back any transaction, reason: " + ex.getLocalizedMessage());                
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Log.info(TAG, "Destroying the servlet container");
    }
}
