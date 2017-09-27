/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "webapp.h"
#include <core/log.h>
#include <data/appsettings.h>
#include <user/userauth.h>
#include <user/user.h>
#include <QApplication>
#include <QMessageBox>


namespace m4e
{
namespace data
{

WebApp::WebApp( QObject* p_parent ) :
 QObject( p_parent )
{
}

WebApp::~WebApp()
{
}

void WebApp::establishConnection()
{
    if ( _connState == ConnEstablished )
    {
        log_warning << TAG << "connection is already established!" << std::endl;
        return;
    }

    _userID    = "";
    _connState = ConnNoConnection;

    QString username = m4e::data::AppSettings::get()->readSettingsValue( M4E_SETTINGS_CAT_USER, M4E_SETTINGS_KEY_USER_LOGIN, "" );
    QString passwd   = m4e::data::AppSettings::get()->readSettingsValue( M4E_SETTINGS_CAT_USER, M4E_SETTINGS_KEY_USER_PW, "" );

    if ( !username.isEmpty() && !passwd.isEmpty() )
    {
        _connState = ConnConnecting;
        m4e::user::UserAuthentication* p_user = getOrCreateUserAuth();
        p_user->requestSignIn( username, passwd );
        username.clear();
        passwd.clear();
    }
}

void WebApp::shutdownConnection()
{
    if ( _connState != ConnEstablished )
    {
        log_warning << TAG << "there is no connection to shutdown!" << std::endl;
        return;
    }

    m4e::user::UserAuthentication* p_user = getOrCreateUserAuth();
    p_user->requestSignOut();

    _userID    = "";
    _userModel = nullptr;
    _connState = ConnNoConnection;
    _connFailReason = "";
}

WebApp::ConnectionState WebApp::getConnectionState() const
{
    return _connState;
}

const QString& WebApp::getConnFailReason() const
{
    return _connFailReason;
}

data::ModelUserPtr WebApp::getUserData() const
{
    return _userModel;
}

void WebApp::requestUserData()
{
    if ( _userID.isEmpty() )
    {
        log_error << TAG << "cannot request for user data, invalid user ID" << std::endl;
        return;
    }
    user::User* p_user = getOrCreateUser();
    p_user->requestUserData( _userID );
}

EventsPtr WebApp::getUserEvents()
{
    EventsPtr events = new Events();
    events->setAllEvents( _events );
    return events;
}

void WebApp::requestUserEvents()
{
    if ( !_p_user )
    {
        log_error << TAG << "cannot request for user events, invalid user" << std::endl;
        return;
    }
    // request for getting all user events
    _p_user->requestAllEvents();
}

void WebApp::requestDocument( const QString& id, const QString& eTag )
{
    // document arrives with signal 'onCacheDocumentReady'
    getOrCreateDocumentCache()->requestDocument( id, eTag );
}

void WebApp::requestUserSearch( const QString& keyword )
{
    getOrCreateUser()->requestUserSearch( keyword );
}

user::UserAuthentication* WebApp::getOrCreateUserAuth()
{
    if ( !_p_userAuth )
    {
        _p_userAuth = new user::UserAuthentication( this );
        connect( _p_userAuth, SIGNAL( onResponseSignInResult( bool, QString, enum m4e::user::UserAuthentication::AuthResultsCode, QString ) ),
                 this, SLOT( onResponseSignInResult( bool, QString, enum m4e::user::UserAuthentication::AuthResultsCode, QString ) ) );

        QString server = m4e::data::AppSettings::get()->readSettingsValue( M4E_SETTINGS_CAT_SRV, M4E_SETTINGS_KEY_SRV_URL, "" );
        _p_userAuth->setServerURL( server );
    }
    return _p_userAuth;
}

user::User* WebApp::getOrCreateUser()
{
    if ( !_p_user )
    {
        _p_user = new user::User( this );
        connect( _p_user, SIGNAL( onResponseUserData( bool, m4e::data::ModelUserPtr ) ),
                 this, SLOT( onResponseUserData( bool, m4e::data::ModelUserPtr ) ) );

        connect( _p_user, SIGNAL( onResponseUserAllEvents( bool, QList< m4e::data::ModelEventPtr > ) ),
                 this, SLOT( onResponseUserAllEvents( bool, QList< m4e::data::ModelEventPtr > ) ) );

        connect( _p_user, SIGNAL( onResponseUserSearch( bool, QList< m4e::data::ModelUserInfoPtr > ) ),
                 this, SLOT( onResponseUserSearch( bool, QList< m4e::data::ModelUserInfoPtr > ) ) );

        QString server = m4e::data::AppSettings::get()->readSettingsValue( M4E_SETTINGS_CAT_SRV, M4E_SETTINGS_KEY_SRV_URL, "" );
        _p_user->setServerURL( server );
    }
    return _p_user;
}

DocumentCache* WebApp::getOrCreateDocumentCache()
{
    if ( !_p_documentCache )
    {
        _p_documentCache = new DocumentCache( this );
        connect( _p_documentCache, SIGNAL( onDocumentReady( m4e::data::ModelDocumentPtr ) ),
                 this, SLOT( onCacheDocumentReady( m4e::data::ModelDocumentPtr ) ) );

        // purge the local cache, delete cached documents which were not used in the past
        _p_documentCache->purgeCache( M4E_LOCAL_CAHCE_EXPIRE_DAYS );
    }
    return _p_documentCache;
}

void WebApp::onResponseSignInResult( bool success, QString userId, m4e::user::UserAuthentication::AuthResultsCode code, QString reason )
{
    if ( success )
    {
        log_verbose << TAG << "successfully signed in user" << std::endl;
        _userID = userId;
        user::User* p_user = getOrCreateUser();
        p_user->requestUserData( userId );
    }
    else
    {
        _connState = ConnFail;
        _connFailReason = reason;

        log_verbose << TAG << "failed to sign in user (" << QString::number( code ).toStdString() << "), reason: " << reason.toStdString() << std::endl;
        QString text = QApplication::translate( "WebApp", "Could not sign in. Check your credentials and try again." );
        QMessageBox msgbox( QMessageBox::Warning, "Sign In", text, QMessageBox::Ok, nullptr );
        msgbox.exec();
    }
}

void WebApp::onResponseUserData( bool success, m4e::data::ModelUserPtr user )
{
    _connState = success ? ConnEstablished : ConnFail;
    _connFailReason = success ? "" : QApplication::translate( "WebApp", "Could not retrieve user data." );
    _userModel = user;

    emit onUserDataReady( user );

    // now request for getting all user events
    _p_user->requestAllEvents();
}

void WebApp::onResponseUserAllEvents( bool success, QList< m4e::data::ModelEventPtr > events )
{
    if ( success )
    {
        _events = events;

    }
    else
    {
        log_warning << TAG << "could not get user's events from server!" << std::endl;
    }

    emit onUserEventsReady( _events );
}

void WebApp::onCacheDocumentReady( m4e::data::ModelDocumentPtr document )
{
    emit onDocumentReady( document );
}

void WebApp::onResponseUserSearch( bool success, QList< m4e::data::ModelUserInfoPtr > users )
{
    if ( success )
    {
        emit onUserSearch( users );
    }
    else
    {
        log_warning << TAG << "could not get user search results!" << std::endl;
        emit onUserSearch( QList< ModelUserInfoPtr >() );
    }
}

} // namespace data
} // namespace m4e
