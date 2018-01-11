/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.auth;

import java.util.*;

/**
 * Basic authorization roles are defined here. They are used for initial
 * application setup. They can be extended by more fine grained roles and
 * permissions.
 *
 * @author boto
 * Date of creation Aug 23, 2017
 */
public enum AppRoles {

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
    GUEST;

    /**
     * Get all roles along their permissions.
     *
     * @return All roles and their permissions
     */
    static Map<String, List<String>> getRoles() {
        Map<String, List<String>> roles = new HashMap<>();
        roles.put(AppRoles.ADMIN.name(), Arrays.asList(
                AppPermissions.READ_SERVER_STATUS.name(),
                AppPermissions.MODIFY_PERMS.name(),
                AppPermissions.MODIFY_ROLES.name(),
                AppPermissions.MODIFY_USER.name(),
                AppPermissions.MODIFY_EVENT.name(),
                AppPermissions.MODIFY_USER_ROLES.name()));

        roles.put(AppRoles.MODERATOR.name(), Arrays.asList(
                AppPermissions.READ_SERVER_STATUS.name(),
                AppPermissions.READ_EVENT.name(),
                AppPermissions.MODIFY_EVENT.name(),
                AppPermissions.READ_USER.name(),
                AppPermissions.READ_USER_ROLES.name(),
                AppPermissions.MODIFY_USER_ROLES.name()));

        roles.put(AppRoles.USER.name(), Arrays.asList(
                AppPermissions.READ_SERVER_STATUS.name(),
                AppPermissions.READ_EVENT.name(),
                AppPermissions.READ_USER.name(),
                AppPermissions.READ_USER_ROLES.name()));

        roles.put(AppRoles.GUEST.name(), Arrays.asList(
                AppPermissions.READ_SERVER_STATUS.name()));

        return roles;
    }
}
