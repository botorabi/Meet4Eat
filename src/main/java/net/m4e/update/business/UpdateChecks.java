/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.update.business;

import net.m4e.common.Strings;
import net.m4e.update.rest.comm.*;
import org.jetbrains.annotations.*;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;


/**
 * Class implementing update check related functionality.
 * 
 * @author boto
 * Date of creation Dec 5, 2017
 */
public class UpdateChecks {

    private final EntityManager entityManager;


    /**
     * Make the EJB container happy.
     */
    protected UpdateChecks() {
        this.entityManager = null;
    }

    /**
     * Create an instance of update checks.
     * 
     * @param entityManager The injected entity manager
     */
    @Inject
    public UpdateChecks(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Check if there is an update for a client.
     * 
     * @param  updateCheckCmd   Information about the requesting client.
     * @return                  The results of the update check
     * @throws Exception        Throws an exception if the input is invalid or imcomplete
     */
    public UpdateCheckResult checkForUpdate(@NotNull UpdateCheckCmd updateCheckCmd) throws Exception {
        String name = updateCheckCmd.getName();
        String os = updateCheckCmd.getOs();
        String flavor = updateCheckCmd.getFlavor();
        String clientVersion = updateCheckCmd.getClientVersion();

        if (Strings.nullOrEmpty(name) || Strings.nullOrEmpty(os) || Strings.nullOrEmpty(clientVersion)) {
            throw new Exception("Incomplete request information");
        }

        UpdateCheckEntity result = getActiveUpdateCheckEntity(name, os, flavor);

        UpdateCheckResult checkResult;
        if ((result == null) || result.getVersion().equals(clientVersion)) {
            checkResult = new UpdateCheckResult("", "", "", 0L);
        }
        else {
            checkResult = new UpdateCheckResult(
                    result.getVersion(),
                    result.getOs(),
                    result.getUrl(),
                    result.getReleaseDate());
        }

        return checkResult;
    }

    @Nullable
    private UpdateCheckEntity getActiveUpdateCheckEntity(final String name, final String os, final String flavor) {
        TypedQuery<UpdateCheckEntity> query;
        if (flavor == null) {
            query = entityManager.createNamedQuery("UpdateCheckEntity.findUpdate", UpdateCheckEntity.class);
        }
        else {
            query = entityManager.createNamedQuery("UpdateCheckEntity.findFlavorUpdate", UpdateCheckEntity.class);
            query.setParameter("flavor", flavor);
        }
        query.setParameter("name", name);
        query.setParameter("os", os);

        try {
            UpdateCheckEntity result = query.getSingleResult();

            return result.isActive() ? result : null;
        }
        catch(Exception ex) {}

        return null;
    }
}
