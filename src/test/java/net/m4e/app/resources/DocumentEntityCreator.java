/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.resources;

/**
 * @author boto
 * Date of creation March 12, 2018
 */
public class DocumentEntityCreator {

    public static final Long DOC_ID = 1000L;
    public static final String DOC_NAME = "Document";
    public static final String DOC_TYPE = DocumentEntity.TYPE_IMAGE;
    public static final String DOC_RES_URL = "/image";
    public static final String DOC_ENCODING = DocumentEntity.ENCODING_BASE64;
    public static final byte[] DOC_CONTENT = "The content....".getBytes();
    public static final String DOC_ETAG = "DOC ETAG...";

    public static DocumentEntity create() {
        DocumentEntity entity = new DocumentEntity();

        entity.setId(DOC_ID);
        entity.setName(DOC_NAME);
        entity.setType(DOC_TYPE);
        entity.setEncoding(DOC_ENCODING);
        entity.setResourceURL(DOC_RES_URL);
        entity.setContent(DOC_CONTENT);
        entity.setETag(DOC_ETAG);

        StatusEntity status = new StatusEntity();
        entity.setStatus(status);

        return entity;
    }
}
