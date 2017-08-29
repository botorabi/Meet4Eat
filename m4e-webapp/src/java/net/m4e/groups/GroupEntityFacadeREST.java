/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.groups;

import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import net.m4e.auth.AuthRole;
import net.m4e.common.ResponseResults;

/**
 * REST services for Group entity operations
 * 
 * @author boto
 * Date of creation Aug 18, 2017
 */
@Stateless
@Path("/rest/groups")
public class GroupEntityFacadeREST extends net.m4e.common.AbstractFacade<GroupEntity> {

    @PersistenceContext(unitName = net.m4e.core.AppConfiguration.PERSITENCE_UNIT_NAME)
    private EntityManager em;

    public GroupEntityFacadeREST() {
        super(GroupEntity.class);
    }

    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grant={AuthRole.USER_ROLE_MODERATOR})
    public String createGroup(GroupEntity entity) {
        if (entity == null) {
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to created group", 400, null);
        }
        super.create(entity);
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "Group successfully created", 200, null);
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grant={AuthRole.USER_ROLE_MODERATOR})
    public String edit(@PathParam("id") Long id, GroupEntity entity) {
        if (entity == null) {
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to update group", 400, null);
        }
        super.edit(entity);
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "Group successfully updated", 200, null);
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grant={AuthRole.USER_ROLE_MODERATOR})
    public String remove(@PathParam("id") Long id) {
        GroupEntity entity = super.find(id);
        if (entity == null) {
            return ResponseResults.buildJSON(ResponseResults.STATUS_NOT_OK, "Failed to find group for deletion", 400, null);
        }
        super.remove(entity);
        return ResponseResults.buildJSON(ResponseResults.STATUS_OK, "Group successfully deleted", 200, null);
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grant={AuthRole.USER_ROLE_MODERATOR, AuthRole.VIRT_ROLE_USER})
    public GroupEntity find(@PathParam("id") Long id) {
        return super.find(id);
    }

    @GET
    @Override
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grant={AuthRole.USER_ROLE_MODERATOR, AuthRole.VIRT_ROLE_USER})
    public List<GroupEntity> findAll() {
        return super.findAll();
    }

    @GET
    @Path("{from}/{to}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grant={AuthRole.VIRT_ROLE_USER})
    public List<GroupEntity> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        return super.findRange(new int[]{from, to});
    }

    @GET
    @Path("count")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.auth.AuthRole(grant={AuthRole.VIRT_ROLE_USER})
    public String countREST() {
        //! NOTE why the hell isn't "int" as return value supported? we workaround this by returning a string.
        return "" + super.count();
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}
