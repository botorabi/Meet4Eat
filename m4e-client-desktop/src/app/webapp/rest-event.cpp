/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include <core/log.h>
#include "rest-event.h"
#include "resultshandler/resp-event.h"


namespace m4e
{
namespace webapp
{

RESTEvent::RESTEvent( QObject* p_parent ) :
 Meet4EatREST( p_parent )
{
}

RESTEvent::~RESTEvent()
{
}

void RESTEvent::getAllEvents()
{
    QUrl url( getResourcePath() + "/rest/events/" );
    auto p_callback = new ResponseGetAllEvents( this );
    getRESTOps()->GET( url, createResultsCallback( p_callback ) );
}

void RESTEvent::getEvent( const QString& eventId )
{
    QUrl url( getResourcePath() + "/rest/events/" + eventId );
    auto p_callback = new ResponseGetEvent( this );
    getRESTOps()->GET( url, createResultsCallback( p_callback ) );
}

} // namespace webapp
} // namespace m4e
