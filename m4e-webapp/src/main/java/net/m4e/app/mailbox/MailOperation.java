/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.mailbox;

/**
 * @author ybroeker
 */
public enum MailOperation {
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
