/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.update;

import net.m4e.common.Strings;

import javax.inject.Inject;

/**
 * This class validates update check entity related inputs from a client.
 * 
 * @author boto
 * Date of creation Sep 8, 2017
 */
public class UpdateCheckEntityInputValidator {

    /* Min/max length for string input fields */
    private final int STRING_INPUT_MIN_LEN  = 1;
    private final int STRING_INPUT_MAX_LEN  = 255;

    private final UpdateChecks updateChecks;

    /**
     * Create an instance of input validator.
     * 
     * @param updateChecks    Update checks instance
     */
    @Inject
    public UpdateCheckEntityInputValidator(UpdateChecks updateChecks) {
        this.updateChecks = updateChecks;
    }

    /**
     * Given a JSON string as input containing data for creating a new update check entity, validate 
     * all fields and return an UpdateCheckEntity, or throw an exception if the validation failed.
     * 
     * @param entityJson       Data for creating a new update check entity in JSON format
     * @return               An UpdateCheckEntity created out of given input
     * @throws Exception     Throws an exception if the validation fails.
     */
    public UpdateCheckEntity validateNewEntityInput(String entityJson) throws Exception {
        UpdateCheckEntity reqentity = updateChecks.importUpdateCheckJSON(entityJson);
        if (reqentity == null) {
            throw new Exception("Failed to validate update check entry, invalid or incomplete input.");
        }

        return validateInput(reqentity);
    }

    /**
     * Given a JSON string as input containing data for updating an existing update check entry, validate 
     * all fields and return an UpdateCheckEntity, or throw an exception if the validation failed.
     * 
     * Note that some user data is not meant to be modified (such as login or e-mail), so those
     * fields are not validated here!
     * 
     * @param entityJson     Data for creating a new update check entry in JSON format
     * @return               A UpdateCheckEntity created out of given input
     * @throws Exception     Throws an exception if the validation fails.
     */
    public UpdateCheckEntity validateUpdateEntityInput(String entityJson) throws Exception {
        UpdateCheckEntity reqentity = updateChecks.importUpdateCheckJSON(entityJson);
        if (reqentity == null) {
            throw new Exception("Failed to validate update check entry, invalid input.");
        }
        if (reqentity.getId() == 0L) {
            throw new Exception("Invalid ID");
        }
        return validateInput(reqentity);
    }

    /**
     * Generate a text describing the string length range.
     * 
     * @param field    String field name
     * @param minLen   Minimal length
     * @param maxLen   Maximal length
     * @return         Range text
     */
    private String getLenRangeText(String field, int maxLen) {
        return field + " must not be empty and not exceed " + maxLen + " characters.";
    }

    /**
     * Validate the entity inputs.
     * 
     * @param entity        Entity to validate
     * @return              The passed entity.
     * @throws Exception    Throw an exception if the input fields are not valid.
     */
    private UpdateCheckEntity validateInput(UpdateCheckEntity entity) throws Exception {
        if (!Strings.checkMinMaxLength(entity.getName(), STRING_INPUT_MIN_LEN, STRING_INPUT_MAX_LEN)) {
            throw new Exception(getLenRangeText("Entry name", STRING_INPUT_MAX_LEN));
        }
        if (!Strings.checkMinMaxLength(entity.getOS(), STRING_INPUT_MIN_LEN, STRING_INPUT_MAX_LEN)) {
            throw new Exception(getLenRangeText("Entry's OS", STRING_INPUT_MAX_LEN));
        }
        if (!Strings.checkMinMaxLength(entity.getVersion(), STRING_INPUT_MIN_LEN, STRING_INPUT_MAX_LEN)) {
            throw new Exception(getLenRangeText("Entry's version", STRING_INPUT_MAX_LEN));
        }
        if (!Strings.checkMinMaxLength(entity.getUrl(), STRING_INPUT_MIN_LEN, STRING_INPUT_MAX_LEN)) {
            throw new Exception(getLenRangeText("Entry's URL", STRING_INPUT_MAX_LEN));
        }
        return entity;
    }
}
