/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.system.maintenance;

import net.m4e.app.event.EventEntity;
import net.m4e.app.event.EventLocationEntity;
import net.m4e.app.event.Events;
import net.m4e.app.resources.DocumentPool;
import net.m4e.app.user.business.UserEntity;
import net.m4e.app.user.business.UserRegistrations;
import net.m4e.app.user.business.Users;
import net.m4e.common.Entities;
import net.m4e.system.core.AppInfoEntity;
import net.m4e.system.core.AppInfos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * Maintenance contains the app maintenance related functionality.
 * 
 * @author boto
 * Date of creation Sep 8, 2017
 */
@ApplicationScoped
public class Maintenance {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UserRegistrations userRegistration;

    private final AppInfos appInfos;

    private final Users users;

    private final Events events;

    private final Entities entities;

    private final DocumentPool docPool;


    /**
     * Default constructor needed by the container.
     */
    protected Maintenance() {
        appInfos = null;
        userRegistration = null;
        users = null;
        events = null;
        entities = null;
        docPool = null;
    }

    /**
     * Create the maintenance instance.
     * 
     * @param appInfos      The ppplication information instance
     * @param registration  The user registration instance
     * @param users         The Users instance
     * @param events        The Events instance
     * @param entities      The Entities instance
     * @param docPool       The document pool instance
     */
    @Inject
    public Maintenance(AppInfos appInfos,
                       UserRegistrations registration,
                       Users users,
                       Events events,
                       Entities entities,
                       DocumentPool docPool) {

        this.appInfos = appInfos;
        this.userRegistration = registration;
        this.users = users;
        this.events = events;
        this.entities = entities;
        this.docPool = docPool;
    }

    /**
     * Give an app info entity export the necessary fields into a JSON object.
     * 
     * @param entity    App info entity to export
     * @return          A JSON object containing builder the proper entity fields
     */
    public JsonObjectBuilder exportInfoJSON(AppInfoEntity entity) {
        int pendingaccounts = userRegistration.getCountPendingAccountActivations();
        int pendingpwresets = userRegistration.getCountPendingPasswordResets();

        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("version", entity.getVersion())
            .add("dateLastMaintenance", entity.getDateLastMaintenance())
            .add("dateLastUpdate", entity.getDateLastUpdate())
            .add("userCountPurge", entity.getUserCountPurge())
            .add("eventCountPurge", entity.getEventCountPurge())
            .add("eventLocationCountPurge", entity.getEventLocationCountPurge())
            .add("pendingAccountRegistration", pendingaccounts)
            .add("pendingPasswordResets", pendingpwresets);
        return json;
    }

    /**
     * Purge all resources which are expired, such as account registrations or
     * password reset requests which passed their expiration duration.
     * 
     * @return Count of purged resources
     */
    public int purgeExpiredResources() {
        return userRegistration.purgeExpiredRequests();
    }

    /**
     * Purge all resources and update the app info by resetting the purge counters
     * and updating "last maintenance time".
     * 
     * @return Count of purged resources
     */
    public int purgeAllResources() {
        int countpurges = purgeExpiredResources();
        countpurges += purgeDeletedResources();
        updateAppInfo();
        return countpurges;
    }

    /**
     * Purge all resources which are marked as deleted.
     * 
     * @return Count of purged resources
     */
    private int purgeDeletedResources() {
        int countpurges = 0;
        List<UserEntity>  theusers = users.getMarkedAsDeletedUsers();
        List<EventEntity> theevents = entities.findAll(EventEntity.class);

        // first purge dead events and make sure that dead users are removed from remaining events
        for (EventEntity event: theevents) {
            try {
                if (event.getStatus().getIsDeleted()) {
                    if (event.getPhoto() != null) {
                        docPool.releasePoolDocument(event.getPhoto());
                    }
                    Collection<EventLocationEntity> locs = event.getLocations();
                    if (locs != null) {
                        // delete the locations
                        locs.stream()
                            .filter((loc) -> (loc.getPhoto() != null))
                            .forEachOrdered((loc) -> {
                                docPool.releasePoolDocument(loc.getPhoto());
                            });
                    }
                    events.deleteEvent(event);
                    countpurges++;
                }
                else {
                    // remove dead members
                    events.removeAnyMember(event, theusers);
                    // purge deleted event locations
                    Collection<EventLocationEntity> locs = event.getLocations();
                    if (locs != null) {
                        Predicate<EventLocationEntity> pred = ev-> ev.getStatus().getIsDeleted();
                        List<EventLocationEntity> deadlocs = locs.stream().filter(pred).collect(Collectors.toList());
                        // update event's location list
                        locs.removeAll(deadlocs);
                        entities.update(event);
                        // delete the locations
                        for (EventLocationEntity loc: deadlocs) {
                            entities.delete(loc);
                        }
                        countpurges += deadlocs.size();
                    }
                }
            }
            catch(Exception ex) {
                LOGGER.warn("Could not delete event: " + event.getId() + ", name: " +
                            event.getName() + ", reason: " + ex.getLocalizedMessage());
            }
        }
        // now remove all dead users
        for (UserEntity user: theusers) {
            try {
                if (user.getPhoto() != null) {
                    docPool.releasePoolDocument(user.getPhoto());
                }
                users.deleteUser(user);
                countpurges++;
            }
            catch(Exception ex) {
                LOGGER.warn("Could not delete user: " + user.getId() + ", name: " +
                            user.getName() + ", reason: " + ex.getLocalizedMessage());
            }
        }
        return countpurges;
    }

    /**
     * Update the app info. It updates the purge counters and
     * the "last maintenance time".
     */
    public void updateAppInfo() {
        // update app info
        AppInfoEntity info = appInfos.getAppInfoEntity();
        if (info == null) {
            LOGGER.warn("Could not update app info");
            return;
        }

        // update the purge counters
        int     purgeusers     = users.getMarkedAsDeletedUsers().size();
        int     purgeevents    = events.getMarkedAsDeletedEvents().size();
        int     purgeeventlocs = events.getMarkedAsDeletedEventLocations().size();

        info.setEventCountPurge(new Long(purgeevents));
        info.setEventLocationCountPurge(new Long(purgeeventlocs));
        info.setUserCountPurge(new Long(purgeusers));
        info.setDateLastMaintenance((new Date().getTime()));
        appInfos.updateAppInfoEntity(info);
    }
}
