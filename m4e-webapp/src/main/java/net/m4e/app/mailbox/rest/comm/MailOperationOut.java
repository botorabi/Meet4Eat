/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.mailbox.rest.comm;

import net.m4e.app.mailbox.MailOperation;

/**
 * @author ybroeker
 */
public class MailOperationOut extends MailOperationIn {
    private String id;

    public MailOperationOut(final String id) {
        this.id = id;
    }

    public MailOperationOut(final MailOperation operation, final String id) {
        this.operation = operation;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }
}
