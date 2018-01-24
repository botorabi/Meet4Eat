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
    private String name;

    @Size(min = UserValidator.USER_INPUT_MIN_LEN_EMAIL, max = UserValidator.USER_INPUT_MAX_LEN_EMAIL)
    private String email;

    private String photo;

    private List<String> roles;

    public UserCmd() {}

    public UserCmd(final String login,
                   final String password,
                   final String name,
                   final String email,
                   final String photo,
                   final List<String> roles) {
        this.login = login;
        this.password = password;
        this.name = name;
        this.email = email;
        this.photo = photo;
        this.roles = roles;
    }

    @JsonbProperty("login")
    public void setLogin(String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    @JsonbProperty("password")
    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    @JsonbProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @JsonbProperty("email")
    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    @JsonbProperty("photo")
    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPhoto() {
        return photo;
    }

    @JsonbProperty("roles")
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getRoles() {
        return roles;
    }

}
