/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.resources;

import io.swagger.annotations.*;
import net.m4e.app.auth.AuthRole;
import net.m4e.common.*;
import org.jetbrains.annotations.NotNull;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

/**
 * REST services for Document entity operations
 * 
 * @author boto
 * Date of creation Sep 16, 2017
 */
@Stateless
@Path("/rest/docs")
@Api(value = "Service for accessing documents")
public class DocumentRestService {

    private final Entities entities;

    /**
     * EJB's default constructor
     */
    protected DocumentRestService() {
        entities = null;
    }

    /**
     * Create the Document entity REST facade.
     * @param entities
     */
    @Inject
    public DocumentRestService(@NotNull Entities entities) {
        this.entities = entities;
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
    @ApiOperation(value = "Find the document with given ID")
    public GenericResponseResult<DocumentInfo> find(@PathParam("id") Long id, @Context HttpServletRequest request) {
        DocumentEntity document = entities.find(DocumentEntity.class, id);
        if ((document == null) || !document.getStatus().getIsActive()) {
            return GenericResponseResult.notFound("Document was not found.");
        }

        return GenericResponseResult.ok("Document was found.", DocumentInfo.fromDocumentEntity(document));
    }
}
