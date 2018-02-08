/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.business;

import net.m4e.app.auth.RoleEntity;
import net.m4e.app.resources.*;
import net.m4e.common.*;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;
import java.io.Serializable;
import java.util.*;


/**
 * A class describing a user
 * 
 * @author boto
 * Date of creation Aug 18, 2017
 */
@Entity
public class UserEntity extends EntityBase implements Serializable, EntityWithPhoto {

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
     * Photo
     */
    @OneToOne(cascade = {CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH})
    private DocumentEntity photo;

    /**
     * Entity profile
     */
    @OneToOne(cascade = CascadeType.ALL)
    private UserProfileEntity profile;       

    /**
     * A list of roles belonging to this user.
     */
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH})
    private Collection<RoleEntity> roles;

    /**
     * User login
     */
    @Column(unique=true, nullable=false)
    private String login;

    /**
     * User name
     */
    @Column(nullable=false)
    private String name;

    /**
     * Password
     */
    @Column(nullable=false)
    private String password;

    /**
     * User's E-Mail address
     */
    @Column(unique=true, nullable=false)
    private String email;

    /**
     * Timestamp of last login (time in milliseconds)
     */
    private Long dateLastLogin = 0L;

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
        return object instanceof UserEntity;
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
     * Get user photo.
     */
    @Override
    public DocumentEntity getPhoto() {
        return photo;
    }

    /**
     * Set the user photo.
     */
    @Override
    public void setPhoto(DocumentEntity photo) {
        this.photo = photo;
    }

    /**
     * Get user's profile entity.
     */
    public UserProfileEntity getProfile() {
        return profile;
    }

    /**
     * Set user's profile entity.
     */
    public void setProfile(UserProfileEntity profile) {
        this.profile = profile;
    }

    /**
     * Get user roles.
     */
    public Collection<RoleEntity> getRoles() {
        return roles;
    }

    /**
     * Get user roles as a string list filled with role names.
     */
    public List<String> getRolesAsString() {
        List<String> stringList = new ArrayList<>();
        if (roles == null) {
            return stringList;
        }
        roles.stream().forEach((role) -> {
            stringList.add(role.getName());
        });
        return stringList;
    }

    /**
     * Set user roles.
     */
    public void setRoles(Collection<RoleEntity> roles) {
        this.roles = roles;
    }

    /**
     * Get the user login.
     */
    public String getLogin() {
        return login;
    }

    /**
     * Set the user login.
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * Get the user name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the user name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get user's password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set user's password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Get the E-Mail address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set the E-Mail address.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Get the timestamp of last login (milliseconds since epoch).
     */
    public Long getDateLastLogin() {
        return dateLastLogin;
    }

    /**
     * Set timestamp of last login (milliseconds since epoch).
     */
    public void setDateLastLogin(Long timeStamp) {
        this.dateLastLogin = timeStamp;
    }
}
