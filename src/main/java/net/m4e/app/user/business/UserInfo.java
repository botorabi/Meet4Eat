/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.business;

import org.jetbrains.annotations.NotNull;

import javax.json.bind.annotation.JsonbTransient;
import java.util.ArrayList;
import java.util.List;

/**
 * A collection of user data fields for exporting to clients.
 *
 * @author boto
 * Date of creation January 23, 2018
 */
public class UserInfo {
    public enum OnlineStatus { ONLINE, OFFLINE }

    private String id;
    private String name;
    private String login;
    private String email;
    private String photoId;
    private String PhotoETag;
    private List<String> roles;
    private long dateLastLogin;
    private long dateCreation;
    private OnlineStatus status;

    public UserInfo() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLogin() {
        return login;
    }

    public String getEmail() {
        return email;
    }

    public String getPhotoId() {
        return photoId;
    }

    public String getPhotoETag() {
        return PhotoETag;
    }

    public List<String> getRoles() {
        return roles;
    }

    public OnlineStatus getStatus() {
        return status;
    }

    public long getDateLastLogin() {
        return dateLastLogin;
    }

    public long getDateCreation() {
        return dateCreation;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }

    public void setPhotoETag(String photoETag) {
        PhotoETag = photoETag;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public void setStatus(OnlineStatus status) {
        this.status = status;
    }

    public void setDateLastLogin(long dateLastLogin) {
        this.dateLastLogin = dateLastLogin;
    }

    public void setDateCreation(long dateCreation) {
        this.dateCreation = dateCreation;
    }

    @JsonbTransient
    public static UserInfo fromUserEntity(@NotNull UserEntity userEntity, final OnlineStatus onlineStatus) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(userEntity.getId().toString());
        userInfo.setName(userEntity.getName());
        userInfo.setEmail(userEntity.getEmail());
        userInfo.setLogin(userEntity.getLogin());
        userInfo.setPhotoId(userEntity.getPhoto() != null ? userEntity.getPhoto().getId().toString() : "");
        userInfo.setPhotoETag(userEntity.getPhoto() != null ? userEntity.getPhoto().getETag() : "");
        userInfo.setDateLastLogin(userEntity.getDateLastLogin());
        userInfo.setDateCreation(userEntity.getStatus() != null ? userEntity.getStatus().getDateCreation() : 0L);
        userInfo.setStatus(onlineStatus);
        userInfo.setRoles(userEntity.getRolesAsString());

        return userInfo;
    }
}
