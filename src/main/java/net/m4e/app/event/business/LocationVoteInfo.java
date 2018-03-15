/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.business;

import org.jetbrains.annotations.NotNull;

import javax.json.bind.annotation.JsonbTransient;
import java.util.*;

/**
 * A collection of event location vote data fields for exporting to clients.
 *
 * @author boto
 * Date of creation February 14, 2018
 */
public class LocationVoteInfo {

    private String id;
    private String eventId;
    private String locationId;
    private String locationName;
    private long timeBegin;
    private long timeEnd;
    private long creationTime;
    private List<Long> userIds;
    private List<String> userNames;


    public LocationVoteInfo() {
    }

    public String getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public String getLocationId() {
        return locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public long getTimeBegin() {
        return timeBegin;
    }

    public long getTimeEnd() {
        return timeEnd;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public List<Long> getUserIds() {
        return userIds;
    }

    public List<String> getUserNames() {
        return userNames;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public void setTimeBegin(long timeBegin) {
        this.timeBegin = timeBegin;
    }

    public void setTimeEnd(long timeEnd) {
        this.timeEnd = timeEnd;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }

    public void setUserNames(List<String> userNames) {
        this.userNames = userNames;
    }

    @JsonbTransient
    public static LocationVoteInfo fromLocationVoteEntity(@NotNull EventLocationVoteEntity voteEntity) {
        LocationVoteInfo voteInfo = new LocationVoteInfo();
        voteInfo.setId(voteEntity.getId().toString());
        voteInfo.setEventId(voteEntity.getEventId().toString());
        voteInfo.setLocationId(voteEntity.getLocationId().toString());
        voteInfo.setLocationName(voteEntity.getLocationName());
        voteInfo.setTimeBegin(voteEntity.getVoteTimeBegin());
        voteInfo.setTimeEnd(voteEntity.getVoteTimeEnd());
        voteInfo.setCreationTime(voteEntity.getCreationTime());

        if (voteEntity.getUserIds() != null) {
            voteInfo.setUserIds(new ArrayList<>(voteEntity.getUserIds()));
        }

        if (voteEntity.getUserNames() != null) {
            voteInfo.setUserNames(new ArrayList<>(voteEntity.getUserNames()));
        }

        return voteInfo;
    }
}
