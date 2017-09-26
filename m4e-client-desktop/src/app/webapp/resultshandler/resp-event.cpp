/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include <core/log.h>
#include "resp-event.h"
#include "../rest-event.h"
#include <data/modelevent.h>
#include <QJsonObject>
#include <QJsonArray>


namespace m4e
{
namespace webapp
{

/**
 * @brief Utility function: Create an event out of given json object.
 *
 * @param json  JSON object containing event information
 * @return      Event model
 */
static data::ModelEventPtr createEvent( const QJsonObject& json )
{
    QString    id             = QString::number( json.value( "id" ).toInt() );
    QString    name           = json.value( "name" ).toString( "" );
    QString    desc           = json.value( "description" ).toString( "" );
    QString    photoid        = QString::number( json.value( "photoId" ).toInt() );
    QString    photoetag      = json.value( "photoETag" ).toString( "" );
    bool       ispublic       = json.value( "public" ).toBool();
    int        eventstart     = json.value( "eventStart" ).toInt( 0 );
    int        repweekdays    = json.value( "repeatWeekDays" ).toInt( 0 );
    int        repdaytime     = json.value( "repeatDayTime" ).toInt( 0 );
    QJsonArray locations      = json.value( "locations" ).toArray();
    QJsonArray members        = json.value( "members" ).toArray();
    QString    ownerid        = QString::number( json.value( "ownerId" ).toInt() );
    QString    ownername      = json.value( "ownerName" ).toString( "" );
    QString    ownerphotoid   = QString::number( json.value( "ownerPhotoId" ).toInt() );
    QString    ownerphotoetag = json.value( "ownerPhotoETag" ).toString( "" );

    data::ModelEventPtr event = new data::ModelEvent();
    event->setId( id );
    event->setName( name );
    event->setDescription( desc );
    event->setIsPublic( ispublic );
    event->setPhotoId( photoid );
    event->setPhotoETag( photoetag );
    event->setRepeatWeekDays( static_cast< uint >( repweekdays ) );
    if ( eventstart > 0 )
    {
        QDateTime start;
        //! NOTE the eventStart is in seconds, not in milliseconds!
        start.setTime_t( static_cast< uint >( eventstart ) );
        event->setStartDate( start );
    }
    uint hour = static_cast< uint >( repdaytime / 60 );
    uint min = static_cast< uint >( repdaytime ) - hour * 60;
    QTime reptime( hour, min, 0 );
    event->setRepeatDayTime( reptime );

    // extract the locations
    QList< data::ModelLocationPtr > locs;
    for ( int i = 0; i < locations.size(); i++ )
    {
        QJsonObject obj = locations.at( i ).toObject();
        QString id        = QString::number( obj.value( "id" ).toInt() );
        QString name      = obj.value( "name" ).toString( "" );
        QString desc      = obj.value( "description" ).toString( "" );
        QString photoid   = QString::number( obj.value( "photoId" ).toInt() );
        QString photoetag = obj.value( "photoETag" ).toString( "" );

        data::ModelLocationPtr l = new data::ModelLocation();
        l->setId( id );
        l->setName( name );
        l->setDescription( desc );
        l->setPhotoId( photoid );
        l->setPhotoETag( photoetag );

        locs.append( l );
    }
    event->setLocations( locs );

    // extract the members
    QList< data::ModelUserInfoPtr > mems;
    for ( int i = 0; i < members.size(); i++ )
    {
        QJsonObject obj = members.at( i ).toObject();
        QString id        = QString::number( obj.value( "id" ).toInt() );
        QString name      = obj.value( "name" ).toString( "" );
        QString photoid   = QString::number( obj.value( "photoId" ).toInt() );
        QString photoetag = obj.value( "photoETag" ).toString( "" );

        data::ModelUserInfoPtr u = new data::ModelUserInfo();
        u->setId( id );
        u->setName( name );
        u->setPhotoId( photoid );
        u->setPhotoETag( photoetag );

        mems.append( u );
    }
    event->setMembers( mems );

    data::ModelUserInfoPtr owner = new data::ModelUserInfo();
    owner->setId( ownerid );
    owner->setName( ownername );
    owner->setPhotoId( ownerphotoid );
    owner->setPhotoETag( ownerphotoetag );
    event->setOwner( owner );

    return event;
}

/******************************************************/
/************* ResponseGetUserAllEvents ***************/
/******************************************************/

ResponseGetAllEvents::ResponseGetAllEvents( RESTEvent* p_requester ) :
 _p_requester( p_requester )
{}

void ResponseGetAllEvents::onRESTResponseSuccess( const QJsonDocument& results )
{
    QJsonDocument datadoc;
    QString       errstring;
    QString       errcode;
    bool res = checkStatus( results, datadoc, errcode, errstring );
    if ( !res )
    {
        emit _p_requester->onRESTEventErrorGetAllEvents( errcode, errstring );
        return;
    }

    QList< m4e::data::ModelEventPtr > events;
    QJsonArray objects = datadoc.array();
    for ( int i = 0; i < objects.size(); i++ )
    {
        QJsonObject obj = objects.at( i ).toObject();
        m4e::data::ModelEventPtr event = createEvent( obj );
        events.append( event );
    }

    emit _p_requester->onRESTEventGetAllEvents( events );
}

void ResponseGetAllEvents::onRESTResponseError( const QString& reason )
{
    emit _p_requester->onRESTEventErrorGetAllEvents( "", reason );
}

/******************************************************/
/*************** ResponseGetUserEvent *****************/
/******************************************************/

ResponseGetEvent::ResponseGetEvent( RESTEvent* p_requester ) :
 _p_requester( p_requester )
{}

void ResponseGetEvent::onRESTResponseSuccess( const QJsonDocument& results )
{
    QJsonDocument datadoc;
    QString       errstring;
    QString       errcode;
    bool res = checkStatus( results, datadoc, errcode, errstring );
    if ( !res )
    {
        emit _p_requester->onRESTEventErrorGetAllEvents( errcode, errstring );
        return;
    }

    emit _p_requester->onRESTEventGetEvent( createEvent( datadoc.object() ) );
}

void ResponseGetEvent::onRESTResponseError( const QString& reason )
{
    emit _p_requester->onRESTEventErrorGetEvent( "", reason );
}

} // namespace webapp
} // namespace m4e
