/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.auth;

import java.util.ArrayList;
import java.util.List;

/**
 * Available permissions
 * MODIFY permissions allow full access, i.e. read, write, and delete operations
 */
public enum AppPermissions {

    /**
     * Read server status
     */
    READ_SERVER_STATUS,
    /**
     * Read permissions
     */
    READ_PERMS,
    /**
     * Modify permissions
     */
    MODIFY_PERMS,
    /**
     * Read roles
     */
    READ_ROLES,
    /**
     * Modify roles
     */
    MODIFY_ROLES,
    /**
     * Read events
     */
    READ_EVENT,
    /**
     * Modify events
     */
    MODIFY_EVENT,
    /**
     * Read user
     */
    READ_USER,
    /**
     * Modify user
     */
    MODIFY_USER,
    /**
     * Read user roles
     */
    READ_USER_ROLES,
    /**
     * Modify user roles
     */
    MODIFY_USER_ROLES;

    /**
     * Get the permission names.
     *
     * @return List of permission names
     */
    static List<String> getPermissionNames() {
        List<String> perms = new ArrayList<>();
        for (AppPermissions perm : AppPermissions.values()) {
            perms.add(perm.name());
        }
        return perms;
    }
    }


