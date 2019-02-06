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
 * Date of creation February 14, 2018
 */
public class LocationVote {
    private final boolean vote;
    private final String votesId;
    private final String eventId;
    private final String locationId;

    public LocationVote(final boolean vote,
                        final String votesId,
                        final String eventId,
                        final String locationId) {
        this.vote = vote;
        this.votesId = votesId;
        this.eventId = eventId;
        this.locationId = locationId;
    }

    public boolean isVote() {
        return vote;
    }

    public String getVotesId() {
        return votesId;
    }

    public String getEventId() {
        return eventId;
    }

    public String getLocationId() {
        return locationId;
    }
}
