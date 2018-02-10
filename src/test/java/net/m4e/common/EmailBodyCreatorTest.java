/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.common;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * @author boto
 * Date of creation February 10, 2018
 */
class EmailBodyCreatorTest {

    final static String TEMPLATE = "This is a @PLACEHOLDER_A@. This is another @PLACEHOLDER_B@";

    Map<String, String> placeHoldersComplete;

    Map<String, String> placeHoldersPartial;

    @BeforeEach
    void setup() {
        placeHoldersComplete = new HashMap<>();
        placeHoldersComplete.put("@PLACEHOLDER_A@", "VALUE_A");
        placeHoldersComplete.put("@PLACEHOLDER_B@", "VALUE_B");

        placeHoldersPartial = new HashMap<>();
        placeHoldersPartial.put("@PLACEHOLDER_A@", "VALUE_A");
    }

    @Test
    void invalidInputs() {
        try {
            EmailBodyCreator.create(null, placeHoldersComplete);
            fail("An invalid input was not detected!");
        }
        catch(RuntimeException ex) {}
    }

    @Test
    void noPlaceholders() {
        String body = EmailBodyCreator.create(TEMPLATE, null);
        assertThat(body).contains("@PLACEHOLDER_A@");
        assertThat(body).contains("@PLACEHOLDER_B@");
    }

    @Test
    void creationAllPlaceHolders() {
        String body = EmailBodyCreator.create(TEMPLATE, placeHoldersComplete);
        assertThat(body).contains("VALUE_A");
        assertThat(body).contains("VALUE_B");
    }

    @Test
    void creationPartialPlaceHolders() {
        String body = EmailBodyCreator.create(TEMPLATE, placeHoldersPartial);
        assertThat(body).contains("VALUE_A");
        assertThat(body).doesNotContain("VALUE_B");
    }
}
