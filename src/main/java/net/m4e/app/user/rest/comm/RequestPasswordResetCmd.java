/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.rest.comm;

import javax.json.bind.annotation.JsonbProperty;

/**
 * @author boto
 * Date of creation January 23, 2018
 */
public class RequestPasswordResetCmd {
    private String email;

    public RequestPasswordResetCmd() {}

    public RequestPasswordResetCmd(final String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    @JsonbProperty("email")
    public void setEmail(String email) {
        this.email = email;
    }
}
