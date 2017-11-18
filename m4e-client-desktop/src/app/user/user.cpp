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
    _p_restUser  = new webapp::RESTUser( this );
    connect( _p_restUser, SIGNAL( onRESTUserGetData( m4e::user::ModelUserPtr ) ), this, SLOT( onRESTUserGetData( m4e::user::ModelUserPtr ) ) );
    connect( _p_restUser, SIGNAL( onRESTUserErrorGetData( QString, QString ) ), this, SLOT( onRESTUserErrorGetData( QString, QString ) ) );
    connect( _p_restUser, SIGNAL( onRESTUserSearchResults( QList< m4e::user::ModelUserInfoPtr > ) ), this, SLOT( onRESTUserSearchResults( QList< m4e::user::ModelUserInfoPtr > ) ) );
}

User::~User()
{
}

void User::setServerURL( const QString &url )
{
    _p_restUser->setServerURL( url );
}

const QString& User::getServerURL() const
{
    return _p_restUser->getServerURL();
}

user::ModelUserPtr User::getUserData()
{
    return _userModel;
}

QString User::getUserId()
{
    return _userModel.valid() ? _userModel->getId() : "";
}

bool User::isUserId( const QString& id )
{
    if ( !_userModel.valid() || _userModel->getId().isEmpty() || id.isEmpty() )
        return false;

    return _userModel->getId() == id;
}

void User::requestUserData( const QString& userId )
{
    _p_restUser->getUserData( userId );
}

void User::requestUserSearch( const QString& keyword )
{
    _p_restUser->searchForUser( keyword );
}

void User::onRESTUserGetData( m4e::user::ModelUserPtr user )
{
    _userModel = user;
    emit onResponseUserData( true, user );
}

void User::onRESTUserErrorGetData( QString errorCode, QString reason )
{
    log_warning << TAG << "failed to get user data: " << errorCode << ", reason: " << reason << std::endl;
    emit onResponseUserData( false, m4e::user::ModelUserPtr() );
}

void User::onRESTUserSearchResults( QList< user::ModelUserInfoPtr > users )
{
    emit onResponseUserSearch( true, users );
}

void User::onRESTUserErrorSearchResults( QString errorCode, QString reason )
{
    log_warning << TAG << "failed to get user search hits: " << errorCode << ", reason: " << reason << std::endl;
    emit onResponseUserSearch( false, QList< user::ModelUserInfoPtr >() );
}

} // namespace user
} // namespace m4e
