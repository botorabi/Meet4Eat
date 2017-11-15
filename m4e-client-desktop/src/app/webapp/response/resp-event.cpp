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
#include <event/modellocationvotes.h>
#include <QJsonObject>
#include <QJsonArray>


namespace m4e
{
namespace webapp
{

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
        m4e::event::ModelEventPtr event = new m4e::event::ModelEvent();
        if ( event->fromJSON( QJsonDocument( obj ) ) )
            events.append( event );
    }

    emit _p_requester->onRESTEventGetEvents( events );
}

void ResponseGetEvents::onRESTResponseError( const QString& reason )
{
    emit _p_requester->onRESTEventErrorGetEvents( "", reason );
}

/******************************************************/
/****************** ResponseNewEvent ******************/
/******************************************************/

ResponseNewEvent::ResponseNewEvent( RESTEvent* p_requester ) :
 _p_requester( p_requester )
{}

void ResponseNewEvent::onRESTResponseSuccess( const QJsonDocument& results )
{
    QJsonDocument datadoc;
    QString       errstring;
    QString       errcode;
    bool res = checkStatus( results, datadoc, errcode, errstring );
    if ( !res )
    {
        emit _p_requester->onRESTEventErrorNewEvent( errcode, errstring );
        return;
    }

    QJsonObject obj  = datadoc.object();
    QString eventid  = obj.value( "id" ).toString( "" );
    emit _p_requester->onRESTEventNewEvent( eventid );
}

void ResponseNewEvent::onRESTResponseError( const QString& reason )
{
    emit _p_requester->onRESTEventErrorNewEvent( "", reason );
}

/******************************************************/
/***************** ResponseDeleteEvent ****************/
/******************************************************/

ResponseDeleteEvent::ResponseDeleteEvent( RESTEvent* p_requester ) :
 _p_requester( p_requester )
{}

void ResponseDeleteEvent::onRESTResponseSuccess( const QJsonDocument& results )
{
    QJsonDocument datadoc;
    QString       errstring;
    QString       errcode;
    bool res = checkStatus( results, datadoc, errcode, errstring );
    if ( !res )
    {
        emit _p_requester->onRESTEventErrorDeleteEvent( errcode, errstring );
        return;
    }

    QJsonObject obj  = datadoc.object();
    QString eventid  = obj.value( "id" ).toString( "" );
    emit _p_requester->onRESTEventDeleteEvent( eventid );
}

void ResponseDeleteEvent::onRESTResponseError( const QString& reason )
{
    emit _p_requester->onRESTEventErrorDeleteEvent( "", reason );
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
    QString eventid  = obj.value( "id" ).toString( "" );
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

    m4e::event::ModelEventPtr event = new m4e::event::ModelEvent();
    if ( event->fromJSON( datadoc ) )
    {
        emit _p_requester->onRESTEventGetEvent( event );
    }
    else
    {
        emit _p_requester->onRESTEventErrorGetEvent( "", "Invalid format!" );
    }
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
    QString eventid  = obj.value( "eventId" ).toString( "" );
    QString memberid = obj.value( "memberId" ).toString( "" );

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
    QString eventid  = obj.value( "eventId" ).toString( "" );
    QString memberid = obj.value( "memberId" ).toString( "" );

    emit _p_requester->onRESTEventRemoveMember( eventid, memberid );
}

void ResponseEventRemoveMember::onRESTResponseError( const QString& reason )
{
    emit _p_requester->onRESTEventErrorRemoveMember( "", reason );
}

/******************************************************/
/************* ResponseEventGetLocation ***************/
/******************************************************/

ResponseEventGetLocation::ResponseEventGetLocation( RESTEvent* p_requester ) :
 _p_requester( p_requester )
{
}

