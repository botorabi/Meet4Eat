/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.user.business;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

/**
 * This entity is used for user registration. It provides an activation token.
 * 
 * @author boto
 * Date of creation Oct 1, 2017
 */
@Entity
public class UserRegistrationEntity implements Serializable {

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
    @OneToOne(optional=true, cascade = {CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH})
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
     * Get registering user.
     * 
     * @return User
     */
    public UserEntity getUser() {
        return user;
    }

    /**
     * Set registering user.
     * 
     * @param user 
     */
    public void setUser(UserEntity user) {
        this.user = user;
    }

    /**
     * User activation needs this token.
     * 
     * @return Activation token
     */
    public String getActivationToken() {
        return activationToken;
    }

    /**
     * Set user activation token.
     * 
     * @param activationToken 
     */
    public void setActivationToken(String activationToken) {
        this.activationToken = activationToken;
    }

    /**
     * Create a new activation token and assign it to 'activationToken'.
     * 
     * @return A new created activation token
     */
    public String createActivationToken() {
        String uuid = UUID.randomUUID().toString();
        setActivationToken(uuid);
        return uuid;
    }

    /**
     * Get the request timestamp in milliseconds.
     * 
     * @return Timestamp of requesting the password reset
     */
    public Long getRequestDate() {
        return requestDate;
    }

    /**
     * Set the request timestamp in milliseconds.
     * 
     * @param requestDate Request timestamp
     */
    public void setRequestDate(Long requestDate) {
        this.requestDate = requestDate;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof UserRegistrationEntity)) {
            return false;
        }
        UserRegistrationEntity other = (UserRegistrationEntity) object;
        return (this.id != null || other.id == null) && (this.id == null || this.id.equals(other.id));
    }

    @Override
    public String toString() {
        return "net.m4e.app.user.business.UserRegistrationEntity[ id=" + id + " ]";
    }
}
