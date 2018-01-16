/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.mailbox.rest;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.*;
import net.m4e.app.auth.AuthRole;
import net.m4e.app.auth.AuthorityConfig;
import net.m4e.app.mailbox.*;
import net.m4e.app.mailbox.rest.comm.*;
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

    private final NewMailValidator validator;

    private final Mails mails;

    /**
     * EJB's default constructor.
     */
    protected MailEntityFacadeREST() {
        this.validator = null;
        this.mails = null;
    }

    @Inject
    public MailEntityFacadeREST(NewMailValidator validator, Mails mails) {
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
    public GenericResponseResult<List<Mail>> getMails(@PathParam("from") Integer from, @PathParam("to") Integer to, @Context HttpServletRequest request) {
        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        if (sessionuser == null) {
            LOGGER.error("Cannot retrieve user mails, no user in session found!");
            return GenericResponseResult.unauthorized("Failed to retrieve user mails, no authentication.");
        }

        List<Mail> userMails = mails.getMails(sessionuser, from, to).stream().collect(Collectors.toList());

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

    /**
     * Send a mail to another user.
     *
     * @param newMail   Mail data in JSON format
     * @param request    HTTP request
     * @return           JSON response
     */
    @POST
    @Path("send")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    @ApiOperation(value = "Send a mail to another user")
    @ApiImplicitParams(@ApiImplicitParam(name = "body", required = true, dataTypeClass = NewMailCmd.class, paramType = "body"))
    public GenericResponseResult<Void> send(@ApiParam(hidden = true) NewMailCmd newMail, @Context HttpServletRequest request) {
        UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        if (sessionuser == null) {
            LOGGER.error("Cannot create mail, no user in session found!");
            return GenericResponseResult.unauthorized("Failed to create a mail, no authentication.");
        }

        MailEntity mail;
        try {
            mail = validator.validateNewEntityInput(newMail, sessionuser);
        }
        catch (Exception ex) {
            LOGGER.warn("Could not send mail, validation failed, reason: {}", ex.getLocalizedMessage());
            return GenericResponseResult.badRequest(ex.getLocalizedMessage());
        }

        //! NOTE we may implement a mechanism to limit the maximal count of user mails
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
     * @param operation     Mail operation
     * @param request       HTTP request
     * @return              JSON response
     */
    @POST
    @Path("operate/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @net.m4e.app.auth.AuthRole(grantRoles={AuthRole.VIRT_ROLE_USER})
    @ApiOperation(value = "Send a mail to another user")
    @ApiImplicitParams(@ApiImplicitParam(name = "body", required = true, dataTypeClass = MailOperationCmd.class, paramType = "body"))
    public GenericResponseResult<ExcecutedMailOperation> operate(@ApiParam("The mail-ID.") @PathParam("id") Long id,
                                                                 @ApiParam(hidden = true) MailOperationCmd operation,
                                                                 @Context HttpServletRequest request) {

        final UserEntity sessionuser = AuthorityConfig.getInstance().getSessionUser(request);
        if (sessionuser == null) {
            LOGGER.error("Cannot delete user mail, no user in session found!");
            return GenericResponseResult.unauthorized("Failed to delete the mail, no authentication.");
        }

        try {
            final ExcecutedMailOperation excecutedMailOperation = mails.performMailOperation(sessionuser.getId(), id, operation.getOperation());

            return GenericResponseResult.ok("User mails were successfully retrieved.", excecutedMailOperation);
        } catch (Exception ex) {
            LOGGER.warn("Could not perform mail operation {} on {}, reason: {}", operation.getOperation(), id, ex.getMessage());
            return GenericResponseResult.badRequest(
                    "Failed to perform mail operation, reason: " + ex.getMessage());
            //TODO response-body
        }
    }
}
