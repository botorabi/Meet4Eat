/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.tests.entity;

import javax.json.bind.annotation.JsonbTransient;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;

import static org.junit.Assert.fail;

/**
 * @author boto
 * Date of creation February 8, 2018
 */
public class EntityIsInstanceOfMeTester<T> extends EntityTestBase<T> {

    private final static String METHOD_NAME = "isInstanceOfMe";

    private final Method isInstanceOfMeMethod;

    public EntityIsInstanceOfMeTester(final Class<T> actual) {
        super(actual);
        isInstanceOfMeMethod = getMethod();
    }

    private Method getMethod() {
        try {
            return findMethodByName(METHOD_NAME);
        } catch (NoSuchMethodException e) {
            failWithMessage("\nEntity of Class:\n  <%s>\nis missing the method <%s>", getActual().getSimpleName(), METHOD_NAME);
        }
        return null;
    }

    public void verifyAll() {
        checkAnnotation();
        checkReturnType();
        checkProperEntityTypeProofing();
    }

    private void checkAnnotation() {
        Annotation annotation = isInstanceOfMeMethod.getAnnotation(JsonbTransient.class);
        if (annotation == null) {
            failWithMessage("\nEntity of Class:\n  <%s>\nis missing the method <%s> with proper annotation:\n  <@JsonbTransient>", getActual().getSimpleName(), METHOD_NAME);
        }
    }

    private void checkReturnType() {
        Object expectedReturnType = Boolean.TYPE;
        Object returnValue = isInstanceOfMeMethod.getReturnType();
        if (!returnValue.equals(expectedReturnType)) {
            failWithMessage("\nEntity of Class:\n  <%s>\nmethod <%s> must return a boolean", getActual().getSimpleName(), METHOD_NAME);
        }
    }

    private void checkProperEntityTypeProofing() {
        Object wrongType = new Object();
        Object rightType = createInstance();

        if (invokeMethod(wrongType) || !invokeMethod(rightType)) {

            String classSimpleName = getActual().getSimpleName();

            failWithMessage("\nEntities of Class:\n  <%s>\nmethod <%s> do not properly check the entity type!\n" +
                    "Suggestion: use this check 'object instanceof %s;'", classSimpleName, METHOD_NAME, classSimpleName);
        }
    }

    private boolean invokeMethod(Object argument) {
        try {
            return (Boolean) isInstanceOfMeMethod.invoke(createInstance(), argument);
        } catch(InvocationTargetException | IllegalAccessException | IllegalArgumentException ex) {
            fail("Could not access method " + METHOD_NAME + ", reason: " + ex.getMessage());
        }
        throw new RuntimeException();
    }
}
