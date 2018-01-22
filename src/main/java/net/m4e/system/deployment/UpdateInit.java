/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.system.deployment;

import net.m4e.app.auth.AuthRole;
import net.m4e.app.auth.AuthorityConfig;
import net.m4e.app.auth.PermissionEntity;
import net.m4e.app.auth.RoleEntity;
import net.m4e.app.resources.StatusEntity;
import net.m4e.app.user.business.UserEntity;
import net.m4e.common.Entities;
import net.m4e.system.core.AppUdateBaseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.lang.invoke.MethodHandles;
import java.util.*;

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
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Make sure to increment this number for every new update class.
     */
    private static final int INC_NUMBER = 0;

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
     * @param entities        Entities instance used for common entity operations
     */
    @Override
    public void performUpdate(EntityManager entityManager, Entities entities) {
        LOGGER.debug("Initial deployment setup, version: " + appVersion + " (" + incUpdateNumber + ")");

        setupPermissions(entities);
        setupRoles(entities);
        setupAdminUser(entities);
    }

    /**
     * Setup all permissions.
     * 
     * @param entities  The Entities instance
     */
    private void setupPermissions(Entities entities) {
        LOGGER.debug("  Setup permissions in database");
        for (String permname: AuthorityConfig.getInstance().getApplicationPermissions()) {
            // check if the permission already exists in database (should actually not happen)
            if (entities.findByField(PermissionEntity.class, "name", permname).size() > 0) {
                LOGGER.debug("  Permission " + permname + " already exists, skip its creation");
                continue;
            }
            LOGGER.debug("  Create permission: " + permname);
            PermissionEntity pe = new PermissionEntity();
            pe.setName(permname);
            entities.create(pe);
        }        
    }

    /**
     * Setup all roles.
     * 
     * @param entities  The Entities instance
     */
    private void setupRoles(Entities entities) {
        LOGGER.debug("  Setup roles in database");

        for (Map.Entry<String, List<String>> role: AuthorityConfig.getInstance().getApplicationRoles().entrySet()) {

            String       rolename = role.getKey();
            List<String> perms    = role.getValue();
            // check if the role already exists in database (should actually not happen)
            if (entities.findByField(RoleEntity.class, "name", rolename).size() > 0) {
                LOGGER.debug("  Role " + rolename + " already exists, skip its creation");
                continue;
            }
            LOGGER.debug("  Create role: " + rolename);
            RoleEntity roleentity = new RoleEntity();
            roleentity.setName(rolename);
            entities.create(roleentity);

            // set all associated permissions
            List<PermissionEntity> permentities = new ArrayList<>();
            for (String perm: perms) {
                // find the permission by its name
                List<PermissionEntity> pe = entities.findByField(PermissionEntity.class, "name", perm);
                if (pe.size() > 1) {
                    LOGGER.warn("*** More than one permisson entry found '" + perm + "' for role '" + rolename + "', taking the first one");
                }
                else if (pe.size() < 1) {
                    LOGGER.error("*** Could not find permission '" + perm + "' for role '" + rolename + "'");
                    continue;
                }
                permentities.addAll(pe);
            }
            String text = "";
            for (PermissionEntity ent: permentities) {
                text += " | " + ent.getName();
            }
            LOGGER.debug("   Setting role permissions: " + text);

            // update the role entity with assigned permissions
            roleentity.setPermissions(permentities);
            entities.update(roleentity);
        }        
    }

    /**
     * Setup a user with administrator rights.
     * 
     * @param entities  The Entities instance
     */
    private void setupAdminUser(Entities entities) {
        LOGGER.debug("  Create user: admin");
        UserEntity user = new UserEntity();
        user.setName("Administrator");
        user.setLogin("admin");
        user.setEmail("dummy AT mail.com");
        user.setPassword(AuthorityConfig.getInstance().createPassword("admin"));
        entities.create(user);

        // setup the entity status
        StatusEntity status = new StatusEntity();
        status.setIdCreator(user.getId());
        status.setIdOwner(user.getId());
        Date now = new Date();
        status.setDateCreation(now.getTime());
        status.setDateLastUpdate(now.getTime());
        user.setStatus(status);

        // setup the roles
        List<RoleEntity> roles = entities.findByField(RoleEntity.class, "name", AuthRole.USER_ROLE_ADMIN);
        if (roles.size() < 1) {
            LOGGER.error("*** Counld not find role '" + AuthRole.USER_ROLE_ADMIN + "' for admin user");
            return;
        }
        user.setRoles(Arrays.asList(roles.get(0)));

        entities.update(user);
        LOGGER.debug("   User 'admin' was successfully created.");
    }
}
