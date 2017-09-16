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
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import net.m4e.system.core.Log;

/**
 * Class entity for an image. An image entity contains image information and data.
 * 
 * @author boto
 * Date of creation 30.08.2017
 */
@Entity
@XmlRootElement
public class ImageEntity implements Serializable {

    /**
     * Used for logging
     */
    private final static String TAG = "ImageEntity";

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
    @OneToOne(optional=false, cascade = CascadeType.ALL)
    private StatusEntity status;       

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
     * Hash code of the image content.
     */
    private String imageHash = "";

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

    /**
     * Get image content hash code.
     * 
     * @return Image content hash code
     */
    public String getImageHash() {
        return imageHash;
    }

    /**
     * Set image content hash code.
     * @param imageHash Hash code
     */
    public void setImageHash(String imageHash) {
        this.imageHash = imageHash;
    }

    /**
     * Update the hash string out of the image content. If the content is empty 
     * then the hash will set to an empty string.
     * 
     * NOTE: Call this method whenever the content was changed.
     */
    @Transient
    public void updateImageHash() {
        if (Objects.isNull(content)) {
            imageHash = "";
            return;
        }

        try {
            String hash;
            MessageDigest diggest = MessageDigest.getInstance("SHA-256");
            diggest.update(content);
            byte data[] = diggest.digest();
            StringBuilder hexstring = new StringBuilder();
            for (int i=0; i < data.length; i++) {
                String hex = Integer.toHexString(0xff & data[i]);
                if (hex.length() == 1) {
                    hexstring.append('0');
                }
                hexstring.append(hex);
            }
            hash = hexstring.toString();
            imageHash = hash;
        }
        catch (NoSuchAlgorithmException ex) {
            Log.error(TAG, "Problem occurred while hashing an image content, reason: " + ex.getLocalizedMessage());
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
