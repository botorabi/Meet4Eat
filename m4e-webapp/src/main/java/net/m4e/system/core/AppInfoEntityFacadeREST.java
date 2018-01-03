/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.system.core;

import net.m4e.app.auth.AuthRole;
import net.m4e.common.ResponseResults;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
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
    public String getInfo() {
        AppInfoEntity info = appInfos.getAppInfoEntity();
        if (info == null) {
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Internal error: no application information exists.", ResponseResults.CODE_INTERNAL_SRV_ERROR, null);
        }
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        jsonresponse.add("version", info.getVersion());
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }
}
