/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.groups;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A class describing a meeting group
 * 
 * @author boto
 * Date of creation Aug 18, 2017
 */
@Entity
@XmlRootElement
public class GroupEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Group name
     */
    private String name;

    /**
     * Group description
     */
    private String description;

    /**
     * User alarm interval in milliseconds
     */
    private Long alarmInterval;

    /**
     * User alarm start in milliseconds, it is in range 0..24 hours.
     * An alarmStart of 0 means 12 AM.
     */
    private Long alarmStart;

    public GroupEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getAlarmInterval() {
        return alarmInterval;
    }

    public void setAlarmInterval(Long alarmInterval) {
        this.alarmInterval = alarmInterval;
    }

    public Long getAlarmStart() {
        return alarmStart;
    }

    public void setAlarmStart(Long alarmStart) {
        this.alarmStart = alarmStart;
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
