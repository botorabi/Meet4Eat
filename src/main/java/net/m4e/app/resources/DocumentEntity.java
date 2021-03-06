/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.resources;

import net.m4e.common.*;

import javax.json.*;
import javax.persistence.*;
import java.io.Serializable;

/**
 * Class entity for an document. A document can be e.g. an image or a PDF file.
 *
 * @author boto
 * Date of creation 30.08.2017
 */
@Entity
public class DocumentEntity extends EntityBase implements Serializable {

    /**
     * Content encoding type Base64
     */
    public static final String ENCODING_BASE64 = "base64";

    /**
     * Content encoding type binary
     */
    public static final String ENCODING_BINARY = "binary";

    /**
     * Content type unknown
     */
    public static final String TYPE_UNKNOWN = "unknown";

    /**
     * Content type image
     */
    public static final String TYPE_IMAGE = "image";

    /**
     * Content type PDF
     */
    public static final String TYPE_PDF = "pdf";

    /**
     * Serialization version
     */
    private static final long serialVersionUID = 1L;

    /**
     * Unique entity ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Entity status
     */
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private StatusEntity status;

    /**
     * Document name
     */
    private String name;

    /**
     * Document type
     */
    private String type = TYPE_UNKNOWN;

    /**
     * Document's resource URL
     */
    private String resourceURL = "";

    /**
     * Content encoding, e.g. base64
     */
    private String encoding = ENCODING_BASE64;

    /**
     * The encoded content
     */
    private byte[] content;

    /**
     * ETag of the document content.
     */
    private String eTag = "";

    /**
     * Get the entity ID.
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * Set the entity ID.
     */
    @Override
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get entity status. It contains information about entity's life-cycle,
     * ownership, etc.
     */
    public StatusEntity getStatus() {
        return status;
    }

    /**
     * Set entity status.
     */
    public void setStatus(StatusEntity status) {
        this.status = status;
    }

    /**
     * Set document name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set document name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the content type. It can be one of TYPE_xxx.
     */
    public String getType() {
        return type;
    }

    /**
     * Set the content type.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Get the resource URL.
     */
    public String getResourceURL() {
        return resourceURL;
    }

    /**
     * Set resource URL.
     */
    public void setResourceURL(String resourceURL) {
        this.resourceURL = resourceURL;
    }

    /**
     * Content encoding, e.g. ENCODING_BASE64 or ENCODING_BINARY
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Get content encoding.
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Update the document's content. The ETag will be updated, too.
     */
    public void updateContent(byte[] content) {
        setContent(content);
        updateETag();
    }

    /**
     * Get document content.
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * Set Document content.
     */
    public void setContent(byte[] content) {
        this.content = content;
    }

    /**
     * Get document etag. It can be used on client side for caching purpose.
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Set document's ETag.
     */
    public void setETag(String eTag) {
        this.eTag = eTag;
    }

    /**
     * Update the hash (ETag) string out of the document content. If the content is empty
     * then the hash will set to an empty string.
     *
     * NOTE: Call this method whenever the content was changed.
     */
    public void updateETag() {
        eTag = "";
        if (content == null) {
            return;
        }
        try {
            eTag = HashCreator.createSHA256(content);
        } catch (Exception e) {}
    }

    /**
     * Check if the document is empty, i.e. it has no content.
     */
    public boolean getIsEmpty() {
        return content == null;
    }
}
