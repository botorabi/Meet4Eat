/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.user.business;

import net.m4e.app.auth.AuthRole;
import net.m4e.app.auth.RoleEntity;
import net.m4e.app.communication.ConnectedClients;
import net.m4e.app.event.EventEntity;
import net.m4e.app.resources.DocumentEntity;
import net.m4e.app.resources.DocumentPool;
import net.m4e.app.resources.StatusEntity;
import net.m4e.app.user.rest.comm.UserCmd;
import net.m4e.common.Entities;
import net.m4e.system.core.AppInfoEntity;
import net.m4e.system.core.AppInfos;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.*;
import java.lang.invoke.MethodHandles;
import java.util.*;


/**
 * A collection of user related utilities
 * 
 * @author boto
 * Date of creation Aug 28, 2017
 */
@ApplicationScoped
public class Users {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Entities entities;

    private final AppInfos appInfos;

    private final DocumentPool docPool;


    /**
     * Defautl constructor, make the container happy.
     */
    protected Users() {
        entities = null;
        appInfos = null;
        docPool = null;
    }

     /**
      * Create an instance of user utilities.
      *
      * @param entities
      * @param appInfos
      * @param docPool
      */
    @Inject
    public Users(Entities entities, AppInfos appInfos, DocumentPool docPool) {
        this.entities = entities;
        this.appInfos = appInfos;
        this.docPool = docPool;
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
        if ((user == null) || (resourceStatus == null)) {
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
     * @return          Return true if at least one of given roles matches.
     */
    public boolean checkUserRoles(UserEntity user, List<String> roles) {
        if (user.getRolesAsString().isEmpty() || roles.isEmpty()) {
            return false;
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
        List<String> allowedroles = Users.getAvailableUserRoles();
        List<String> reqroles = requestingUser.getRolesAsString();
        boolean isadmin  = reqroles.contains(AuthRole.USER_ROLE_ADMIN);
        // check if any invalid role definitions exist, e.g. a normal user is not permitted to request for an admin role.
        if (requestedRoles != null) {
            for (RoleEntity role: requestedRoles) {
                if (!allowedroles.contains(role.getName())) {
                    LOGGER.warn("*** Invalid role '" + role.getName() + "' was requested, ignoring it.");
                    continue;
                }
                if (!isadmin && role.getName().contentEquals(AuthRole.USER_ROLE_ADMIN)) {
                    LOGGER.warn("*** Requesting user has no sufficient permission for requesting for  role '" + role.getName() + "', ignoring it.");
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
     * @param creatorID     ID of creator, let null in order to take the new user itself as creator.
     * @return              New created entity
     * @throws Exception    Throws exception if something went wrong.
     */
    public UserEntity createNewUser(UserEntity inputEntity, Long creatorID) {
        // setup the new entity
        UserEntity newuser = new UserEntity();
        newuser.setLogin(inputEntity.getLogin());
        newuser.setPassword(inputEntity.getPassword());
        newuser.setName(inputEntity.getName());
        newuser.setEmail((inputEntity.getEmail() != null) ? inputEntity.getEmail() : "");
        newuser.setRoles(new ArrayList<>());
        addUserRoles(newuser, inputEntity.getRolesAsString());

        // setup the status
        StatusEntity status = new StatusEntity();
        Date now = new Date();
        status.setDateCreation(now.getTime());
        status.setDateLastUpdate(now.getTime());

        try {
            createUserEntity(newuser);
            status.setIdOwner(newuser.getId());
            status.setIdCreator((creatorID != null) ? creatorID: newuser.getId());
            newuser.setStatus(status);
            // NOTE this call updates the entity in database, no need to call users.updateUser!
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
    public void createUserEntity(UserEntity user) {
        // we have to remove the role collection and re-add it after entity creation,
        //   otherwise new roles are created instead of using existing ones!
        List<String> roles = user.getRolesAsString();
        user.setRoles(null);
        entities.create(user);
        // now add set the roles
        if (roles.size() > 0) {
            addUserRoles(user, roles);
            entities.update(user);
        }
    }

    /**
     * Update user in database.
     * 
     * @param user User entity to update
     */
    public void updateUser(UserEntity user) {
        try {
            entities.update(user);
        }
        catch (Exception ex) {
            LOGGER.error("*** Could not update user '" + user.getLogin() + "'");
        }
    }

    /**
     * Update the user image with the content of given image. Make sure that the
     * document content and encoding are set properly.
     * 
     * @param user          User entity
     * @param image         Image to set to given event
     * @throws Exception    Throws exception if any problem occurred.
     */
    public void updateUserImage(UserEntity user, DocumentEntity image) throws Exception {
        // make sure that the resource URL is set
        image.setResourceURL("/User/Image");
        docPool.updatePhoto(user, image);
    }

    /**
     * Mark a user as deleted by setting its status deletion time stamp. This
     * method also updates the system app info entity.
     * 
     * @param user          User entity
     * @throws Exception    Throws exception if any problem occurred.
     */
    public void markUserAsDeleted(UserEntity user) throws Exception {
        StatusEntity status = user.getStatus();
        if (status == null) {
            throw new Exception("User has no status field!");
        }
        status.setDateDeletion((new Date().getTime()));
        entities.update(user);

        // update the app stats
        AppInfoEntity appinfo = appInfos.getAppInfoEntity();
        if (appinfo == null) {
            throw new Exception("Problem occured while retrieving AppInfo entity!");
        }
        appinfo.incrementUserCountPurge(1L);
        entities.update(appinfo);
    }

    /**
     * Get all users which are marked as deleted.
     * 
     * @return List of users which are marked as deleted.
     */
    public List<UserEntity> getMarkedAsDeletedUsers() {
        List<UserEntity> users = entities.findAll(UserEntity.class);
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
    public void deleteUser(UserEntity user) {
        entities.delete(user);
    }

    /**
     * Try to find a user with given user ID.
     * 
     * @param id User ID
     * @return Return user entity if found, otherwise return null.
     */
    public UserEntity findUser(Long id) {
        UserEntity user = entities.find(UserEntity.class, id);
        return user;
    }

    /**
     * Try to find a user with given login.
     * 
     * @param login User login
     * @return Return user entity if found, otherwise return null.
     */
    public UserEntity findUser(String login) {
        List<UserEntity> foundentities = entities.findByField(UserEntity.class, "login", login);
        if (foundentities.size() == 1) {
            return foundentities.get(0);
        }
        else if (foundentities.size() > 1) {
            LOGGER.error("*** Fatal error, more than one user with same login '" + login + "' exist in database!");
        }
        return null;
    }

    /**
     * Try to find a user with given email.
     * 
     * @param email User's email
     * @return Return user entity if found, otherwise return null.
     */
    public UserEntity findUserByEmail(String email) {
        List<UserEntity> foundentities = entities.findByField(UserEntity.class, "email", email);
        if (foundentities.size() == 1) {
            return foundentities.get(0);
        }
        else if (foundentities.size() > 1) {
            LOGGER.error("*** Fatal error, more than one user with same email '" + email + "' exist in database!");
        }
        return null;
    }

    /**
     * Get IDs of all users which are relatives of a given user.
     * Relatives are other users which are in the same events as the user or
     * friends etc. The term 'relative' is elsewhere known as "connection" (e.g. in LikedIn),
     * however in order to avoid any confusion with WebSocket connections in this app,
     * we use the term 'relative'.
     * 
     * NOTE: the current data architecture has no back association from users to the events
     * they are involved in (by being a member). So we do a forward search by iterating
     * all events and evaluating their members. This solution does not scale well, we may
     * consider an optimization in data structure or a kind of an in-memory lookup cache in future.
     * 
     * @param user  User we search for relatives for
     * @return      List of IDs of all other users which are relatives of 'user'.
     */
    public List<Long> getUserRelatives(UserEntity user) {
        Set<Long> relatives = new HashSet();
        List<EventEntity> events = entities.findAll(EventEntity.class);
        for (EventEntity event: events) {
            if ((event.getStatus() == null) || !event.getStatus().getIsActive()) {
                continue;
            }
            Collection<UserEntity> members = event.getMembers();
            if (members == null) {
                continue;
            }

            Long ownerid = event.getStatus().getIdOwner();
            if (Objects.equals(ownerid, user.getId()) || members.contains(user)) {
                for (UserEntity u: event.getMembers()) {
                    relatives.add(u.getId());
                }
                relatives.add(ownerid);
            }
        }

        return new ArrayList(relatives);
    }

    /**
     * Update user's last login timestamp.
     * 
     * @param user User entity to update
     */
    public void updateUserLastLogin(UserEntity user) {
        user.setDateLastLogin((new Date().getTime()));
        try {
            entities.update(user);
        }
        catch (Exception ex) {
            LOGGER.error("*** Could not update user's last login timestamp '" + user.getLogin() + "'");
        }
    }

    /**
     * Add the given roles to entity. If the user has no roles container, then one is created.
     * 
     * @param user      User entity
     * @param roles     User roles
     */
    public void addUserRoles(UserEntity user, List<String> roles) {
        for (String role: roles) {
            // ignore empty strings
            if (role.isEmpty()) {
                continue;
            }
            List<RoleEntity> ent = entities.findByField(RoleEntity.class, "name", role);
            if (ent.size() != 1) {
                LOGGER.error("*** Unexpected count of role type found in database '" + role + "', count: " + ent.size());
                continue;
            }
            if (user.getRoles() == null) {
                user.setRoles(new ArrayList<>());
            }
            user.getRoles().add(ent.get(0));
        }
    }

    /**
     * Give a user entity export the necessary fields.
     *
     * @param entity        User entity to export
     * @param connections   Real-time user connections
     * @return              User info
     */
    public UserInfo exportUser(@NotNull UserEntity entity, ConnectedClients connections) {
        boolean online = (connections.getConnectedUser(entity.getId()) != null);
        return UserInfo.fromUserEntity(entity, online ? UserInfo.OnlineStatus.ONLINE : UserInfo.OnlineStatus.OFFLINE);
    }

    /**
     * Export the given users considering the authenticated user.
     * If the authenticated user has an admin role then all existing users are
     * exported, otherwise only the user himself/herself is exported.
     *
     * @param users         List of users to export
     * @param authUser      Authenticated user
     * @param connections   Real-time user connections
     * @return              List of all exported users
     */
    public List<UserInfo> exportUsers(List<UserEntity> users, UserEntity authUser, ConnectedClients connections) {
        List<UserInfo> allUsers = new ArrayList<>();
        // if the user has no admin role then return only himself
        if (!checkUserRoles(authUser, Arrays.asList(AuthRole.USER_ROLE_ADMIN))) {
            allUsers.add(exportUser(authUser, connections));
        }
        else {
            for (UserEntity user: users) {
                if (user.getStatus().getIsActive()) {
                    allUsers.add(exportUser(user, connections));
                }
            }
        }
        return allUsers;
    }

    /**
     * Give an user input data import the necessary fields and create a user entity.
     * 
     * @param userCmd Data representing an user entity
     * @return           User entity or null if the data was not appropriate
     */
    public UserEntity importUser(UserCmd userCmd) {
        if (userCmd == null) {
            return null;
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setLogin(userCmd.getLogin());
        userEntity.setName(userCmd.getName());
        userEntity.setPassword(userCmd.getPassword());
        userEntity.setEmail(userCmd.getEmail());

        List<String> userRoles = new ArrayList<>();
        for (int i = 0; i < userCmd.getRoles().size(); i++) {
            userRoles.add(userCmd.getRoles().get(i));
        }
        addUserRoles(userEntity, userRoles);

        if (userCmd.getPhoto() != null) {
            DocumentEntity image = new DocumentEntity();
            // currently we expect only base64 encoded images here
            image.setEncoding(DocumentEntity.ENCODING_BASE64);
            image.updateContent(userCmd.getPhoto().getBytes());
            image.setType(DocumentEntity.TYPE_IMAGE);
            userEntity.setPhoto(image);
        }

        return userEntity;
    }
}
