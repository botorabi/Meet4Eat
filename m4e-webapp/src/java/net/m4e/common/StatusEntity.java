/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.common;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * StatusEntity contains status information used for internal house keeping,
 * database life-cycle management, business login, and more. Some of the
 * information such as "date of creation" can be exposed to clients.
 * This entity is meant to be aggregated by other entities.
 * 
 * @author boto
 * Date of creation Aug 30, 2017
 */
@Entity
public class StatusEntity implements Serializable {

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
     * Date of creation in millisecond
     */
    private Long dateCreation = 0L;

    /**
     * Date of last update in millisecond
     */
    private Long dateLastUpdate = 0L;

    /**
     * Date of deletion. This is considered during data purging.
     */
    private Long dateDeletion = 0L;

    /**
     * ID of creator.
     */
    private Long idCreator = 0L;

    /**
     * ID of current owner.
     */
    private Long idOwner = 0L;

    /**
     * Get the entity ID.
     * 
     * @return Entity ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Set the entity ID.
     * 
     * @param id Entity ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get date of creation in millisecond.
     * 
     * @return Date of creation
     */
    public Long getDateCreation() {
        return dateCreation;
    }

    /**
     * Set date of creation in millisecond.
     * 
     * @param dateCreation Date of creation
     */
    public void setDateCreation(Long dateCreation) {
        this.dateCreation = dateCreation;
    }

    /**
     * Get date of last update in millisecond.
     * 
     * @return Date of last update
     */
    public Long getDateLastUpdate() {
        return dateLastUpdate;
    }

    /**
     * Set date of last update in millisecond.
     * 
     * @param dateLastUpdate Date of last update
     */
    public void setDateLastUpdate(Long dateLastUpdate) {
        this.dateLastUpdate = dateLastUpdate;
    }

    /**
     * Get date of deletion in millisecond. The value 0 means no deletion.
     * 
     * @return Date of deletion
     */
    public Long getDateDeletion() {
        return dateDeletion;
    }

    /**
     * Set date of deletion in millisecond.
     * 
     * @param dateDeletion Date of deletion
     */
    public void setDateDeletion(Long dateDeletion) {
        this.dateDeletion = dateDeletion;
    }

    /**
     * Is the entity deleted?
     * 
     * @return Return true if the entity was deleted.
     */
    public boolean getIsDeleted() {
        return dateDeletion != 0L;
    }

    /**
     * Get the ID of creator, the interpretation of "creator" is subject of the
     * business logic.
     * 
     * @return Creator ID
     */
    public Long getIdCreator() {
        return idCreator;
    }

    /**
     * Set the ID of creator, the interpretation of "creator" is subject of the
     * business logic.
     * 
     * @param idCreator Creator ID
     */
    public void setIdCreator(Long idCreator) {
        this.idCreator = idCreator;
    }

    /**
     * Get the ID of owner, the interpretation of "owner" is subject of the
     * business logic.
     * 
     * @return Owner ID
     */
    public Long getIdOwner() {
        return idOwner;
    }

    /**
     * Set the ID of owner, the interpretation of "owner" is subject of the
     * business logic.
     * 
     * @param idOwner Owner ID
     */
    public void setIdOwner(Long idOwner) {
        this.idOwner = idOwner;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof StatusEntity)) {
            return false;
        }
        StatusEntity other = (StatusEntity) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "net.m4e.common.StatusEntity[ id=" + id + " ]";
    }
}
