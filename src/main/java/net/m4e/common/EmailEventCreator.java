/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.common;

import net.m4e.app.notification.SendEmailEvent;

import java.util.List;

/**
 * This class is used for creating a SenEmailEvent.
 *
 * @author boto
 * Date of creation February 2, 2018
 */
public class EmailEventCreator {

    private List<String> recipients;
    private String subject;
    private String body;

    private EmailEventCreator(final List<String> recipients, final String subject, final String body) {
        this.recipients = recipients;
        this.subject = subject;
        this.body = body;
    }

    public static SendEmailEvent plainTextMail(final List<String> recipients, final String subject, final String body) {
        EmailEventCreator mc = new EmailEventCreator(recipients, subject, body);
        SendEmailEvent event = mc.createEvent();
        event.setHtmlBody(false);
        return event;
    }

    public static SendEmailEvent htmlMail(final List<String> recipients, final String subject, final String body) {
        EmailEventCreator mc = new EmailEventCreator(recipients, subject, body);
        SendEmailEvent event = mc.createEvent();
        event.setHtmlBody(true);
        return event;
    }

    private SendEmailEvent createEvent() {
        SendEmailEvent event = new SendEmailEvent();
        event.setRecipients(recipients);
        event.setSubject(subject);
        event.setBody(body);
        return event;
    }
}
