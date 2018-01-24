/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.rest.comm;

import net.m4e.app.user.rest.UserValidator;

import javax.json.bind.annotation.JsonbProperty;
import javax.validation.constraints.Size;

/**
 * @author boto
 * Date of creation January 22, 2018
 */
public class LoginCmd {

    @Size(min = UserValidator.USER_INPUT_MIN_LEN_LOGIN, max = UserValidator.USER_INPUT_MAX_LEN_LOGIN)
    private String login;

    @Size(min = UserValidator.USER_INPUT_MIN_LEN_PASSWD, max = UserValidator.USER_INPUT_MAX_LEN_PASSWD)
    private String password;

    public LoginCmd() {}

    public LoginCmd(final String login,
                    final String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    @JsonbProperty("login")
    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    @JsonbProperty("password")
    public void setPassword(String password) {
        this.password = password;
    }
}
