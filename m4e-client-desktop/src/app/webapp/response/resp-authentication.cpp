/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include <core/log.h>
#include "resp-authentication.h"
#include <webapp/request/rest-authentication.h>


namespace m4e
{
namespace webapp
{

/******************************************************/
/**************** ResponseAuthState *******************/
/******************************************************/

ResponseAuthState::ResponseAuthState( RESTAuthentication* p_requester ) :
 _p_requester( p_requester )
{}

void ResponseAuthState::onRESTResponseSuccess( const QJsonDocument& results )
{
    QJsonDocument datadoc;
    QString       errstring;
    QString       errcode;
    bool res = checkStatus( results, datadoc, errcode, errstring );
    if ( !res )
    {
        emit _p_requester->onRESTAuthenticationErrorAuthState( errcode, errstring );
        return;
    }

    QJsonObject data = datadoc.object();
    QString     auth = data.value( "auth" ).toString( "" );
    QString     sid  = data.value( "sid" ).toString( "" );
    QString     id   = data.value( "id" ).toString( "" );
    emit _p_requester->onRESTAuthenticationAuthState( auth == "yes", sid, id );
}

void ResponseAuthState::onRESTResponseError( const QString& reason )
{
    emit _p_requester->onRESTAuthenticationErrorAuthState( "", reason );
}


/******************************************************/
/******************* ResponseLogin ********************/
/******************************************************/

ResponseLogin::ResponseLogin( RESTAuthentication* p_requester ) :
 _p_requester( p_requester )
{}

void ResponseLogin::onRESTResponseSuccess( const QJsonDocument& results )
{
    QJsonDocument datadoc;
    QString       errstring;
    QString       errcode;
    bool res = checkStatus( results, datadoc, errcode, errstring );
    if ( !res )
    {
        emit _p_requester->onRESTAuthenticationErrorLogin( errcode, errstring );
        return;
    }

    QJsonObject data = datadoc.object();
    QString id = data.value( "id" ).toString( "" );
    emit _p_requester->onRESTAuthenticationLogin( id );
}

void ResponseLogin::onRESTResponseError( const QString& reason )
{
    emit _p_requester->onRESTAuthenticationErrorLogin( "", reason );
}

/******************************************************/
/******************* ResponseLogout *******************/
/******************************************************/

ResponseLogout::ResponseLogout( RESTAuthentication* p_requester ) :
 _p_requester( p_requester )
{}

void ResponseLogout::onRESTResponseSuccess( const QJsonDocument& results )
{
    QJsonDocument datadoc;
    QString       errstring;
    QString       errcode;
    bool res = checkStatus( results, datadoc, errcode, errstring );
    if ( !res )
    {
        emit _p_requester->onRESTAuthenticationErrorLogout( errcode, errstring );
        return;
    }

    emit _p_requester->onRESTAuthenticationLogout();
}

void ResponseLogout::onRESTResponseError( const QString& reason )
{
    emit _p_requester->onRESTAuthenticationErrorLogout( "", reason );
}

} // namespace webapp
} // namespace m4e
