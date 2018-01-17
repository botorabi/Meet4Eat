/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.mailbox.rest.comm;

import net.m4e.app.mailbox.business.MailOperation;

/**
 * @author ybroeker
 */
public class MailOperationCmd {
    private MailOperation operation;

    public MailOperationCmd() {
    }

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
