/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.rest.comm;

import javax.json.bind.annotation.JsonbProperty;

/**
 * @author boto
 * Date of creation February 19, 2018
 */
public class EventCmd {

    private String name;

    private String description;

    private boolean isPublic;

    private String photo;

    private Long eventStart;

    private Long repeatWeekDays;

    private Long repeatDayTime;

    private Long votingTimeBegin;


    public EventCmd() {}

    public EventCmd(final String name,
                    final String description,
                    final boolean isPublic,
                    final String photo,
                    final Long eventStart,
                    final Long repeatWeekDays,
                    final Long repeatDayTime,
                    final Long votingTimeBegin) {
        this.name = name;
        this.description = description;
        this.isPublic = isPublic;
        this.photo = photo;
        this.eventStart = eventStart;
        this.repeatWeekDays = repeatWeekDays;
        this.repeatDayTime = repeatDayTime;
        this.votingTimeBegin = votingTimeBegin;
    }

    @JsonbProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @JsonbProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @JsonbProperty("public")
    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public boolean getIsPublic() {
        return isPublic;
    }

    @JsonbProperty("photo")
    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPhoto() {
        return photo;
    }

    @JsonbProperty("eventStart")
    public void setEventStart(Long eventStart) {
        this.eventStart = eventStart;
    }

    public Long getEventStart() {
        return eventStart;
    }

    @JsonbProperty("repeatWeekDays")
    public void setRepeatWeekDays(Long repeatWeekDays) {
        this.repeatWeekDays = repeatWeekDays;
    }

    public Long getRepeatWeekDays() {
        return repeatWeekDays;
    }

    @JsonbProperty("repeatDayTime")
    public void setRepeatDayTime(Long repeatDayTime) {
        this.repeatDayTime = repeatDayTime;
    }

    public Long getRepeatDayTime() {
        return repeatDayTime;
    }

    @JsonbProperty("votingTimeBegin")
    public void setVotingTimeBegin(Long votingTimeBegin) {
        this.votingTimeBegin = votingTimeBegin;
    }

    public Long getVotingTimeBegin() {
        return votingTimeBegin;
    }
}
