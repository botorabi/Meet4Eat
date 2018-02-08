/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event;

import net.m4e.app.resources.*;
import net.m4e.app.user.business.UserEntity;
import net.m4e.common.*;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;

/**
 * A class describing an event
 * 
 * @author boto
 * Date of creation Aug 18, 2017
 */
@Entity
public class EventEntity extends EntityBase implements Serializable, EntityWithPhoto {

    /**
     * Serialization version
     */
    private static final long serialVersionUID = 1L;

    /**
     * Unique entity ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Entity status
     */
    @OneToOne(optional=false, cascade = CascadeType.ALL)
    private StatusEntity status;       

    /**
     * Event name
     */
    private String name;

    /**
     * Event description
     */
    private String description;

    /**
     * Is the event accessible for other users?
     */
    private boolean isPublic = false;

    /**
     * Photo
     */
    @OneToOne(cascade = {CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH})
    private DocumentEntity photo;

    /**
     * Event members
     */
    @OneToMany(targetEntity=UserEntity.class, cascade = {CascadeType.REFRESH, CascadeType.DETACH})
    private Collection<UserEntity> members;

    /**
     * A list of locations which represent possible meeting points
     */
    @OneToMany(targetEntity=EventLocationEntity.class, cascade = CascadeType.ALL)
    private Collection<EventLocationEntity> locations;

    /**
     * Event start time in seconds.
     */
    private Long eventStart = 0L;

    /**
     * A bit field defining which week days the event should be repeated.
     * Monday to Sunday are represented by bits 0 to 6.
     */
    private Long repeatWeekDays = 0L;

    /**
     * For repeating events this defines the day time in seconds.
     */
    private Long repeatDayTime = 0L;

    /**
     * Time offset used for beginning the voting before the event time was reached (in seconds)
     */
    private Long votingTimeBegin = 0L;

    /**
     * Get the entity ID.
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * Set the entity ID.
     */
    @Override
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Check if the object is an instance of this entity.
     */
    @Override
    @JsonbTransient
    public boolean isInstanceOfMe(Object object) {
        return object instanceof EventEntity;
    }

    /**
     * Get entity status. It contains information about entity's life-cycle,
     * ownership, etc.
     */
    public StatusEntity getStatus() {
        return status;
    }

    /**
     * Set entity status.
     */
    public void setStatus(StatusEntity status) {
        this.status = status;
    }

    /**
     * Get event name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set event name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get event description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set event description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Is the event public? If not then only event owner and members have access.
     */
    public boolean getIsPublic() {
        return isPublic;
    }

    /**
     * Set the event public flag.
     * If the event is not public then only event owner and members have access.
     */
    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    /**
     * Get event photo.
     */
    @Override
    public DocumentEntity getPhoto() {
        return photo;
    }

    /**
     * Set the event photo.
     * 
     * @param photo DocumentEntity containing the photo
     */
    @Override
    public void setPhoto(DocumentEntity photo) {
        this.photo = photo;
    }

    /**
     * Get the event start time.
     */
    public Long getEventStart() {
        return eventStart;
    }

    /**
     * Set the event start time.
     */
    public void setEventStart(Long eventStart) {
        this.eventStart = eventStart;
    }

    /**
     * Get week days for a repeating event.
     */
    public Long getRepeatWeekDays() {
        return repeatWeekDays;
    }

    /**
     * Set week days for a repeating event.
     */
    public void setRepeatWeekDays(Long repeatWeekDays) {
        this.repeatWeekDays = repeatWeekDays;
    }

    /**
     * Get day time for a repeating event.
     * The returned time is in UTC in order to avoid time zone conflicts!
     */
    public Long getRepeatDayTime() {
        return repeatDayTime;
    }

    /**
     * Set day time for a repeating event.
     * This time must be in UTC in order to avoid time zone conflicts!
     */
    public void setRepeatDayTime(Long repeatDayTime) {
        this.repeatDayTime = repeatDayTime;
    }

    /**
     * Get the begin of voting time. This is the time offset before the event takes place
     * and can be used to remind users about an upcoming event. Votes are accepted only
     * during the time window: [(event start) .. (event start - voting time begin)]
     */
    public Long getVotingTimeBegin() {
        return votingTimeBegin;
    }

    /**
     * Set the begin of voting time. This is an offset to event start.
     */
    public void setVotingTimeBegin(Long votingTimeBegin) {
        this.votingTimeBegin = votingTimeBegin;
    }

    /**
     * Get event members.
     */
    public Collection<UserEntity> getMembers() {
        return members;
    }

    /**
     * Set event members.
     */
    public void setMembers(Collection<UserEntity> members) {
        this.members = members;
    }

    /**
     * Get event locations.
     */
    public Collection<EventLocationEntity> getLocations() {
        return locations;
    }

    /**
     * Set event locations.
     */
    public void setLocations(Collection<EventLocationEntity> locations) {
        this.locations = locations;
    }
}
