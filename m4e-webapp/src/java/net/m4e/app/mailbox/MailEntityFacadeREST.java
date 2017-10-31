/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.mailbox;

import javax.ejb.Stateless;
import javax.json.JsonArrayBuilder;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import net.m4e.app.auth.AuthRole;
import net.m4e.app.auth.AuthorityConfig;
import net.m4e.app.user.UserEntity;
import net.m4e.common.ResponseResults;
import net.m4e.system.core.Log;

/**
 * REST services for mailbox functionality
 * 
 * @author boto
 * Date of creation Oct 31, 2017
 */
@Stateless
@Path("/rest/mails")
public class MailEntityFacadeREST extends net.m4e.common.AbstractFacade<MailEntity> {

    /**
     * Used for logging
     */
    private final static String TAG = "MailEntityFacadeREST";

    /**
     * Entity manager needed for entity retrieval and modifications.
     */
    @PersistenceContext(unitName = net.m4e.system.core.AppConfiguration.PERSITENCE_UNIT_NAME)
    private EntityManager entityManager;

    /**
     * Construct the stateless bean.
     */
    public MailEntityFacadeREST() {
        super(MailEntity.class);
    }

    /**
     * Get user mails in given range. Pass 0/0 in order to get all mails.
     * 
     * @param from       Range begin
     * @param to         Range end
     * @param request    HTTP request
     * @return           JSON response
     */
    @GET
    @Path("{from}/{to}")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public String getMails(@PathParam("from") Integer from, @PathParam("to") Integer to, @Context HttpServletRequest request) {
        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        if (sessionuser == null) {
            Log.error(TAG, "*** Internal error, cannot retrieve user mails, no user in session found!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to retrieve user mails, no authentication.", ResponseResults.CODE_UNAUTHORIZED, null);
        }

        Mails mails = new Mails(entityManager);
        JsonArrayBuilder usermails = mails.exportUserMails(sessionuser, from, to);
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "User mails were successfully retrieved.", ResponseResults.CODE_OK, usermails.build().toString());
    }

    /**
     * Send a mail to another user.
     * 
     * @param mailJson   Mail data in JSON format
     * @param request    HTTP request
     * @return           JSON response
     */
    @POST
    @Path("send")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public String createUser(String mailJson, @Context HttpServletRequest request) {
        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        if (sessionuser == null) {
            Log.error(TAG, "*** Internal error, cannot create mail, no user in session found!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to create a mail, no authentication.", ResponseResults.CODE_UNAUTHORIZED, null);
        }

        MailEntity mail;
        try {
            MailEntityInputValidator validator = new MailEntityInputValidator(entityManager);
            mail = validator.validateNewEntityInput(mailJson);
        }
        catch (Exception ex) {
            Log.warning(TAG, "*** Could not send mail, validation failed, reason: " + ex.getLocalizedMessage());
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, ex.getLocalizedMessage(), ResponseResults.CODE_BAD_REQUEST, null);
        }

        //! NOTE we may implement a mechanism to limit the maximal count of user mails

        mail.setSenderId(sessionuser.getId());
        mail.setUnread(true);
        Mails mails = new Mails(entityManager);
        try {
            mails.createMailEntity(mail);
        }
        catch (Exception ex) {
            Log.warning(TAG, "*** Could not send mail, problem occurred while creating mail entity, reason: " + ex.getLocalizedMessage());
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Problem occurred while sending mail", ResponseResults.CODE_INTERNAL_SRV_ERROR, null);
        }

        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "Mail was successfully sent.", ResponseResults.CODE_OK, null);
    }

    /**
     * Get the entity manager.
     * 
     * @return Entity manager
     */
    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }
}
