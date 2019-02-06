/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.system.core;

import io.swagger.annotations.*;
import net.m4e.app.auth.AuthRole;
import net.m4e.common.GenericResponseResult;
import org.jetbrains.annotations.NotNull;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * REST API for getting application information.
 *
 * @author boto
 * Date of creation Aug 16, 2017
 */
@Stateless
@Path("/rest/appinfo")
@Api(value = "Application information")
public class AppInfoRestService {

    private final AppInfos appInfos;

    /**
     * Create the REST bean.
     */
    @Inject
    public AppInfoRestService(@NotNull AppInfos appInfos) {
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
    @ApiOperation("Get app version information")
    public GenericResponseResult<ResponseDataAppInfo> getInfo() {
        AppInfoEntity info = appInfos.getAppInfoEntity();
        if (info == null) {
            return GenericResponseResult.internalError("Internal error: no application information exists.");
        }
        ResponseDataAppInfo appInfo = new ResponseDataAppInfo(info.getVersion());
        return GenericResponseResult.ok("App information", appInfo);
    }


    /**
     * Class describing the response data for GET (getInfo)
     */
    public static class ResponseDataAppInfo {
        private String version;

        public ResponseDataAppInfo(final String version) {
            this.version = version;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }
}
