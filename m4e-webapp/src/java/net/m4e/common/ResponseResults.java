/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.common;

import java.io.Serializable;
import java.util.Objects;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * Class for creating request responses.
 * 
 * @author boto
 * Date of creation Aug 18, 2017
 */
@XmlRootElement
public class ResponseResults implements Serializable {

    /**
     * Status string for OK
     */
    public final static String STATUS_OK     = "ok";

    /**
     * Status string for Not-OK
     */
    public final static String STATUS_NOT_OK = "nok";

    /**
     * Serialization version
     */
    private static final long serialVersionUID = 1L;

    /**
     * Status: 'ok' or 'nok'
     */
    private String status = "";

    /**
     * Response description
     */
    private String description = "";

    /**
     * Response code
     */
    private int code = 200;

    /**
     * Response data.
     */
    private String data = "";

    /**
     * Build a response results object with given details.
     * 
     * @param status
     * @param description
     * @param code
     * @param data
     * @return Response results object
     */
    public static ResponseResults build(String status, String description, int code, String data) {
        return new ResponseResults(status, description, code, data);
    }

    /**
     * Build a response results string in JSON formate with given details.
     * 
     * @param status
     * @param description
     * @param code
     * @param data
     * @return Response results in JSON format
     */
    public static String buildJSON(String status, String description, int code, String data) {
        return new ResponseResults(status, description, code, data).getJSON();
    }

    /**
     * Default constructor
     */
    public ResponseResults() {
    }

    /**
     * Create a response with given details.
     * 
     * @param status
     * @param description
     * @param code
     * @param data
     */
    public ResponseResults(String status, String description, int code, String data) {
        this.status = status;
        this.description = description;
        this.code = code;
        this.data = data;
    }

    @Override
    public int hashCode() {
        return status.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ResponseResults other = (ResponseResults) obj;
        if (this.code != other.code) {
            return false;
        }
        if (!Objects.equals(this.status, other.status)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (!Objects.equals(this.data, other.data)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "net.m4e.common.ResponseResults[ status=" + status + ", code=" + code + ", description=" + description + " ]";
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
