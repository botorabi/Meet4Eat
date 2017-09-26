/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.user;

import java.util.List;
import java.util.Objects;
import javax.persistence.EntityManager;
import net.m4e.common.StringUtils;

/**
 * This class validates user entity related inputs from a client.
 * 
 * @author boto
 * Date of creation Sep 8, 2017
 */
public class UserEntityInputValidator {

    /* Min/max string length for user input fields */
    private final int USER_INPUT_MIN_LEN_LOGIN  = 8;
    private final int USER_INPUT_MAX_LEN_LOGIN  = 32;
    private final int USER_INPUT_MIN_LEN_NAME   = 6;
    private final int USER_INPUT_MAX_LEN_NAME   = 32;
    private final int USER_INPUT_MIN_LEN_PASSWD = 8;
    private final int USER_INPUT_MAX_LEN_PASSWD = 255; // NOTE: This is the length of pw hash.
    private final int USER_INPUT_MIN_LEN_EMAIL  = 3;
    private final int USER_INPUT_MAX_LEN_EMAIL  = 128;

    private final EntityManager entityManager;

    /**
     * Create an instance of input validator.
     * 
     * @param entityManager    Entity manager
     */
    public UserEntityInputValidator(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Given a JSON string as input containing data for creating a new user, validate 
     * all fields and return an UserEntity, or throw an exception if the validation failed.
     * 
     * @param userJson       Data for creating a new user in JSON format
     * @return               A UserEntity created out of given input
     * @throws Exception     Throws an exception if the validation fails.
     */
    public UserEntity validateNewEntityInput(String userJson) throws Exception {
        UserUtils userutils = new UserUtils(entityManager);
        UserEntity reqentity = userutils.importUserJSON(userJson);
        if (reqentity == null) {
            throw new Exception("Failed to create user, invalid input.");
        }

        // perform user name, login and passwd checks
        if (!StringUtils.checkMinMaxLength(reqentity.getLogin(), USER_INPUT_MIN_LEN_LOGIN, USER_INPUT_MAX_LEN_LOGIN)) {
            throw new Exception(getLenRangeText("User's login name", USER_INPUT_MIN_LEN_LOGIN, USER_INPUT_MAX_LEN_LOGIN));
        }

        if (!StringUtils.checkMinMaxLength(reqentity.getName(), USER_INPUT_MIN_LEN_NAME, USER_INPUT_MAX_LEN_NAME)) {
            throw new Exception(getLenRangeText("User name", USER_INPUT_MIN_LEN_NAME, USER_INPUT_MAX_LEN_NAME));
        }

        if (!StringUtils.checkMinMaxLength(reqentity.getPassword(), USER_INPUT_MIN_LEN_PASSWD, USER_INPUT_MAX_LEN_PASSWD)) {
            throw new Exception(getLenRangeText("The password", USER_INPUT_MIN_LEN_PASSWD, USER_INPUT_MAX_LEN_PASSWD));
        }

        if (!StringUtils.checkMinMaxLength(reqentity.getEmail(), USER_INPUT_MIN_LEN_EMAIL, USER_INPUT_MAX_LEN_EMAIL)) {
            throw new Exception(getLenRangeText("The E-Mail address", USER_INPUT_MIN_LEN_EMAIL, USER_INPUT_MAX_LEN_EMAIL));
        }

        if (Objects.nonNull(userutils.findUser(reqentity.getLogin()))) {
            throw new Exception("Login name is not available.");            
        }

        // validate the roles
        List<String> allowedroles = UserUtils.getAvailableUserRoles();
        List<String> reqentityroles = reqentity.getRolesAsString();
        for (int i = 0; i < reqentityroles.size(); i++) {
            if (!allowedroles.contains(reqentityroles.get(i))) {
                throw new Exception("Failed to update user, unsupported role '" + reqentityroles.get(i) + "'detected.");
            }
        }
        return reqentity;
    }

    /**
     * Given a JSON string as input containing data for updating an existing user, validate 
     * all fields and return an UserEntity, or throw an exception if the validation failed.
     * 
     * @param userJson       Data for creating a new user in JSON format
     * @return               A UserEntity created out of given input
     * @throws Exception     Throws an exception if the validation fails.
     */
    public UserEntity validateUpdateEntityInput(String userJson) throws Exception {
        UserUtils  userutils = new UserUtils(entityManager);
        UserEntity reqentity = userutils.importUserJSON(userJson);
        if (reqentity == null) {
            throw new Exception("Failed to update user, invalid input.");
        }

        // NOTE: for updating an entity, the some fields may not exist. those fields do not get changed, it is.
        if (Objects.nonNull(reqentity.getLogin()) && !StringUtils.checkMinMaxLength(reqentity.getLogin(), USER_INPUT_MIN_LEN_LOGIN, USER_INPUT_MAX_LEN_LOGIN)) {
            throw new Exception(getLenRangeText("User's login name", USER_INPUT_MIN_LEN_LOGIN, USER_INPUT_MAX_LEN_LOGIN));
        }

        if (Objects.nonNull(reqentity.getName()) && !StringUtils.checkMinMaxLength(reqentity.getName(), USER_INPUT_MIN_LEN_NAME, USER_INPUT_MAX_LEN_NAME)) {
            throw new Exception(getLenRangeText("User name", USER_INPUT_MIN_LEN_NAME, USER_INPUT_MAX_LEN_NAME));
        }

        if (Objects.nonNull(reqentity.getPassword()) && !StringUtils.checkMinMaxLength(reqentity.getPassword(), USER_INPUT_MIN_LEN_PASSWD, USER_INPUT_MAX_LEN_PASSWD)) {
            throw new Exception(getLenRangeText("The password", USER_INPUT_MIN_LEN_PASSWD, USER_INPUT_MAX_LEN_PASSWD));
        }

        if (Objects.nonNull(reqentity.getEmail()) && !StringUtils.checkMinMaxLength(reqentity.getEmail(), USER_INPUT_MIN_LEN_EMAIL, USER_INPUT_MAX_LEN_EMAIL)) {
            throw new Exception(getLenRangeText("The E-Mail address", USER_INPUT_MIN_LEN_EMAIL, USER_INPUT_MAX_LEN_EMAIL));
        }

        // validate the roles
        List<String> allowedroles = UserUtils.getAvailableUserRoles();
        List<String> reqentityroles = reqentity.getRolesAsString();
        for (int i = 0; i < reqentityroles.size(); i++) {
            if (!allowedroles.contains(reqentityroles.get(i))) {
                throw new Exception("Failed to update user, unsupported role '" + reqentityroles.get(i) + "'detected.");
            }
        }
        return reqentity;
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
