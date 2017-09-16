/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.system.maintenance;

import java.util.Objects;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import net.m4e.app.auth.AuthRole;
import net.m4e.common.ResponseResults;
import net.m4e.system.core.AppInfoEntity;
import net.m4e.system.core.AppInfoUtils;

/**
 * REST Web Service for maintenance tasks
 *
 * @author boto
 * Date of creation Sep 8, 2017
 */
@Stateless
@Path("/rest/maintenance")
@TransactionManagement(TransactionManagementType.BEAN)
public class MaintenanceFacadeREST {

    /**
     * Used for logging
     */
    private final static String TAG = "MaintenanceFacadeREST";

    /**
     * Entity manager needed for entity retrieval and modifications.
     */
    @PersistenceContext(unitName = net.m4e.system.core.AppConfiguration.PERSITENCE_UNIT_NAME)
    private EntityManager entityManager;

    /**
     * User transaction needed for entity modifications.
     */
    @Resource
    private UserTransaction userTransaction;

    /**
     * Creates a new instance of MaintenanceResource
     */
    public MaintenanceFacadeREST() {
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
        AppInfoUtils autils = new AppInfoUtils(entityManager, null);
        AppInfoEntity info = autils.getAppInfoEntity();
        if (Objects.isNull(info)) {
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Internal error: no application information exists.", ResponseResults.CODE_INTERNAL_SRV_ERROR, null);
        }
        MaintenanceUtils mutils = new MaintenanceUtils(entityManager, userTransaction);
        String appstats = mutils.exportInfoJSON(info).build().toString();
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "System stats", ResponseResults.CODE_OK, appstats);
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
        MaintenanceUtils mutils = new MaintenanceUtils(entityManager, userTransaction);
        int countpurges = mutils.purgeResources();
        jsonresponse.add("countPurges", countpurges);
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "" +  countpurges + " resources were purged.", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }

    /**
     * TODO: implement following services:
     * 
     * - send maintenance notification to clients
     * - start/stop maintenance
     */
}
