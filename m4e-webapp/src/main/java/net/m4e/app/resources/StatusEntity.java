/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.resources;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * StatusEntity contains status information used for internal house keeping,
 * database life-cycle management, business logic, and more. Some of the
 * information such as "date of creation" can be exposed to clients, while other
 * information such as "date of deletion" can be used for application maintenance
 * tasks such as purging resources. This entity also holds information about
 * resource ownership.
 * StatusEntity is meant to be aggregated by other entities which need such
 * additional information.
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
     * Date of ban. Trolls get banned.
     */
    private Long dateBan = 0L;

    /**
     * ID of creator.
     */
    private Long idCreator = 0L;

    /**
     * ID of current owner.
     */
    private Long idOwner = 0L;

    /**
     * Entity reference count. It can be used for managing shared resources.
     */
    private Long referenceCount = 0L;

    /**
     * Some resources (such as users) need an activation process. This flag can be
     * used for storing the activation state.
     */
    private boolean enabled = true;

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
     * Get date of ban in millisecond. The value 0 means no ban.
     * 
     * @return Date of ban
     */
    public Long getDateBan() {
        return dateBan;
    }

    /**
     * Set date of ban in millisecond.
     * 
     * @param dateBan Date of ban
     */
    public void setDateBan(Long dateBan) {
        this.dateBan = dateBan;
    }

    /**
     * Is the entity active? An entity gets inactive either if it is disabled, deleted, or banned.
     * 
     * @return Return true if the entity is active.
     */
    public boolean getIsActive() {
        return enabled && (dateDeletion == 0L) && (dateBan == 0L);
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

    /**
     * Set the reference count. This can be used for managing shared resources.
     * 
     * @param referenceCount Entity reference count
     */
    public void setReferenceCount(Long referenceCount) {
        this.referenceCount = referenceCount;
    }

    /**
     * Get the reference count. This can be used for managing shared resources.
     * 
     * @return Entity reference count
     */
    public Long getReferenceCount() {
        return this.referenceCount;
    }

    /**
     * Increase the reference count.
     * 
     * @return Current reference count
     */
    public Long increaseRefCount() {
        referenceCount++;
        return referenceCount;
    }

    /**
     * Decrease the reference count.
     * 
     * @return Current reference count
     */
    public Long decreaseRefCount() {
        referenceCount--;
        return referenceCount;
    }

    /**
     * Is the resource enabled? See method getIsActive.
     * 
     * @return true if the resource is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set the enable flag.
     * 
     * @param enabled The enable flag  
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
        return (this.id != null || other.id == null) && (this.id == null || this.id.equals(other.id));
    }

    @Override
    public String toString() {
        return "net.m4e.common.StatusEntity[ id=" + id + " ]";
    }
}
