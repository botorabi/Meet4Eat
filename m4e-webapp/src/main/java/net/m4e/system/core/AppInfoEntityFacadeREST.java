/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.system.core;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.m4e.app.auth.AuthRole;
import net.m4e.common.GenericResponseResult;
import net.m4e.common.ResponseResults;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
    @ApiOperation("Get app version information")
    public GenericResponseResult<ResponseDataAppInfo> getInfo() {
        AppInfoEntity info = appInfos.getAppInfoEntity();
        if (info == null) {
            return new GenericResponseResult<>(ResponseResults.STATUS_NOT_OK, "Internal error: no application information exists.", ResponseResults.CODE_INTERNAL_SRV_ERROR, null);
        }
        ResponseDataAppInfo appInfo = new ResponseDataAppInfo(info.getVersion());
        return new GenericResponseResult<>(ResponseResults.STATUS_OK, "", ResponseResults.CODE_OK, appInfo);
    }


    /**
     * Class describing the response data for GET (getInfo)
     */
    public static class ResponseDataAppInfo {
        public String version;

        public ResponseDataAppInfo(final String version) {
            this.version = version;
        }
    }
}
