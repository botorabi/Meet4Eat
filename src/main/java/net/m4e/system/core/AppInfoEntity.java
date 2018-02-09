/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.system.core;

import net.m4e.common.EntityBase;

import javax.persistence.*;
import java.io.Serializable;

/**
 * This entity contains general application information which can also be used for
 * maintenance and statistics reports.
 *
 * NOTE: This entity exists only once in database, it is created automatically
 * by AppUpdateManager.
 *
 * @author boto
 * Date of creation Aug 16, 2017
 */
@Entity
public class AppInfoEntity extends EntityBase implements Serializable {

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
     * Current application version
     */
    private String version = "";

    /**
     * Date of last update (milliseconds since epoch)
     */
    private Long dateLastUpdate = 0L;

    /**
     * Date of last maintenance run (database purging etc.)
     */
    private Long dateLastMaintenance = 0L;

    /**
     * Count of user entities which are marked as deleted and can be purged.
     */
    private Long userCountPurge = 0L;

    /**
     * Count of event entities which are marked as deleted and can be purged.
     */
    private Long eventCountPurge = 0L;

    /**
     * Count of event location entities which are marked as deleted and can be purged.
     */
    private Long eventLocationCountPurge = 0L;


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
     * Get the app version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Set the app version.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Get the date of last update (milliseconds since last epoch).
     */
    public Long getDateLastUpdate() {
        return dateLastUpdate;
    }

    /**
     * Set the date of last update (milliseconds since last epoch).
     */
    public void setDateLastUpdate(Long lastUpdate) {
        this.dateLastUpdate = lastUpdate;
    }

    /**
     * Get date of last maintenance run (database purge, etc.)
     */
    public Long getDateLastMaintenance() {
        return dateLastMaintenance;
    }

    /**
     * Set date of last maintenance run (database purge, etc.)
     */
    public void setDateLastMaintenance(Long dateLastMaintenance) {
        this.dateLastMaintenance = dateLastMaintenance;
    }

    /**
     * Get the count of UserEntity entries which are marked as deleted.
     */
    public Long getUserCountPurge() {
        return userCountPurge;
    }

    /**
     * Set the count of UserEntity entries which are marked as deleted.
     */
    public void setUserCountPurge(Long userCountPurge) {
        this.userCountPurge = userCountPurge;
    }

    /**
     * Increment the user purge counter by given 'count'.
     */
    public void incrementUserCountPurge(Long count) {
        userCountPurge += count;
    }

    /**
     * Get the count of EventEntity entries which are marked as deleted.
     */
    public Long getEventCountPurge() {
        return eventCountPurge;
    }

    /**
     * Set the count of EventEntity entries which are marked as deleted.
     */
    public void setEventCountPurge(Long eventCountPurge) {
        this.eventCountPurge = eventCountPurge;
    }

    /**
     * Increment the event purge counter by given 'count'.
     */
    public void incrementEventCountPurge(Long count) {
        eventCountPurge += count;
    }

    /**
     * Get the count of EventLocationEntity entries which are marked as deleted.
     */
    public Long getEventLocationCountPurge() {
        return eventLocationCountPurge;
    }

    /**
     * Set the count of EventLocationEntity entries which are marked as deleted.
     */
    public void setEventLocationCountPurge(Long eventLocationCountPurge) {
        this.eventLocationCountPurge = eventLocationCountPurge;
    }

    /**
     * Increment the event location purge counter by given 'count'.
     */
    public void incrementEventLocationCountPurge(Long count) {
        eventLocationCountPurge += count;
    }
}
