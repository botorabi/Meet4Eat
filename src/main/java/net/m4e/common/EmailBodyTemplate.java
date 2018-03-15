/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.common;

import java.util.*;

/**
 * Base class for email body templates. A mail body template supports the assembly of a
 * mail body consisting of several placeholders which are replaced by concrete values.
 *
 * @author boto
 * Date of creation February 2, 2018
 */
public abstract class EmailBodyTemplate {

    protected Map<String, String> placeHolders = new HashMap<>();

    public EmailBodyTemplate() {
        for (String key: registerPlaceHolders()) {
            placeHolders.put(key, "<?>");
        }
    }

    /**
     * Use this method for feeding the actual placeholder values.
     */
    public void setPlaceHolderValue(String key, String value) {
        if (!placeHolders.containsKey(key)) {
            throw new IllegalArgumentException("Invalid placeholder key");
        }
        if (value == null) {
            throw new IllegalArgumentException("Placeholder value must not be NULL");
        }
        placeHolders.put(key, value);
    }

    public Map<String, String> getPlaceHolders() {
        return placeHolders;
    }

    /**
     * Return a list of all placeholders.
     */
    protected abstract List<String> registerPlaceHolders();

    /**
     * The final body text including placeholder keys should be assembled in this method.
     */
    public abstract String createTemplate();
}
