/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.tests;

import java.util.*;

/**
 * @author ybroeker
 */
public class AssertionStack {

    public static void removePathFromStacktrace(AssertionError assertionError, String path) {
        List<StackTraceElement> filtered = getStacktraceAsList(assertionError);

        for (StackTraceElement element : assertionError.getStackTrace()) {
            if (element.getClassName().contains(path)) {
                filtered.remove(element);
            }
        }

        setStacktraceFromList(assertionError, filtered);
    }

    public static void removeTopEntriesFromStacktrace(AssertionError assertionError, int countRemovals) {
        List<StackTraceElement> filtered = getStacktraceAsList(assertionError);
        int maxRemoval = Math.min(countRemovals, filtered.size());

        for(int i = 0; i < maxRemoval; i++) {
            filtered.remove(0);
        }

        setStacktraceFromList(assertionError, filtered);
    }

    private static List<StackTraceElement> getStacktraceAsList(AssertionError assertionError) {
        List<StackTraceElement> trace = new ArrayList<>(assertionError.getStackTrace().length);
        java.util.Collections.addAll(trace, assertionError.getStackTrace());
        return trace;
    }

    private static void setStacktraceFromList(AssertionError assertionError, List<StackTraceElement> trace) {
        StackTraceElement[] newStackTrace = trace.toArray(new StackTraceElement[trace.size()]);
        assertionError.setStackTrace(newStackTrace);
    }
}
