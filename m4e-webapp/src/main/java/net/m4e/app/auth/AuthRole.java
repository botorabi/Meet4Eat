/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for defining which roles are permitted to access a web resource
 * 
 * @author boto
 * Date of creation Aug 23, 2017 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AuthRole {

    /**
     * Name of user role: administrator
     */
    String USER_ROLE_ADMIN = "ADMIN";

    /**
     * Name of user role: moderator
     */
    String USER_ROLE_MODERATOR = "MODERATOR";

    /**
     * Name of virtual role GUEST. This role is set automatically by
     * authorization checker for users who are not authenticated (not logged in).
     * 
     * NOTE: Don't use this role explicitly in UserEntity. It should be only used 
     * on web resources.
     */
    String VIRT_ROLE_GUEST = "GUEST";

    /**
     * Name of virtual role USER. This role is set automatically by
     * authorization checker for users who are authenticated (logged in).
     * 
     * NOTE: Don't use this role explicitly in UserEntity. It should be only used 
     * on web resources.
     */
    String VIRT_ROLE_USER = "USER";

    /**
     * Name of virtual role ENDPOINT_CHECK. This is a special role which lets
     * end-point's business logic perform the authorization check depending on 
     * more complex conditions.
     * For instance, the business logic may check for resource ownership and
     * decide to grant access or not. The authorization checker will always
     * grant access to a resource marked with this role.
     * 
     * NOTE: Don't use this role explicitly in UserEntity. It should be only used 
     * on web resources.
     */
    String VIRT_ENDPOINT_CHECK = "ENDPOINT_CHECK";

    /**
     * List of roles which have access to a resource.
     * 
     * @return A list of role names
     */
    String[] grantRoles() default "";

    /**
     * List of permissions which have access to a resource.
     * 
     * @return A list of permission names
     */
    String[] grantPermissions() default "";
}
