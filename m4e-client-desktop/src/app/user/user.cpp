/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include <core/log.h>
#include "user.h"
#include <QApplication>


namespace m4e
{
namespace user
{

User::User( QObject* p_parent ) :
 QObject( p_parent )
{
    _p_restUser  = new m4e::webapp::RESTUser( this );
    _p_restEvent = new m4e::webapp::RESTEvent( this );
    connect( _p_restUser, SIGNAL( onRESTUserGetData( m4e::data::ModelUserPtr ) ), this, SLOT( onRESTUserGetData( m4e::data::ModelUserPtr ) ) );
    connect( _p_restUser, SIGNAL( onRESTUserErrorGetData( QString, QString ) ), this, SLOT( onRESTUserErrorGetData( QString, QString ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventGetAllEvents( QList< m4e::data::ModelEventPtr > ) ), this, SLOT( onRESTEventGetAllEvents( QList< m4e::data::ModelEventPtr > ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventErrorGetAllEvents( QString, QString ) ), this, SLOT( onRESTEventErrorGetAllEvents( QString, QString ) ) );
}

User::~User()
{
}

void User::setServerURL(const QString &url)
{
    _p_restUser->setServerURL( url );
    _p_restEvent->setServerURL( url );
}

const QString& User::getServerURL() const
{
    return _p_restUser->getServerURL();
}

void User::requestUserData( const QString userId )
{
    _p_restUser->getUserData( userId );
}

void User::requestAllEvents()
{
    _p_restEvent->getAllEvents();
}

void User::onRESTUserGetData( m4e::data::ModelUserPtr user )
{
    log_verbose << TAG << "got user data: " << user->getName().toStdString() << std::endl;
    emit onResponseUserData( true, user );
}

void User::onRESTUserErrorGetData( QString errorCode, QString reason )
{
    log_verbose << TAG << "failed to get user data: " << errorCode.toStdString() << ", reason: " << reason.toStdString() << std::endl;
    emit onResponseUserData( false, m4e::data::ModelUserPtr() );
}

void User::onRESTEventGetAllEvents( QList< data::ModelEventPtr > events )
{
    log_verbose << TAG << "got user events: " << QString::number( events.size() ).toStdString() << std::endl;
    emit onResponseUserAllEvents( true, events );
}

void User::onRESTEventErrorGetAllEvents( QString errorCode, QString reason )
{
    log_verbose << TAG << "failed to get user events: " << errorCode.toStdString() << ", reason: " << reason.toStdString() << std::endl;
    emit onResponseUserAllEvents( false, QList< data::ModelEventPtr >() );
}

} // namespace user
} // namespace m4e
