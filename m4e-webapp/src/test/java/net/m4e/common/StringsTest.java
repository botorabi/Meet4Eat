package net.m4e.common;


import org.junit.Test;

import static net.m4e.common.Strings.checkMinMaxLength;
import static net.m4e.common.Strings.limitStringLen;
import static org.junit.Assert.*;


public class StringsTest {

    @Test
    public void limitStringLen_null() {
        assertEquals(limitStringLen(null, 0), null);
    }

    @Test
    public void limitStringLen_longer() {
        assertEquals(limitStringLen("Test", 3), "Tes");
    }

    @Test
    public void limitStringLen_equal() {
        assertEquals(limitStringLen("Test", 4), "Test");
    }

    @Test
    public void limitStringLen_shorter() {
        assertEquals(limitStringLen("Test", 5), "Test");
    }


    @Test
    public void checkMinMaxLength_shorterThanMin() {
        assertFalse(checkMinMaxLength("Test", 5, 10));
    }

    @Test
    public void checkMinMaxLength_equalMin() {
        assertTrue(checkMinMaxLength("Test", 4, 10));
    }

    @Test
    public void checkMinMaxLength_longerThanMin() {
        assertTrue(checkMinMaxLength("Test", 3, 10));
    }

    @Test
    public void checkMinMaxLength_shorterThanMax() {
        assertTrue(checkMinMaxLength("Test", 0, 5));
    }

    @Test
    public void checkMinMaxLength_equalMax() {
        assertTrue(checkMinMaxLength("Test", 0, 4));
    }

    @Test
    public void checkMinMaxLength_longerThanMax() {
        assertFalse(checkMinMaxLength("Test", 0, 3));
    }
}
