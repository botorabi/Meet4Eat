/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */


#include <configuration.h>
#include <core/core.h>


int main( int argc, char *argv[] )
{
    m4e::core::Core appcore;
    appcore.initialize( argc, argv );
    appcore.start();
    appcore.shutdown();

    return 0;
}
