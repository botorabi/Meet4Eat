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
public class MailCount {
    private final long totalMails;
    private final long unreadMails;

    public MailCount(final long totalMails, final long unreadMails) {
        this.totalMails = totalMails;
        this.unreadMails = unreadMails;
    }

    public long getTotalMails() {
        return totalMails;
    }

    public long getUnreadMails() {
        return unreadMails;
    }
}
