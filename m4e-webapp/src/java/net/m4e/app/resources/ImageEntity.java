/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.resources;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Class entity for an image. An image entity contains image information and data.
 * 
 * @author boto
 * Date of creation 30.08.2017
 */
@Entity
public class ImageEntity implements Serializable {

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
     * Content encoding type Base64
     */
    public final static String ENCODING_BASE64 = "base64";

    /**
     * Content encoding type binary
     */
    public final static String ENCODING_BINARY = "binary";

    /**
     * Image name
     */
    private String name;

    /**
     * Image's resource URL
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
     * Get ID.
     * @return ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Set ID.
     * @param id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Set image name.
     * @return Image name
     */
    public String getName() {
        return name;
    }

    /**
     * Set image name.
     * 
     * @param name Image name
     */
    public void setName(String name) {
        this.name = name;
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
     * Get image content.
     * 
     * @return The image content.
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * Set Image content.
     * 
     * @param content Image content
     */
    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ImageEntity)) {
            return false;
        }
        ImageEntity other = (ImageEntity) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "net.m4e.common.ImageEntity[ id=" + id + " ]";
    }
}
