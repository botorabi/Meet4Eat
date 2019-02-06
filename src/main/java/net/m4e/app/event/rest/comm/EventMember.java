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
public class EventMember {

    public enum OnlineStatus {
        online,
        offline
    }

    private String id;
    private String name;
    private String photoId;
    private String PhotoETag;
    private OnlineStatus status;


    public EventMember() {
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
        return PhotoETag;
    }

    public OnlineStatus getStatus() {
        return status;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }

    public void setPhotoETag(String photoETag) {
        PhotoETag = photoETag;
    }

    public void setStatus(OnlineStatus status) {
        this.status = status;
    }
}