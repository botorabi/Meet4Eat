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
    connect( _p_restUser, SIGNAL( onRESTUserUpdateData( QString ) ), this, SLOT( onRESTUserUpdateData( QString ) ) );
    connect( _p_restUser, SIGNAL( onRESTUserErrorUpdateData( QString, QString ) ), this, SLOT( onRESTUserErrorUpdateData( QString, QString ) ) );
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
    setLastError();
    _p_restUser->getUserData( userId );
}

bool User::requestUpdateUserData( const QString& name, const QString& password, doc::ModelDocumentPtr photo )
{
    setLastError();
    if ( !_userModel.valid() || _userModel->getId().isEmpty() )
        return false;

    _p_restUser->updateUserData( _userModel->getId(), name, password, photo );
    return true;
}

void User::requestUserSearch( const QString& keyword )
{
    setLastError();
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
    setLastError( reason, errorCode );
    emit onResponseUserData( false, m4e::user::ModelUserPtr() );
}

void User::onRESTUserUpdateData( QString userId )
{
    // user data was updated, refresh it
    if ( _userModel.valid() && ( userId == _userModel->getId() ) )
    {
        log_debug << TAG << "updating user data" << std::endl;
        requestUserData( userId );
    }

    emit onResponseUpdateUserData( true, userId );
}

void User::onRESTUserErrorUpdateData( QString errorCode, QString reason )
{
    log_warning << TAG << "failed to update user data: " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponseUpdateUserData( false, "" );
}

void User::onRESTUserSearchResults( QList< user::ModelUserInfoPtr > users )
{
    emit onResponseUserSearch( true, users );
}

void User::onRESTUserErrorSearchResults( QString errorCode, QString reason )
{
    log_warning << TAG << "failed to get user search hits: " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponseUserSearch( false, QList< user::ModelUserInfoPtr >() );
}

void User::setLastError( const QString& error, const QString& errorCode )
{
    _lastError = error;
    _lastErrorCode = errorCode;
}

} // namespace user
} // namespace m4e
