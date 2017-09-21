/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include "events.h"

namespace m4e
{
namespace data
{

Events::Events()
{
}

bool Events::updateEvent( ModelEventPtr event )
{
    QList< ModelEventPtr >::iterator ev = _events.begin();
    for ( ; ev != _events.end(); ++ev )
    {
        if ( ( *ev )->getId() == event->getId() )
        {
            *ev = event;
            return true;
        }
    }
    return false;
}

bool Events::removeEvent( const QString& id )
{
    QList< ModelEventPtr >::iterator ev = _events.begin();
    for ( ; ev != _events.end(); ++ev )
    {
        if ( ( *ev )->getId() == id )
        {
            _events.erase( ev );
            return true;
        }
    }
    return false;
}

ModelEventPtr Events::getEvent( const QString& eventId )
{
    ModelEventPtr event;
    QList< ModelEventPtr >::const_iterator ev = _events.begin();
    for ( ; ev != _events.end(); ++ev )
    {
        if ( ( *ev )->getId() == eventId )
        {
            event = *ev;
            break;
        }
    }
    return event;
}

} // namespace data
} // namespace m4e
