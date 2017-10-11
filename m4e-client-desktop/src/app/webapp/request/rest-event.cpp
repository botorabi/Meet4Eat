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
    QUrl url( getResourcePath() + "/rest/events" );
    auto p_callback = new ResponseGetEvents( this );
    getRESTOps()->GET( url, createResultsCallback( p_callback ) );
}

void RESTEvent::getEvent( const QString& eventId )
{
    QUrl url( getResourcePath() + "/rest/events/" + eventId );
    auto p_callback = new ResponseGetEvent( this );
    getRESTOps()->GET( url, createResultsCallback( p_callback ) );
}

void RESTEvent::updateEvent( event::ModelEventPtr event )
{
    //! NOTE we do not request for setting the photo here!
    QJsonObject obj;
    obj.insert( "name",           event->getName() );
    obj.insert( "description",    event->getDescription() );
    obj.insert( "public",         event->getIsPublic() );
    obj.insert( "eventStart",     event->getStartDate().toSecsSinceEpoch() );
    obj.insert( "repeatDayTime",  event->getRepeatDayTime().msecsSinceStartOfDay() / 1000 );
    obj.insert( "repeatWeekDays", int( event->getRepeatWeekDays() ) );
    QJsonDocument json( obj );

    QUrl url( getResourcePath() + "/rest/events/" + event->getId() );
    auto p_callback = new ResponseUpdateEvent( this );
    getRESTOps()->PUT( url, createResultsCallback( p_callback ), json );
}

void RESTEvent::addMember( const QString& eventId, const QString& memberId )
{
    QUrl url( getResourcePath() + "/rest/events/addmember/" + eventId + "/" + memberId );
    auto p_callback = new ResponseEventAddMember( this );
    getRESTOps()->PUT( url, createResultsCallback( p_callback ) );
}

void RESTEvent::removeMember( const QString& eventId, const QString& memberId )
{
    QUrl url( getResourcePath() + "/rest/events/removemember/" + eventId + "/" + memberId );
    auto p_callback = new ResponseEventRemoveMember( this );
    getRESTOps()->PUT( url, createResultsCallback( p_callback ) );
}

void RESTEvent::addLocation( const QString& eventId, event::ModelLocationPtr location )
{
    QUrl url( getResourcePath() + "/rest/events/putlocation/" + eventId );
    auto p_callback = new ResponseEventAddLocation( this );
    getRESTOps()->PUT( url, createResultsCallback( p_callback ), location->toJSON() );
}

void RESTEvent::removeLocation( const QString& eventId, const QString& locationId )
{
    QUrl url( getResourcePath() + "/rest/events/removelocation/" + eventId + "/" + locationId );
    auto p_callback = new ResponseEventRemoveLocation( this );
    getRESTOps()->POST( url, createResultsCallback( p_callback ) );
}

} // namespace webapp
} // namespace m4e
