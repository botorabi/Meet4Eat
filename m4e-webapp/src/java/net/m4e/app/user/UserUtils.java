/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.user;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
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
import net.m4e.app.auth.AuthRole;
import net.m4e.app.auth.RoleEntity;
import net.m4e.app.resources.ImageEntity;
import net.m4e.app.resources.ImagePool;
import net.m4e.common.EntityUtils;
import net.m4e.app.resources.StatusEntity;
import net.m4e.system.core.AppInfoEntity;
import net.m4e.system.core.AppInfoUtils;
import net.m4e.system.core.Log;

/**
 * A collection of user related utilities
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
    public static List<String> getAvailableUserRoles() {
        List<String> roles = new ArrayList<>();
        roles.add(AuthRole.USER_ROLE_ADMIN);
        roles.add(AuthRole.USER_ROLE_MODERATOR);
        return roles;
    }

    /**
     * Check if the given user has an ADMIN role or is the owner of a resource.
     * 
     * @param user              User which is checked for admin role and ownership
     * @param resourceStatus    Resource status object
     * @return                  Return true if the given user has ADMIN role or 
     *                           is the owner of a resource, otherwise return false.
     */
    public boolean userIsOwnerOrAdmin(UserEntity user, StatusEntity resourceStatus) {
        if (Objects.isNull(user) || Objects.isNull(resourceStatus)) {
            return false;
        }
        return Objects.equals(user.getId(), resourceStatus.getIdOwner()) ||
               checkUserRoles(user, Arrays.asList(AuthRole.USER_ROLE_ADMIN));
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
     * Given a requesting user, check the requested roles and eliminate invalid
     * roles from returned role set. Also douplicates are eliminated. On validating
     * requested roles, the requesting user's roles are checked too.
     * 
     * @param requestingUser    User requesting for roles
     * @param requestedRoles    Request roles
     * @return A set of valid roles.
     */
    public Collection<RoleEntity> adaptRequestedRoles(UserEntity requestingUser, Collection<RoleEntity> requestedRoles) {
        Collection<RoleEntity> res = new HashSet<>();
        List<String> allowedroles = UserUtils.getAvailableUserRoles();
        List<String> reqroles = requestingUser.getRolesAsString();
        boolean isadmin  = reqroles.contains(AuthRole.USER_ROLE_ADMIN);
        // check if any invalid role definitions exist, e.g. a normal user is not permitted to request for an admin role.
        if (Objects.nonNull(requestedRoles)) {
            for (RoleEntity role: requestedRoles) {
                if (!allowedroles.contains(role.getName())) {
                    Log.warning(TAG, "*** Invalid role '" + role.getName() + "' was requested, ignoring it.");
                    continue;
                }
                if (!isadmin && role.getName().contentEquals(AuthRole.USER_ROLE_ADMIN)) {
                    Log.warning(TAG, "*** Requesting user has no sufficient permission for requesting for  role '" + role.getName() + "', ignoring it.");
                    continue;
                }
                res.add(role);
            }
        }
        return res;
    }

    /**
     * Create a new user entity basing on data in given input entity.
     * 
     * @param inputEntity   Input data for new entity
     * @param creatorID     ID of creator
     * @return              New created entity
     * @throws Exception    Throws exception if something went wrong.
     */
    public UserEntity createNewUser(UserEntity inputEntity, Long creatorID) throws Exception {
        // setup the new entity
        UserEntity newuser = new UserEntity();
        newuser.setLogin(inputEntity.getLogin());
        newuser.setPassword(inputEntity.getPassword());
        newuser.setName(inputEntity.getName());
        newuser.setEmail(Objects.nonNull(inputEntity.getEmail()) ? inputEntity.getEmail() : "");
        newuser.setRoles(new ArrayList<>());
        addUserRoles(newuser, inputEntity.getRolesAsString());

        // setup the status
        StatusEntity status = new StatusEntity();
        status.setIdCreator(creatorID);
        Date now = new Date();
        status.setDateCreation(now.getTime());
        status.setDateLastUpdate(now.getTime());

        try {
            createUserEntity(newuser);
            status.setIdOwner(newuser.getId());
            newuser.setStatus(status);
            // NOTE this call updates the entity in database, no need to call userutils.updateUser!
            updateUserLastLogin(newuser);
        }
        catch (Exception ex) {
            throw ex;
        }
        return newuser;
    }

    /**
     * Given an user entity filled with all its fields, create it in database.
     * 
     * @param user          User entity
     * @throws Exception    Throws exception if any problem occurred.
     */
    public void createUserEntity(UserEntity user) throws Exception {
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
     * Update user in database.
     * 
     * @param user User entity to update
     */
    public void updateUser(UserEntity user) {
        EntityUtils eutils = new EntityUtils(entityManager, userTransaction);
        try {
            eutils.updateEntity(user);
        }
        catch (Exception ex) {
            Log.error(TAG, "*** Could not update user '" + user.getLogin() + "'");
        }
    }

    /**
     * Update the user image with the content of given image.
     * 
     * @param user          User entity
     * @param image         Image to set to given event
     * @throws Exception    Throws exception if any problem occurred.
     */
    void updateUserImage(UserEntity user, ImageEntity image) throws Exception {
        ImagePool imagepool = new ImagePool(entityManager,userTransaction);
        ImageEntity img = imagepool.getOrCreatePoolImage(image.getImageHash());
        if (!imagepool.compareImageHash(user.getPhoto(), img.getImageHash())) {
            imagepool.releasePoolImage(user.getPhoto());
        }
        img.setContent(image.getContent());
        img.updateImageHash();
        img.setEncoding(image.getEncoding());
        img.setResourceURL("/User/Image");
        user.setPhoto(img);
    }

    /**
     * Mark a user as deleted by setting its status deletion time stamp. This
     * method also updates the system app info entity.
     * 
     * @param user          User entity
     * @throws Exception    Throws exception if any problem occurred.
     */
    public void markUserAsDeleted(UserEntity user) throws Exception {
        EntityUtils eutils = new EntityUtils(entityManager, userTransaction);
        StatusEntity status = user.getStatus();
        if (Objects.isNull(status)) {
            throw new Exception("User has no status field!");
        }
        status.setDateDeletion((new Date().getTime()));
        eutils.updateEntity(user);

        // update the app stats
        AppInfoUtils autils = new AppInfoUtils(entityManager, userTransaction);
        AppInfoEntity appinfo = autils.getAppInfoEntity();
        if (Objects.isNull(appinfo)) {
            throw new Exception("Problem occured while retrieving AppInfo entity!");
        }
        appinfo.incrementUserCountPurge(1L);
        eutils.updateEntity(appinfo);
    }

    /**
     * Get all users which are marked as deleted.
     * 
     * @return List of users which are marked as deleted.
     */
    public List<UserEntity> getMarkedAsDeletedUsers() {
        EntityUtils eutils = new EntityUtils(entityManager, userTransaction);
        List<UserEntity> users = eutils.findAllEntities(UserEntity.class);
        List<UserEntity> deletedusers = new ArrayList<>();
        for (UserEntity user: users) {
            if (user.getStatus().getIsDeleted()) {
                deletedusers.add(user);
            }
        }
        return deletedusers;
    }

    /**
     * Delete the given entity in database.
     * 
     * @param user          User entity
     * @throws Exception    Throws exception if any problem occurred.
     */
    public void deleteUser(UserEntity user) throws Exception {
        EntityUtils eutils = new EntityUtils(entityManager, userTransaction);
        eutils.deleteEntity(user);
    }

    /**
     * Try to find a user with given user ID.
     * 
     * @param id User ID
     * @return Return user entity if found, otherwise return null.
     */
    public UserEntity findUser(Long id) {
        EntityUtils eutils = new EntityUtils(entityManager, userTransaction);
        UserEntity user = eutils.findEntity(UserEntity.class, id);
        return user;
    }

    /**
     * Try to find a user with given login.
     * 
     * @param login User login
     * @return Return user entity if found, otherwise return null.
     */
    public UserEntity findUser(String login) {
        EntityUtils eutils = new EntityUtils(entityManager, userTransaction);
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
        EntityUtils eutils = new EntityUtils(entityManager, userTransaction);
        user.setDateLastLogin((new Date().getTime()));
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
        EntityUtils eutils = new EntityUtils(entityManager, userTransaction);
        for (String role: roles) {
            // ignore empty strings
            if (role.isEmpty()) {
                continue;
            }
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
        json.add("dateLastLogin", "" + (Objects.nonNull(entity.getDateLastLogin()) ? entity.getDateLastLogin() : 0));
        json.add("dateCreation", "" + (Objects.nonNull(entity.getStatus()) ? entity.getStatus().getDateCreation() : 0));
        JsonArrayBuilder roles = Json.createArrayBuilder();
        for (RoleEntity r: entity.getRoles()) {
            roles.add(r.getName());
        }
        json.add("roles", roles);
        json.add("photoId", Objects.nonNull(entity.getPhoto()) ? entity.getPhoto().getId() : 0);
        // the ETag can be used on a client for caching purpose
        json.add("photoETag", Objects.nonNull(entity.getPhoto()) ? entity.getPhoto().getImageHash(): "");
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
        String login, passwd, email, name, photo;
        List<String> userroles = new ArrayList<>();
        try {
            JsonReader jreader = Json.createReader(new StringReader(jsonString));
            JsonObject jobject = jreader.readObject();
            login  = jobject.getString("login", null);
            passwd = jobject.getString("password", null);
            name   = jobject.getString("name", null);
            email  = jobject.getString("email", null);
            photo  = jobject.getString("photo", null);
            
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
        entity.setLogin(login);
        entity.setName(name);
        entity.setPassword(passwd);
        entity.setEmail(email);

        if (Objects.nonNull(photo)) {
            ImageEntity image = new ImageEntity();
            // currently we expect only base64 encoded images here
            image.setEncoding(ImageEntity.ENCODING_BASE64);
            image.setContent(photo.getBytes());
            image.updateImageHash();
            entity.setPhoto(image);
        }

        return entity;
    }

    /**
     * Export the given users to JSON considering the authenticated user.
     * If the authenticated user has an admin role then all existing users are
     * exported, otherwise only the user himself/herself is exported.
     * 
     * @param users     List of users to export
     * @param authUser  Authenticated user
     * @return          JSON array containing all exported users
     */
    public JsonArrayBuilder exportUsersJSON(List<UserEntity> users, UserEntity authUser) {
        JsonArrayBuilder allusers = Json.createArrayBuilder();
        // if the user has no admin role then return only himself
        if (!checkUserRoles(authUser, Arrays.asList(AuthRole.USER_ROLE_ADMIN))) {
            allusers.add(exportUserJSON(authUser));
        }
        else {
            for (UserEntity user: users) {
                if (user.getStatus().getIsActive()) {
                    allusers.add(exportUserJSON(user));
                }
            }
        }
        return allusers;
    }
}
