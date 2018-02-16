/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.business;

import net.m4e.app.resources.DocumentEntity;
import net.m4e.common.UserEntityCreator;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base test class for Users
 *
 * @author boto
 * Date of creation February 6, 2018
 */
class UsersMiscTest extends UsersTestBase {

    @Nested
    class Misc {

        @Test
        void defaultConstructor() {
            new Users();
        }

        @Test
        void updateUser() {
            UserEntity user = UserEntityCreator.create();
            users.updateUser(user);
        }

        @Test
        void updateUserImage() {
            UserEntity user = UserEntityCreator.create();
            DocumentEntity photo = user.getPhoto();
            users.updateUserImage(user, photo);
        }

        @Test
        void getUserRelatives() {
            UserEntity user = UserEntityCreator.create();
            assertThat(users.getUserRelatives(user)).isEmpty();
        }
    }
}
