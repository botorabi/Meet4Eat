/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include <settings/appsettings.h>
#include <core/log.h>
#include "userauth.h"
#include <QApplication>


namespace m4e
{
namespace user
{

UserAuthentication::UserAuthentication( QObject* p_parent ) :
 QObject( p_parent )
{
    _p_restAuth = new webapp::RESTAuthentication( this );
    connect( _p_restAuth, SIGNAL( onRESTAuthenticationAuthState( bool, QString, QString ) ), this, SLOT( onRESTAuthenticationAuthState( bool, QString, QString ) ) );
    connect( _p_restAuth, SIGNAL( onRESTAuthenticationErrorAuthState( QString, QString ) ), this, SLOT( onRESTAuthenticationErrorAuthState( QString, QString ) ) );
    connect( _p_restAuth, SIGNAL( onRESTAuthenticationLogin( QString ) ), this, SLOT( onRESTAuthenticationLogin( QString ) ) );
    connect( _p_restAuth, SIGNAL( onRESTAuthenticationErrorLogin( QString, QString ) ), this, SLOT( onRESTAuthenticationErrorLogin( QString, QString ) ) );
    connect( _p_restAuth, SIGNAL( onRESTAuthenticationLogout() ), this, SLOT( onRESTAuthenticationLogout() ) );
    connect( _p_restAuth, SIGNAL( onRESTAuthenticationErrorLogout( QString, QString ) ), this, SLOT( onRESTAuthenticationErrorLogout( QString, QString ) ) );
}

UserAuthentication::~UserAuthentication()
{
}

void UserAuthentication::setServerURL(const QString &url)
{
    _p_restAuth->setServerURL( url );
}

const QString& UserAuthentication::getServerURL() const
{
    return _p_restAuth->getServerURL();
}

void UserAuthentication::requestAuthState()
{
    _userName.clear();
    _password.clear();
    _p_restAuth->getAuthState();
}

void UserAuthentication::requestSignIn( const QString& userName, const QString& password )
{
    if ( getServerURL().isEmpty() )
    {
        log_verbose << TAG << "cannot sign in user, missing server address" << std::endl;
        emit onResponseSignInResult( false, "", AuthCodeMissingServerAddress, QApplication::translate( "UserAuthentication", "Missing server address" ) );
        return;
    }

    _userName = userName;
    _password = password;

    if ( _userName.isEmpty() || _password.isEmpty() )
    {
        log_verbose << TAG << "cannot authenticate user, missing user credentials" << std::endl;
        emit onResponseSignInResult( false, "", AuthCodeMissingCredentials, QApplication::translate( "UserAuthentication", "Missing credentials" ) );
        return;
    }

    _p_restAuth->getAuthState();
}

void UserAuthentication::requestSignOut()
{
    _p_restAuth->logout();
}

void UserAuthentication::onRESTAuthenticationAuthState( bool authenticated, QString sid, QString userId )
{
    //! NOTE this slot is used for login process and auth state request.
    //!      if both, user name and login are empty then we want to check only the auth state
    if ( _userName.isEmpty() && _password.isEmpty() )
    {
        emit onResponseAuthState( authenticated, userId );
        return;
    }

    if ( !authenticated )
    {
        _p_restAuth->login( sid, _userName, _password );
        _userName.clear();
        _password.clear();
    }
    else
    {
        emit onResponseSignInResult( true, userId, AuthCodeSuccess, QApplication::translate( "UserAuthentication", "Authentication successful" ) );
    }
}

void UserAuthentication::onRESTAuthenticationErrorAuthState( QString errorCode, QString reason )
{
    log_warning << TAG << "failed to get auth state: " << errorCode << ", reason: " << reason << std::endl;
    if ( _userName.isEmpty() && _password.isEmpty() )
    {
        emit onResponseAuthState( false, "" );
        return;
    }
    _userName.clear();
    _password.clear();
    emit onResponseSignInResult( false, "", AuthCodeServerNotReachable, QApplication::translate( "UserAuthentication", "Server is not reachable." ) );
}

void UserAuthentication::onRESTAuthenticationLogin( QString userId )
{
    log_verbose << TAG << "successfully logged in" << std::endl;
    emit onResponseSignInResult( true, userId, AuthCodeSuccess, QApplication::translate( "UserAuthentication", "Authentication successful" ) );
}

void UserAuthentication::onRESTAuthenticationErrorLogin( QString errorCode, QString reason )
{
    log_warning << TAG << "failed to login, error code: " << errorCode << ", reason: " << reason << std::endl;
    QString text = QApplication::translate( "UserAuthentication", "Failed to authenticate user. Reason: " ) + reason;
    emit onResponseSignInResult( false, "", AuthCodeInvalidCredentials, text );
}

void UserAuthentication::onRESTAuthenticationLogout()
{
    log_verbose << TAG << "successfully logged out" << std::endl;
    emit onResponseSignOutResult( true, AuthCodeSuccess, QApplication::translate( "UserAuthentication", "Sign out successful" ) );
}

void UserAuthentication::onRESTAuthenticationErrorLogout( QString errorCode, QString reason )
{
    log_warning << TAG << "failed to logout, error code: " << errorCode << ", reason: " << reason << std::endl;
    QString text = QApplication::translate( "UserAuthentication", "Failed to sign out user. Reason: " ) + reason;
    emit onResponseSignOutResult( false, AuthCodeOtherReason, text );
}

} // namespace user
} // namespace m4e
