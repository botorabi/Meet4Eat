/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.rest.comm;

/**
 * @author boto
 * Date of creation January 23, 2018
 */
public class SearchHitUser {

    private final String id;
    private final String name;
    private final String photoId;
    private final String photoETag;

    public SearchHitUser(final String id, final String name,
                         final String photoId, final String photoETag) {
        this.id = id;
        this.name = name;
        this.photoId = photoId;
        this.photoETag = photoETag;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhotoId() {
        return photoId;
    }

    public String getPhotoETag() {
        return photoETag;
    }
}
