/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.system.core;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import net.m4e.app.auth.AuthRole;
import net.m4e.common.GenericResponseResult;
import net.m4e.common.ResponseResults;

/**
 * REST API for getting application information.
 *
 * @author boto
 * Date of creation Aug 16, 2017
 */
@Stateless
@Path("/rest/appinfo")
public class AppInfoEntityFacadeREST {

    private final AppInfos appInfos;

    /**
     * EJB's default constructor.
     */
    public AppInfoEntityFacadeREST() {
        this.appInfos = null;
    }

    /**
     * Create the REST bean.
     * 
     * @param appInfos 
     */
    @Inject
    public AppInfoEntityFacadeREST(AppInfos appInfos) {
        this.appInfos = appInfos;
    }

    /**
     * Get app version information.
     *
     * @return JSON response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @AuthRole(grantRoles = {AuthRole.VIRT_ROLE_GUEST})
    public GenericResponseResult<AppInfo> getInfo() {
        AppInfoEntity info = appInfos.getAppInfoEntity();
        if (info == null) {
            return new GenericResponseResult<>(ResponseResults.STATUS_NOT_OK, "Internal error: no application information exists.", ResponseResults.CODE_INTERNAL_SRV_ERROR, null);
        }
        AppInfo appInfo = new AppInfo(info.getVersion());

        return new GenericResponseResult<>(ResponseResults.STATUS_OK, "", ResponseResults.CODE_OK, appInfo);
    }

    public static class AppInfo {
        //TODO: Replace with annotated AppInfoEntity
        public String version;

        public AppInfo(final String version) {
            this.version = version;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(final String version) {
            this.version = version;
        }
    }
}
