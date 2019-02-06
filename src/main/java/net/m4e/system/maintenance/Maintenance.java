/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.system.maintenance;

import net.m4e.app.event.business.*;
import net.m4e.app.resources.DocumentPool;
import net.m4e.app.user.business.*;
import net.m4e.common.Entities;
import net.m4e.system.core.*;
import net.m4e.system.maintenance.business.MaintenanceInfo;
import org.jetbrains.annotations.NotNull;
import org.slf4j.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.*;
import java.lang.invoke.MethodHandles;
import java.util.*;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UserRegistrations userRegistration;

    private final AppInfos appInfos;

    private final Users users;

    private final Events events;

    private final EventLocations eventLocations;

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
        eventLocations = null;
        entities = null;
        docPool = null;
    }

    /**
     * Create the maintenance instance.
     * 
     * @param appInfos      The application information instance
     * @param registration  The user registration instance
     * @param users         The Users instance
     * @param events        The Events instance
     * @param entities      The Entities instance
     * @param docPool       The document pool instance
     */
    @Inject
    public Maintenance(@NotNull AppInfos appInfos,
                       @NotNull UserRegistrations registration,
                       @NotNull Users users,
                       @NotNull Events events,
                       @NotNull EventLocations eventLocations,
                       @NotNull Entities entities,
                       @NotNull DocumentPool docPool) {

        this.appInfos = appInfos;
        this.userRegistration = registration;
        this.users = users;
        this.events = events;
        this.eventLocations = eventLocations;
        this.entities = entities;
        this.docPool = docPool;
    }

    /**
     * Export the maintenance info.
     */
    public MaintenanceInfo exportInfo(@NotNull AppInfoEntity infoEntity) {
        return MaintenanceInfo.fromInfoEntity(infoEntity, userRegistration);
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
        int countPurges = purgeExpiredResources();
        countPurges += purgeDeletedResources();
        updateAppInfo();
        return countPurges;
    }

    /**
     * Purge all resources which are marked as deleted.
     * 
     * @return Count of purged resources
     */
    private int purgeDeletedResources() {
        List<UserEntity>  deletedUsers = users.getMarkedAsDeletedUsers();
        List<EventEntity> eventEntities = entities.findAll(EventEntity.class);

        int countPurges = purgeEvents(deletedUsers, eventEntities);

        countPurges += purgeUsers(deletedUsers);

        return countPurges;
    }

    private int purgeUsers(List<UserEntity> deletedUsers) {
        int countPurges = 0;
        // remove all dead users
        for (UserEntity user: deletedUsers) {
            try {
                if (user.getPhoto() != null) {
                    docPool.releasePoolDocument(user.getPhoto());
                }
                users.deleteUser(user);
                countPurges++;
            }
            catch(Exception ex) {
                LOGGER.warn("Could not delete user: " + user.getId() + ", name: " +
                            user.getName() + ", reason: " + ex.getLocalizedMessage());
            }
        }
        return countPurges;
    }

    private int purgeEvents(List<UserEntity> userEntities, List<EventEntity> eventEntities) {
        int countPurges = 0;
        // purge dead events and make sure that dead users are removed from remaining events
        for (EventEntity event: eventEntities) {
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
                    countPurges++;
                }
                else {
                    // remove dead members
                    events.removeAnyMember(event, userEntities);
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
                        countPurges += deadlocs.size();
                    }
                }
            }
            catch(Exception ex) {
                LOGGER.warn("Could not delete event: " + event.getId() + ", name: " +
                            event.getName() + ", reason: " + ex.getMessage());
            }
        }
        return countPurges;
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
        int     purgeevents    = events.getEventsMarkedAsDeleted().size();
        int     purgeeventlocs = eventLocations.getLocationsMarkedAsDeleted().size();

        info.setEventCountPurge(new Long(purgeevents));
        info.setEventLocationCountPurge(new Long(purgeeventlocs));
        info.setUserCountPurge(new Long(purgeusers));
        info.setDateLastMaintenance((new Date().getTime()));
        appInfos.updateAppInfoEntity(info);
    }
}
