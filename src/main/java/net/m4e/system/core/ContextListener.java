/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.system.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.transaction.*;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;


/**
 * Web application lifecycle listener.
 *
 * @author boto
 * Date of creation Aug 20, 2017
 */
public class ContextListener implements ServletContextListener {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
        LOGGER.info("Starting the servlet container");

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
            LOGGER.error("problem occurred while update checking, reason: " + problem.getLocalizedMessage());
            try {
                userTransaction.rollback();
            }
            catch(SystemException ex) {
                LOGGER.error("problem occurred while rolling back transaction, reason: " + ex.getLocalizedMessage());
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("Destroying the servlet container");
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
            LOGGER.warn("No account registration config file was found, using defaults!");
        }        
    }
}
