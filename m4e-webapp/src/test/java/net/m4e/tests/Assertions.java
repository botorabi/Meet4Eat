package net.m4e.tests;

import net.m4e.common.GenericResponseResult;

/**
 * @author ybroeker
 */
public class Assertions {

    public static <T> ResponseAssert<T> assertThat(GenericResponseResult<T> actual) {
        return new ResponseAssert<>(actual);
    }
}
