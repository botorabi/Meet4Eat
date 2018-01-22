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
 * This entity is used for resetting user password.
 * 
 * @author boto
 * Date of creation Oct 19, 2017
 */
@Entity
public class UserPasswordResetEntity implements Serializable {

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
    @OneToOne(optional=false, cascade = {CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH})
    private UserEntity user;

    /**
     * Password reset token
     */
    @Column(nullable=false)
    private String resetToken;

    /**
     * Timestamp of password reset request, used for checking the expiration period.
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
     * Get the user whos password is going to get reset.
     * 
     * @return User
     */
    public UserEntity getUser() {
        return user;
    }

    /**
     * Set the user whos password we want to reset.
     * 
     * @param user 
     */
    public void setUser(UserEntity user) {
        this.user = user;
    }

    /**
     * The password reset process needs this token.
     * 
     * @return Password reset token
     */
    public String getResetToken() {
        return resetToken;
    }

    /**
     * Set password reset token.
     * 
     * @param resetToken 
     */
    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    /**
     * Create a new password reset token and assign it to 'resetToken'.
     * 
     * @return A new password reset token
     */
    public String createResetToken() {
        String uuid = UUID.randomUUID().toString();
        setResetToken(uuid);
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
        if (!(object instanceof UserPasswordResetEntity)) {
            return false;
        }
        UserPasswordResetEntity other = (UserPasswordResetEntity) object;
        return (this.id != null || other.id == null) && (this.id == null || this.id.equals(other.id));
    }

    @Override
    public String toString() {
        return "net.m4e.app.user.business.UserPasswordResetEntity[ id=" + id + " ]";
    }
}
