/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.system.core;

import javax.ws.rs.core.Application;
import java.util.Set;

/**
 * Central application configuration
 *
 * @author boto
 * Date of creation Aug 20, 2017 
 */
@javax.ws.rs.ApplicationPath(AppConfiguration.REST_BASE_URL)
public class AppStart extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        addRestResourceClasses(resources);
        return resources;
    }

    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
     * all resources defined in the project.
     * If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(net.m4e.app.event.EventEntityFacadeREST.class);
        resources.add(net.m4e.app.event.EventLocationVoteEntityFacadeREST.class);
        resources.add(net.m4e.app.mailbox.MailEntityFacadeREST.class);
        resources.add(net.m4e.app.resources.DocumentEntityFacadeREST.class);
        resources.add(net.m4e.app.user.UserAuthenticationFacadeREST.class);
        resources.add(net.m4e.app.user.UserEntityFacadeREST.class);
        resources.add(net.m4e.system.core.AppInfoEntityFacadeREST.class);
        resources.add(net.m4e.system.maintenance.MaintenanceFacadeREST.class);
        resources.add(net.m4e.update.UpdateCheckEntityFacadeREST.class);
    }

}
