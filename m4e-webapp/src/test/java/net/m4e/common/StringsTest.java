/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.common;


import org.junit.jupiter.api.Test;

import static net.m4e.common.Strings.checkMinMaxLength;
import static net.m4e.common.Strings.limitStringLen;
import static org.assertj.core.api.Assertions.assertThat;


class StringsTest {
    @Test
    void limitStringLen_null() {
        assertThat(limitStringLen(null, 0)).isEqualTo(null);
    }

    @Test
    void limitStringLen_longer() {
        assertThat(limitStringLen("Test", 3)).isEqualTo("Tes");
    }

    @Test
    void limitStringLen_equal() {
        assertThat(limitStringLen("Test", 4)).isEqualTo("Test");
    }

    @Test
    void limitStringLen_shorter() {
        assertThat(limitStringLen("Test", 5)).isEqualTo("Test");
    }


    @Test
    void checkMinMaxLength_shorterThanMin() {
        assertThat(checkMinMaxLength("Test", 5, 10)).isFalse();
        assertThat(checkMinMaxLength(null, 5, 10)).isFalse();
    }

    @Test
    void checkMinMaxLength_equalMin() {
        assertThat(checkMinMaxLength("Test", 4, 10)).isTrue();
    }

    @Test
    void checkMinMaxLength_longerThanMin() {
        assertThat(checkMinMaxLength("Test", 3, 10)).isTrue();
    }

    @Test
    void checkMinMaxLength_shorterThanMax() {
        assertThat(checkMinMaxLength("Test", 0, 5)).isTrue();
    }

    @Test
    void checkMinMaxLength_equalMax() {
        assertThat(checkMinMaxLength("Test", 0, 4)).isTrue();
    }

    @Test
    void checkMinMaxLength_longerThanMax() {
        assertThat(checkMinMaxLength("Test", 0, 3)).isFalse();
    }
}
