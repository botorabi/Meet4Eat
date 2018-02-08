/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.common;

import javax.json.bind.annotation.JsonbTransient;

/**
 * This interface defining least contracts of all entities.
 * 
 * @author boto
 * Date of creation February 7, 2018
 */
public interface EntityContracts {

    /**
     * Get the entity ID.
     */
    Long getId();

    /**
     * Set the entity ID.
     */
    void setId(Long id);

    /**
     * Implement this method in the actual entity class.
     * Given the entity class is called MyEntity, it must perform the following check:
     *
     *   return object instanceof MyEntity;
     */
    boolean isInstanceOfMe(Object object);
}
