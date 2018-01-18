/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.mailbox.business;

import java.util.stream.Stream;

import net.m4e.app.mailbox.business.MailOperation.MailOperationAdapter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

/**
 * @author ybroeker
 */
class MailOperationTest {

    @ParameterizedTest
    @ArgumentsSource(OperationProvider.class)
    void fromString(String str, MailOperation operation) {
        Assertions.assertThat(MailOperation.fromString(str)).isEqualTo(operation);
    }

    @Nested
    class MailOperationAdapterTest {
        MailOperationAdapter adapter = new MailOperationAdapter();

        @ParameterizedTest
        @ArgumentsSource(OperationProvider.class)
        void fromJson(String str, MailOperation operation) {
            Assertions.assertThat(adapter.adaptFromJson(str)).isEqualTo(operation);
        }

        @ParameterizedTest
        @ArgumentsSource(OperationProvider.class)
        void toJson(String str, MailOperation operation) {
            Assertions.assertThat(adapter.adaptToJson(operation)).isEqualTo(str);
        }

    }

    static class OperationProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) {
            return Stream.of(
                    Arguments.of("trash", MailOperation.TRASH),
                    Arguments.of("read", MailOperation.READ),
                    Arguments.of("unread", MailOperation.UNREAD),
                    Arguments.of("untrash", MailOperation.UNTRASH)

            );
        }
    }
}
