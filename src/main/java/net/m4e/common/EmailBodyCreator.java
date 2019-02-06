/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.common;

import java.util.*;

/**
 * Create a mail body given a body template and values for placeholders in the body template.
 *
 * @author boto
 * Date of creation February 2, 2018
 */
public class EmailBodyCreator {

    private String template;
    private Map<String, String> placeHolders;

    private EmailBodyCreator(final String template, final Map<String, String> placeHolders) {
        this.template = template;
        this.placeHolders = placeHolders;
    }

    /**
     * Create the final body given a template and its placeholder values.
     */
    public static String create(final String template, final Map<String, String> placeHolders) {
        if (template == null) {
            throw new RuntimeException("Invalid body template");
        }

        if (placeHolders == null) {
            return template;
        }

        EmailBodyCreator bc = new EmailBodyCreator(template, placeHolders);
        return bc.resolvePlaceholders();
    }

    private String resolvePlaceholders() {
        for (Map.Entry<String, String> entry: placeHolders.entrySet()) {
            template = template.replaceAll(entry.getKey(), entry.getValue());
        }
        return template;
    }
}
