/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import net.m4e.auth.RoleEntity;
import net.m4e.common.StatusEntity;


/**
 * A class describing a user
 * 
 * @author boto
 * Date of creation Aug 18, 2017
 */
@Entity
@XmlRootElement
public class UserEntity implements Serializable {

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
     * Entity profile
     */
    @OneToOne(optional=true, cascade = CascadeType.ALL)
    private UserProfileEntity profile;       

    /**
     * User login
     */
    @Column(unique=true)
    private String login;

    /**
     * User name
     */
    private String name;

    /**
     * Password
     */
    private String password;

    /**
     * User's E-Mail address
     */
    private String email;

    /**
     * Timestamp of last login (time in milliseconds)
     */
    private Long dateLastLogin;

    /**
     * A list of roles belonging to this user.
     */
    @OneToMany(cascade = CascadeType.ALL)
    private Collection<RoleEntity> roles;

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
     * Get user' profile entity.
     * 
     * @return Entity profile
     */
    public UserProfileEntity getProfile() {
        return profile;
    }

    /**
     * Set entity prfile.
     * 
     * @param profile Entity profile
     */
    public void setStatus(UserProfileEntity profile) {
        this.profile = profile;
    }

    /**
     *
     * @return
     */
    public String getLogin() {
        return login;
    }

    /**
     *
     * @param login
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * Get user name.
     * 
     * @return  User name
     */
    public String getName() {
        return name;
    }

    /**
     * Set user name.
     * 
     * @param name User name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get user's password.
     * 
     * @return User's password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set user's password.
     * 
     * @param password  User's password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Get E-Mail address.
     * 
     * @return E-Mail address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set E-Mail address.
     * 
     * @param email E-Mail address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Get the timestamp of last login.
     * 
     * @return Last login timestamp
     */
    public Long getDateLastLogin() {
        return dateLastLogin;
    }

    /**
     * Set timestamp of current login
     * 
     * @param timeStamp Current timestamp
     */
    public void setDateLastLogin(Long timeStamp) {
        this.dateLastLogin = timeStamp;
    }

    /**
     * Get user roles.
     * 
     * @return User roles
     */
    @XmlTransient
    public Collection<RoleEntity> getRoles() {
        return roles;
    }

    /**
     * Get user roles as a string list filled with role names.
     * 
     * @return User roles as string list
     */
    @XmlTransient
    public List<String> getRolesAsString() {
        List<String> stringlist = new ArrayList<>();
        if (Objects.isNull(roles)) {
            return stringlist;
        }
        roles.stream().forEach((role) -> {
            stringlist.add(role.getName());
        });
        return stringlist;
    }

    /**
     * Set user roles.
     * 
     * @param roles User roles
     */
    public void setRoles(Collection<RoleEntity> roles) {
        this.roles = roles;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof UserEntity)) {
            return false;
        }
        UserEntity other = (UserEntity) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "user.UserEntity[ id=" + id + " ]";
    }
}
