/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.common;

import java.util.Objects;

/**
 * Base class for entities.

 * @author boto
 * Date of creation February 7, 2018
 */
public abstract class EntityBase implements EntityContracts {

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (getId() != null ? getId().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if ((object == null) || (this.getClass() != object.getClass())) {
            return false;
        }

        EntityContracts that = (EntityContracts) object;
        return this.getId() != null && Objects.equals(this.getId(), that.getId());
    }

    @Override
    public String toString() {
        String classPath = getClass().getPackage().getName() + "." + getClass().getSimpleName();
        return classPath + "[ id=" + getId() + " ]";
    }
}
