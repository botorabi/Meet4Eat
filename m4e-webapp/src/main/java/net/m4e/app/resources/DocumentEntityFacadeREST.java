/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.resources;

import net.m4e.app.auth.AuthRole;
import net.m4e.common.Entities;
import net.m4e.common.ResponseResults;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * REST services for Document entity operations
 * 
 * @author boto
 * Date of creation Sep 16, 2017
 */
@Stateless
@Path("/rest/docs")
public class DocumentEntityFacadeREST {

    private final Entities entities;

    /**
     * EJB's default constructor
     */
    protected DocumentEntityFacadeREST() {
        entities = null;
    }

    /**
     * Create the Document entity REST facade.
     * @param entities
     */
    @Inject
    public DocumentEntityFacadeREST(Entities entities) {
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
    public String find(@PathParam("id") Long id, @Context HttpServletRequest request) {
        JsonObjectBuilder jsonresponse = Json.createObjectBuilder();
        jsonresponse.add("id", id.toString());
        DocumentEntity document = entities.find(DocumentEntity.class, id);
        if ((document == null) || !document.getStatus().getIsActive()) {
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Document was not found.", ResponseResults.CODE_NOT_FOUND, jsonresponse.build().toString());
        }

        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "Document was found.", ResponseResults.CODE_OK, document.toJsonString());
    }
}
