/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.common;

/**
 * A collection of string related utilities
 *
 * @author boto
 * @since Sep 5, 2017
 */
public class Strings {

    /**
     * Given an input string, limit its length. The method also handles null as input.
     *
     * @param input  the input string
     * @param maxLen the maximal character length
     * @return Return length limited string, or null if the input was null.
     */
    public static String limitStringLen(final String input, final int maxLen) {
        if (input == null) {
            return null;
        }
        if (input.length() > maxLen) {
            return input.substring(0, maxLen);
        }
        return input;
    }

    /**
     * Check if the input string length is in given range.
     *
     * @param input  the string to test
     * @param minLen the min length, inclusive
     * @param maxLen the max length, inclusive
     * @return true if the string has a length in given range, otherwise return false.
     */
    public static boolean checkMinMaxLength(final String input, final int minLen, final int maxLen) {
        if (input == null) {
            return false;
        }
        final int len = input.length();
        return (len >= minLen) && (len <= maxLen);
    }
}
