/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event;

import net.m4e.app.resources.DocumentEntity;
import net.m4e.app.resources.StatusEntity;
import net.m4e.app.user.business.UserEntity;
import net.m4e.common.EntityWithPhoto;

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
public class EventEntity implements Serializable, EntityWithPhoto {

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
    @OneToOne(optional=true, cascade = {CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH})
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
     * Create an event entity.
     */
    public EventEntity() {
    }

    /**
     * Get event ID.
     * 
     * @return Event ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Set Event ID.
     * 
     * @param id Event ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get entity status. It contains information about entity's life-cycle,
     * ownership, etc.
     * 
     * @return Entity status
     */
    public StatusEntity getStatus() {
        return status;
    }

    /**
     * Set entity status.
     * 
     * @param status Entity status
     */
    public void setStatus(StatusEntity status) {
        this.status = status;
    }

    /**
     * Get event name.
     * 
     * @return Event name
     */
    public String getName() {
        return name;
    }

    /**
     * Set event name.
     * 
     * @param name Event name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get event description.
     * 
     * @return Event description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set event description.
     * 
     * @param description Event description 
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Is the event public? If not then only event owner and members have access.
     * 
     * @return Return true if the event is public.
     */
    public boolean getIsPublic() {
        return isPublic;
    }

    /**
     * Set the event public flag.
     * If the event is not public then only event owner and members have access.
     * 
     * @param isPublic Event public event
     */
    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    /**
     * Get event photo.
     * 
     * @return DocumentEntity containing the photo
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
     * 
     * @return Event start in seconds since epoch
     */
    public Long getEventStart() {
        return eventStart;
    }

    /**
     * Set the event start time.
     * 
     * @param eventStart Event start in seconds since epoch
     */
    public void setEventStart(Long eventStart) {
        this.eventStart = eventStart;
    }

    /**
     * Get week days for a repeating event.
     * 
     * @return Week days
     */
    public Long getRepeatWeekDays() {
        return repeatWeekDays;
    }

    /**
     * Set week days for a repeating event.
     * 
     * @param repeatWeekDays Week days
     */
    public void setRepeatWeekDays(Long repeatWeekDays) {
        this.repeatWeekDays = repeatWeekDays;
    }

    /**
     * Get day time for a repeating event.
     * The returned time is in UTC in order to avoid time zone conflicts!
     * 
     * @return Day time in seconds
     */
    public Long getRepeatDayTime() {
        return repeatDayTime;
    }

    /**
     * Set day time for a repeating event.
     * This time must be in UTC in order to avoid time zone conflicts!
     * 
     * @param repeatDayTime Day time in seconds
     */
    public void setRepeatDayTime(Long repeatDayTime) {
        this.repeatDayTime = repeatDayTime;
    }

    /**
     * Get the begin of voting time. This is the time offset before the event takes place
     * and can be used to remind users about an upcoming event. Votes are accepted only
     * during the time window: [(event start) .. (event start - voting time begin)]
     * 
     * @return Begin of voting time in seconds
     */
    public Long getVotingTimeBegin() {
        return votingTimeBegin;
    }

    /**
     * Set the begin of voting time. This is an offset to event start.
     * 
     * @param votingTimeBegin Begin of voting time in seconds
     */
    public void setVotingTimeBegin(Long votingTimeBegin) {
        this.votingTimeBegin = votingTimeBegin;
    }

    /**
     * Get event members.
     * 
     * @return Event members
     */
    public Collection<UserEntity> getMembers() {
        return members;
    }

    /**
     * Set event members.
     * 
     * @param members Event members
     */
    public void setMembers(Collection<UserEntity> members) {
        this.members = members;
    }

    /**
     * Get event locations.
     * 
     * @return Event locations
     */
    public Collection<EventLocationEntity> getLocations() {
        return locations;
    }

    /**
     * Set event locations.
     * 
     * @param locations event locations
     */
    public void setLocations(Collection<EventLocationEntity> locations) {
        this.locations = locations;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof EventEntity)) {
            return false;
        }
        EventEntity other = (EventEntity) object;
        return (this.id != null || other.id == null) && (this.id == null || this.id.equals(other.id));
    }

    @Override
    public String toString() {
        return "net.m4e.events.EventEntity[ id=" + id + " ]";
    }
}
