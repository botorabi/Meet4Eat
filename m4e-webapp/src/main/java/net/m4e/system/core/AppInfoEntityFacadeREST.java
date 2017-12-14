/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.system.core;

import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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

    @PersistenceContext(unitName = AppConfiguration.PERSITENCE_UNIT_NAME)
    private EntityManager entityManager;

    public AppInfoEntityFacadeREST() {
        super(AppInfoEntity.class);
    }

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
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_GUEST})
    public String getInfo() {
        AppInfos autils = new AppInfos(entityManager);
        AppInfoEntity info = autils.getAppInfoEntity();
        if (info == null) {
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Internal error: no application information exists.", ResponseResults.CODE_INTERNAL_SRV_ERROR, null);
        }
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        jsonresponse.add("version", info.getVersion());
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }
}
