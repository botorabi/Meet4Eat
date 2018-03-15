/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.auth;

import net.m4e.common.EntityBase;

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
public class RoleEntity extends EntityBase implements Serializable {

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
     * Get the role name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the role name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the role permissions.
     */
    public Collection<PermissionEntity> getPermissions() {
        return permissions;
    }

    /**
     * Set the role permissions.
     */
    public void setPermissions(Collection<PermissionEntity> permissions) {
        this.permissions = permissions;
    }
}
