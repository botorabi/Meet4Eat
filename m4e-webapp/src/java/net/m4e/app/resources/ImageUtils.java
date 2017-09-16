/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.resources;

import java.util.Objects;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

/**
 * A collection of image related utilities
 * 
 * @author boto
 * Date of creation Sep 16, 2017
 */
public class ImageUtils {

    /**
     * Used for logging
     */
    private final static String TAG = "ImageUtils";

    private final EntityManager entityManager;

    private final UserTransaction userTransaction;

    /**
     * Create an instance of image utilities.
     * 
     * @param entityManager    Entity manager
     * @param userTransaction  User transaction
     */
    public ImageUtils(EntityManager entityManager, UserTransaction userTransaction) {
        this.entityManager = entityManager;
        this.userTransaction = userTransaction;
    }

    /**
     * Give an image entity export the necessary fields into a JSON object.
     * 
     * @param image     Image entity to export
     * @return          A JSON object containing builder the proper entity fields
     */
    public JsonObjectBuilder exportImageJSON(ImageEntity image) {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("id", Objects.nonNull(image.getId()) ? image.getId() : 0);
        json.add("name", Objects.nonNull(image.getName()) ? image.getName() : "");
        json.add("content", Objects.nonNull(image.getContent()) ? new String(image.getContent()) : "");
        json.add("encoding", Objects.nonNull(image.getEncoding()) ? image.getEncoding() : "");
        return json;
    }
}
