/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.auth;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic authorization roles are defined here. They are used for initial
 * application setup. They can be extended by more fine grained roles and 
 * permissions.
 * 
 * @author boto
 * Date of creation Aug 23, 2017
 */
public class DefaultRoles {

    /**
     * Roles
     */
    public enum Roles {
        /**
         * System administrator
         */
        ADMIN,

        /**
         * Moderator
         */
        MODERATOR,

        /**
         * Authenticated user (already logged in)
         */
        USER,

        /**
         * Guest with limited rights
         */
        GUEST,

        /**
         * This is a special role which lets the business logic perform the
         * authorization check depending on more complex conditions.
         * For instance, the business logic may check for resource ownership and
         * decide weather to grant access or not. The authorization checker will always
         * grant access to a resource marked with this role.
         */
        NOCHECK
    }

    /**
     * Lookup for roles and associated permissions
     */
    private final Map<String, List<String>> roles;

    /**
     * Construct the instance.
     */
    public DefaultRoles() {
        roles = new HashMap<>();
        setupRoles();
    }

    /**
     * Get all roles along their permissions.
     * 
     * @return All roles and their permissions
     */
    public Map<String, List<String>> getRoles() {
        return roles;
    }

    /**
     * Setup all default roles and their permissions.
     */
    private void setupRoles() {
        roles.put(Roles.ADMIN.name(), Arrays.asList(
            DefaultPermissions.Perm.READ_SERVER_STATUS.name(),
            DefaultPermissions.Perm.MODIFY_PERMS.name(),
            DefaultPermissions.Perm.MODIFY_ROLES.name(),
            DefaultPermissions.Perm.MODIFY_USER.name(),
            DefaultPermissions.Perm.MODIFY_EVENT.name(),
            DefaultPermissions.Perm.MODIFY_USER_ROLES.name()));

        roles.put(Roles.MODERATOR.name(), Arrays.asList(
            DefaultPermissions.Perm.READ_SERVER_STATUS.name(),
            DefaultPermissions.Perm.READ_EVENT.name(),
            DefaultPermissions.Perm.MODIFY_EVENT.name(),
            DefaultPermissions.Perm.READ_USER.name(),
            DefaultPermissions.Perm.READ_USER_ROLES.name(),
            DefaultPermissions.Perm.MODIFY_USER_ROLES.name()));

        roles.put(Roles.USER.name(), Arrays.asList(
            DefaultPermissions.Perm.READ_SERVER_STATUS.name(),
            DefaultPermissions.Perm.READ_EVENT.name(),
            DefaultPermissions.Perm.READ_USER.name(),
            DefaultPermissions.Perm.READ_USER_ROLES.name()));

        roles.put(Roles.GUEST.name(), Arrays.asList(
            DefaultPermissions.Perm.READ_SERVER_STATUS.name()));
    }
}