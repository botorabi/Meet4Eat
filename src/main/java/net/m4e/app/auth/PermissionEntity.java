/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.auth;

import net.m4e.common.EntityBase;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;
import java.io.Serializable;

/**
 * This entity describes a single permission.
 * 
 * @author boto
 * Date of creation Aug 21, 2017
 */
@Entity
public class PermissionEntity extends EntityBase implements Serializable {

    /**
     * Serialization version
     */
    private static final long serialVersionUID = 1L;

    /**
     * Entity's unique ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Permission name
     */
    private String name;

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
        return object instanceof PermissionEntity;
    }

    /**
     * Get permission name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set permission name.
     */
    public void setName(String name) {
        this.name = name;
    }
}
