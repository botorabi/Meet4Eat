package net.m4e.tests;

/**
 * @author ybroeker
 */
public class EntityAssertions {

    public static <T> EntityAssert<T> assertThat(Class<T> actual) {
        return new EntityAssert<>(actual);
    }
}
