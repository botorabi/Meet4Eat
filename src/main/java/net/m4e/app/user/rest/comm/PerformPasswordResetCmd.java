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
public class PerformPasswordResetCmd {
    private String password;

    public PerformPasswordResetCmd() {}

    public PerformPasswordResetCmd(final String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    @JsonbProperty("password")
    public void setPassword(String password) {
        this.password = password;
    }
}
