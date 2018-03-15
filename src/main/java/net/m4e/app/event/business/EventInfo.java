/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.business;

import net.m4e.app.communication.ConnectedClients;
import net.m4e.app.event.rest.comm.*;
import net.m4e.app.user.business.*;
import org.jetbrains.annotations.NotNull;

import javax.json.bind.annotation.JsonbTransient;
import java.util.*;

/**
 * @author boto
 * Date of creation February 20, 2018
 */
public class EventInfo {
    private String id;
    private String name;
    private String description;
    private boolean isPublic;
    private String photoId;
    private String photoETag;
    private Long eventStart;
    private Long repeatWeekDays;
    private Long repeatDayTime;
    private Long votingTimeBegin;
    private List<EventMember> members;
    private List<EventLocation> locations;
    private String ownerId;
    private String ownerName;
    private String ownerPhotoId;
    private String ownerPhotoETag;
    private EventMember.OnlineStatus ownerStatus;

    public EventInfo() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public String getPhotoId() {
        return photoId;
    }

    public String getPhotoETag() {
        return photoETag;
    }

    public Long getEventStart() {
        return eventStart;
    }

    public Long getRepeatWeekDays() {
        return repeatWeekDays;
    }

    public Long getRepeatDayTime() {
        return repeatDayTime;
    }

    public Long getVotingTimeBegin() {
        return votingTimeBegin;
    }

    public List<EventMember> getMembers() {
        return members;
    }

