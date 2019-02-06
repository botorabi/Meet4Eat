/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.common;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * @author boto
 * Date of creation February 10, 2018
 */
class EmailBodyTemplateTest {

    final static String TEMPLATE = "This is a @PLACEHOLDER_A@. This is another @PLACEHOLDER_B@";

    final static List<String> PLACEHOLDERS = Arrays.asList("@PLACEHOLDER_A@", "@PLACEHOLDER_B@");

    final static String VALID_PLACEHOLDER = "@PLACEHOLDER_A@";

    final static String INVALID_PLACEHOLDER = "@INVALID_HOLDER@";


    class BodyTemplate extends EmailBodyTemplate {

        @Override
        protected List<String> registerPlaceHolders() {
            return PLACEHOLDERS;
        }

        @Override
        public String createTemplate() {
            return TEMPLATE;
        }
    }


    @Test
    void invalidPlaceHolderKey() {
        try {
            BodyTemplate template = new BodyTemplate();
            template.setPlaceHolderValue(INVALID_PLACEHOLDER, "");

            fail("An invalid placeholder key was not detected!");

        } catch(IllegalArgumentException ex) {}
    }

    @Test
    void validPlaceHolder() {
        try {
            BodyTemplate template = new BodyTemplate();
            template.setPlaceHolderValue(VALID_PLACEHOLDER, "");

        } catch(IllegalArgumentException ex) {
            fail("Setting a valid placeholder failed!");
        }
    }

    @Test
    void getPlaceHolders() {
        BodyTemplate template = new BodyTemplate();

        assertThat(template.getPlaceHolders().size()).isEqualTo(PLACEHOLDERS.size());
    }
}
