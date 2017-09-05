/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.core;

import java.util.Set;
import javax.ws.rs.core.Application;

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
        resources.add(net.m4e.core.AppInfoEntityFacadeREST.class);
        resources.add(net.m4e.event.EventEntityFacadeREST.class);
        resources.add(net.m4e.user.UserAuthenticationFacadeREST.class);
        resources.add(net.m4e.user.UserEntityFacadeREST.class);
    }
}
