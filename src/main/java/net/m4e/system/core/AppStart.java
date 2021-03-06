/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.system.core;

import io.swagger.jaxrs.config.BeanConfig;

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

    public AppStart() {
        String appVersion = AppConfiguration.getInstance().getConfigValue(AppConfiguration.TOKEN_APP_VERSION);

        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion(appVersion);
        beanConfig.setTitle("Meat4Eat");
        beanConfig.setSchemes(new String[] {"http", "https"});
        beanConfig.setHost("localhost:8080");
        beanConfig.setBasePath("/m4e/" + AppConfiguration.REST_BASE_URL);
        beanConfig.setResourcePackage("net.m4e");
        beanConfig.setScan(true);
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        addRestResourceClasses(resources);

        resources.add(io.swagger.jaxrs.listing.ApiListingResource.class);
        resources.add(io.swagger.jaxrs.listing.SwaggerSerializers.class);

        return resources;
    }

    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(net.m4e.app.event.rest.EventRestService.class);
        resources.add(net.m4e.app.event.rest.EventLocationVoteRestService.class);
        resources.add(net.m4e.app.mailbox.rest.MailRestService.class);
        resources.add(net.m4e.app.resources.DocumentRestService.class);
        resources.add(net.m4e.app.user.rest.UserAuthenticationRestService.class);
        resources.add(net.m4e.app.user.rest.UserRestService.class);
        resources.add(net.m4e.system.core.AppInfoRestService.class);
        resources.add(net.m4e.system.maintenance.rest.MaintenanceRestService.class);
        resources.add(net.m4e.update.rest.UpdateCheckRestService.class);
    }
}
