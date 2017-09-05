/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.core;

import java.util.List;
import java.util.Objects;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import net.m4e.auth.AuthRole;
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
    private EntityManager em;

    public AppInfoEntityFacadeREST() {
        super(AppInfoEntity.class);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_GUEST})
    public String getInfo() {
        AppInfoEntity info = getInfoEntity();
        if (Objects.isNull(info)) {
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Internal error: no application information exists.", ResponseResults.CODE_INTERNAL_SRV_ERROR, null);
        }
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        jsonresponse.add("version", info.getVersion());
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "", ResponseResults.CODE_OK, jsonresponse.build().toString());
    }

    @POST
    @Path("stats")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grantRoles={AuthRole.USER_ROLE_ADMIN})
    public String getStats() {
        AppInfoEntity info = getInfoEntity();
        if (Objects.isNull(info)) {
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Internal error: no application information exists.", ResponseResults.CODE_INTERNAL_SRV_ERROR, null);
        }
        String appstats = exportAppInfoJSON(info).build().toString();
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "", ResponseResults.CODE_OK, appstats);
    }

    /**
     * Get the application info entity.
     * 
     * @return App info entity, or null if there is some problem to retrieve it.
     */
    private AppInfoEntity getInfoEntity() {
        List<AppInfoEntity> infos = super.findAll();
        if (infos.size() != 1) {
            Log.error(TAG, "*** Unexpected count of app info entity detected: " + infos.size());
            return null;
        }
        return infos.get(0);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /**
     * Give an app info entity export the necessary fields into a JSON object.
     * 
     * @param entity    App info entity to export
     * @return          A JSON object containing builder the proper entity fields
     */
    public JsonObjectBuilder exportAppInfoJSON(AppInfoEntity entity) {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("version", entity.getVersion());
        json.add("dateLastMaintenance", entity.getDateLastMaintenance());
        json.add("dateLastUpdate", entity.getDateLastUpdate());
        json.add("userCountPurge", entity.getUserCountPurge());
        json.add("eventCountPurge", entity.getEventCountPurge());
        return json;
    }
}
