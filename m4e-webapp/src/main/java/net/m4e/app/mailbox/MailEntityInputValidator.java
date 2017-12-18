/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.mailbox;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import net.m4e.app.user.UserEntity;
import net.m4e.app.user.Users;
import net.m4e.common.Strings;

/**
 * This class validates mailbox related inputs from a client.
 * 
 * @author boto
 * Date of creation Oct 31, 2017
 */
@ApplicationScoped
public class MailEntityInputValidator {

    /* Min/max string length for user input fields */
    private final int USER_INPUT_MIN_LEN_SUBJECT  = 1;
    private final int USER_INPUT_MAX_LEN_SUBJECT  = 32;
    private final int USER_INPUT_MAX_ATTACHMENTS  = 5;

    private final Users users;

    private final Mails mails;


    /**
     * Default constructor.
     */
    protected MailEntityInputValidator() {
        users = null;
        mails = null;
    }

    /**
     * Create the validator.
     * 
     * @param users  Users instance
     * @param mails  Mails instance
     */
    @Inject
    public MailEntityInputValidator(Users users, Mails mails) {
        this.users = users;
        this.mails = mails;
    }

    /**
     * Given a JSON string as input containing data for creating a new mail, validate 
     * all fields and return a MailEntity, or throw an exception if the validation failed.
     * 
     * @param mailJson       Data for creating a new mail in JSON format
     * @return               A MailEntity created out of given input
     * @throws Exception     Throws an exception if the validation fails.
     */
    public MailEntity validateNewEntityInput(String mailJson) throws Exception {
        MailEntity mail = mails.importMailJSON(mailJson);
        if (mail == null) {
            throw new Exception("Failed to send mail, invalid input.");
        }
        if (mail.getReceiverId() == 0L) {
            throw new Exception("Failed to send mail, invalid recipient.");
        }
        UserEntity recipient = users.findUser(mail.getReceiverId());
        if ((recipient == null) || !recipient.getStatus().getIsActive()) {
            throw new Exception("Failed to send mail, recipient does not exist.");
        }

        // set the recipient name
        mail.setReceiverName(recipient.getName());

        if (!Strings.checkMinMaxLength(mail.getSubject(), USER_INPUT_MIN_LEN_SUBJECT, USER_INPUT_MAX_LEN_SUBJECT)) {
            throw new Exception("Mail subject must be at least " + USER_INPUT_MIN_LEN_SUBJECT + " characters long and have maximal " + USER_INPUT_MAX_LEN_SUBJECT + " characters.");
        }

        if ((mail.getAttachments() != null) && (mail.getAttachments().size() > USER_INPUT_MAX_ATTACHMENTS)) {
            throw new Exception("Exceeded the maximal count (" + USER_INPUT_MAX_ATTACHMENTS + ") of attachments.");
        }
        return mail;
    }
}
