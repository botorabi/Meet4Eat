/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.core;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Entity holding information about the application
 * 
 * @author boto
 * Date of creation Aug 16, 2017
 */
@Entity
@XmlRootElement
public class AppInfoEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Current application version
     */
    private String version;

    /**
     * Last update (milliseconds since epoche)
     */
    private int lastUpdate;

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
     * Get the date of last update (milliseconds since last epoche).
     * 
     * @return Last update
     */
    public int getLastUpdate() {
        return lastUpdate;
    }

    /**
     * Set the date of last update (milliseconds since last epoche).
     * 
     * @param lastUpdate Date of last update
     */
    public void setLastUpdate(int lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AppInfoEntity)) {
            return false;
        }
        AppInfoEntity other = (AppInfoEntity) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "net.m4e.app.AppInfoEntity[ id=" + id + " ]";
    }   
}
