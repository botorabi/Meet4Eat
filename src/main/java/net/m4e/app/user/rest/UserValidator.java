/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.user.rest;

import net.m4e.app.auth.AuthorityConfig;
import net.m4e.app.user.business.UserEntity;
import net.m4e.app.user.business.Users;
import net.m4e.app.user.rest.comm.UserCmd;
import net.m4e.common.Strings;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;


/**
 * This class validates user entity related inputs from a client.
 * 
 * @author boto
 * Date of creation Sep 8, 2017
 */
@ApplicationScoped
public class UserValidator {

    /* Min/max string length for user input fields */
    public static final int USER_INPUT_MIN_LEN_LOGIN  = 6;
    public static final int USER_INPUT_MAX_LEN_LOGIN  = 32;
    public static final int USER_INPUT_MIN_LEN_NAME   = 6;
    public static final int USER_INPUT_MAX_LEN_NAME   = 32;
    public static final int USER_INPUT_MIN_LEN_PASSWD = 8;
    public static final int USER_INPUT_MAX_LEN_PASSWD = 255; // NOTE: This is the length of pw hash.
    public static final int USER_INPUT_MIN_LEN_EMAIL  = 3;
    public static final int USER_INPUT_MAX_LEN_EMAIL  = 128;

    private final Users users;


    /**
     * Default constructor needed by the container.
     */
    protected UserValidator() {
        users = null;
    }

    /**
     * Create an instance of input validator.
     * 
     * @param users The users instance
     */
    @Inject
    public UserValidator(Users users) {
        this.users = users;
    }

    /**
     * Validate all fields of given user data and return an UserEntity, or throw an exception if the validation failed.
     * 
     * @param userCmd     Data for creating a new user
     * @return               A UserEntity created out of given input
     * @throws Exception     Throws an exception if the validation fails.
     */
    public UserEntity validateNewEntityInput(UserEntity requestingUser, UserCmd userCmd) throws Exception {
        UserEntity newEntity = users.importUser(userCmd);
        if (newEntity == null) {
            throw new Exception("Failed to create user, invalid input.");
        }

        // perform user name, login and passwd checks
        if (!Strings.checkMinMaxLength(newEntity.getLogin(), USER_INPUT_MIN_LEN_LOGIN, USER_INPUT_MAX_LEN_LOGIN)) {
            throw new Exception(getLenRangeText("User's login name", USER_INPUT_MIN_LEN_LOGIN, USER_INPUT_MAX_LEN_LOGIN));
        }

        if (!Strings.checkMinMaxLength(newEntity.getName(), USER_INPUT_MIN_LEN_NAME, USER_INPUT_MAX_LEN_NAME)) {
            throw new Exception(getLenRangeText("User name", USER_INPUT_MIN_LEN_NAME, USER_INPUT_MAX_LEN_NAME));
        }

        if (!Strings.checkMinMaxLength(newEntity.getPassword(), USER_INPUT_MIN_LEN_PASSWD, USER_INPUT_MAX_LEN_PASSWD)) {
            throw new Exception(getLenRangeText("The password", USER_INPUT_MIN_LEN_PASSWD, USER_INPUT_MAX_LEN_PASSWD));
        }

        if (AuthorityConfig.getInstance().createPassword("").equals(newEntity.getPassword())) {
            throw new Exception("The password must not be empty.");            
        }

        if (!Strings.checkMinMaxLength(newEntity.getEmail(), USER_INPUT_MIN_LEN_EMAIL, USER_INPUT_MAX_LEN_EMAIL)) {
            throw new Exception(getLenRangeText("The E-Mail address", USER_INPUT_MIN_LEN_EMAIL, USER_INPUT_MAX_LEN_EMAIL));
        }

        // check if a user with given login or email address exists
        //! NOTE for this check also users marked as deleted are considered! the login name and email address must be
        //       unique in the database, it is.
        if (users.findUserByEmail(newEntity.getEmail()) != null) {
            throw new Exception("A user with given email already exists.");
        }
        if (users.findUser(newEntity.getLogin()) != null) {
            throw new Exception("Login name is not available.");
        }

        // validate the roles
        List<String> allowedRoles = Users.getAvailableUserRoles();
        List<String> reqEntityRoles = newEntity.getRolesAsString();
        for (int i = 0; i < reqEntityRoles.size(); i++) {
            if (!allowedRoles.contains(reqEntityRoles.get(i))) {
                throw new Exception("Failed to update user, unsupported role '" + reqEntityRoles.get(i) + "'detected.");
            }
        }

        // validate and adapt requested user roles
        newEntity.setRoles(users.adaptRequestedRoles(requestingUser, newEntity.getRoles()));

        return newEntity;
    }

    /**
     * Given a JSON string as input containing data for updating an existing user, validate 
     * all fields and return an UserEntity, or throw an exception if the validation failed.
     * 
     * Note that some user data is not meant to be modified (such as login or e-mail), so those
     * fields are not validated here!
     * 
     * @param userCmd        Data for updating an existing user
     * @return               A UserEntity created out of given input
     * @throws Exception     Throws an exception if the validation fails.
     */
    public UserEntity validateUpdateEntityInput(UserCmd userCmd) throws Exception {
        UserEntity userEntity = users.importUser(userCmd);
        if (userEntity == null) {
            throw new Exception("Failed to update user, invalid input.");
        }

        // NOTE: for updating an entity, some fields may not exist, those fields do not get changed.
        if ((userEntity.getName() != null) && !Strings.checkMinMaxLength(userEntity.getName(), USER_INPUT_MIN_LEN_NAME, USER_INPUT_MAX_LEN_NAME)) {
            throw new Exception(getLenRangeText("User name", USER_INPUT_MIN_LEN_NAME, USER_INPUT_MAX_LEN_NAME));
        }

        if ((userEntity.getPassword() != null) && !Strings.checkMinMaxLength(userEntity.getPassword(), USER_INPUT_MIN_LEN_PASSWD, USER_INPUT_MAX_LEN_PASSWD)) {
            throw new Exception(getLenRangeText("The password", USER_INPUT_MIN_LEN_PASSWD, USER_INPUT_MAX_LEN_PASSWD));
        }

        // validate the roles
        List<String> allowedRoles = Users.getAvailableUserRoles();
        List<String> reqEntityRoles = userEntity.getRolesAsString();
        for (int i = 0; i < reqEntityRoles.size(); i++) {
            if (!allowedRoles.contains(reqEntityRoles.get(i))) {
                throw new Exception("Failed to update user, unsupported role '" + reqEntityRoles.get(i) + "'detected.");
            }
        }
        return userEntity;
    }

    /**
     * Generate a text describing the string length range.
     * 
     * @param field    String field name
     * @param minLen   Minimal length
     * @param maxLen   Maximal length
     * @return         Range text
     */
    private String getLenRangeText(String field, int minLen, int maxLen) {
        return field + " must be at least " + minLen + " and not exceed " + maxLen + " characters.";
    }
}
