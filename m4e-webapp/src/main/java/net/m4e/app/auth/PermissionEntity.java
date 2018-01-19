/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.auth;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * This entity describes a single permission.
 * 
 * @author boto
 * Date of creation Aug 21, 2017
 */
@Entity
public class PermissionEntity implements Serializable {

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
     * Get permission name.
     * 
     * @return The permission name
     */
    public String getName() {
        return name;
    }

    /**
     * Set permission name.
     * 
     * @param name Permission name
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof PermissionEntity)) {
            return false;
        }
        PermissionEntity other = (PermissionEntity) object;
        return (this.id != null || other.id == null) && (this.id == null || this.id.equals(other.id));
    }

    @Override
    public String toString() {
        return "net.m4e.auth.PermissionEntity[ id=" + id + ", name=" + name + " ]";
    }
}
