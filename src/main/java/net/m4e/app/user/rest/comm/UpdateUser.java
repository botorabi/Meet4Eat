/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.rest.comm;

/**
 * @author boto
 * Date of creation January 23, 2018
 */
public class UpdateUser {
    private final String userId;

    public UpdateUser(final String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
