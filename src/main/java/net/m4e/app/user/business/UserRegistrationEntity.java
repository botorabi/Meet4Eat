/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.user.business;

import net.m4e.common.EntityBase;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * This entity is used for user registration. It provides an activation token.
 * 
 * @author boto
 * Date of creation Oct 1, 2017
 */
@Entity
public class UserRegistrationEntity extends EntityBase implements Serializable {

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
     * User this activation is used for
     */
    @OneToOne(cascade = {CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH})
    private UserEntity user;

    /**
     * User activation token
     */
    @Column(nullable=false)
    private String activationToken;

    /**
     * Timestamp of account registration request, used for checking the expiration period.
     */
    private Long requestDate = 0L;

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
        return object instanceof UserRegistrationEntity;
    }

    /**
     * Get registering user.
     */
    public UserEntity getUser() {
        return user;
    }

    /**
     * Set registering user.
     */
    public void setUser(UserEntity user) {
        this.user = user;
    }

    /**
     * Get the user activation token.
     */
    public String getActivationToken() {
        return activationToken;
    }

    /**
     * Set user activation token.
     */
    public void setActivationToken(String activationToken) {
        this.activationToken = activationToken;
    }

    /**
     * Create a new activation token and assign it to 'activationToken'.
     */
    public String createActivationToken() {
        String uuid = UUID.randomUUID().toString();
        setActivationToken(uuid);
        return uuid;
    }

    /**
     * Get the request timestamp in milliseconds since epoch.
     */
    public Long getRequestDate() {
        return requestDate;
    }

    /**
     * Set the request timestamp in milliseconds since epoch.
     */
    public void setRequestDate(Long requestDate) {
        this.requestDate = requestDate;
    }
}
