/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.user.business;

import net.m4e.app.resources.DocumentEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * UserProfileEntity holds user's profile information such as bio, birthday etc.
 * 
 * @author boto
 * Date of creation 30.08.2017
 */
@Entity
public class UserProfileEntity implements Serializable {

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
     * Birthday
     */
    private Long birthday;

    /**
     * Biography
     */
    private String bio;

    /**
     * Photo, a photo may be a sharable icon
     */
    @OneToOne(optional=true, cascade = {CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH})
    private DocumentEntity photo;

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
     * Get birthday.
     * 
     * @return Birthday in millisecond since epoch
     */
    public Long getBirthday() {
        return birthday;
    }

    /**
     * Set birthday.
     * 
     * @param birthday Birthday
     */
    public void setBirthday(Long birthday) {
        this.birthday = birthday;
    }

    /**
     * Get biography.
     * 
     * @return Biography
     */
    public String getBio() {
        return bio;
    }

    /**
     * Set biography.
     * 
     * @param bio Biography
     */
    public void setBio(String bio) {
        this.bio = bio;
    }

    /**
     * Get photo.
     * 
     * @return DocumentEntity containing the photo
     */
    public DocumentEntity getPhoto() {
        return photo;
    }

    /**
     * Set the profile photo.
     * 
     * @param photo DocumentEntity containing the photo
     */
    public void setPhoto(DocumentEntity photo) {
        this.photo = photo;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof UserProfileEntity)) {
            return false;
        }
        UserProfileEntity other = (UserProfileEntity) object;
        return (this.id != null || other.id == null) && (this.id == null || this.id.equals(other.id));
    }

    @Override
    public String toString() {
        return "net.m4e.user.UserProfileEntity[ id=" + id + " ]";
    }

}
