/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.user.business;

import net.m4e.app.resources.DocumentEntity;
import net.m4e.common.EntityBase;

import javax.persistence.*;
import java.io.Serializable;

/**
 * UserProfileEntity holds user's profile information such as bio, birthday etc.
 * 
 * @author boto
 * Date of creation 30.08.2017
 */
@Entity
public class UserProfileEntity extends EntityBase implements Serializable {

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
    @OneToOne(cascade = {CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH})
    private DocumentEntity photo;

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
     * Get birthday.
     */
    public Long getBirthday() {
        return birthday;
    }

    /**
     * Set birthday.
     */
    public void setBirthday(Long birthday) {
        this.birthday = birthday;
    }

    /**
     * Get biography.
     */
    public String getBio() {
        return bio;
    }

    /**
     * Set biography.
     */
    public void setBio(String bio) {
        this.bio = bio;
    }

    /**
     * Get photo.
     */
    public DocumentEntity getPhoto() {
        return photo;
    }

    /**
     * Set the profile photo.
     */
    public void setPhoto(DocumentEntity photo) {
        this.photo = photo;
    }
}
