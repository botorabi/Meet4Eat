/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.system.maintenance;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import net.m4e.app.auth.AuthRole;
import net.m4e.common.ResponseResults;
import net.m4e.system.core.AppInfoEntity;
import net.m4e.system.core.AppInfos;
import net.m4e.system.core.Log;

/**
 * REST Web Service for maintenance tasks
 *
 * @author boto
 * Date of creation Sep 8, 2017
 */
@Stateless
@Path("/rest/maintenance")
public class MaintenanceFacadeREST {

    /**
     * Used for logging
     */
    private final static String TAG = "MaintenanceFacadeREST";

    private final Maintenance maintenance;

    private final AppInfos appInfos;

    /**
     * EJB's default constructor.
     */
    protected MaintenanceFacadeREST() {
        this.maintenance = null;
        this.appInfos = null;
    }

    /**
     * Create the bean.
     * 
     * @param maintenance   The maintenance instance
     * @param appInfos      AppInfos instance used for accessing application information such as version and stats
     */
    @Inject
    public MaintenanceFacadeREST(Maintenance maintenance, AppInfos appInfos) {
        this.maintenance = maintenance;
        this.appInfos = appInfos;
    }

    /**
     * Get system statistics information.
     * 
     * @return JSON response
     */
    @GET
    @Path("stats")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.USER_ROLE_ADMIN})
    public String stats() {
        AppInfoEntity info = appInfos.getAppInfoEntity();
        if (info == null) {
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Internal error: no application information exists.", ResponseResults.CODE_INTERNAL_SRV_ERROR, null);
        }
        String appstats = maintenance.exportInfoJSON(info).build().toString();
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "System stats", ResponseResults.CODE_OK, appstats);
    }

    /**
     * Perform purging resources.
     * 
     * @return JSON response
     */
    @GET
    @Path("purge")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.USER_ROLE_ADMIN})
    public String purgeResources() {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        int countpurges = maintenance.purgeAllResources();
        Log.info(TAG, "total count of " + countpurges + " resources were purged");
        jsonresponse.add("countPurges", countpurges);
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "" +  countpurges + " resources were purged.", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }

    /**
     * TODO: implement following services:
     * 
     * - send maintenance notification to clients
     * - start/stop maintenance
     */
}
