/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.resources;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class entity for an document. A document can be e.g. an image or a PDF file.
 *
 * @author boto
 * Date of creation 30.08.2017
 */
@Entity
public class DocumentEntity implements Serializable {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
    //@ApiModelProperty(hidden = true)
    @JsonbTransient
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
     * Get the ID.
     *
     * @return The document ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Set the ID.
     *
     * @param id    The document ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get entity status. It contains information about entity's life-cycle,
     * ownership, etc.
     *
     * @return Entity status
     */
    public StatusEntity getStatus() {
        return status;
    }

    /**
     * Set entity status.
     *
     * @param status Entity status
     */
    public void setStatus(StatusEntity status) {
        this.status = status;
    }

    /**
     * Set document name.
     *
     * @return Document name
     */
    public String getName() {
        return name;
    }

    /**
     * Set document name.
     *
     * @param name Document name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the content type. It can be one of TYPE_xxx.
     *
     * @return Content type
     */
    public String getType() {
        return type;
    }

    /**
     * Set the content type.
     *
     * @param type Content type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Get the resource URL.
     *
     * @return Resource URL
     */
    public String getResourceURL() {
        return resourceURL;
    }

    /**
     * Set resource URL.
     *
     * @param resourceURL Resource URL
     */
    public void setResourceURL(String resourceURL) {
        this.resourceURL = resourceURL;
    }

    /**
     * Content encoding, e.g. ENCODING_BASE64 or ENCODING_BINARY
     *
     * @return Content encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Get content encoding.
     *
     * @param encoding Content encoding
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Update the document's content. The ETag will be updated, too.
     * 
     * @param content The new document content
     */
    public void updateContent(byte[] content) {
        setContent(content);
        updateETag();
    }

    /**
     * Get document content.
     *
     * @return The document content.
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * Set Document content.
     *
     * @param content Document content
     */
    public void setContent(byte[] content) {
        this.content = content;
    }

    /**
     * Get document etag. It can be used on client side for caching purpose.
     *
     * @return Document etag
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Set document etag.
     *
     * @param etag The document ETag
     */
    public void setDocumentETag(String etag) {
        this.eTag = etag;
    }

    /**
     * Update the hash (etag) string out of the document content. If the content is empty
     * then the hash will set to an empty string.
     * <p>
     * NOTE: Call this method whenever the content was changed.
     */
    public void updateETag() {
        if (content == null) {
            eTag = "";
            return;
        }

        try {
            String hash;
            MessageDigest diggest = MessageDigest.getInstance("SHA-256");
            diggest.update(content);
            byte data[] = diggest.digest();
            StringBuilder hexstring = new StringBuilder();
            for (int i = 0; i < data.length; i++) {
                String hex = Integer.toHexString(0xff & data[i]);
                if (hex.length() == 1) {
                    hexstring.append('0');
                }
                hexstring.append(hex);
            }
            hash = hexstring.toString();
            eTag = hash;
        } catch (NoSuchAlgorithmException ex) {
            LOGGER.error("Problem occurred while hashing an document content, reason: " + ex.getLocalizedMessage());
        }
    }

    /**
     * Check if the document is empty, i.e. it has no content.
     * 
     * @return Return true if the document is empty, otherwise false.
     */
    public boolean getIsEmpty() {
        return content == null;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof DocumentEntity)) {
            return false;
        }
        DocumentEntity other = (DocumentEntity) object;
        return (this.id != null || other.id == null) && (this.id == null || this.id.equals(other.id));
    }

    @Override
    public String toString() {
        return "net.m4e.common.DocumentEntity[ id=" + id + " ]";
    }

    /**
     * Export all fields into a JSON string
     *
     * @return A JSON string containing all entity fields with their respective values
     */
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
    private <T> T getOrDefault(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }
}
