/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.mailbox.rest.comm;

import net.m4e.app.mailbox.rest.NewMailValidator;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

/**
 * @author ybroeker
 */
public class NewMailCmd {

    @Size(min = NewMailValidator.USER_INPUT_MIN_LEN_SUBJECT, max = NewMailValidator.USER_INPUT_MAX_LEN_SUBJECT)
    private String subject;

    private String content;

    @Min(0)
    private Long receiverId;

    public NewMailCmd() {}

    public NewMailCmd(final String subject,
                      final String content,
                      final Long receiverId) {
        this.subject = subject;
        this.content = content;
        this.receiverId = receiverId;
    }

    public String getSubject() {
        return subject;
    }

    @JsonbProperty("subject")
    public void setSubject(final String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    @JsonbProperty("content")
    public void setContent(final String content) {
        this.content = content;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    @JsonbProperty("receiverId")
    public void setReceiverId(final Long receiverId) {
        this.receiverId = receiverId;
    }
}
