/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef TESTEVENTS_H
#define TESTEVENTS_H

#include <QtTest/QtTest>


class TestEvents : public QObject
{
    Q_OBJECT

    private slots:

        void        initTestCase();

        void        cleanupTestCase();

        /** ##################### **/

        void        testLocation();

        void        testEvent();

        void        testManipulateEvent();
};

#endif // TESTEVENTS_H
