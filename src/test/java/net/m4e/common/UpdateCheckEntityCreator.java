/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.common;

import net.m4e.update.business.UpdateCheckEntity;

import java.time.Instant;

/**
 * @author boto
 * Date of creation March 13, 2018
 */
public class UpdateCheckEntityCreator {

    public static Long CHECK_ENTRY_ID = 1000L;
    public static String CHECK_ENTRY_NAME = "name";
    public static String CHECK_ENTRY_OS = "Linux";
    public static String CHECK_ENTRY_FLAVOR = "Flavor";
    public static String CHECK_ENTRY_VERSION = "1.2.3";
    public static String CHECK_ENTRY_URL = "https://update.org";
    public static Long CHECK_ENTRY_RELEASE_DATE = Instant.now().getEpochSecond();
    public static boolean CHECK_ENTRY_ACTIVE = true;

    /**
     * Create an update check entity.
     */
    static public UpdateCheckEntity create() {
        UpdateCheckEntity entity = new UpdateCheckEntity();
        entity.setId(CHECK_ENTRY_ID);
        entity.setName(CHECK_ENTRY_NAME);
        entity.setOs(CHECK_ENTRY_OS);
        entity.setFlavor(CHECK_ENTRY_FLAVOR);
        entity.setVersion(CHECK_ENTRY_VERSION);
        entity.setUrl(CHECK_ENTRY_URL);
        entity.setReleaseDate(CHECK_ENTRY_RELEASE_DATE);
        entity.setActive(CHECK_ENTRY_ACTIVE);

        return entity;
    }
}
