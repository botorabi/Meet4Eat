/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.common;

import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.stubbing.Answer;

import java.security.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;

/**
 * @author boto
 * Date of creation February 10, 2018
 */
class HashCreatorTest {

    final String CONTENT = "This is a content for hashing";

    @Test
    void createSHA256() throws Exception {
        assertThat(HashCreator.createSHA256(CONTENT.getBytes())).isNotEmpty();
    }

    @Test
    void createSHA512() throws Exception {
        assertThat(HashCreator.createSHA512(CONTENT.getBytes())).isNotEmpty();
    }
}