    public List<EventLocation> getLocations() {
        return locations;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getOwnerPhotoId() {
        return ownerPhotoId;
    }

    public String getOwnerPhotoETag() {
        return ownerPhotoETag;
    }

    public EventMember.OnlineStatus getOwnerStatus() {
        return ownerStatus;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }

    public void setPhotoETag(String photoETag) {
        this.photoETag = photoETag;
    }

    public void setEventStart(Long eventStart) {
        this.eventStart = eventStart;
    }

    public void setRepeatWeekDays(Long repeatWeekDays) {
        this.repeatWeekDays = repeatWeekDays;
    }

    public void setRepeatDayTime(Long repeatDayTime) {
        this.repeatDayTime = repeatDayTime;
    }

    public void setVotingTimeBegin(Long votingTimeBegin) {
        this.votingTimeBegin = votingTimeBegin;
    }

    public void setMembers(List<EventMember> members) {
        this.members = members;
    }

    public void setLocations(List<EventLocation> locations) {
        this.locations = locations;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public void setOwnerPhotoId(String ownerPhotoId) {
        this.ownerPhotoId = ownerPhotoId;
    }

    public void setOwnerPhotoETag(String ownerPhotoETag) {
        this.ownerPhotoETag = ownerPhotoETag;
    }

    public void setOwnerStatus(EventMember.OnlineStatus ownerStatus) {
        this.ownerStatus = ownerStatus;
    }


    @JsonbTransient
    public static EventInfo fromEventEntity(@NotNull EventEntity eventEntity, @NotNull ConnectedClients connectedClients, @NotNull Users users) {
        EventInfo eventInfo = new EventInfo();

        //! NOTE we cannot take setupEventXXX as method name as JSON-B does not like it (because of beginning "set").
        createEventInfo(eventInfo, eventEntity);
        createEventMembers(eventInfo, eventEntity, connectedClients);
        createEventOwnership(eventInfo, eventEntity, users, connectedClients);
        createEventLocations(eventInfo, eventEntity);

        return eventInfo;
    }

    @JsonbTransient
    private static void createEventInfo(@NotNull EventInfo eventInfo, @NotNull final EventEntity eventEntity) {
        eventInfo.setId("" + eventEntity.getId());
        eventInfo.setName(eventEntity.getName());
        eventInfo.setDescription(eventEntity.getDescription());
        eventInfo.setPublic(eventEntity.getIsPublic());
        eventInfo.setPhotoId(eventEntity.getPhoto() != null ? eventEntity.getPhoto().getId().toString() : "");
        eventInfo.setPhotoETag(eventEntity.getPhoto() != null ? eventEntity.getPhoto().getETag() : "");
        eventInfo.setEventStart(eventEntity.getEventStart());
        eventInfo.setRepeatDayTime(eventEntity.getRepeatDayTime());
        eventInfo.setRepeatWeekDays(eventEntity.getRepeatWeekDays());
        eventInfo.setVotingTimeBegin(eventEntity.getVotingTimeBegin());
    }

    @JsonbTransient
    private static void createEventMembers(@NotNull EventInfo eventInfo,
                                           @NotNull final EventEntity eventEntity,
                                           @NotNull final ConnectedClients connectedClients) {
        eventInfo.setMembers(new ArrayList<>());
        if (eventEntity.getMembers() != null) {
            eventEntity.getMembers()
                    .stream()
                    .filter(member -> member.getStatus().getIsActive())
                    .map(member -> {
                        EventMember eventMember = new EventMember();
                        eventMember.setId(member.getId().toString());
                        eventMember.setName(member.getName());
                        eventMember.setPhotoId((member.getPhoto() != null) ? member.getPhoto().getId().toString() : "");
                        eventMember.setPhotoETag((member.getPhoto() != null) ? member.getPhoto().getETag() : "");
                        boolean online = (connectedClients.getConnectedUser(member.getId()) != null);
                        eventMember.setStatus(online ? EventMember.OnlineStatus.online : EventMember.OnlineStatus.offline);
                        return eventMember;
                    })
                    .forEach(member -> eventInfo.getMembers().add(member));
        }
    }

    @JsonbTransient
    private static void createEventOwnership(@NotNull EventInfo eventInfo,
                                             @NotNull final EventEntity eventEntity,
                                             @NotNull final Users users,
                                             @NotNull final ConnectedClients connectedClients) {
        String ownerName, ownerPhotoETag;
        Long ownerPhotoId;
        Long ownerId = eventEntity.getStatus().getIdOwner();
        UserEntity owner = users.findUser(ownerId);
        boolean ownerOnline;

        if ((owner == null) || !owner.getStatus().getIsActive()) {
            ownerOnline = false;
            ownerId = 0L;
            ownerName = "";
            ownerPhotoId = 0L;
            ownerPhotoETag = "";
        }
        else {
            ownerName = owner.getName();
            ownerPhotoId = (owner.getPhoto() != null) ? owner.getPhoto().getId() : 0L;
            ownerPhotoETag = (owner.getPhoto() != null) ? owner.getPhoto().getETag() : "";
            ownerOnline = (connectedClients.getConnectedUser(owner.getId()) != null);
        }

        eventInfo.setOwnerId((ownerId > 0)? ownerId.toString() : "");
        eventInfo.setOwnerName(ownerName);
        eventInfo.setOwnerPhotoId((ownerPhotoId > 0)? ownerPhotoId.toString() : "");
        eventInfo.setOwnerPhotoETag(ownerPhotoETag);
        eventInfo.setOwnerStatus(ownerOnline ? EventMember.OnlineStatus.online : EventMember.OnlineStatus.offline);
    }

    @JsonbTransient
    private static void createEventLocations(@NotNull EventInfo eventInfo, @NotNull final EventEntity eventEntity) {
        eventInfo.setLocations(new ArrayList<>());
        if (eventEntity.getLocations() != null) {
            eventEntity.getLocations()
                .stream()
                .filter(location -> location.getStatus().getIsActive())
                .map(location -> {
                    EventLocation eventLocation = new EventLocation();
                    eventLocation.setId(location.getId().toString());
                    eventLocation.setName(location.getName());
                    eventLocation.setDescription(location.getDescription());
                    eventLocation.setPhotoId(location.getPhoto() != null ? location.getPhoto().getId().toString() : "");
                    eventLocation.setPhotoETag(location.getPhoto() != null ? location.getPhoto().getETag() : "");

                    return eventLocation;
                })
                .forEach(location -> eventInfo.getLocations().add(location));
        }
    }
}
