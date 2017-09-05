/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.auth;

import java.util.ArrayList;
import java.util.List;

/**
 * Default authorization permissions are defined here. They are used for initial
 * application setup.
 * 
 * @author boto
 * Date of creation Aug 21, 2017
 */
public class DefaultPermissions {

    /**
     * Default permissions
     * MODIFY permissions allow full access, i.e. read, write, and delete operations
     */
    public static enum Perm {
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
        MODIFY_USER_ROLES
    }

    /**
     * List of permission names
     */
    private final List<String> perms;

    /**
     * Construct the instance.
     */
    public DefaultPermissions() {
        perms = new ArrayList<>();
        for(Perm perm: Perm.values()) {
            perms.add(perm.name());
        }
    }

    /**
     * Get the default permission names.
     * 
     * @return List of permission names
     */
    public List<String> getPermissionNames() {
        return perms;
    }
}
