/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.mailbox.rest.comm;

/**
 * @author ybroeker
 */
public class UnreadMailCount {
    private final long unreadMails;

    public UnreadMailCount(final long unreadMails) {
        this.unreadMails = unreadMails;
    }

    public long getUnreadMails() {
        return unreadMails;
    }
}
