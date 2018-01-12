/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.mailbox.cmds;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import net.m4e.app.mailbox.MailEntityInputValidator;

/**
 * ! TODO get the attachments
 */
public final class NewMailCmd {

    @Size(min = 1, max = MailEntityInputValidator.USER_INPUT_MAX_LEN_SUBJECT)
    private String subject;

    private String content;

    @Min(0)
    private Long receiverId;

    @JsonbCreator
    public NewMailCmd(@JsonbProperty("subject") final String subject,
                      @JsonbProperty("content") final String content,
                      @JsonbProperty("receiverId") final Long receiverId) {
        this.subject = subject;
        this.content = content;
        this.receiverId = receiverId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(final String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(final Long receiverId) {
        this.receiverId = receiverId;
    }
}
