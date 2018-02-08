/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.event;

import net.m4e.app.resources.*;
import net.m4e.common.*;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;
import java.io.Serializable;

/**
 * This entity describes an event location.
 * 
 * @author boto
 * Date of creation Aug 31, 2017
 */
@Entity
public class EventLocationEntity extends EntityBase implements Serializable, EntityWithPhoto {

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
    @OneToOne(cascade = {CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH})
    private DocumentEntity photo;

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
        return object instanceof EventLocationEntity;
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
     * Get event location name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set event location name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get event location description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set event location description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get event location photo.
     */
    @Override
    public DocumentEntity getPhoto() {
        return photo;
    }

    /**
     * Set event location photo.
     */
    @Override
    public void setPhoto(DocumentEntity photo) {
        this.photo = photo;
    }
}
