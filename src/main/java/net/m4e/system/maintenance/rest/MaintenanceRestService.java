/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.system.maintenance.rest;

import io.swagger.annotations.*;
import net.m4e.app.auth.AuthRole;
import net.m4e.common.GenericResponseResult;
import net.m4e.system.core.*;
import net.m4e.system.maintenance.Maintenance;
import net.m4e.system.maintenance.business.MaintenanceInfo;
import net.m4e.system.maintenance.rest.comm.PurgeCount;
import org.jetbrains.annotations.NotNull;
import org.slf4j.*;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.lang.invoke.MethodHandles;

/**
 * REST Web Service for maintenance tasks
 *
 * @author boto
 * Date of creation Sep 8, 2017
 */
@Stateless
@Path("/rest/maintenance")
@Api(value = "System Maintenance service")
public class MaintenanceRestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Maintenance maintenance;

    private final AppInfos appInfos;

    /**
     * Make the EJB container happy (e.g. for the case that we want to inject this bean in another bean).
     */
    protected MaintenanceRestService() {
        maintenance = null;
        appInfos = null;
    }

    /**
     * Create the bean.
     * 
     * @param maintenance   The maintenance instance
     * @param appInfos      AppInfos instance used for accessing application information such as version and stats
     */
    @Inject
    public MaintenanceRestService(@NotNull Maintenance maintenance, @NotNull AppInfos appInfos) {
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
    @ApiOperation(value = "Get system maintenance statistics")
    public GenericResponseResult<MaintenanceInfo> stats() {
        AppInfoEntity infoEntity = appInfos.getAppInfoEntity();
        if (infoEntity == null) {
            return GenericResponseResult.internalError("Internal error: no application information exists.");
        }
        return GenericResponseResult.ok("System maintenance stats", maintenance.exportInfo(infoEntity));
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
    @ApiOperation(value = "Purge the system by removing unnecessary resources")
    public GenericResponseResult<PurgeCount> purgeResources() {
        int countPurges = maintenance.purgeAllResources();
        LOGGER.info("total count of {} resources were purged", countPurges);
        return GenericResponseResult.ok("" +  countPurges + " resources were purged.", new PurgeCount(countPurges));
    }

    /**
     * TODO: implement following services:
     * 
     * - send maintenance notification to clients
     * - start/stop maintenance
     */
}
