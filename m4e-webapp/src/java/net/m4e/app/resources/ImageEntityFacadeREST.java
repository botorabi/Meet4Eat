/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.resources;

import java.util.Objects;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.UserTransaction;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import net.m4e.app.auth.AuthRole;
import net.m4e.common.AbstractFacade;
import net.m4e.common.ResponseResults;

/**
 * REST services for Image entity operations
 * 
 * @author boto
 * Date of creation Sep 16, 2017
 */
@Stateless
@Path("/rest/images")
@TransactionManagement(TransactionManagementType.BEAN)
public class ImageEntityFacadeREST extends AbstractFacade<ImageEntity> {

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
     * Create the image entity REST facade.
     */
    public ImageEntityFacadeREST() {
        super(ImageEntity.class);
    }

    /**
     * Get the entity manager.
     * 
     * @return   Entity manager
     */
    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * Get the image with given ID.
     * 
     * @param id         Image entity ID
     * @param request    HTTP request
     * @return           JSON response
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public String find(@PathParam("id") Long id, @Context HttpServletRequest request) {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        jsonresponse.add("id", id);
        ImageEntity image = super.find(id);
        if (Objects.isNull(image) || !image.getStatus().getIsActive()) {
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Image was not found.", ResponseResults.CODE_NOT_FOUND, jsonresponse.build().toString());
        }

        ImageUtils utils = new ImageUtils(entityManager, userTransaction);
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "Image was found.", ResponseResults.CODE_OK, utils.exportImageJSON(image).build().toString());
    }
}
