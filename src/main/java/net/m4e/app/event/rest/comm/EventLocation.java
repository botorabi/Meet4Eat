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

    private String id;
    private String name;
    private String description;
    private String photoId;
    private String photoETag;

    public EventLocation() {}

    public EventLocation(final String id,
                         final String name,
                         final String description,
                         final String photoId,
                         final String photoETag) {
        this.id = id;
        this.name = name;
        this.description = description;
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

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }

    public void setPhotoETag(String photoETag) {
        this.photoETag = photoETag;
    }
}
