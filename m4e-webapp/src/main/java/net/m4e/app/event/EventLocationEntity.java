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
import net.m4e.common.EntityWithPhoto;

import javax.persistence.*;
import java.io.Serializable;

/**
 * This entity describes an event location.
 * 
 * @author boto
 * Date of creation Aug 31, 2017
 */
@Entity
public class EventLocationEntity implements Serializable, EntityWithPhoto {

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
     * Photo
     */
    @OneToOne(optional=true, cascade = {CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH})
    private DocumentEntity photo;

    /**
     * Create an event location instance.
     */
    public EventLocationEntity() {
    }

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
     * Get event location name.
     * 
     * @return Event location name
     */
    public String getName() {
        return name;
    }

    /**
     * Set event location name.
     * 
     * @param name Event location name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get event location description.
     * 
     * @return Event location description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set event location description.
     * 
     * @param description Event location description 
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get event location photo.
     * 
     * @return DocumentEntity containing the photo
     */
    @Override
    public DocumentEntity getPhoto() {
        return photo;
    }

    /**
     * Set event location photo.
     * 
     * @param photo DocumentEntity containing the photo
     */
    @Override
    public void setPhoto(DocumentEntity photo) {
        this.photo = photo;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof EventLocationEntity)) {
            return false;
        }
        EventLocationEntity other = (EventLocationEntity) object;
        return (this.id != null || other.id == null) && (this.id == null || this.id.equals(other.id));
    }

    @Override
    public String toString() {
        return "net.m4e.events.EventLocationEntity[ id=" + id + " ]";
    }
}
