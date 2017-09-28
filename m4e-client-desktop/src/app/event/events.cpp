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

void Events::requestGetEvents()
{
    _p_restEvent->getEvents();
}

void Events::requestAddMember(const QString& eventId, const QString& memberId)
{
    _p_restEvent->addMember( eventId, memberId );
}

void Events::onRESTEventGetEvents( QList< event::ModelEventPtr > events )
{
    log_verbose << TAG << "got events: " << QString::number( events.size() ).toStdString() << std::endl;
    _events = events;
    emit onResponseGetEvents( true, events );
}

void Events::onRESTEventErrorGetEvents( QString errorCode, QString reason )
{
    log_verbose << TAG << "failed to get events: " << errorCode.toStdString() << ", reason: " << reason.toStdString() << std::endl;
    emit onResponseGetEvents( false, QList< event::ModelEventPtr >() );
}

void Events::onRESTEventAddMember( QString eventId, QString memberId )
{
    log_verbose << TAG << "new member added to event: " << eventId.toStdString() << "/" << memberId.toStdString() << std::endl;
    emit onResponseAddMember( true, eventId, memberId );
}

void Events::onRESTEventErrorAddMember( QString errorCode, QString reason )
{
    log_verbose << TAG << "failed to add new member to event: " << errorCode.toStdString() << ", reason: " << reason.toStdString() << std::endl;
    emit onResponseAddMember( false, "", "" );
}

} // namespace event
} // namespace m4e
