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
#include <webapp/response/resp-event.h>


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

void RESTEvent::getEvents()
{
    QUrl url( getResourcePath() + "/rest/events/" );
    auto p_callback = new ResponseGetEvents( this );
    getRESTOps()->GET( url, createResultsCallback( p_callback ) );
}

void RESTEvent::getEvent( const QString& eventId )
{
    QUrl url( getResourcePath() + "/rest/events/" + eventId );
    auto p_callback = new ResponseGetEvent( this );
    getRESTOps()->GET( url, createResultsCallback( p_callback ) );
}

void RESTEvent::addMember( const QString& eventId, const QString& memberId )
{
    QUrl url( getResourcePath() + "/rest/events/addmember/" + eventId + "/" + memberId );
    auto p_callback = new ResponseEventAddMember( this );
    getRESTOps()->GET( url, createResultsCallback( p_callback ) );
}

} // namespace webapp
} // namespace m4e
