/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.tests;

import net.m4e.common.GenericResponseResult;

/**
 * @author ybroeker
 */
public class ResponseAssertions extends org.assertj.core.api.Assertions {

    public static <T> ResponseAssert<T> assertThat(GenericResponseResult<T> actual) {
        return new ResponseAssert<>(actual);
    }
}
