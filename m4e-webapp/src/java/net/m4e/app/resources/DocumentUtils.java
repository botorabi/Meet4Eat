/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.resources;

import java.util.Objects;
import javax.json.Json;
import javax.json.JsonObjectBuilder;

/**
 * A collection of document related utilities
 * 
 * @author boto
 * Date of creation Sep 16, 2017
 */
public class DocumentUtils {

    /**
     * Create an instance of document utilities.
     */
    public DocumentUtils() {
    }

    /**
     * Give an document entity export the necessary fields into a JSON object.
     * 
     * @param document  Document entity to export
     * @return          A JSON object containing builder the proper entity fields
     */
    public JsonObjectBuilder exportDocumentJSON(DocumentEntity document) {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("id", Objects.nonNull(document.getId()) ? document.getId() : 0);
        json.add("name", Objects.nonNull(document.getName()) ? document.getName() : "");
        json.add("type", Objects.nonNull(document.getType()) ? document.getType() : "");
        json.add("content", Objects.nonNull(document.getContent()) ? new String(document.getContent()) : "");
        json.add("eTag", Objects.nonNull(document.getETag()) ? document.getETag() : "");
        json.add("encoding", Objects.nonNull(document.getEncoding()) ? document.getEncoding() : "");
        return json;
    }
}
