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
#include <webapp/request/rest-event.h>
#include <event/modelevent.h>
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
static event::ModelEventPtr createEvent( const QJsonObject& json )
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

    event::ModelEventPtr ev = new event::ModelEvent();
    ev->setId( id );
    ev->setName( name );
    ev->setDescription( desc );
    ev->setIsPublic( ispublic );
    ev->setPhotoId( photoid );
    ev->setPhotoETag( photoetag );
    ev->setRepeatWeekDays( static_cast< uint >( repweekdays ) );
    if ( eventstart > 0 )
    {
        QDateTime start;
        //! NOTE the eventStart is in seconds, not in milliseconds!
        start.setTime_t( static_cast< uint >( eventstart ) );
        ev->setStartDate( start );
    }
    uint hour = static_cast< uint >( repdaytime / ( 60 * 60 ) );
    uint min = static_cast< uint >( repdaytime / 60 ) - hour * 60;
    QTime reptime( hour, min, 0 );
    ev->setRepeatDayTime( reptime );

    // extract the locations
    QList< event::ModelLocationPtr > locs;
    for ( int i = 0; i < locations.size(); i++ )
    {
        QJsonObject obj = locations.at( i ).toObject();
        QString id        = QString::number( obj.value( "id" ).toInt() );
        QString name      = obj.value( "name" ).toString( "" );
        QString desc      = obj.value( "description" ).toString( "" );
        QString photoid   = QString::number( obj.value( "photoId" ).toInt() );
        QString photoetag = obj.value( "photoETag" ).toString( "" );

        event::ModelLocationPtr l = new event::ModelLocation();
        l->setId( id );
        l->setName( name );
        l->setDescription( desc );
        l->setPhotoId( photoid );
        l->setPhotoETag( photoetag );

        locs.append( l );
    }
    ev->setLocations( locs );

    // extract the members
    QList< user::ModelUserInfoPtr > mems;
    for ( int i = 0; i < members.size(); i++ )
    {
        QJsonObject obj = members.at( i ).toObject();
        user::ModelUserInfoPtr u = new user::ModelUserInfo();
        if ( !u->fromJSON( QJsonDocument( obj ) ) )
        {
            log_warning << "createEvent " << "invalid JSON format detected, ignoring search result!" << std::endl;
        }
        else
        {
            mems.append( u );
        }
    }
    ev->setMembers( mems );

    user::ModelUserInfoPtr owner = new user::ModelUserInfo();
    owner->setId( ownerid );
    owner->setName( ownername );
    owner->setPhotoId( ownerphotoid );
    owner->setPhotoETag( ownerphotoetag );
    ev->setOwner( owner );

    return ev;
}

/******************************************************/
/***************** ResponseGetEvents ******************/
/******************************************************/

ResponseGetEvents::ResponseGetEvents( RESTEvent* p_requester ) :
 _p_requester( p_requester )
{}

void ResponseGetEvents::onRESTResponseSuccess( const QJsonDocument& results )
{
    QJsonDocument datadoc;
    QString       errstring;
    QString       errcode;
    bool res = checkStatus( results, datadoc, errcode, errstring );
    if ( !res )
    {
        emit _p_requester->onRESTEventErrorGetEvents( errcode, errstring );
        return;
    }

    QList< m4e::event::ModelEventPtr > events;
    QJsonArray objects = datadoc.array();
    for ( int i = 0; i < objects.size(); i++ )
    {
        QJsonObject obj = objects.at( i ).toObject();
        m4e::event::ModelEventPtr event = createEvent( obj );
        events.append( event );
    }

    emit _p_requester->onRESTEventGetEvents( events );
}

void ResponseGetEvents::onRESTResponseError( const QString& reason )
{
    emit _p_requester->onRESTEventErrorGetEvents( "", reason );
}

/******************************************************/
/**************** ResponseUpdateEvent *****************/
/******************************************************/

ResponseUpdateEvent::ResponseUpdateEvent( RESTEvent* p_requester ) :
 _p_requester( p_requester )
{}

void ResponseUpdateEvent::onRESTResponseSuccess( const QJsonDocument& results )
{
    QJsonDocument datadoc;
    QString       errstring;
    QString       errcode;
    bool res = checkStatus( results, datadoc, errcode, errstring );
    if ( !res )
    {
        emit _p_requester->onRESTEventErrorUpdateEvent( errcode, errstring );
        return;
    }

    QJsonObject obj  = datadoc.object();
    QString eventid  = QString::number( obj.value( "id" ).toInt() );
    emit _p_requester->onRESTEventUpdateEvent( eventid );
}

void ResponseUpdateEvent::onRESTResponseError( const QString& reason )
{
    emit _p_requester->onRESTEventErrorUpdateEvent( "", reason );
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
        emit _p_requester->onRESTEventErrorGetEvent( errcode, errstring );
        return;
    }

    emit _p_requester->onRESTEventGetEvent( createEvent( datadoc.object() ) );
}

void ResponseGetEvent::onRESTResponseError( const QString& reason )
{
    emit _p_requester->onRESTEventErrorGetEvent( "", reason );
}

/******************************************************/
/************** ResponseEventAddMember ****************/
/******************************************************/

ResponseEventAddMember::ResponseEventAddMember( RESTEvent* p_requester ) :
 _p_requester( p_requester )
{}

void ResponseEventAddMember::onRESTResponseSuccess( const QJsonDocument& results )
{
    QJsonDocument datadoc;
    QString       errstring;
    QString       errcode;
    bool res = checkStatus( results, datadoc, errcode, errstring );
    if ( !res )
    {
        emit _p_requester->onRESTEventErrorAddMember( errcode, errstring );
        return;
    }

    QJsonObject obj  = datadoc.object();
    QString eventid  = QString::number( obj.value( "eventId" ).toInt() );
    QString memberid = QString::number( obj.value( "memberId" ).toInt() );

    emit _p_requester->onRESTEventAddMember( eventid, memberid );
}

void ResponseEventAddMember::onRESTResponseError( const QString& reason )
{
    emit _p_requester->onRESTEventErrorAddMember( "", reason );
}

/******************************************************/
/************* ResponseEventRemoveMember **************/
/******************************************************/

ResponseEventRemoveMember::ResponseEventRemoveMember( RESTEvent* p_requester ) :
 _p_requester( p_requester )
{}

void ResponseEventRemoveMember::onRESTResponseSuccess( const QJsonDocument& results )
{
    QJsonDocument datadoc;
    QString       errstring;
    QString       errcode;
    bool res = checkStatus( results, datadoc, errcode, errstring );
    if ( !res )
    {
        emit _p_requester->onRESTEventErrorRemoveMember( errcode, errstring );
        return;
    }

    QJsonObject obj  = datadoc.object();
    QString eventid  = QString::number( obj.value( "eventId" ).toInt() );
    QString memberid = QString::number( obj.value( "memberId" ).toInt() );

    emit _p_requester->onRESTEventRemoveMember( eventid, memberid );
}

void ResponseEventRemoveMember::onRESTResponseError( const QString& reason )
{
    emit _p_requester->onRESTEventErrorRemoveMember( "", reason );
}

} // namespace webapp
} // namespace m4e
