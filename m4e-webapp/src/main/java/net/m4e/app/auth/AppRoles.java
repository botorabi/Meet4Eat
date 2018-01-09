/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.auth;

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
public interface AppRoles {

    /**
     * Roles
     */
    enum Roles {
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
     * Get all roles along their permissions.
     * 
     * @return All roles and their permissions
     */
    static Map<String, List<String>> getRoles() {
        Map<String, List<String>> roles = new HashMap<>();
        roles.put(Roles.ADMIN.name(), Arrays.asList(
                AppPermissions.Perm.READ_SERVER_STATUS.name(),
                AppPermissions.Perm.MODIFY_PERMS.name(),
                AppPermissions.Perm.MODIFY_ROLES.name(),
                AppPermissions.Perm.MODIFY_USER.name(),
                AppPermissions.Perm.MODIFY_EVENT.name(),
                AppPermissions.Perm.MODIFY_USER_ROLES.name()));

        roles.put(Roles.MODERATOR.name(), Arrays.asList(
                AppPermissions.Perm.READ_SERVER_STATUS.name(),
                AppPermissions.Perm.READ_EVENT.name(),
                AppPermissions.Perm.MODIFY_EVENT.name(),
                AppPermissions.Perm.READ_USER.name(),
                AppPermissions.Perm.READ_USER_ROLES.name(),
                AppPermissions.Perm.MODIFY_USER_ROLES.name()));

        roles.put(Roles.USER.name(), Arrays.asList(
                AppPermissions.Perm.READ_SERVER_STATUS.name(),
                AppPermissions.Perm.READ_EVENT.name(),
                AppPermissions.Perm.READ_USER.name(),
                AppPermissions.Perm.READ_USER_ROLES.name()));

        roles.put(Roles.GUEST.name(), Arrays.asList(
                AppPermissions.Perm.READ_SERVER_STATUS.name()));

        return roles;
    }
}