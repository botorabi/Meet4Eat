/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.mailbox.rest;

import net.m4e.app.mailbox.business.MailEntity;
import net.m4e.app.mailbox.rest.comm.NewMailCmd;
import net.m4e.app.user.business.UserEntity;
import net.m4e.app.user.business.Users;
import net.m4e.common.Strings;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Date;

/**
 * This class validates mailbox related inputs from a client.
 * 
 * @author boto
 * Date of creation Oct 31, 2017
 */
@ApplicationScoped
public class NewMailValidator {

    /* Min/max string length for user input fields */
    public static final int USER_INPUT_MIN_LEN_SUBJECT = 1;
    public static final int USER_INPUT_MAX_LEN_SUBJECT = 32;
    public static final int USER_INPUT_MAX_ATTACHMENTS = 5;

    private final Users users;


    /**
     * Default constructor.
     */
    protected NewMailValidator() {
        users = null;
    }

    /**
     * Create the validator.
     * 
     * @param users  Users instance
     */
    @Inject
    public NewMailValidator(Users users) {
        this.users = users;
    }

    /**
     * Given the input for a new mail, validate all its fields and return a MailEntity, or throw an
     * exception if the validation failed.
     * 
     * @param mail           The mail
     * @param sender         The user who tries to send the mail
     * @return               A MailEntity created out of given input
     * @throws Exception     Throws an exception if the validation fails.
     */
    public MailEntity validateNewEntityInput(NewMailCmd mail, UserEntity sender) throws Exception {
        if (mail == null) {
            throw new Exception("Failed to send mail, invalid input.");
        }
        if (sender == null) {
            throw new Exception("Failed to send mail, invalid sender.");
        }
        if (mail.getReceiverId() == 0L) {
            throw new Exception("Failed to send mail, invalid recipient.");
        }
        UserEntity recipient = users.findUser(mail.getReceiverId());
        if ((recipient == null) || !recipient.getStatus().getIsActive()) {
            throw new Exception("Failed to send mail, recipient does not exist.");
        }
        if (!Strings.checkMinMaxLength(mail.getSubject(), USER_INPUT_MIN_LEN_SUBJECT, USER_INPUT_MAX_LEN_SUBJECT)) {
            throw new Exception("Mail subject must be at least " + USER_INPUT_MIN_LEN_SUBJECT + " characters long and have maximal " + USER_INPUT_MAX_LEN_SUBJECT + " characters.");
        }
        /** TODO handle attachments
        if ((mail.getAttachments() != null) && (mail.getAttachments().size() > USER_INPUT_MAX_ATTACHMENTS)) {
            throw new Exception("Exceeded the maximal count (" + USER_INPUT_MAX_ATTACHMENTS + ") of attachments.");
        }
        */

        MailEntity mailentity = new MailEntity();
        mailentity.setReceiverId(mail.getReceiverId());
        mailentity.setReceiverName(recipient.getName());
        mailentity.setSubject(mail.getSubject());
        mailentity.setContent(mail.getContent());
        mailentity.setSendDate((new Date()).getTime());
        mailentity.setSenderId(sender.getId());
        mailentity.setSenderName(sender.getName());

        return mailentity;
    }
}
