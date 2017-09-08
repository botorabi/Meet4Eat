/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.system.maintenance;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import net.m4e.app.event.EventEntity;
import net.m4e.app.event.EventUtils;
import net.m4e.app.user.UserEntity;
import net.m4e.app.user.UserUtils;
import net.m4e.common.EntityUtils;
import net.m4e.system.core.AppInfoEntity;
import net.m4e.system.core.AppInfoUtils;
import net.m4e.system.core.Log;

/**
 * A collection of maintenance utilities
 * 
 * @author boto
 * Date of creation Sep 8, 2017
 */
public class MaintenanceUtils {

    /**
     * Used for logging
     */
    private final static String TAG = "MaintenanceUtils";

    private final EntityManager entityManager;

    private final UserTransaction userTransaction;

    /**
     * Create the utils instance for given entity manager and user transaction object.
     * 
     * @param entityManager   Entity manager
     * @param userTransaction User transaction
     */
    public MaintenanceUtils(EntityManager entityManager, UserTransaction userTransaction) {
        this.entityManager = entityManager;
        this.userTransaction = userTransaction;
    }

    /**
     * Give an app info entity export the necessary fields into a JSON object.
     * 
     * @param entity    App info entity to export
     * @return          A JSON object containing builder the proper entity fields
     */
    public JsonObjectBuilder exportInfoJSON(AppInfoEntity entity) {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("version", entity.getVersion());
        json.add("dateLastMaintenance", entity.getDateLastMaintenance());
        json.add("dateLastUpdate", entity.getDateLastUpdate());
        json.add("userCountPurge", entity.getUserCountPurge());
        json.add("eventCountPurge", entity.getEventCountPurge());
        return json;
    }

    /**
     * Purge resources and update the app info by resetting the purge counters
     * and updating "last maintenance time".
     * 
     * @return Count of purged resources
     */
    public int purgeResources() {
        int countpurges = purgeDeletedResources();
        updateAppInfo();
        return countpurges;
    }

    /**
     * Purge all resources which are marked as deleted.
     * 
     * @return Count of purged resources
     */
    private int purgeDeletedResources() {
        UserUtils   userutils   = new UserUtils(entityManager, userTransaction);
        EventUtils  eventutils  = new EventUtils(entityManager, userTransaction);
        EntityUtils entityutils = new EntityUtils(entityManager, userTransaction);

        int countpurges = 0;

        List<UserEntity>  users  = userutils.getMarkedAsDeletedUsers();
        List<EventEntity> events = entityutils.findAllEntities(EventEntity.class);

        // first purge dead events and make sure that dead users are removed from remaining events
        for (EventEntity event: events) {
            try {
                if (event.getStatus().getIsDeleted()) {
                    eventutils.deleteEvent(event);
                    countpurges++;
                }
                else {
                    // remove dead members
                    eventutils.removeAnyMember(event, users);
                }
            }
            catch(Exception ex) {
                Log.warning(TAG, "Could not delete event: " + event.getId() + ", name: " +
                            event.getName() + ", reason: " + ex.getLocalizedMessage());
            }
        }
        // now remove all dead users
        for (UserEntity user: users) {
            try {
                userutils.deleteUser(user);
                countpurges++;
            }
            catch(Exception ex) {
                Log.warning(TAG, "Could not delete user: " + user.getId() + ", name: " +
                            user.getName() + ", reason: " + ex.getLocalizedMessage());
            }
        }
        return countpurges;
    }

    /**
     * Update the app info after purging. It resets the purge counters and
     * updates the "last maintenance time".
     */
    public void updateAppInfo() {
        // upate app info
        AppInfoUtils autils = new AppInfoUtils(entityManager, userTransaction);
        AppInfoEntity info = autils.getAppInfoEntity();
        if (Objects.isNull(info)) {
            Log.warning(TAG, "Could not update app info");
            return;
        }

        // update the purge counters
        UserUtils   userutils   = new UserUtils(entityManager, userTransaction);
        EventUtils  eventutils  = new EventUtils(entityManager, userTransaction);
        int purgeusers  = userutils.getMarkedAsDeletedUsers().size();
        int purgeevents = eventutils.getMarkedAsDeletedEvents().size();

        info.setEventCountPurge(new Long(purgeevents));
        info.setUserCountPurge(new Long(purgeusers));
        info.setDateLastMaintenance((new Date().getTime()));
        autils.updateAppInfoEntity(info);
    }
}
