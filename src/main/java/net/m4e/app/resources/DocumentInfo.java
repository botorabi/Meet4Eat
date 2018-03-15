/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.resources;

import org.jetbrains.annotations.NotNull;

import javax.json.bind.annotation.*;

/**
 * @author boto
 * Date of creation February 20, 2018
 */
public class DocumentInfo {
    private String id;
    private String name;
    private String type;
    private String resourceURL;
    private String content;
    private String eTag;
    private String encoding;

    public DocumentInfo() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getResourceURL() {
        return resourceURL;
    }

    public String getContent() {
        return content;
    }

    @JsonbProperty("eTag")
    public String getETag() {
        return eTag;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setResourceURL(String url) {
        this.resourceURL = url;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @JsonbProperty("eTag")
    public void setETag(String eTag) {
        this.eTag = eTag;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @JsonbTransient
    public static DocumentInfo fromDocumentEntity(@NotNull DocumentEntity documentEntity) {
        DocumentInfo documentInfo = new DocumentInfo();

        documentInfo.setId("" + documentEntity.getId());
        documentInfo.setName(documentEntity.getName());
        documentInfo.setType(documentEntity.getType());
        documentInfo.setResourceURL(documentEntity.getResourceURL());
        documentInfo.setContent(new String(documentEntity.getContent()));
        documentInfo.setETag(documentEntity.getETag());
        documentInfo.setEncoding(documentEntity.getEncoding());

        return documentInfo;
    }
}
