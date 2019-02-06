/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.common;

import net.m4e.app.resources.DocumentEntity;
import org.jetbrains.annotations.NotNull;

/**
 * A class helping to create photo documents.

 * @author boto
 * Date of creation February 16, 2018
 */
public class PhotoCreator {

    public static final String DEFAULT_ENCODING = DocumentEntity.ENCODING_BASE64;

    /**
     * Create a photo.
     */
    public static DocumentEntity createPhoto(@NotNull byte[] content) {
        DocumentEntity photo = new DocumentEntity();

        photo.setEncoding(DEFAULT_ENCODING);
        photo.updateContent(content);
        photo.setType(DocumentEntity.TYPE_IMAGE);

        return photo;
    }
}
