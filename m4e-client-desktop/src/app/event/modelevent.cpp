/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include "modelevent.h"

namespace m4e
{
namespace event
{

ModelLocationPtr ModelEvent::getLocation( const QString &id )
{
    ModelLocationPtr location;
    QList< ModelLocationPtr >::iterator loc = _locations.begin();
    for ( ; loc != _locations.end(); ++loc )
    {
        if ( ( *loc )->getId() == id )
        {
            location = *loc;
            break;
        }
    }
    return location;
}

} // namespace event
} // namespace m4e
