/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.common;

import static net.m4e.common.ResponseResults.*;

/**
 * @author ybroeker
 */
public class GenericResponseResult<T> {

    /**
     * Status: 'ok' or 'nok'
     */
    private String status = STATUS_NOT_OK;

    /**
     * Response description
     */
    private String description = "";

    /**
     * Response code
     */
    private int code = CODE_OK;

    /**
     * Response data, expected to be in JSON format if it exists.
     */
    private T data;


    public GenericResponseResult(final String status, final String description, final int code, final T data) {
        this.status = status;
        this.description = description;
        this.code = code;
        this.data = data;
    }

    public GenericResponseResult() {
    }


    public static <T> GenericResponseResult<T> ok(final String description, final T data) {
        return new GenericResponseResult<>(STATUS_OK, description, CODE_OK, data);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public void setCode(final int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(final T data) {
        this.data = data;
    }
}
