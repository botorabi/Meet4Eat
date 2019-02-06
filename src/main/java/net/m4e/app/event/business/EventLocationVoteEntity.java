/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.business;

import net.m4e.common.EntityBase;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

import static javax.persistence.FetchType.EAGER;

/**
 * Entity for storing the results of event location votes.
 * 
 * @author boto
 * Date of creation Nov 11, 2017
 */
@Entity
@NamedQueries({
    /**
     * Find all votes for all event locations in a given time window.
     * 
     * Query parameters:
     * 
     * timeBegin - timeEnd      The voting time window
     * eventId                  The event containing the locations
     */
    @NamedQuery(
      name = "EventLocationVoteEntity.findVotes",
      query = "SELECT vote FROM EventLocationVoteEntity vote WHERE vote.creationTime >= :timeBegin AND vote.creationTime <= :timeEnd AND vote.eventId = :eventId"
    ),
    /**
     * Find all votes for a given event location in a given time window.
     * 
     * Query parameters:
     * 
     * timeBegin - timeEnd      The voting time window
     * locationId               The ID of event location
     */
    @NamedQuery(
      name = "EventLocationVoteEntity.findLocationVotes",
      query = "SELECT vote FROM EventLocationVoteEntity vote WHERE vote.creationTime >= :timeBegin AND vote.creationTime <= :timeEnd AND vote.locationId = :locationId"
    )
})
public class EventLocationVoteEntity extends EntityBase implements Serializable {

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
     * Begin of voting in seconds
     */
    private Long voteTimeBegin = 0L;

    /**
     * End of voting in seconds
     */
    private Long voteTimeEnd = 0L;

    /**
     * Timestamp of vote entry creation
     */
    private Long creationTime = 0L;

    /**
     * ID of the event containing the location
     */
    private Long eventId = 0L;

    /**
     * Event location ID
     */
    private Long locationId = 0L;

    /**
     * Event location name
     */
    private String locationName = "";

    /**
     * IDs of users voted for this location
     */
    @ElementCollection(fetch = EAGER)
    private Set<Long> userIds;

    /**
     * Names of users voted for this location.
     * This is a redundant information for reducing the server traffic as the
     * names can be also retrieved from user IDs. However, that would mean another
     * entity fetch (UserEntity). In most use-cases knowing the voting user names
     * is completely sufficient.
     */
    @ElementCollection(fetch = EAGER)
    private Set<String> userNames;

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
     * Get the begin of voting.
     */
    public Long getVoteTimeBegin() {
        return voteTimeBegin;
    }

    /**
     * Set the begin of voting.
     */
    public void setVoteTimeBegin(Long voteTimeBegin) {
        this.voteTimeBegin = voteTimeBegin;
    }

    /**
     * Get the end of voting.
     */
    public Long getVoteTimeEnd() {
        return voteTimeEnd;
    }

    /**
     * Set the time of voting end.
     */
    public void setVoteTimeEnd(Long voteTimeEnd) {
        this.voteTimeEnd = voteTimeEnd;
    }

    /**
     * Get the creation time of this vote entry.
     */
    public Long getCreationTime() {
        return creationTime;
    }

    /**
     * Set the creation time of this vote entry.
     */
    public void setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * Get the ID of event the location belongs to.
     */
    public Long getEventId() {
        return eventId;
    }

    /**
     * Set the event ID.
     */
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    /**
     * Get the location ID.
     */
    public Long getLocationId() {
        return locationId;
    }

    /**
     * Set the location ID.
     */
    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    /**
     * Get the location name.
     */
    public String getLocationName() {
        return locationName;
    }

    /**
     * Set the location name.
     */
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    /**
     * Get the IDs of users who voted for this location.
     */
    public Set<Long> getUserIds() {
        return userIds;
    }

    /**
     * Set the IDs of users who voted for this location.
     */
    public void setUserIds(Set<Long> userIds) {
        this.userIds = userIds;
    }

    /**
     * Add the given user ID to voted user IDs.
     */
    public void addUserId(Long userId) {
        if (userIds == null) {
            userIds = new HashSet();
        }
        userIds.add(userId);
    }

    /**
     * Remove the given user ID from voted user IDs.
     */
    public boolean removeUserId(Long userId) {
        if (userIds == null) {
            return false;
        }
        return userIds.remove(userId);
    }

    /**
     * Get the names of users who voted for this location.
     */
    public Set<String> getUserNames() {
        return userNames;
    }

    /**
     * Set the names of users who voted for this location.
     */
    public void setUserNames(Set<String> userNames) {
        this.userNames = userNames;
    }

    /**
     * Add the given user name to voted user names.
     */
    public void addUserName(String userName) {
        if (userNames == null) {
            userNames = new HashSet();
        }
        userNames.add(userName);
    }

    /**
     * Remove the given user name from voted user names.
     */
    public boolean removeUserName(String userName) {
        if (userNames == null) {
            return false;
        }
        return userNames.remove(userName);
    }
}
