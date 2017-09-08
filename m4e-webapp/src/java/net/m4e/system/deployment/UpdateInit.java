/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.system.deployment;

import net.m4e.system.core.AppUdateBaseHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import net.m4e.app.auth.AuthRole;
import net.m4e.app.auth.AuthorityConfig;
import net.m4e.app.auth.PermissionEntity;
import net.m4e.app.auth.RoleEntity;
import net.m4e.common.EntityUtils;
import net.m4e.app.resources.StatusEntity;
import net.m4e.system.core.Log;
import net.m4e.app.user.UserEntity;

/**
 * Deployment updater for initial version of app installation.
 * This is meant to be used for the very first app start. It is a good place
 * to setup initial database structures, etc.
 * 
 * @author boto
 * Date of creation Aug 22, 2017
 */
public class UpdateInit extends AppUdateBaseHandler {

    /**
     * Used for logging
     */
    private final static String TAG = "UpdateInit";

    /**
     * Make sure to increment this number for every new update class.
     */
    private static final int    INC_NUMBER = 0;

    /**
     * Application version this update belongs to
     */
    private static final String APP_VERSION = "0.0.0";

    /**
     * Construct the update instance.
     */
    public UpdateInit() {
        incUpdateNumber = INC_NUMBER;
        appVersion = APP_VERSION;
    }

    /**
     * Perform the update.
     * 
     * @param entityManager   For the case that any entity structure manipulation is needed
     * @param userTransaction Used for manipulating entities in database
     * @throws Exception This exception is thrown if something went wrong.
     */
    @Override
    public void performUpdate(EntityManager entityManager, UserTransaction userTransaction) throws Exception {
        Log.debug(TAG, "Initial deployment setup, version: " + appVersion + " (" + incUpdateNumber + ")");

        setupPermissions(entityManager, userTransaction);
        setupRoles(entityManager, userTransaction);
        setupAdminUser(entityManager, userTransaction);
    }

    /**
     * Setup all permissions.
     * 
     * @param entityManager     Entity manager
     * @param userTransaction   User transaction
     * @throws Exception        Thrown if something went wrong.
     */
    private void setupPermissions(EntityManager entityManager, UserTransaction userTransaction) throws Exception {
        EntityUtils eutils = new EntityUtils(entityManager, userTransaction);
        Log.debug(TAG, "  Setup permissions in database");
        for (String permname: AuthorityConfig.getInstance().getDefaultPermissions()) {
            // check if the permission already exists in database (should actually not happen)
            if (eutils.findEntityByField(PermissionEntity.class, "name", permname).size() > 0) {
                Log.debug(TAG, "  Permission " + permname + " already exists, skip its creation");
                continue;
            }
            Log.debug(TAG, "  Create permission: " + permname);
            PermissionEntity pe = new PermissionEntity();
            pe.setName(permname);
            eutils.createEntity(pe);
        }        
    }

    /**
     * Setup all roles.
     * 
     * @param entityManager     Entity manager
     * @param userTransaction   User transaction
     * @throws Exception        Thrown if something went wrong.
     */
    private void setupRoles(EntityManager entityManager, UserTransaction userTransaction) throws Exception {
        EntityUtils eutils = new EntityUtils(entityManager, userTransaction);
        Log.debug(TAG, "  Setup roles in database");

        for (Map.Entry<String, List<String>> role: AuthorityConfig.getInstance().getDefaultRoles().entrySet()) {

            String       rolename = role.getKey();
            List<String> perms    = role.getValue();
            // check if the role already exists in database (should actually not happen)
            if (eutils.findEntityByField(RoleEntity.class, "name", rolename).size() > 0) {
                Log.debug(TAG, "  Role " + rolename + " already exists, skip its creation");
                continue;
            }
            Log.debug(TAG, "  Create role: " + rolename);
            RoleEntity roleentity = new RoleEntity();
            roleentity.setName(rolename);
            eutils.createEntity(roleentity);

            // set all associated permissions
            List<PermissionEntity> permentities = new ArrayList<>();
            for (String perm: perms) {
                // find the permission by its name
                List<PermissionEntity> pe = eutils.findEntityByField(PermissionEntity.class, "name", perm);
                if (pe.size() > 1) {
                    Log.warning(TAG, "*** More than one permisson entry found '" + perm + "' for role '" + rolename + "', taking the first one");
                }
                else if (pe.size() < 1) {
                    Log.error(TAG, "*** Could not find permission '" + perm + "' for role '" + rolename + "'");
                    continue;
                }
                permentities.addAll(pe);
            }
            String text = "";
            for (PermissionEntity ent: permentities) {
                text += " | " + ent.getName();
            }
            Log.debug(TAG, "   Setting role permissions: " + text);

            // update the role entity with assigned permissions
            roleentity.setPermissions(permentities);
            eutils.updateEntity(roleentity);
        }        
    }

    /**
     * Setup a user with administrator rights.
     * 
     * @param entityManager     Entity manager
     * @param userTransaction   User transaction
     * @throws Exception        Thrown if something went wrong.
     */
    private void setupAdminUser(EntityManager entityManager, UserTransaction userTransaction) throws Exception {
        EntityUtils eutils = new EntityUtils(entityManager, userTransaction);
        Log.debug(TAG, "  Create user: admin");
        UserEntity user = new UserEntity();
        user.setName("Administrator");
        user.setLogin("admin");
        user.setPassword(AuthorityConfig.getInstance().createPassword("admin"));
        eutils.createEntity(user);

        // setup the entity status
        StatusEntity status = new StatusEntity();
        status.setIdCreator(user.getId());
        status.setIdOwner(user.getId());
        Date now = new Date();
        status.setDateCreation(now.getTime());
        status.setDateLastUpdate(now.getTime());
        user.setStatus(status);

        // setup the roles
        List<RoleEntity> roles = eutils.findEntityByField(RoleEntity.class, "name", AuthRole.USER_ROLE_ADMIN);
        if (roles.size() < 1) {
            Log.error(TAG, "*** Counld not find role '" + AuthRole.USER_ROLE_ADMIN + "' for admin user");
            return;
        }
        user.setRoles(Arrays.asList(roles.get(0)));

        eutils.updateEntity(user);
        Log.debug(TAG, "   User 'admin' was successfully created.");
    }
}
