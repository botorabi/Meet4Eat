/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.auth;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;

/**
 * This entity describes a user role which may be associated to permissions.
 * 
 * @author boto
 * Date of creation Aug 21, 2017
 */
@Entity
public class RoleEntity implements Serializable {

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
     * A list of permissions belonging to this role.
     */
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH})
    private Collection<PermissionEntity> permissions;

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

    /**
     * Get the role permissions.
     * 
     * @return Role permissions
     */
    public Collection<PermissionEntity> getPermissions() {
        return permissions;
    }

    /**
     * Set the role permissions.
     * 
     * @param permissions Role permissions
     */
    public void setPermissions(Collection<PermissionEntity> permissions) {
        this.permissions = permissions;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof RoleEntity)) {
            return false;
        }
        RoleEntity other = (RoleEntity) object;
        return (this.id != null || other.id == null) && (this.id == null || this.id.equals(other.id));
    }

    @Override
    public String toString() {
        return "net.m4e.auth.PermissionEntity[ id=" + id + ", name=" + name + " ]";
    }
}
