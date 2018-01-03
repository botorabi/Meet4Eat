/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.system.core;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

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
public class AppInfoEntity implements Serializable {

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
     *
     * @return Entity ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Set the entity ID.
     *
     * @param id Entity ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get the app version.
     *
     * @return App version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Set the app version.
     *
     * @param version App version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Get the date of last update (milliseconds since last epoch).
     *
     * @return Last update
     */
    public Long getDateLastUpdate() {
        return dateLastUpdate;
    }

    /**
     * Set the date of last update (milliseconds since last epoch).
     *
     * @param lastUpdate Date of last update
     */
    public void setDateLastUpdate(Long lastUpdate) {
        this.dateLastUpdate = lastUpdate;
    }

    /**
     * Get date of last maintenance run (database purge, etc.)
     *
     * @return Date of last maintenance run
     */
    public Long getDateLastMaintenance() {
        return dateLastMaintenance;
    }

    /**
     * Set date of last maintenance run (database purge, etc.)
     *
     * @param dateLastMaintenance Date of last maintenance run
     */
    public void setDateLastMaintenance(Long dateLastMaintenance) {
        this.dateLastMaintenance = dateLastMaintenance;
    }

    /**
     * Get the count of UserEntity entries which are marked as deleted.
     *
     * @return Count of user entities which can be purged
     */
    public Long getUserCountPurge() {
        return userCountPurge;
    }

    /**
     * Set the count of UserEntity entries which are marked as deleted.
     *
     * @param userCountPurge of user entities which can be purged
     */
    public void setUserCountPurge(Long userCountPurge) {
        this.userCountPurge = userCountPurge;
    }

    /**
     * Increment the user purge counter by given 'count'.
     *
     * @param count Count of incrementation.
     */
    public void incrementUserCountPurge(Long count) {
        userCountPurge += count;
    }

    /**
     * Get the count of EventEntity entries which are marked as deleted.
     *
     * @return Count of event entities which can be purged
     */
    public Long getEventCountPurge() {
        return eventCountPurge;
    }

    /**
     * Set the count of EventEntity entries which are marked as deleted.
     *
     * @param eventCountPurge Count of event entities which can be purged
     */
    public void setEventCountPurge(Long eventCountPurge) {
        this.eventCountPurge = eventCountPurge;
    }

    /**
     * Increment the event purge counter by given 'count'.
     *
     * @param count the count of incrementation.
     */
    public void incrementEventCountPurge(Long count) {
        eventCountPurge += count;
    }

    /**
     * Get the count of EventLocationEntity entries which are marked as deleted.
     *
     * @return Count of event entities which can be purged
     */
    public Long getEventLocationCountPurge() {
        return eventLocationCountPurge;
    }

    /**
     * Set the count of EventLocationEntity entries which are marked as deleted.
     *
     * @param eventLocationCountPurge Count of event entities which can be purged
     */
    public void setEventLocationCountPurge(Long eventLocationCountPurge) {
        this.eventLocationCountPurge = eventLocationCountPurge;
    }

    /**
     * Increment the event location purge counter by given 'count'.
     *
     * @param count the count of incrementation
     */
    public void incrementEventLocationCountPurge(Long count) {
        eventLocationCountPurge += count;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof AppInfoEntity)) {
            return false;
        }
        AppInfoEntity that = (AppInfoEntity) object;

        return this.id != null && Objects.equals(this.id, that.id);
    }

    @Override
    public String toString() {
        return "net.m4e.app.AppInfoEntity[ id=" + id + " ]";
    }
}
