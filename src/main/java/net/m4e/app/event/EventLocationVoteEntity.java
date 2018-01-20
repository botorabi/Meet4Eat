/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

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
public class EventLocationVoteEntity implements Serializable {

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
    private Set<Long> userIds;

    /**
     * Names of users voted for this location.
     * This is a redundant information for reducing the server traffic as the
     * names can be also retrieved from user IDs. However, that would mean another
     * entity fetch (UserEntity). In most use-cases knowing the voting user names
     * is completely sufficient.
     */
    private Set<String> userNames;

    /**
     * Get event location ID.
     * 
     * @return Event location ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Set event location ID.
     * 
     * @param id Event location ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get the begin of voting.
     * 
     * @return Time of voting begin in seconds since epoch
     */
    public Long getVoteTimeBegin() {
        return voteTimeBegin;
    }

    /**
     * Set the begin of voting.
     * 
     * @param voteTimeBegin Time of voting begin in seconds since epoch
     */
    public void setVoteTimeBegin(Long voteTimeBegin) {
        this.voteTimeBegin = voteTimeBegin;
    }

    /**
     * Get the end of voting.
     * 
     * @return Time of voting end in seconds since epoch
     */
    public Long getVoteTimeEnd() {
        return voteTimeEnd;
    }

    /**
     * Set the time of voting end.
     * 
     * @param voteTimeEnd Time of voting end in seconds since epoch
     */
    public void setVoteTimeEnd(Long voteTimeEnd) {
        this.voteTimeEnd = voteTimeEnd;
    }

    /**
     * Get the creation time of this vote entry.
     * 
     * @return Creation time in seconds
     */
    public Long getCreationTime() {
        return creationTime;
    }

    /**
     * Set the creation time of this vote entry.
     * 
     * @param creationTime Creation timestamp in seconds
     */
    public void setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * Get the ID of event the location belongs to.
     * 
     * @return The location ID
     */
    public Long getEventId() {
        return eventId;
    }

    /**
     * Set the event ID.
     * 
     * @param eventId The event ID
     */
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    /**
     * Get the location ID.
     * 
     * @return The location ID
     */
    public Long getLocationId() {
        return locationId;
    }

    /**
     * Set the location ID.
     * 
     * @param locationId The location ID
     */
    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    /**
     * Get the location name.
     * 
     * @return The location name
     */
    public String getLocationName() {
        return locationName;
    }

    /**
     * Set the location name.
     * 
     * @param locationName The location name
     */
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    /**
     * Get the IDs of users who voted for this location.
     * 
     * @return IDs of voting users
     */
    public Set<Long> getUserIds() {
        return userIds;
    }

    /**
     * Set the IDs of users who voted for this location.
     * 
     * @param userIds Users who voted for this location
     */
    public void setUserIds(Set<Long> userIds) {
        this.userIds = userIds;
    }

    /**
     * Add the given user ID to voted user IDs.
     * 
     * @param userId  User ID to add to voters
     */
    public void addUserId(Long userId) {
        if (userIds == null) {
            userIds = new HashSet();
        }
        userIds.add(userId);
    }

    /**
     * Remove the given user ID from voted user IDs.
     * 
     * @param userId    User ID to remove from voters
     * @return          Return false if the user was not in voters list before.
     */
    public boolean removeUserId(Long userId) {
        if (userIds == null) {
            return false;
        }
        userIds.remove(userId);
        return true;
    }

    /**
     * Get the names of users who voted for this location.
     * 
     * @return Names of voting users
     */
    public Set<String> getUserNames() {
        return userNames;
    }

    /**
     * Set the names of users who voted for this location.
     * 
     * @param userNames Users who voted for this location
     */
    public void setUserNames(Set<String> userNames) {
        this.userNames = userNames;
    }

    /**
     * Add the given user name to voted user names.
     * 
     * @param userName  User name to add to voters
     */
    public void addUserName(String userName) {
        if (userNames == null) {
            userNames = new HashSet();
        }
        userNames.add(userName);
    }

    /**
     * Remove the given user name from voted user names.
     * 
     * @param userName  User name to remove from voters
     * @return          Return false if the user was not in voters list before.
     */
    public boolean removeUserName(String userName) {
        if (userNames == null) {
            return false;
        }
        userNames.remove(userName);
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof EventLocationVoteEntity)) {
            return false;
        }
        EventLocationVoteEntity other = (EventLocationVoteEntity) object;
        return (this.id != null || other.id == null) && (this.id == null || this.id.equals(other.id));
    }

    @Override
    public String toString() {
        return "net.m4e.app.event.EventLocationVoteEntity[ id=" + id + " ]";
    }    
}
