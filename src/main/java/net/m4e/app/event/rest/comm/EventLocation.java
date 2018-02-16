/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.rest.comm;

/**
 * @author boto
 * Date of creation January 23, 2018
 */
public class EventLocation {

    private final String id;

    private final String name;

    private final String description;

    private final String photoId;

    private final String photoETag;

    public EventLocation(final String id,
                         final String name,
                         final String decription,
                         final String photoId,
                         final String photoETag) {
        this.id = id;
        this.name = name;
        this.description = decription;
        this.photoId = photoId;
        this.photoETag = photoETag;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPhotoId() {
        return photoId;
    }

    public String getPhotoETag() {
        return photoETag;
    }
}
