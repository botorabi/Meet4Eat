/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.update.business;

import net.m4e.common.Strings;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * This class validates update check entity related inputs from a client.
 * 
 * @author boto
 * Date of creation Sep 8, 2017
 */
public class UpdateCheckValidator {

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
    public UpdateCheckValidator(UpdateChecks updateChecks) {
        this.updateChecks = updateChecks;
    }

    /**
     * Given an update check entity, validate all fields and throw an exception if the validation failed.
     * 
     * @param inputEntity    Input data for creating a new update check entity
     * @throws Exception     Throws an exception if the validation fails.
     */
    public void validateNewEntityInput(@NotNull UpdateCheckEntity inputEntity) throws Exception {
        validateInput(inputEntity);
    }

    /**
     * Given data for updating an existing update check entry, validate all fields and throw an
     * exception if the validation failed.
     *
     * @param inputEntity    Input data for updating an existing check entry
     * @throws Exception     Throws an exception if the validation fails.
     */
    public void validateUpdateEntityInput(@NotNull UpdateCheckEntity inputEntity) throws Exception {
        if (inputEntity.getId() == null || inputEntity.getId() == 0L) {
            throw new Exception("Invalid ID");
        }
        validateInput(inputEntity);
    }

    /**
     * Generate a text describing the string length range.
     * 
     * @param field    String field name
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
        if (!Strings.checkMinMaxLength(entity.getOs(), STRING_INPUT_MIN_LEN, STRING_INPUT_MAX_LEN)) {
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
