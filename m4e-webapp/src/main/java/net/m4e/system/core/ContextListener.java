/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.system.core;

import java.io.InputStream;
import javax.annotation.Resource;
import javax.inject.Inject;
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

    private final AppUpdateManager updateManager;

    @Resource
    private UserTransaction userTransaction;

    /**
     * Create the context listener
     * 
     * @param updateManager The update manager
     */
    @Inject
    public ContextListener(AppUpdateManager updateManager) {
        this.updateManager = updateManager;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Log.info(TAG, "Starting the servlet container");

        // save the context parameters in app configuration
        ServletContext ctx = sce.getServletContext();
        setupConfiguration(ctx);

        // handle a possible deployment update
        Throwable problem = null;
        try {
            String appversion = AppConfiguration.getInstance().getConfigValue(AppConfiguration.TOKEN_APP_VERSION);
            // embed the update tasks in user transaction
            userTransaction.begin();
            updateManager.checkForUpdate(appversion);
            userTransaction.commit();
        }
        catch(NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            problem = ex;
        }
        if (problem != null) {
            Log.error( TAG, "problem occurred while update checking, reason: " + problem.getLocalizedMessage());
            try {
                userTransaction.rollback();
            }
            catch(SystemException ex) {
                Log.error( TAG, "problem occurred while rolling back transaction, reason: " + ex.getLocalizedMessage());                
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Log.info(TAG, "Destroying the servlet container");
    }

    /**
     * Setup the app configuration.
     * 
     * @param ctx Servlet context
     */
    private void setupConfiguration(ServletContext ctx) {
        // save the context parameters in app configuration
        String appversion = ctx.getInitParameter(AppConfiguration.TOKEN_APP_VERSION);
        AppConfiguration.getInstance().setConfigValue(AppConfiguration.TOKEN_APP_VERSION, appversion);
        String mailercfg = ctx.getInitParameter(AppConfiguration.TOKEN_MAILER_CONFIG_FILE);
        AppConfiguration.getInstance().setConfigValue(AppConfiguration.TOKEN_MAILER_CONFIG_FILE, mailercfg);

        // setup the user registration configuration
        String accountregcfg = ctx.getInitParameter(AppConfiguration.TOKEN_ACC_REGISTRATION_CONFIG_FILE);
        InputStream configcontent = ctx.getResourceAsStream("/WEB-INF/" + accountregcfg);
        AppConfiguration.getInstance().setupAccountRegistrationConfig(configcontent);
        if (configcontent == null) {
            Log.warning(TAG, "No account registration config file was found, using defaults!");
        }        
    }
}
