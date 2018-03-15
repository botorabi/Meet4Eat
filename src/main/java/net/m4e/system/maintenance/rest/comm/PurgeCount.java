/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.system.maintenance.rest.comm;

/**
 * @author boto
 * Date of creation March 15, 2018
 */
public class PurgeCount {

    private final long countPurges;

    public PurgeCount(final long countPurges) {
        this.countPurges = countPurges;
    }

    public long getCountPurges() {
        return countPurges;
    }
}
