/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.resources;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import net.m4e.system.core.Log;

/**
 * Class entity for an document. A document can be e.g. an image or a PDF file.
 *
 * @author boto
 * Date of creation 30.08.2017
 */
@Entity
public class DocumentEntity implements Serializable {

    /**
     * Used for logging
     */
    private final static String TAG = "DocumentEntity";

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
    private String resourceURL;

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
     * Export all fields into a JSON string
     *
     * @return A JSON string containing all entity fields with their respective values
     */
    public String toJsonString() {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("id", getOrDefault(id, 0L));
        json.add("name", getOrDefault(name, ""));
        json.add("type", getOrDefault(type, ""));
        json.add("content", content == null ? "" : new String(content));
        json.add("eTag", getOrDefault(eTag, ""));
        json.add("encoding", getOrDefault(encoding, ""));
        return json.build().toString();
    }

    private <T> T getOrDefault(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    /**
     * Get ID.
     *
     * @return ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Set ID.
     *
     * @param id
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
     * Get the resouce URL.
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
        if (Objects.isNull(content)) {
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
            Log.error(TAG, "Problem occurred while hashing an document content, reason: " + ex.getLocalizedMessage());
        }
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
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "net.m4e.common.DocumentEntity[ id=" + id + " ]";
    }
}
