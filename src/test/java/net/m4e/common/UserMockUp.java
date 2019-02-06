/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.common;

import net.m4e.app.auth.AuthRole;
import net.m4e.app.user.business.*;
import org.mockito.Mockito;

import java.util.Arrays;

/**
 * @author boto
 * Date of creation February 20, 2018
 */
public class UserMockUp {

    public static final Long USER_ID_ADMIN = 10000L;
    public static final String USER_NAME_ADMIN = "admin";
    public static final Long USER_ID_NON_ADMIN = 20000L;
    public static final String USER_NAME_NON_ADMIN = "nonadmin";
    public static final Long USER_ID_OTHER = 30000L;
    public static final String USER_NAME_OTHER = "otheruser";

    private Users users;

    public UserMockUp(Users users) {
        this.users = users;
    }

    public UserEntity mockAdminUser() {
        UserEntity user = UserEntityCreator.createWithRoles(Arrays.asList(AuthRole.USER_ROLE_ADMIN));
        user.setId(USER_ID_ADMIN);
        user.setName(USER_NAME_ADMIN);

        Mockito.when(users.findUser(USER_NAME_ADMIN)).thenReturn(user);

        return user;
    }

    public UserEntity mockUser(final Long id, final String name) {
        UserEntity user = UserEntityCreator.create();
        user.setId(id);
        user.setName(name);

        Mockito.when(users.findUser(id)).thenReturn(user);
        Mockito.when(users.findUser(name)).thenReturn(user);

        return user;
    }

    public UserEntity mockSomeUser() {
        return mockUser(USER_ID_OTHER, USER_NAME_OTHER);
    }
}
