/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <QApplication>
#include <testevents.h>

int main( int argc, char *argv[] )
{
    QApplication app(argc, argv);
    app.setAttribute( Qt::AA_Use96Dpi, true);
    QTEST_DISABLE_KEYPAD_NAVIGATION
    //QTEST_ADD_GPU_BLACKLIST_SUPPORT
    QTEST_SET_MAIN_SOURCE_PATH

    TestEvents testevents;
    int res = QTest::qExec( &testevents, argc, argv );

    // NOTE add further test cases here...

    return res;
}