void ResponseEventGetLocation::onRESTResponseSuccess( const QJsonDocument& results )
{
    QJsonDocument datadoc;
    QString       errstring;
    QString       errcode;
    bool res = checkStatus( results, datadoc, errcode, errstring );
    if ( !res )
    {
        emit _p_requester->onRESTEventErrorGetLocation( errcode, errstring );
        return;
    }

    event::ModelLocationPtr location = new event::ModelLocation();
    if ( !location->fromJSON( datadoc ) )
    {
        log_warning << TAG << "invalid JSON format detected, ignoring location data!" << std::endl;
        emit _p_requester->onRESTEventErrorGetLocation( "", "Invalid location format" );
    }
    else
    {
        emit _p_requester->onRESTEventGetLocation( location );
    }
}

void ResponseEventGetLocation::onRESTResponseError( const QString& reason )
{
    emit _p_requester->onRESTEventErrorGetLocation( "", reason );
}

/******************************************************/
/************* ResponseEventAddLocation ***************/
/******************************************************/

ResponseEventAddLocation::ResponseEventAddLocation( RESTEvent* p_requester ) :
 _p_requester( p_requester )
{
}

void ResponseEventAddLocation::onRESTResponseSuccess( const QJsonDocument& results )
{
    QJsonDocument datadoc;
    QString       errstring;
    QString       errcode;
    bool res = checkStatus( results, datadoc, errcode, errstring );
    if ( !res )
    {
        emit _p_requester->onRESTEventErrorAddLocation( errcode, errstring );
        return;
    }

    QJsonObject obj    = datadoc.object();
    QString eventid    = obj.value( "eventId" ).toString( "" );
    QString locationid = obj.value( "locationId" ).toString( "" );

    emit _p_requester->onRESTEventAddLocation( eventid, locationid );
}

void ResponseEventAddLocation::onRESTResponseError( const QString& reason )
{
    emit _p_requester->onRESTEventErrorAddLocation( "", reason );
}

/******************************************************/
/************ ResponseEventRemoveLocation *************/
/******************************************************/

ResponseEventRemoveLocation::ResponseEventRemoveLocation( RESTEvent* p_requester ) :
 _p_requester( p_requester )
{
}

void ResponseEventRemoveLocation::onRESTResponseSuccess( const QJsonDocument& results )
{
    QJsonDocument datadoc;
    QString       errstring;
    QString       errcode;
    bool res = checkStatus( results, datadoc, errcode, errstring );
    if ( !res )
    {
        emit _p_requester->onRESTEventErrorRemoveLocation( errcode, errstring );
        return;
    }

    QJsonObject obj    = datadoc.object();
    QString eventid    = obj.value( "eventId" ).toString( "" );
    QString locationid = obj.value( "locationId" ).toString( "" );

    emit _p_requester->onRESTEventRemoveLocation( eventid, locationid );
}

void ResponseEventRemoveLocation::onRESTResponseError( const QString& reason )
{
    emit _p_requester->onRESTEventErrorRemoveLocation( "", reason );
}

/******************************************************/
/************ ResponseEventUpdateLocation *************/
/******************************************************/

ResponseEventUpdateLocation::ResponseEventUpdateLocation( RESTEvent* p_requester ) :
 _p_requester( p_requester )
{
}

void ResponseEventUpdateLocation::onRESTResponseSuccess( const QJsonDocument& results )
{
    QJsonDocument datadoc;
    QString       errstring;
    QString       errcode;
    bool res = checkStatus( results, datadoc, errcode, errstring );
    if ( !res )
    {
        emit _p_requester->onRESTEventErrorUpdateLocation( errcode, errstring );
        return;
    }

    QJsonObject obj    = datadoc.object();
    QString eventid    = obj.value( "eventId" ).toString( "" );
    QString locationid = obj.value( "locationId" ).toString( "" );

    emit _p_requester->onRESTEventUpdateLocation( eventid, locationid );
}

void ResponseEventUpdateLocation::onRESTResponseError( const QString& reason )
{
    emit _p_requester->onRESTEventErrorUpdateLocation( "", reason );
}

