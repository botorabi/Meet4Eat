/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.common;

import net.m4e.app.resources.DocumentEntity;

/**
 * This interface is used in all entities which have a photo resource.
 * 
 * @author boto
 * Date of creation Oct 27, 2017
 */
public interface EntityWithPhoto {

    /**
     * Get entity's photo.
     * 
     * @return DocumentEntity containing the photo
     */
    DocumentEntity getPhoto();

    /**
     * Set entity's photo.
     * 
     * @param photo DocumentEntity containing the photo
     */
     void setPhoto(DocumentEntity photo);
}
