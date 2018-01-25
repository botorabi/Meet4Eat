package net.m4e.tests;

import java.util.logging.Logger;

/**
 * @author ybroeker
 */
public class EntityAssertions {
    private static final Logger LOG = Logger.getLogger(EntityAssertions.class.getName());

    public static <T> EntityAssert<T> assertThat(Class<T> actual) {
        return new EntityAssert<>(actual);
    }
}
