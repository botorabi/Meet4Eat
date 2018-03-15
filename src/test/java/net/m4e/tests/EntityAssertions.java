/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.tests;

/**
 * @author ybroeker
 */
public class EntityAssertions {

    public static <T> EntityAssert<T> assertThat(Class<T> actual) {
        return new EntityAssert<>(actual);
    }
}
