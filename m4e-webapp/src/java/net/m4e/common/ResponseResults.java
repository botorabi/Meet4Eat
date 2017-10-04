/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.common;

import javax.json.Json;
import javax.json.JsonObjectBuilder;


/**
 * Class for creating request responses. Every response contains a status, a code, 
 * and a data field which is expected to be in JSON format or null if no data needed.
 * 
 * @author boto
 * Date of creation Aug 18, 2017
 */
public class ResponseResults {

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
    private String data;

    /**
     * Create a response instance.
     */
    public ResponseResults() {
    }

    /**
     * Build a response results object with given details.
     * 
     * @param status        Response status, STATUS_OK or not STATUS_NOK
     * @param description   Description
     * @param code          Detailed code, usually one of HTTP codes CODE_xxx
     * @param data          Optional data, if it exists the it must be in JSON format.
     * @return Response results object
     */
    public static ResponseResults build(String status, String description, int code, String data) {
        return new ResponseResults(status, description, code, data);
    }

    /**
     * Build a response results string in JSON formate with given details.
     * 
     * @param status        Response status, STATUS_OK or not STATUS_NOK
     * @param description   Description
     * @param code          Detailed code, usually one of HTTP codes CODE_xxx
     * @param data          Optional data, if it exists the it must be in JSON format.
     * @return Response results in JSON format
     */
    public static String buildJSON(String status, String description, int code, String data) {
        return new ResponseResults(status, description, code, data).getJSON();
    }

    /**
     * Create a response with given details.
     * 
     * @param status        Response status, STATUS_OK or not STATUS_NOK
     * @param description   Description
     * @param code          Detailed code, usually one of HTTP codes CODE_xxx
     * @param data          Optional data, if it exists the it must be in JSON format.
     */
    public ResponseResults(String status, String description, int code, String data) {
        this.status = status;
        this.description = description;
        this.code = code;
        this.data = data;
    }

    /**
     * Get the JSON formated string out of the response.
     * 
     * @return JSON string representing the response
     */
    public String getJSON() {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("status", ((status != null) ? status : ""));
        json.add("description", ((description != null) ? description : ""));
        json.add("code", code);
        json.add("data", ((data != null) ? data : ""));
        return json.build().toString();
    }

    /**
     * Get the status.
     * 
     * @return  Status string
     */
    public String getStatus() {
        return status;
    }

    /**
     * Set the status.
     * 
     * @param status Status string
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Get the description.
     * 
     * @return  Description string
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description.
     * 
     * @param description Description string
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the response code.
     * 
     * @return  Response code (integer)
     */
    public int getCode() {
        return code;
    }

    /**
     * Set the response code.
     * 
     * @param code Response code as integer.
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * Get the response data.
     * 
     * @return  Response data
     */
    public String getData() {
        return data;
    }

    /**
     * Set the response data.
     * 
     * @param data Response data.
     */
    public void setData(String data) {
        this.data = data;
    }
}
