/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.groups;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import net.m4e.common.ImageEntity;
import net.m4e.common.StatusEntity;
import net.m4e.user.UserEntity;

/**
 * A class describing a group of users
 * 
 * @author boto
 * Date of creation Aug 18, 2017
 */
@Entity
public class GroupEntity implements Serializable {

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
     * Group name
     */
    private String name;

    /**
     * Group description
     */
    private String description;

    /**
     * Photo
     */
    @OneToOne(optional=true, cascade = CascadeType.ALL)
    private ImageEntity photo;

    /**
     * Group members
     */
    @OneToMany(targetEntity=UserEntity.class, cascade = {CascadeType.REFRESH, CascadeType.DETACH})
    private Collection<UserEntity> members;

    /**
     * A list of locations which represent possible meeting points
     */
    @OneToMany(targetEntity=GroupLocationEntity.class, cascade = CascadeType.ALL)
    private Collection<GroupLocationEntity> locations;

    /**
     * User alarm start time in millisecond.
     */
    private Long alarmStart;

    /**
     * User alarm interval in millisecond.
     */
    private Long alarmInterval;

    /**
     * Create a group entity.
     */
    public GroupEntity() {
    }

    /**
     * Get group ID.
     * 
     * @return Group ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Set Group ID.
     * 
     * @param id Group ID
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
     * Get group name.
     * 
     * @return Group name
     */
    public String getName() {
        return name;
    }

    /**
     * Set group name.
     * 
     * @param name Group name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get group description.
     * 
     * @return Group description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set group description.
     * 
     * @param description Group description 
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get group photo.
     * 
     * @return ImageEntity containing the photo
     */
    public ImageEntity getPhoto() {
        return photo;
    }

    /**
     * Set the group photo.
     * 
     * @param photo ImageEntity containing the photo
     */
    public void setPhoto(ImageEntity photo) {
        this.photo = photo;
    }

    /**
     * Get alarm start time.
     * 
     * @return Alarm start in millisecond
     */
    public Long getAlarmStart() {
        return alarmStart;
    }

    /**
     * Set alarm start time.
     * 
     * @param alarmStart Alarm start in millisecond
     */
    public void setAlarmStart(Long alarmStart) {
        this.alarmStart = alarmStart;
    }

    /**
     * Get group members.
     * 
     * @return Group members
     */
    public Collection<UserEntity> getMembers() {
        return members;
    }

    /**
     * Set group members.
     * 
     * @param members Group members
     */
    public void setMembers(Collection<UserEntity> members) {
        this.members = members;
    }

    /**
     * Get group locations.
     * 
     * @return Group locations
     */
    public Collection<GroupLocationEntity> getLocations() {
        return locations;
    }

    /**
     * Set group locations.
     * 
     * @param locations Group locations
     */
    public void setLocations(Collection<GroupLocationEntity> locations) {
        this.locations = locations;
    }

    /**
     * Get alarm interval. A value 0 means no periodic alarm.
     * 
     * @return Alarm interval in millisecond.
     */
    public Long getAlarmInterval() {
        return alarmInterval;
    }

    /**
     * Set alarm interval. Let it be 0 in order to disable periodic alarm.
     * 
     * @param alarmInterval Alarm interval in millisecond.
     */
    public void setAlarmInterval(Long alarmInterval) {
        this.alarmInterval = alarmInterval;
    }


    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof GroupEntity)) {
            return false;
        }
        GroupEntity other = (GroupEntity) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "net.m4e.groups.GroupEntity[ id=" + id + " ]";
    }
}
