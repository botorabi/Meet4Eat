/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
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
public class ResponseResults extends GenericResponseResult<String> {

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
    public static String toJSON(String status, String description, int code, String data) {
        return new ResponseResults(status, description, code, data).toJSON();
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
        this.setStatus(status);
        this.setDescription(description);
        this.setCode(code);
        this.setData(data);
    }

    /**
     * Get the JSON formated string out of the response.
     * 
     * @return JSON string representing the response
     */
    public String toJSON() {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("status", ((getStatus() != null) ? getStatus() : ""))
                .add("description", ((getDescription() != null) ? getDescription() : ""))
                .add("code", getCode())
                .add("data", ((getData() != null) ? getData() : ""));
        return json.build().toString();
    }
}
