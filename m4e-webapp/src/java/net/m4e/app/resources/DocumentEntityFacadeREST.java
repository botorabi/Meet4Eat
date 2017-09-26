/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.resources;

import java.util.Objects;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
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
 * REST services for Document entity operations
 * 
 * @author boto
 * Date of creation Sep 16, 2017
 */
@Stateless
@Path("/rest/docs")
public class DocumentEntityFacadeREST extends AbstractFacade<DocumentEntity> {

    /**
     * Entity manager needed for entity retrieval and modifications.
     */
    @PersistenceContext(unitName = net.m4e.system.core.AppConfiguration.PERSITENCE_UNIT_NAME)
    private EntityManager entityManager;

    /**
     * Create the Document entity REST facade.
     */
    public DocumentEntityFacadeREST() {
        super(DocumentEntity.class);
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
     * Get the document with given ID.
     * 
     * @param id         Document entity ID
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
        DocumentEntity document = super.find(id);
        if (Objects.isNull(document) || !document.getStatus().getIsActive()) {
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Document was not found.", ResponseResults.CODE_NOT_FOUND, jsonresponse.build().toString());
        }

        DocumentUtils utils = new DocumentUtils();
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "Document was found.", ResponseResults.CODE_OK, utils.exportDocumentJSON(document).build().toString());
    }
}
