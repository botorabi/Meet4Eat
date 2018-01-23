/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.rest.comm;

import net.m4e.app.user.business.UserEntity;
import net.m4e.app.user.rest.UserValidator;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * @author boto
 * Date of creation January 23, 2018
 */
public class UserCmd {

    @Size(min = UserValidator.USER_INPUT_MIN_LEN_LOGIN, max = UserValidator.USER_INPUT_MAX_LEN_LOGIN)
    private String login;

    @Size(min = UserValidator.USER_INPUT_MIN_LEN_PASSWD, max = UserValidator.USER_INPUT_MAX_LEN_PASSWD)
    private String password;

    @Size(min = UserValidator.USER_INPUT_MIN_LEN_NAME, max = UserValidator.USER_INPUT_MAX_LEN_NAME)
    private
    String name;

    @Size(min = UserValidator.USER_INPUT_MIN_LEN_EMAIL, max = UserValidator.USER_INPUT_MAX_LEN_EMAIL)
    private
    String email;

    private String photo;

    private List<String> roles;

    @JsonbCreator
    public UserCmd(@JsonbProperty("login") final String login,
                   @JsonbProperty("password") final String password,
                   @JsonbProperty("name") final String name,
                   @JsonbProperty("email") final String email,
                   @JsonbProperty("photo") final String photo,
                   @JsonbProperty("roles") final List<String> roles) {
        this.login = login;
        this.password = password;
        this.name = name;
        this.email = email;
        this.photo = photo;
        this.roles = roles;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoto() {
        return photo;
    }

    public List<String> getRoles() {
        return roles;
    }
}
