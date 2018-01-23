/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.common;

/**
 * @author ybroeker
 */
public class GenericResponseResult<T> {

    /**
     * Status string for OK
     */
    public final static String STATUS_OK = "ok";

    /**
     * Status string for Not-OK
     */
    public final static String STATUS_NOT_OK = "nok";

    /**
     * Code for OK (HTTP code is used)
     */
    public final static int CODE_OK = 200;

    /**
     * Code for bad request
     */
    public final static int CODE_BAD_REQUEST = 400;

    /**
     * Code for not authorized
     */
    public final static int CODE_UNAUTHORIZED = 401;

    /**
     * Code for forbidden
     */
    public final static int CODE_FORBIDDEN = 403;

    /**
     * Code for not found
     */
    public final static int CODE_NOT_FOUND = 404;

    /**
     * Code for not acceptable
     */
    public final static int CODE_NOT_ACCEPTABLE = 406;

    /**
     * Code for not internal server error
     */
    public final static int CODE_INTERNAL_SRV_ERROR = 500;

    /**
     * Code for service unavailable
     */
    public final static int CODE_SERVICE_UNAVAILABLE = 503;

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

    /**
     * This method is used for allowing the call of result methods with or without a 'data' as second parameter.
     */
    private static final <T> T getResponseData(final T... data) {
        return data == null ? null : (data.length > 0 ? data[0] : null);
    }

    /**
     * 200.
     */
    public static <T> GenericResponseResult<T> ok(final String description, final T... data) {
        return new GenericResponseResult<>(STATUS_OK, description, CODE_OK, getResponseData(data));
    }

    /**
     * 400.
     */
    public static <T> GenericResponseResult<T> badRequest(final String desc, final T... data) {
        return new GenericResponseResult<>(STATUS_NOT_OK, desc, CODE_BAD_REQUEST, getResponseData(data));
    }

    /**
     * 401
     */
    public static <T> GenericResponseResult<T> unauthorized(final String desc, final T... data) {
        return new GenericResponseResult<>(STATUS_NOT_OK, desc, CODE_UNAUTHORIZED, getResponseData(data));
    }

    /**
     * 403
     */
    public static <T> GenericResponseResult<T> forbidden(final String desc, final T... data) {
        return new GenericResponseResult<>(STATUS_NOT_OK, desc, CODE_FORBIDDEN, getResponseData(data));
    }

    /**
     * 404
     */
    public static <T> GenericResponseResult<T> notFound(final String desc, final T... data) {
        return new GenericResponseResult<>(STATUS_NOT_OK, desc, CODE_NOT_FOUND, getResponseData(data));
    }

    /**
     * 406
     */
    public static <T> GenericResponseResult<T> notAcceptable(final String desc, final T... data) {
        return new GenericResponseResult<>(STATUS_NOT_OK, desc, CODE_NOT_ACCEPTABLE, getResponseData(data));
    }

    /**
     * 500.
     */
    public static <T> GenericResponseResult<T> internalError(final String desc, final T... data) {
        return new GenericResponseResult<>(STATUS_NOT_OK, desc, CODE_INTERNAL_SRV_ERROR, getResponseData(data));
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
