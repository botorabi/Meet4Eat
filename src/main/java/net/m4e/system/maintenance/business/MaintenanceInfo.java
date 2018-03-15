/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.system.maintenance.business;

import net.m4e.app.user.business.UserRegistrations;
import net.m4e.system.core.AppInfoEntity;
import org.jetbrains.annotations.NotNull;

import javax.json.bind.annotation.JsonbTransient;

/**
 * @author boto
 * Date of creation March 15, 2018
 */
public class MaintenanceInfo {

    private String version = "";
    private Long dateLastUpdate = 0L;
    private Long dateLastMaintenance = 0L;
    private Long userCountPurge = 0L;
    private Long eventCountPurge = 0L;
    private Long eventLocationCountPurge = 0L;
    private Long pendingAccountRegistration = 0L;
    private Long pendingPasswordResets = 0L;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Long getDateLastUpdate() {
        return dateLastUpdate;
    }

    public void setDateLastUpdate(Long dateLastUpdate) {
        this.dateLastUpdate = dateLastUpdate;
    }

    public Long getDateLastMaintenance() {
        return dateLastMaintenance;
    }

    public void setDateLastMaintenance(Long dateLastMaintenance) {
        this.dateLastMaintenance = dateLastMaintenance;
    }

    public Long getUserCountPurge() {
        return userCountPurge;
    }

    public void setUserCountPurge(Long userCountPurge) {
        this.userCountPurge = userCountPurge;
    }

    public Long getEventCountPurge() {
        return eventCountPurge;
    }

    public void setEventCountPurge(Long eventCountPurge) {
        this.eventCountPurge = eventCountPurge;
    }

    public Long getEventLocationCountPurge() {
        return eventLocationCountPurge;
    }

    public void setEventLocationCountPurge(Long eventLocationCountPurge) {
        this.eventLocationCountPurge = eventLocationCountPurge;
    }

    public Long getPendingAccountRegistration() {
        return pendingAccountRegistration;
    }

    public void setPendingAccountRegistration(Long pendingAccountRegistration) {
        this.pendingAccountRegistration = pendingAccountRegistration;
    }

    public Long getPendingPasswordResets() {
        return pendingPasswordResets;
    }

    public void setPendingPasswordResets(Long pendingPasswordResets) {
        this.pendingPasswordResets = pendingPasswordResets;
    }

    @JsonbTransient
    public static MaintenanceInfo fromInfoEntity(@NotNull final AppInfoEntity infoEntity, final UserRegistrations userRegistration) {
        MaintenanceInfo info = new MaintenanceInfo();
        info.setVersion(infoEntity.getVersion());
        info.setDateLastUpdate(infoEntity.getDateLastUpdate());
        info.setDateLastMaintenance(infoEntity.getDateLastMaintenance());
        info.setUserCountPurge(infoEntity.getUserCountPurge());
        info.setEventCountPurge(infoEntity.getEventCountPurge());
        info.setEventLocationCountPurge(infoEntity.getEventLocationCountPurge());
        info.setVersion(infoEntity.getVersion());
        info.setPendingAccountRegistration((long)userRegistration.getCountPendingAccountActivations());
        info.setPendingPasswordResets((long)userRegistration.getCountPendingPasswordResets());

        return info;
    }

}
