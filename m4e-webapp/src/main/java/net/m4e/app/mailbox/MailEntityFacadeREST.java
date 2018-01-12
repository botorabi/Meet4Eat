/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.mailbox;

import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.*;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.*;
import net.m4e.app.auth.AuthRole;
import net.m4e.app.auth.AuthorityConfig;
import net.m4e.app.user.UserEntity;
import net.m4e.common.GenericResponseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    @ApiOperation(value = "Get user mails in given range", notes = "Pass 0/0 in order to get all mails")
    public GenericResponseResult<List<MailEntity>> getMails(@PathParam("from") Integer from, @PathParam("to") Integer to, @Context HttpServletRequest request) {
        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        if (sessionuser == null) {
            LOGGER.error("Cannot retrieve user mails, no user in session found!");
            return GenericResponseResult.unauthorized("Failed to retrieve user mails, no authentication.");
        }

        List<MailEntity> userMails = mails.getMails(sessionuser, from, to).stream().map(Mail::getMailEntity).collect(Collectors.toList());

        return GenericResponseResult.ok("User mails were successfully retrieved.", userMails);
    }

    /**
     *Get the count of total and unread mails.
     * 
     * @param request    HTTP request
     * @return           JSON response
     */
    @GET
    @Path("count")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    @ApiOperation(value = "Get the count of total and unread mails")
    public GenericResponseResult<MailCount> getCount(@Context HttpServletRequest request) {
        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        if (sessionuser == null) {
            LOGGER.error("Cannot retrieve count of mails, no user in session found!");
            return GenericResponseResult.unauthorized("Failed to retrieve count of mails, no authentication.");
        }

        long total  = mails.getCountTotalMails(sessionuser);
        long unread = mails.getCountUnreadMails(sessionuser);
        MailCount mailCount = new MailCount(total, unread);
        return GenericResponseResult.ok("Count of mails was successfully retrieved.", mailCount);
    }

    static class MailCount {
        public final long totalMails;
        public final long unreadMails;

        public MailCount(final long totalMails, final long unreadMails) {
            this.totalMails = totalMails;
            this.unreadMails = unreadMails;
        }
    }

    /**
     * Get the count of unread mails.
     *
     * TODO: Needed?
     *
     * @param request    HTTP request
     * @return           JSON response
     */
    @GET
    @Path("countUnread")
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    @ApiOperation(value = "Get the count of unread mails")
    public GenericResponseResult<UnreadMailCount> getCountUnread(@Context HttpServletRequest request) {
        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        if (sessionuser == null) {
            LOGGER.error("Cannot retrieve count of unread mails, no user in session found!");
            return GenericResponseResult.unauthorized("Failed to retrieve count of unread mails, no authentication.");
        }

        long unread = mails.getCountUnreadMails(sessionuser);
        UnreadMailCount unreadMailCount = new UnreadMailCount(unread);
        return GenericResponseResult.ok("Count of unread mails was successfully retrieved.", unreadMailCount);
    }

    static class UnreadMailCount {
        public final long unreadMails;

        public UnreadMailCount(final long unreadMails) {
            this.unreadMails = unreadMails;
        }
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
    @ApiOperation(value = "Send a mail to another user")
    @ApiImplicitParams(@ApiImplicitParam(name = "body", dataTypeClass = NewMail.class, paramType = "body"))
    public GenericResponseResult<Void> send(@ApiParam(hidden = true) String mailJson, @Context HttpServletRequest request) {
        //TODO: NewMail aus Parameter instead of String
        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        if (sessionuser == null) {
            LOGGER.error("Cannot create mail, no user in session found!");
            return GenericResponseResult.unauthorized("Failed to create a mail, no authentication.");
        }

        MailEntity mail;
        try {
            mail = validator.validateNewEntityInput(mailJson);
        }
        catch (Exception ex) {
            LOGGER.warn("Could not send mail, validation failed, reason: {}", ex.getLocalizedMessage());
            return GenericResponseResult.badRequest(ex.getLocalizedMessage());
        }

        //! NOTE we may implement a mechanism to limit the maximal count of user mails

        mail.setSenderId(sessionuser.getId());
        mail.setSenderName(sessionuser.getName());
        try {
            mails.createMail(mail);
        }
        catch (Exception ex) {
            LOGGER.warn("Could not send mail, problem occurred while creating mail entity, reason: {}", ex.getLocalizedMessage());
            return GenericResponseResult.internalError("Problem occurred while sending mail");
        }

        return GenericResponseResult.ok("Mail was successfully sent.", null);
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
    @ApiOperation(value = "Send a mail to another user")
    @ApiImplicitParams(@ApiImplicitParam(name = "body", dataTypeClass = MailOperationCmd.class, paramType = "body"))
    public GenericResponseResult<MailOperationResponse> operate(@ApiParam("The mail-ID.") @PathParam("id") Long id,
                                                                @ApiParam(hidden = true) String operationJson,
                                                                @Context HttpServletRequest request) {

        final MailOperationResponse mailOperationResponse = new MailOperationResponse(id.toString());

        final UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        if (sessionuser == null) {
            LOGGER.error("Cannot delete user mail, no user in session found!");
            return GenericResponseResult.unauthorized("Failed to delete the mail, no authentication.");
        }

        final String op;
        try {
            JsonReader jreader = Json.createReader(new StringReader(operationJson));
            JsonObject jobject = jreader.readObject();
            op = jobject.getString("operation", null);

            mailOperationResponse.setOperation(MailOperation.fromString(op));

            mails.performMailOperation(sessionuser.getId(), id, op);
        } catch (Exception ex) {
            LOGGER.warn("Could not perform mail operation, reason: " + ex.getLocalizedMessage());
            return GenericResponseResult.badRequest(
                    "Failed to perform mail operation, reason: " + ex.getLocalizedMessage(),
                    mailOperationResponse);
        }

        return GenericResponseResult.ok("User mails were successfully retrieved.", mailOperationResponse);
    }

    enum MailOperation {
        TRASH, UNTRASH, READ, UNREAD;

        static MailOperation fromString(String string) {
            for (final MailOperation mailOperation : values()) {
                if (mailOperation.toString().equalsIgnoreCase(string)) {
                    return mailOperation;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    class MailOperationCmd {
        MailOperation operation;

        public MailOperationCmd(final MailOperation operation) {
            this.operation = operation;
        }

        public MailOperation getOperation() {
            return operation;
        }

        public void setOperation(final MailOperation operation) {
            this.operation = operation;
        }
    }

    class MailOperationResponse {
        MailOperation operation;
        String id;

        public MailOperation getOperation() {
            return operation;
        }

        public void setOperation(final MailOperation operation) {
            this.operation = operation;
        }

        public String getId() {
            return id;
        }

        public void setId(final String id) {
            this.id = id;
        }

        public MailOperationResponse(final String id) {
            this.id = id;
        }

        public MailOperationResponse(final MailOperation operation, final String id) {
            this.operation = operation;
            this.id = id;
        }
    }

}
