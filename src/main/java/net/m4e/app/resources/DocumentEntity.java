/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.resources;

import net.m4e.common.*;

import javax.json.*;
import javax.json.bind.annotation.JsonbTransient;
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
    public final static String ENCODING_BASE64 = "base64";

    /**
     * Content encoding type binary
     */
    public final static String ENCODING_BINARY = "binary";

    /**
     * Content type unknown
     */
    public final static String TYPE_UNKNOWN = "unknown";

    /**
     * Content type image
     */
    public final static String TYPE_IMAGE = "image";

    /**
     * Content type PDF
     */
    public final static String TYPE_PDF = "pdf";

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
     * Check if the object is an instance of this entity.
     */
    @Override
    @JsonbTransient
    public boolean isInstanceOfMe(Object object) {
        return object instanceof DocumentEntity;
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
     * Set document's etag.
     */
    public void setDocumentETag(String etag) {
        this.eTag = etag;
    }

    /**
     * Update the hash (etag) string out of the document content. If the content is empty
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
        } catch (Exception ex) {}
    }

    /**
     * Check if the document is empty, i.e. it has no content.
     */
    public boolean getIsEmpty() {
        return content == null;
    }

    /**
     * Export all fields into a JSON string
     */
    //! TODO move this method out  of the entity class
    public String toJsonString() {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("id", getOrDefault(id.toString(), ""))
            .add("name", getOrDefault(name, ""))
            .add("type", getOrDefault(type, ""))
            .add("content", (content == null) ? "" : new String(content))
            .add("eTag", getOrDefault(eTag, ""))
            .add("encoding", getOrDefault(encoding, ""));

            return json.build().toString();
    }

    /**
     * Get the given value, if it does not exit (i.e. null) then return a given default.
     */
    //! TODO move this method out  of the entity class
    private <T> T getOrDefault(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }
}
