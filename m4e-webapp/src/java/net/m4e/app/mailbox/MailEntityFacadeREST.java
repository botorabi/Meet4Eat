/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.mailbox;

import java.io.StringReader;
import java.math.BigDecimal;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
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
     * Get the count of total and unread mails.
     * 
     * @param request    HTTP request
     * @return           JSON response
     */
    @GET
    @Path("count")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public String getCount(@Context HttpServletRequest request) {
        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        if (sessionuser == null) {
            Log.error(TAG, "*** Internal error, cannot retrieve count of mails, no user in session found!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to retrieve count of mails, no authentication.", ResponseResults.CODE_UNAUTHORIZED, null);
        }

        Mails mails = new Mails(entityManager);
        long total  = mails.getCountTotalMails(sessionuser);
        long unread = mails.getCountUnreadMails(sessionuser);
        JsonObjectBuilder resp = Json.createObjectBuilder();
        resp.add("totalMails", total);
        resp.add("unreadMails", unread);
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "Count of mails was successfully retrieved.", ResponseResults.CODE_OK, resp.build().toString());
    }

    /**
     * Get the count of unread mails.
     * 
     * @param request    HTTP request
     * @return           JSON response
     */
    @GET
    @Path("countUnread")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public String getCountUnread(@Context HttpServletRequest request) {
        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        if (sessionuser == null) {
            Log.error(TAG, "*** Internal error, cannot retrieve count of unread mails, no user in session found!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to retrieve count of unread mails, no authentication.", ResponseResults.CODE_UNAUTHORIZED, null);
        }

        Mails mails = new Mails(entityManager);
        long unread = mails.getCountUnreadMails(sessionuser);
        JsonObjectBuilder resp = Json.createObjectBuilder();
        resp.add("unreadMails", unread);
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "Count of unread mails was successfully retrieved.", ResponseResults.CODE_OK, resp.build().toString());
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
    public String send(String mailJson, @Context HttpServletRequest request) {
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
        mail.setSenderName(sessionuser.getName());
        Mails mails = new Mails(entityManager);
        try {
            mails.createMail(mail);
        }
        catch (Exception ex) {
            Log.warning(TAG, "*** Could not send mail, problem occurred while creating mail entity, reason: " + ex.getLocalizedMessage());
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Problem occurred while sending mail", ResponseResults.CODE_INTERNAL_SRV_ERROR, null);
        }

        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "Mail was successfully sent.", ResponseResults.CODE_OK, null);
    }

    /**
     * Perform an operation on the mail with given ID.
     * The JSON request must have a field called 'operation' with a value of 
     * a supported operation:
     * 
     *   'trash'
     *   'untrash'
     *   'read'
     *   'unread'
     *   'countUnread'   This operation does not need a valid mail ID
     * 
     * @param id            The mail ID
     * @param operationJson JSON containing the requested operation
     * @param request       HTTP request
     * @return              JSON response
     */
    @POST
    @Path("operate/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    public String operate(@PathParam("id") Long id, String operationJson, @Context HttpServletRequest request) {
        JsonObjectBuilder resp = Json.createObjectBuilder();
        resp.add("id", id.toString());
        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        if (sessionuser == null) {
            Log.error(TAG, "*** Internal error, cannot delete user mail, no user in session found!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to delete the mail, no authentication.", ResponseResults.CODE_UNAUTHORIZED, resp.build().toString());
        }

        String op;
        try {
            Mails mails = new Mails(entityManager);
            JsonReader jreader = Json.createReader(new StringReader(operationJson));
            JsonObject jobject = jreader.readObject();
            op = jobject.getString("operation", null);
            mails.performMailOperation(sessionuser.getId(), id, op);
        }
        catch(Exception ex) {
            Log.warning(TAG, "*** Could not perform mail operation, reason: " + ex.getLocalizedMessage());
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to perform mail operation, reason: " + ex.getLocalizedMessage(), ResponseResults.CODE_BAD_REQUEST, resp.build().toString());
        }

        resp = resp.add("operation", op);
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "User mails were successfully retrieved.", ResponseResults.CODE_OK, resp.build().toString());
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
