/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include <core/log.h>
#include "rest-authentication.h"
#include "m4e-api/m4e-response.h"
#include "resultshandler/resp-authentication.h"
#include <QCryptographicHash>


namespace m4e
{
namespace webapp
{

RESTAuthentication::RESTAuthentication( QObject* p_parent ) :
 Meet4EatREST( p_parent )
{
}

RESTAuthentication::~RESTAuthentication()
{
}

QString RESTAuthentication::createHash( const QString& input )
{
    QString res = input;
    for ( unsigned int i = 0; i < 10; i++ )
    {
        res = QCryptographicHash::hash( res.toUtf8(), QCryptographicHash::Sha512 ).toHex();
    }
    return res;
}

void RESTAuthentication::getAuthState()
{
    QUrl url( getResourcePath() + "/rest/authentication/state" );
    auto p_callback = new ResponseAuthState( this );
    getRESTOps()->GET( url, createResultsCallback( p_callback ) );
}

void RESTAuthentication::login( const QString& sid, const QString& userName, const QString& password )
{
    QUrl url( getResourcePath() + "/rest/authentication/login" );

    auto p_callback = new ResponseLogin( this );
    QJsonDocument json;
    QJsonObject jobject;
    jobject["login"] = userName;
    jobject["password"] = createHash( password + sid );
    json.setObject( jobject );
    getRESTOps()->POST( url, createResultsCallback( p_callback ), json );
}

void RESTAuthentication::logout()
{
    QUrl url( getResourcePath() + "/rest/authentication/logout" );
    auto p_callback = new ResponseLogout( this );
    getRESTOps()->POST( url, createResultsCallback( p_callback ) );
}

} // namespace webapp
} // namespace m4e
