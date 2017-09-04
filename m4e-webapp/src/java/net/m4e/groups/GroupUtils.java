/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.groups;

import java.io.StringReader;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import net.m4e.common.EntityUtils;
import net.m4e.common.ImageEntity;
import net.m4e.common.StatusEntity;
import net.m4e.core.AppInfoEntity;
import net.m4e.core.AppInfoUtils;
import net.m4e.core.Log;
import net.m4e.user.UserEntity;

/**
 * A collection of group related utilities
 *
 * @author boto
 * Date of creation Sep 4, 2017
 */
public class GroupUtils {

    /**
     * Used for logging
     */
    private final static String TAG = "GroupUtils";

    private final EntityManager entityManager;

    private final UserTransaction userTransaction;

    /**
     * Create an instance of group utilities.
     * 
     * @param entityManager    Entity manager
     * @param userTransaction  User transaction
     */
    public GroupUtils(EntityManager entityManager, UserTransaction userTransaction) {
        this.entityManager = entityManager;
        this.userTransaction = userTransaction;
    }

    /**
     * Given an group entity filled with all its fields, create it in database.
     * 
     * @param group         Group entity
     * @throws Exception    Throws exception if any problem occurred.
     */
    public void createGroup(GroupEntity group) throws Exception {
        EntityUtils eutils = new EntityUtils(entityManager, userTransaction);

        // photo and members are shared objects, so remove them before group creation
        ImageEntity photo = group.getPhoto();
        group.setPhoto(null);
        Collection<UserEntity> members = group.getMembers();
        group.setMembers(null);

        eutils.createEntity(group);

        // now re-add photo and members to group entity and update it
        group.setPhoto(photo);
        group.setMembers(members);

        eutils.updateEntity(group);
    }

    /**
     * Update group.
     * 
     * @param group Group entity to update
     */
    public void updateGroup(GroupEntity group) {
        EntityUtils eutils = new EntityUtils(entityManager, userTransaction);
        try {
            eutils.updateEntity(group);
        }
        catch (Exception ex) {
            Log.error(TAG, "*** Could not update group '" + group.getName() + "', id: " + group.getId());
        }
    }

    /**
     * Mark a group as deleted by setting its status deletion time stamp. This
     * method also updates the system app info entity.
     * 
     * @param group         Group entity
     * @throws Exception    Throws exception if any problem occurred.
     */
    public void markGroupAsDeleted(GroupEntity group) throws Exception {
        EntityUtils eutils = new EntityUtils(entityManager, userTransaction);
        StatusEntity status = group.getStatus();
        if (Objects.isNull(status)) {
            throw new Exception("Group has no status field!");
        }
        status.setDateDeletion((new Date().getTime()));
        eutils.updateEntity(group);

        // update the app stats
        AppInfoUtils autils = new AppInfoUtils(entityManager, userTransaction);
        AppInfoEntity appinfo = autils.getAppInfoEntity();
        if (Objects.isNull(appinfo)) {
            throw new Exception("Problem occured while retrieving AppInfo entity!");
        }
        appinfo.incrementGroupCountPurge(1L);
        eutils.updateEntity(appinfo);
    }

    /**
     * Delete the given group entity in database.
     * 
     * @param group         Group entity
     * @throws Exception    Throws exception if any problem occurred.
     */
    public void deleteGroup(GroupEntity group) throws Exception {
        EntityUtils eutils = new EntityUtils(entityManager, userTransaction);
        eutils.deleteEntity(group);
    }

    /**
     * Give a group entity export the necessary fields into a JSON object.
     * 
     * @param entity    Group entity to export
     * @return          A JSON object containing builder the proper entity fields
     */
    public JsonObjectBuilder exportGroupJSON(GroupEntity entity) {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("id", Objects.nonNull(entity.getId()) ? entity.getId() : 0);
        json.add("name", Objects.nonNull(entity.getName()) ? entity.getName() : "");
        json.add("description", Objects.nonNull(entity.getDescription()) ? entity.getDescription(): "");
        json.add("photoId", Objects.nonNull(entity.getPhoto()) ? entity.getPhoto().getId(): 0);
        json.add("eventStart", Objects.nonNull(entity.getEventStart()) ? entity.getEventStart(): 0);
        json.add("eventInterval", "" + (Objects.nonNull(entity.getEventInterval()) ? entity.getEventInterval(): 0));

        JsonObjectBuilder members = Json.createObjectBuilder();
        if (Objects.nonNull(entity.getMembers())) {
            for (UserEntity m: entity.getMembers()) {
                members.add("id", m.getId());
                members.add("name", Objects.nonNull(m.getName()) ? m.getName() : "");
            }
        }
        json.add("members", members);

        JsonObjectBuilder locations = Json.createObjectBuilder();
        if (Objects.nonNull(entity.getLocations())) {
            for (GroupLocationEntity l: entity.getLocations()) {
                locations.add("id", l.getId());
                locations.add("name", Objects.nonNull(l.getName()) ? l.getName() : "");
            }
        }
        json.add("locations", locations);

        return json;
    }

    /**
     * Give a JSON string import the necessary fields and create a group entity.
     * 
     * NOTE: Group members and locations are not imported by this method.
     * 
     * @param jsonString JSON string representing a group entity
     * @return           Group entity or null if the JSON string was not appropriate
     */
    public GroupEntity importGroupJSON(String jsonString) {
        if (Objects.isNull(jsonString)) {
            return null;
        }

        String name, description;
        Long eventstart, eventinterval, photoid;
        try {
            JsonReader jreader = Json.createReader(new StringReader(jsonString));
            JsonObject jobject = jreader.readObject();

            name          = jobject.getString("name", null);
            description   = jobject.getString("description", null);
            photoid       = Long.parseLong(jobject.getString("photoId", "0"));
            eventstart    = Long.parseLong(jobject.getString("eventStart", "0"));
            eventinterval = Long.parseLong(jobject.getString("eventInterval", "0"));
        }
        catch(Exception ex) {
            Log.warning(TAG, "Could not setup user entity out of given JSON string, reason: " + ex.getLocalizedMessage());
            return null;
        }

        GroupEntity entity = new GroupEntity();
        if (Objects.nonNull(name)) {
            entity.setName(name);
        }
        if (Objects.nonNull(description)) {
            entity.setDescription(description);
        }
        if (eventstart > 0L) {
            entity.setEventStart(eventstart);
        }
        if (eventinterval > 0L) {
            entity.setEventInterval(eventinterval);
        }
        if (photoid > 0L) {
            EntityUtils eutils = new EntityUtils(entityManager, userTransaction);
            ImageEntity image = eutils.findEntity(ImageEntity.class, photoid);
            entity.setPhoto(image);
        }

        return entity;
    }
}
