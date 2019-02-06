/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
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
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.transaction.*;
import java.lang.invoke.MethodHandles;


/**
 * Web application lifecycle listener.
 *
 * @author boto
 * Date of creation Aug 20, 2017
 */
public class ContextListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final AppUpdater appUpdater;

    @Resource
    private UserTransaction userTransaction;

    @Inject
    public ContextListener(AppUpdater appUpdater) {
        this.appUpdater = appUpdater;
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        LOGGER.info("Starting the servlet container");

        AppConfiguration.getInstance().setup(event.getServletContext());

        checkAndPerformDeployment();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("Destroying the servlet container");
    }

    protected void checkAndPerformDeployment() {
        try {
            handlePossibleUpdate();
        }
        catch(Exception exception) {
            handleUpdateProblem(exception);
        }
    }

    protected void handlePossibleUpdate() throws Exception {
        // we need to embed the update tasks in user transaction
        userTransaction.begin();
        appUpdater.updateIfNeeded();
        userTransaction.commit();
    }

    protected void handleUpdateProblem(Exception exception) {
        LOGGER.error("problem occurred while update checking, reason: {}", exception.getMessage());
        try {
            userTransaction.rollback();
        }
        catch(Exception rollbackException) {
            LOGGER.error("problem occurred while rolling back transaction, reason: {}", rollbackException.getMessage());
        }
    }
}
