/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.update.rest.comm;

/**
 * @author boto
 * Date of creation March 12, 2018
 */
public class UpdateCheckCount {

    private final long count;

    public UpdateCheckCount(final long count) {
        this.count = count;
    }

    public long getCount() {
        return count;
    }
}
