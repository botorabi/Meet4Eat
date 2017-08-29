/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.core;

import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
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

    @PersistenceContext(unitName = AppConfiguration.PERSITENCE_UNIT_NAME)
    private EntityManager em;

    public AppInfoEntityFacadeREST() {
        super(AppInfoEntity.class);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    /* Grant access to any role */
    @net.m4e.auth.AuthRole(grant={AuthRole.VIRT_ROLE_GUEST})
    public String getInfo() {
        List<AppInfoEntity> infos = super.findAll();
        if (infos.size() < 1) {
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "No application information exists", 404, null);
        }
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "", 200, infos.get(0).getVersion());
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}
