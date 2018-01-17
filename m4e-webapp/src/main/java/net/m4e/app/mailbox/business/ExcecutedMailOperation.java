/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.mailbox.business;

/**
 * @author ybroeker
 */
public class ExcecutedMailOperation {
    private String id;
    private MailOperation operation;

    public ExcecutedMailOperation(final String id) {
        this.id = id;
    }

    public ExcecutedMailOperation(final MailOperation operation, final String id) {
        this.operation = operation;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public MailOperation getOperation() {
        return operation;
    }
}
