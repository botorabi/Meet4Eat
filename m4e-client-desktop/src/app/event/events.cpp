/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "events.h"
#include <core/log.h>


namespace m4e
{
namespace event
{

Events::Events( QObject* p_parent ) :
 QObject( p_parent )
{
    _p_restEvent = new webapp::RESTEvent( this );
    connect( _p_restEvent, SIGNAL( onRESTEventGetEvents( QList< m4e::event::ModelEventPtr > ) ), this, SLOT( onRESTEventGetEvents( QList< m4e::event::ModelEventPtr > ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventErrorGetEvents( QString, QString ) ), this, SLOT( onRESTEventErrorGetEvents( QString, QString ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventUpdateEvent( QString ) ), this, SLOT( onRESTEventUpdateEvent( QString ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventErrorUpdateEvent( QString, QString ) ), this, SLOT( onRESTEventErrorUpdateEvent( QString, QString ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventAddMember( QString, QString ) ), this, SLOT( onRESTEventAddMember( QString, QString ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventErrorAddMember( QString, QString ) ), this, SLOT( onRESTEventErrorAddMember( QString, QString ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventRemoveMember( QString, QString ) ), this, SLOT( onRESTEventRemoveMember( QString, QString ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventErrorRemoveMember( QString, QString ) ), this, SLOT( onRESTEventErrorRemoveMember( QString, QString ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventAddLocation( QString, QString ) ), this, SLOT( onRESTEventAddLocation( QString, QString ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventErrorAddLocation( QString, QString ) ), this, SLOT( onRESTEventErrorAddLocation( QString, QString ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventRemoveLocation( QString, QString ) ), this, SLOT( onRESTEventRemoveLocation( QString, QString ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventErrorRemoveLocation( QString, QString ) ), this, SLOT( onRESTEventErrorRemoveLocation( QString, QString ) ) );
}

Events::~Events()
{
}

void Events::setServerURL(const QString &url)
{
    _p_restEvent->setServerURL( url );
}

const QString& Events::getServerURL() const
{
    return _p_restEvent->getServerURL();
}

QList< event::ModelEventPtr > Events::getUserEvents()
{
    return _events;
}

//######### Requests ############//

void Events::requestGetEvents()
{
    setLastError();
    _p_restEvent->getEvents();
}

void Events::requestUpdateEvent( ModelEventPtr event )
{
    setLastError();
    _p_restEvent->updateEvent( event );
}

void Events::requestAddMember (const QString& eventId, const QString& memberId )
{
    setLastError();
    _p_restEvent->addMember( eventId, memberId );
}

void Events::requestRemoveMember( const QString& eventId, const QString& memberId )
{
    setLastError();
    _p_restEvent->removeMember( eventId, memberId );
}

void Events::requestAddLocation( const QString& eventId, ModelLocationPtr location )
{
    setLastError();
    _p_restEvent->addLocation( eventId, location );
}

void Events::requestRemoveLocation( const QString& eventId, const QString& locationId )
{
    setLastError();
    _p_restEvent->removeLocation( eventId, locationId );
}

//######### Responses ############//

void Events::onRESTEventGetEvents( QList< event::ModelEventPtr > events )
{
    log_verbose << TAG << "got events: " << QString::number( events.size() ).toStdString() << std::endl;
    _events = events;
    emit onResponseGetEvents( true, events );
}

void Events::onRESTEventErrorGetEvents( QString errorCode, QString reason )
{
    log_verbose << TAG << "failed to get events: " << errorCode.toStdString() << ", reason: " << reason.toStdString() << std::endl;
    setLastError( reason, errorCode );
    emit onResponseGetEvents( false, QList< event::ModelEventPtr >() );
}

void Events::onRESTEventUpdateEvent( QString eventId )
{
    log_verbose << TAG << "event was updated: " << eventId.toStdString() << std::endl;
    emit onResponseUpdateEvent( true, eventId );
}

void Events::onRESTEventErrorUpdateEvent( QString errorCode, QString reason )
{
    log_verbose << TAG << "failed to update event: " << errorCode.toStdString() << ", reason: " << reason.toStdString() << std::endl;
    setLastError( reason, errorCode );
    emit onResponseUpdateEvent( false, "" );
}

void Events::onRESTEventAddMember( QString eventId, QString memberId )
{
    log_verbose << TAG << "new member added to event: " << eventId.toStdString() << "/" << memberId.toStdString() << std::endl;
    emit onResponseAddMember( true, eventId, memberId );
}

void Events::onRESTEventErrorAddMember( QString errorCode, QString reason )
{
    log_verbose << TAG << "failed to add new member to event: " << errorCode.toStdString() << ", reason: " << reason.toStdString() << std::endl;
    setLastError( reason, errorCode );
    emit onResponseAddMember( false, "", "" );
}

void Events::onRESTEventRemoveMember( QString eventId, QString memberId )
{
    log_verbose << TAG << "member removed from event: " << eventId.toStdString() << "/" << memberId.toStdString() << std::endl;
    emit onResponseRemoveMember( true, eventId, memberId );
}

void Events::onRESTEventErrorRemoveMember( QString errorCode, QString reason )
{
    log_verbose << TAG << "failed to remove member from event: " << errorCode.toStdString() << ", reason: " << reason.toStdString() << std::endl;
    setLastError( reason, errorCode );
    emit onResponseRemoveMember( false, "", "" );
}

void Events::onRESTEventAddLocation( QString eventId, QString locationId )
{
    log_verbose << TAG << "new location added to event: " << eventId.toStdString() << "/" << locationId.toStdString() << std::endl;
    emit onResponseAddLocation( true, eventId, locationId );
}

void Events::onRESTEventErrorAddLocation( QString errorCode, QString reason )
{
    log_verbose << TAG << "failed to add new location to event: " << errorCode.toStdString() << ", reason: " << reason.toStdString() << std::endl;
    setLastError( reason, errorCode );
    emit onResponseAddLocation( false, "", "" );
}

void Events::onRESTEventRemoveLocation( QString eventId, QString locationId )
{
    log_verbose << TAG << "location removed from event: " << eventId.toStdString() << "/" << locationId.toStdString() << std::endl;
    emit onResponseRemoveLocation( true, eventId, locationId );
}

void Events::onRESTEventErrorRemoveLocation( QString errorCode, QString reason )
{
    log_verbose << TAG << "failed to remove location from event: " << errorCode.toStdString() << ", reason: " << reason.toStdString() << std::endl;
    setLastError( reason, errorCode );
    emit onResponseRemoveLocation( false, "", "" );
}

void Events::setLastError( const QString& error, const QString& errorCode )
{
    _lastError = error;
    _lastErrorCode = errorCode;
}

} // namespace event
} // namespace m4e
