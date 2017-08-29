/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.user;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import net.m4e.auth.AuthRole;
import net.m4e.auth.RoleEntity;
import net.m4e.common.EntityUtils;
import net.m4e.core.Log;

/**
 * A collection of user utilities
 * 
 * @author boto
 * Date of creation Aug 28, 2017
 */
public class UserUtils {

    /**
     * Used for logging
     */
    private final static String TAG = "UserUtils";

    private final EntityManager entityManager;

    private final UserTransaction userTransaction;

    /**
     * Create an instance of user utilities.
     * 
     * @param entityManager    Entity manager
     * @param userTransaction  User transaction
     */
    public UserUtils(EntityManager entityManager, UserTransaction userTransaction) {
        this.entityManager = entityManager;
        this.userTransaction = userTransaction;
    }

    /**
     * Get available non-virtual user roles.
     * 
     * @return User roles
     */
    public List<String> getAvailableUserRoles() {
        List<String> roles = new ArrayList<>();
        roles.add(AuthRole.USER_ROLE_ADMIN);
        roles.add(AuthRole.USER_ROLE_MODERATOR);
        return roles;
    }

    /**
     * Check if at least one of given roles matches to user's roles.
     * 
     * @param user      User for role checking
     * @param roles     Roles to check against user's roles
     * @return          Return true if at least one role matches or both, user's
     *                    and given roles are empty.
     */
    public boolean checkUserRoles(UserEntity user, List<String> roles) {
        if (user.getRolesAsString().isEmpty() && roles.isEmpty()) {
            return true;
        }
        return user.getRolesAsString().stream().anyMatch((role) -> (roles.contains(role)));
    }

    /**
     * Given an user entity filled with all its fields, create it in database.
     * 
     * @param user          User entity
     * @throws Exception    Throws exception if any problem occurred.
     */
    public void createUser(UserEntity user) throws Exception {
        EntityUtils eutils = new EntityUtils(entityManager, userTransaction);
        // we have to remove the role collection and re-add it after entity creation,
        //   otherwise new roles are created instead of using existing ones!
        List<String> roles = user.getRolesAsString();
        user.setRoles(null);
        eutils.createEntity(user);
        // now add set the roles
        if (roles.size() > 0) {
            addUserRoles(user, roles);
            eutils.updateEntity(user);
        }
    }

    /**
     * Delete the given entity in database.
     * 
     * @param user          User entity
     * @throws Exception    Throws exception if any problem occurred.
     */
    public void deleteUser(UserEntity user) throws Exception {
        EntityUtils eutils = new EntityUtils(entityManager, userTransaction);
        // first we remove all aggregates such as roles
        // this will delete all bridge rows in database
        user.setRoles(null);
        eutils.updateEntity(user);
        // now we are safe to delete the entity
        eutils.deleteEntity(user);
    }

    /**
     * Try to find a user with given login.
     * 
     * @param login User login
     * @return Return user entity if found, otherwise return null.
     */
    public UserEntity findUser(String login) {
        EntityUtils eutils = new EntityUtils(getEntityManager(), getUserTransaction());
        List<UserEntity> entities = eutils.findEntityByField(UserEntity.class, "login", login);
        if (entities.size() == 1) {
            return entities.get(0);
        }
        else if (entities.size() > 1) {
            Log.error(TAG, "*** Fatal error, more than one user with same login '" + login + "' exist in database!");
        }
        return null;
    }

    /**
     * Update user's last login timestamp.
     * 
     * @param user User entity to update
     */
    public void updateUserLastLogin(UserEntity user) {
        EntityUtils eutils = new EntityUtils(getEntityManager(), getUserTransaction());
        user.setLastLoginTime((new Date().getTime()));
        try {
            eutils.updateEntity(user);
        }
        catch (Exception ex) {
            Log.error(TAG, "*** Could not update user's last login timestamp '" + user.getLogin() + "'");
        }
    }

    /**
     * Add the given roles to entity. If the user has no roles container, then one is created.
     * 
     * @param user      User entity
     * @param roles     User roles
     */
    public void addUserRoles(UserEntity user, List<String> roles) {
        EntityUtils eutils = new EntityUtils(getEntityManager(), getUserTransaction());
        for (String role: roles) {
            List<RoleEntity> ent = eutils.findEntityByField(RoleEntity.class, "name", role);
            if (ent.size() != 1) {
                Log.error(TAG, "*** Unexpected count of role type found in database '" + role + "', count: " + ent.size());
                continue;
            }
            if (user.getRoles() == null) {
                user.setRoles(new ArrayList<>());
            }
            user.getRoles().add(ent.get(0));
        }
    }

    /**
     * Give a user entity export the necessary fields into a JSON object.
     * 
     * @param entity    User entity to export
     * @return          A JSON object containing builder the proper entity fields
     */
    public JsonObjectBuilder exportUserJSON(UserEntity entity) {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("id", Objects.nonNull(entity.getId()) ? entity.getId() : 0);
        json.add("name", Objects.nonNull(entity.getName()) ? entity.getName() : "");
        json.add("login", Objects.nonNull(entity.getLogin()) ? entity.getLogin() : "");
        json.add("email", Objects.nonNull(entity.getEmail()) ? entity.getEmail() : "");
        json.add("lastLoginTime", "" + (Objects.nonNull(entity.getLastLoginTime()) ? entity.getLastLoginTime() : 0));
        JsonArrayBuilder roles = Json.createArrayBuilder();
        for (RoleEntity r: entity.getRoles()) {
            roles.add(r.getName());
        }
        json.add("roles", roles);
        return json;
    }

    /**
     * Give a JSON string import the necessary fields and create a user entity.
     * 
     * @param jsonString JSON string representing an user entity
     * @return           User entity or null if the JSON string was not appropriate
     */
    public UserEntity importUserJSON(String jsonString) {
        if (Objects.isNull(jsonString)) {
            return null;
        }

        // try to get login and password
        String login, passwd, email, name;
        List<String> userroles = new ArrayList<>();
        try {
            JsonReader jreader = Json.createReader(new StringReader(jsonString));
            JsonObject jobject = jreader.readObject();
            login  = jobject.getString("login", null);
            passwd = jobject.getString("password", null);
            name   = jobject.getString("name", null);
            email  = jobject.getString("email", null);
            
            JsonArray r = jobject.getJsonArray("roles");
            List<JsonString> roles = r.getValuesAs(JsonString.class);
            for (int i = 0; i < roles.size(); i++) {
                userroles.add(roles.get(i).getString());
            }
        }
        catch(Exception ex) {
            Log.warning(TAG, "Could not setup user entity out of given JSON string, reason: " + ex.getLocalizedMessage());
            return null;
        }

        UserEntity entity = new UserEntity();
        addUserRoles(entity, userroles);
        entity.setName(name);
        entity.setPassword(passwd);
        entity.setEmail(email);
        entity.setLastLoginTime(0L);
        entity.setLogin(login);
        return entity;
    }

    /**
     * Get the entity manager.
     * 
     * @return Entity manager
     */
    private EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * Get the user transaction instance.
     * @return User transaction
     */
    private UserTransaction getUserTransaction() {
        return userTransaction;
    }
}
