/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
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
public class AddRemoveEventLocation {
    private String eventId;
    private String locationId;

    public AddRemoveEventLocation(final String eventId,
                                  final String locationId) {
        this.eventId = eventId;
        this.locationId = locationId;
    }

    public String getEventId() {
        return eventId;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }
}
