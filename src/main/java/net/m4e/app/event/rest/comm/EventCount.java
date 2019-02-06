/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.rest.comm;

/**
 * @author boto
 * Date of creation February 20, 2018
 */
public class EventCount {
    private final long count;

    public EventCount(final long count) {
        this.count = count;
    }

    public long getCount() {
        return count;
    }
}
