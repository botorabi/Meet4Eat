/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.mailbox;

import io.swagger.annotations.Api;
import net.m4e.app.auth.AuthRole;
import net.m4e.app.auth.AuthorityConfig;
import net.m4e.app.user.UserEntity;
import net.m4e.common.ResponseResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.*;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;

/**
 * REST services for mailbox functionality
 * 
 * @author boto
 * Date of creation Oct 31, 2017
 */
@Stateless
@Path("/rest/mails")
@Api(value = "Mails services")
public class MailEntityFacadeREST {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final MailEntityInputValidator validator;

    private final Mails mails;

    /**
     * EJB's default constructor.
     */
    protected MailEntityFacadeREST() {
        this.validator = null;
        this.mails = null;
    }

    @Inject
    public MailEntityFacadeREST(MailEntityInputValidator validator, Mails mails) {
        this.validator = validator;
        this.mails = mails;
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
            LOGGER.error("*** Internal error, cannot retrieve user mails, no user in session found!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to retrieve user mails, no authentication.", ResponseResults.CODE_UNAUTHORIZED, null);
        }

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
            LOGGER.error("*** Internal error, cannot retrieve count of mails, no user in session found!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to retrieve count of mails, no authentication.", ResponseResults.CODE_UNAUTHORIZED, null);
        }

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
            LOGGER.error("*** Internal error, cannot retrieve count of unread mails, no user in session found!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to retrieve count of unread mails, no authentication.", ResponseResults.CODE_UNAUTHORIZED, null);
        }

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
            LOGGER.error("*** Internal error, cannot create mail, no user in session found!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to create a mail, no authentication.", ResponseResults.CODE_UNAUTHORIZED, null);
        }

        MailEntity mail;
        try {
            mail = validator.validateNewEntityInput(mailJson);
        }
        catch (Exception ex) {
            LOGGER.warn("*** Could not send mail, validation failed, reason: " + ex.getLocalizedMessage());
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, ex.getLocalizedMessage(), ResponseResults.CODE_BAD_REQUEST, null);
        }

        //! NOTE we may implement a mechanism to limit the maximal count of user mails

        mail.setSenderId(sessionuser.getId());
        mail.setSenderName(sessionuser.getName());
        try {
            mails.createMail(mail);
        }
        catch (Exception ex) {
            LOGGER.warn("*** Could not send mail, problem occurred while creating mail entity, reason: " + ex.getLocalizedMessage());
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
            LOGGER.error("*** Internal error, cannot delete user mail, no user in session found!");
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to delete the mail, no authentication.", ResponseResults.CODE_UNAUTHORIZED, resp.build().toString());
        }

        String op;
        try {
            JsonReader jreader = Json.createReader(new StringReader(operationJson));
            JsonObject jobject = jreader.readObject();
            op = jobject.getString("operation", null);
            mails.performMailOperation(sessionuser.getId(), id, op);
        }
        catch(Exception ex) {
            LOGGER.warn("*** Could not perform mail operation, reason: " + ex.getLocalizedMessage());
            return ResponseResults.toJSON(ResponseResults.STATUS_NOT_OK, "Failed to perform mail operation, reason: " + ex.getLocalizedMessage(), ResponseResults.CODE_BAD_REQUEST, resp.build().toString());
        }

        resp = resp.add("operation", op);
        return ResponseResults.toJSON(ResponseResults.STATUS_OK, "User mails were successfully retrieved.", ResponseResults.CODE_OK, resp.build().toString());
    }
}