/******************************************************/
/************ ResponseEventSetLocationVote ************/
/******************************************************/

ResponseEventSetLocationVote::ResponseEventSetLocationVote( RESTEvent* p_requester ) :
 _p_requester( p_requester )
{
}

void ResponseEventSetLocationVote::onRESTResponseSuccess( const QJsonDocument& results )
{
    QJsonDocument datadoc;
    QString       errstring;
    QString       errcode;
    bool res = checkStatus( results, datadoc, errcode, errstring );
    if ( !res )
    {
        emit _p_requester->onRESTEventErrorSetLocationVote( errcode, errstring );
        return;
    }
    QJsonObject obj     = datadoc.object();
    QString votesid     = obj.value( "votesId" ).toString( "" );
    QString eventid     = obj.value( "eventId" ).toString( "" );
    QString locationid  = obj.value( "locationId" ).toString( "" );
    bool    vote        = obj.value( "vote" ).toBool( false );

    emit _p_requester->onRESTEventSetLocationVote( eventid, locationid, votesid, vote );
}

void ResponseEventSetLocationVote::onRESTResponseError( const QString& reason )
{
    emit _p_requester->onRESTEventErrorSetLocationVote( "", reason );
}

/******************************************************/
/******** ResponseEventGetLocationVotesByTime *********/
/******************************************************/

ResponseEventGetLocationVotesByTime::ResponseEventGetLocationVotesByTime( RESTEvent* p_requester ) :
 _p_requester( p_requester )
{
}

void ResponseEventGetLocationVotesByTime::onRESTResponseSuccess( const QJsonDocument& results )
{
    QJsonDocument datadoc;
    QString       errstring;
    QString       errcode;
    bool res = checkStatus( results, datadoc, errcode, errstring );
    if ( !res )
    {
        emit _p_requester->onRESTEventErrorGetLocationVotesByTime( errcode, errstring );
        return;
    }

    QList< event::ModelLocationVotesPtr > votes;
    QJsonArray votelist = datadoc.array();
    for ( int i = 0; i < votelist.size(); i++ )
    {
        event::ModelLocationVotesPtr v = new event::ModelLocationVotes();
        QJsonObject obj = votelist.at( i ).toObject();
        if ( v->fromJSON( QJsonDocument( obj ) ) )
        {
           votes.append( v );
        }
        else
        {
            log_warning << TAG << "cannot import votes, invalid format!" << std::endl;
        }
    }
    emit _p_requester->onRESTEventGetLocationVotesByTime( votes );
}

void ResponseEventGetLocationVotesByTime::onRESTResponseError( const QString& reason )
{
    emit _p_requester->onRESTEventErrorGetLocationVotesByTime( "", reason );
}

/******************************************************/
/********* ResponseEventGetLocationVotesById **********/
/******************************************************/

ResponseEventGetLocationVotesById::ResponseEventGetLocationVotesById( RESTEvent* p_requester ) :
 _p_requester( p_requester )
{
}

void ResponseEventGetLocationVotesById::onRESTResponseSuccess( const QJsonDocument& results )
{
    QJsonDocument datadoc;
    QString       errstring;
    QString       errcode;
    bool res = checkStatus( results, datadoc, errcode, errstring );
    if ( !res )
    {
        emit _p_requester->onRESTEventErrorGetLocationVotesById( errcode, errstring );
        return;
    }

    QJsonObject obj = datadoc.object();
    event::ModelLocationVotesPtr votes = new event::ModelLocationVotes();
    if ( votes->fromJSON( QJsonDocument( obj ) ) )
    {
        emit _p_requester->onRESTEventGetLocationVotesById( votes );
    }
    else
    {
        log_warning << TAG << "cannot import votes, invalid format!" << std::endl;
    }
}

void ResponseEventGetLocationVotesById::onRESTResponseError( const QString& reason )
{
    emit _p_requester->onRESTEventErrorGetLocationVotesById( "", reason );
}

} // namespace webapp
} // namespace m4e
