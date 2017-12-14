/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.system.core;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import net.m4e.app.auth.AuthRole;
import net.m4e.common.ResponseResults;

/**
 * REST API for getting application information.
 *
 * @author boto
 * Date of creation Aug 16, 2017
 */
@Stateless
@Path("/rest/appinfo")
public class AppInfoEntityFacadeREST extends net.m4e.common.AbstractFacade<AppInfoEntity> {

    /**
     * Used for logging
     */
    private final static String TAG = "AppInfoEntityFacadeREST";


    private final AppInfos autils;

    @Inject
    public AppInfoEntityFacadeREST(AppInfos autils) {
        super(AppInfoEntity.class);
        this.autils = autils;
    }

    @Inject
    private EntityManager entityManager;


    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * Get app version information.
     *
     * @return JSON response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles = {AuthRole.VIRT_ROLE_GUEST})
    public String getInfo() {
        AppInfoEntity info = autils.getAppInfoEntity();
        if (info == null) {
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Internal error: no application information exists.", ResponseResults.CODE_INTERNAL_SRV_ERROR, null);
        }
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        jsonresponse.add("version", info.getVersion());
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }
}
