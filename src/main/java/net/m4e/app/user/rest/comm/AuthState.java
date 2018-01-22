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
 * Date of creation January 22, 2018
 */
public class AuthState {
    private final boolean auth;
    private final String id;
    private final String sid;

    public AuthState(final boolean auth, final String id, String sid) {
        this.auth = auth;
        this.id = id;
        this.sid = sid;
    }

    public boolean isAuth() {
        return auth;
    }

    public String getId() {
        return id;
    }

    public String getSid() {
        return sid;
    }
}
