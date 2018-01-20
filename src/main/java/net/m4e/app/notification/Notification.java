/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.notification;

import java.util.Map;
import java.util.logging.Logger;

/**
 * @author ybroeker
 */
public class Notification {
    private static final Logger LOG = Logger.getLogger(Notification.class.getName());

    String subject;
    String text;
    String type;
    Map<String, Object> data;

    public Notification() {
    }

    public Notification(final String subject, final String text, final String type, final Map<String, Object> data) {
        this.subject = subject;
        this.text = text;
        this.type = type;
        this.data = data;
    }

    public String getSubject() {
        return subject;
    }

    public String getText() {
        return text;
    }

    public String getType() {
        return type;
    }

    public Map<String, Object> getData() {
        return data;
    }
}
