/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.rest.comm;

/**
 * @author boto
 * Date of creation February 21, 2018
 */
public class AddRemoveEventMember {
    private String eventId;
    private String memberId;

    public AddRemoveEventMember(final String eventId,
                                final String memberId) {
        this.eventId = eventId;
        this.memberId = memberId;
    }

    public String getEventId() {
        return eventId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }
}
