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

void RESTEvent::createEvent( event::ModelEventPtr event )
{
    //! TODO move the json related conversion to the event model

    QJsonObject obj;
    obj.insert( "name",           event->getName() );
    obj.insert( "description",    event->getDescription() );
    obj.insert( "public",         event->getIsPublic() );
    obj.insert( "eventStart",     event->getStartDate().toSecsSinceEpoch() );
    obj.insert( "repeatDayTime",  event->getRepeatDayTime().msecsSinceStartOfDay() / 1000 );
    obj.insert( "repeatWeekDays", int( event->getRepeatWeekDays() ) );
    QJsonDocument json( obj );

    QUrl url( getResourcePath() + "/rest/events/create" );
    auto p_callback = new ResponseNewEvent( this );
    getRESTOps()->POST( url, createResultsCallback( p_callback ), json );
}

void RESTEvent::deleteEvent( const QString& eventId )
{
    QUrl url( getResourcePath() + "/rest/events/" + eventId );
    auto p_callback = new ResponseDeleteEvent( this );
    getRESTOps()->DELETE( url, createResultsCallback( p_callback ) );
}

void RESTEvent::updateEvent( event::ModelEventPtr event )
{
    QJsonObject obj;
    obj.insert( "name",           event->getName() );
    obj.insert( "description",    event->getDescription() );
    obj.insert( "public",         event->getIsPublic() );
    obj.insert( "eventStart",     event->getStartDate().toSecsSinceEpoch() );
    obj.insert( "repeatDayTime",  event->getRepeatDayTime().msecsSinceStartOfDay() / 1000 );
    obj.insert( "repeatWeekDays", int( event->getRepeatWeekDays() ) );

    // was the photo updated?
    if ( event->getUpdatedPhoto().valid() )
    {
        obj.insert( "photo", QString( event->getUpdatedPhoto()->getContent() ) );
    }

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

void RESTEvent::getLocation( const QString& eventId, const QString& locationId )
{
    QUrl url( getResourcePath() + "/rest/events/location/" + eventId + "/" + locationId );
    auto p_callback = new ResponseEventGetLocation( this );
    getRESTOps()->GET( url, createResultsCallback( p_callback ) );
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

void RESTEvent::updateLocation(const QString& eventId, event::ModelLocationPtr location)
{
    //! NOTE the REST service is the same as for adding a new event location, but the location is expected to hava a valid ID here
    QUrl url( getResourcePath() + "/rest/events/putlocation/" + eventId );
    auto p_callback = new ResponseEventUpdateLocation( this );
    getRESTOps()->PUT( url, createResultsCallback( p_callback ), location->toJSON() );
}

} // namespace webapp
} // namespace m4e
