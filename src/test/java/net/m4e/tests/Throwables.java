package net.m4e.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author ybroeker
 */
public class Throwables {
    private static final Logger LOG = Logger.getLogger(Throwables.class.getName());

    static void removeFromStacktrace(AssertionError assertionError, String className) {
        List<StackTraceElement> filtered = new ArrayList<>(assertionError.getStackTrace().length);
        java.util.Collections.addAll(filtered, assertionError.getStackTrace());

        StackTraceElement previous = null;
        for (StackTraceElement element : assertionError.getStackTrace()) {
            if (element.getClassName().contains(className)) {
                filtered.remove(element);
                // Handle the case when AssertJ builds a ComparisonFailure by reflection (see ShouldBeEqual.newAssertionError
                // method), the stack trace looks like:
                //
                // java.lang.reflect.Constructor.newInstance(Constructor.java:501),
                // org.assertj.core.error.ConstructorInvoker.newInstance(ConstructorInvoker.java:34),
                //
                // We want to remove java.lang.reflect.Constructor.newInstance element because it is related to AssertJ.
                //if (previous != null && JAVA_LANG_REFLECT_CONSTRUCTOR.equals(previous.getClassName())
                //        && element.getClassName().contains(ORG_ASSERTJ_CORE_ERROR_CONSTRUCTOR_INVOKER)) {
                //    filtered.remove(previous);
                //}
            }
            previous = element;
        }
        StackTraceElement[] newStackTrace = filtered.toArray(new StackTraceElement[filtered.size()]);
        assertionError.setStackTrace(newStackTrace);
    }

}
