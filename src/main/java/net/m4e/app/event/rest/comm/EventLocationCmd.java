/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.rest.comm;

import javax.json.bind.annotation.JsonbProperty;

/**
 * @author boto
 * Date of creation February 16, 2018
 */
public class EventLocationCmd {

    private String id;

    private String name;

    private String description;

    private String photo;

    public EventLocationCmd() {}

    public EventLocationCmd(final String id,
                            final String name,
                            final String description,
                            final String photo) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.photo = photo;
    }

    @JsonbProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @JsonbProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @JsonbProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @JsonbProperty("photo")
    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPhoto() {
        return photo;
    }
}
