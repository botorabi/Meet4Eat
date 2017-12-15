/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.common;

import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;


/**
 * Test class for ResponseResults
 * 
 * @author boto
 * @since Dec 15, 2017
 */
public class ResponseResultsTest {

    /**
     * Test of build method, of class ResponseResults.
     */
    @Test
    public void testBuild() {
        System.out.println("build");

        String status      = "42";
        String description = "43";
        int    code        = 44;
        String data        = "45";

        ResponseResults result = ResponseResults.build(status, description, code, data);

        assertThat(result.getStatus()).isEqualTo(status);
        assertThat(result.getDescription()).isEqualTo(description);
        assertThat(result.getCode()).isEqualTo(code);
        assertThat(result.getData()).isEqualTo(data);
    }

    /**
     * Test of toJSON method, of class ResponseResults.
     */
    @Test
    public void testToJSON_4args() {
        System.out.println("toJSON");

        String status      = "42";
        String description = "43";
        int    code        = 44;
        String data        = "45";

        String result = ResponseResults.toJSON(status, description, code, data);
        checkToJson(result, status, description, code, data);
    }

    /**
     * Test of toJSON method, of class ResponseResults.
     */
    @Test
    public void testToJSON_0args() {
        System.out.println("toJSON");

        ResponseResults instance = new ResponseResults();
        String result = instance.toJSON();
        checkToJson(result, ResponseResults.STATUS_NOT_OK, "", ResponseResults.CODE_OK, "");
    }

    /**
     * Test of getStatus method, of class ResponseResults.
     */
    @Test
    public void testGetStatus() {
        System.out.println("getStatus");

        ResponseResults instance = new ResponseResults();
        instance.setStatus("GET_STATUS");
        String result = instance.toJSON();
        checkToJson(result, "GET_STATUS", "", ResponseResults.CODE_OK, "");
    }

    /**
     * Test of setStatus method, of class ResponseResults.
     */
    @Test
    public void testSetStatus() {
        ResponseResults instance = new ResponseResults();
        instance.setStatus("SET_STATUS");
        String result = instance.toJSON();
        checkToJson(result, "SET_STATUS", "", ResponseResults.CODE_OK, "");
    }

    /**
     * Test of getDescription method, of class ResponseResults.
     */
    @Test
    public void testGetDescription() {
        ResponseResults instance = new ResponseResults();
        instance.setDescription("GET_DESC");
        String result = instance.toJSON();
        checkToJson(result, ResponseResults.STATUS_NOT_OK, "GET_DESC", ResponseResults.CODE_OK, "");
    }

    /**
     * Test of setDescription method, of class ResponseResults.
     */
    @Test
    public void testSetDescription() {
        ResponseResults instance = new ResponseResults();
        instance.setDescription("SET_DESC");
        String result = instance.toJSON();
        checkToJson(result, ResponseResults.STATUS_NOT_OK, "SET_DESC", ResponseResults.CODE_OK, "");
    }

    /**
     * Test of getCode method, of class ResponseResults.
     */
    @Test
    public void testGetCode() {
        ResponseResults instance = new ResponseResults();
        instance.setCode(142);
        String result = instance.toJSON();
        checkToJson(result, ResponseResults.STATUS_NOT_OK, "", 142, "");
    }

    /**
     * Test of setCode method, of class ResponseResults.
     */
    @Test
    public void testSetCode() {
        ResponseResults instance = new ResponseResults();
        instance.setCode(242);
        String result = instance.toJSON();
        checkToJson(result, ResponseResults.STATUS_NOT_OK, "", 242, "");
    }

    /**
     * Test of getData method, of class ResponseResults.
     */
    @Test
    public void testGetData() {
        ResponseResults instance = new ResponseResults();
        instance.setData("GET_DATA");
        String result = instance.toJSON();
        checkToJson(result, ResponseResults.STATUS_NOT_OK, "", ResponseResults.CODE_OK, "GET_DATA");
    }

    /**
     * Test of setData method, of class ResponseResults.
     */
    @Test
    public void testSetData() {
        ResponseResults instance = new ResponseResults();
        instance.setData("SET_DATA");
        String result = instance.toJSON();
        checkToJson(result, ResponseResults.STATUS_NOT_OK, "", ResponseResults.CODE_OK, "SET_DATA");
    }    

    /**
     * Check the JSON conversion
     */
    private void checkToJson(String jsonString, String status, String description, int code, String data) {
        JsonReader jreader = Json.createReader(new StringReader(jsonString));
        try {
            JsonObject jobject = jreader.readObject();
            assertThat(jobject.getString("status", "")).isEqualTo(status);
            assertThat(jobject.getString("description", "")).isEqualTo(description);
            assertThat(jobject.getString("data", "")).isEqualTo(data);
            assertThat(jobject.getInt("code", 0)).isEqualTo(code);
        }
        catch(Exception ex) {
            fail("Invalid json format: " + ex.getLocalizedMessage());
        }
    }
}
