/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.mailbox.business;

import javax.json.bind.adapter.JsonbAdapter;
import javax.json.bind.annotation.JsonbTypeAdapter;

/**
 * @author ybroeker
 */
@JsonbTypeAdapter(MailOperation.MailOperationAdapter.class)
public enum MailOperation {

    TRASH, UNTRASH, READ, UNREAD;

    public static MailOperation fromString(String string) {
        for (final MailOperation mailOperation : values()) {
            if (mailOperation.toString().equalsIgnoreCase(string)) {
                return mailOperation;
            }
        }

        return null;
    }

    public static class MailOperationAdapter implements JsonbAdapter<MailOperation, String> {

        public MailOperationAdapter() {
        }

        @Override
        public String adaptToJson(final MailOperation obj) {
            return obj.name().toLowerCase();
        }

        @Override
        public MailOperation adaptFromJson(final String obj) {
            return MailOperation.fromString(obj);
        }
    }
}
