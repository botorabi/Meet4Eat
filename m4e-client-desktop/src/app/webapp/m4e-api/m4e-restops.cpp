/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include <core/log.h>
#include "m4e-restops.h"

namespace m4e
{
namespace webapp
{

QNetworkCookieJar* Meet4EatRESTOperations::_s_cookie = nullptr;

Meet4EatRESTOperations::Meet4EatRESTOperations( QObject* p_parent ) :
 QObject( p_parent )
{
    _p_nam = new QNetworkAccessManager( this );
    _s_cookie = _s_cookie ? _s_cookie : new QNetworkCookieJar();
    _p_nam->setCookieJar( _s_cookie );
    // in order to share the cookie jar, we have to reset its parent!
    _s_cookie->setParent( nullptr );
    connect( _p_nam, SIGNAL( finished( QNetworkReply* ) ), this, SLOT( onReplyFinished( QNetworkReply* ) ) );
}

Meet4EatRESTOperations::~Meet4EatRESTOperations()
{
}

void Meet4EatRESTOperations::GET( const QUrl& url, unsigned int requestId )
{
    startRequest( url, QNetworkAccessManager::GetOperation, requestId, QJsonDocument() );
}

void Meet4EatRESTOperations::POST( const QUrl& url, unsigned int requestId, const QJsonDocument& json )
{
    startRequest( url, QNetworkAccessManager::PostOperation, requestId, json );
}

void Meet4EatRESTOperations::PUT( const QUrl& url, unsigned int requestId, const QJsonDocument& json )
{
    startRequest( url, QNetworkAccessManager::PutOperation, requestId, json );
}

void Meet4EatRESTOperations::DELETE( const QUrl& url, unsigned int requestId )
{
    startRequest( url, QNetworkAccessManager::DeleteOperation, requestId, QJsonDocument() );
}

QNetworkCookieJar* Meet4EatRESTOperations::getCookies()
{
    return _s_cookie;
}

void Meet4EatRESTOperations::resetCookie()
{
    delete _s_cookie;
    _s_cookie = new QNetworkCookieJar();
}

void Meet4EatRESTOperations::startRequest( const QUrl& url, enum QNetworkAccessManager::Operation op, unsigned int requestId, const QJsonDocument& json )
{
    QNetworkRequest request( url );
    request.setHeader( QNetworkRequest::ContentTypeHeader, "application/json" );

    QNetworkReply* p_reply = nullptr;

    switch ( op )
    {
        case QNetworkAccessManager::GetOperation:
        {
            p_reply  = _p_nam->get( request );
        }
        break;

        case QNetworkAccessManager::PostOperation:
        {
            p_reply  = _p_nam->post( request, json.toJson() );
        }
        break;

        case QNetworkAccessManager::PutOperation:
        {
            p_reply  = _p_nam->put( request, json.toJson() );
        }
        break;

        case QNetworkAccessManager::DeleteOperation:
        {
            p_reply  = _p_nam->deleteResource( request );
        }
        break;

        default:
        {
            log_warning << TAG << "invalid access method: " + op << std::endl;
        }
    }

    if ( p_reply != nullptr )
    {
        p_reply->setProperty( "requestId", requestId );
    }
}

void Meet4EatRESTOperations::onReplyFinished( QNetworkReply* p_reply )
{
    unsigned int reqid = p_reply->property( "requestId" ).toUInt();

    if ( p_reply->error() != QNetworkReply::NoError )
    {
        QString errstring = "Could not connect web api: " + p_reply->request().url().toString() + ". Reason: " + p_reply->errorString();
        emit onResponseFailed( reqid, errstring );
    }
    else
    {
        //log_verbose << TAG << "successfully connected web api: " << p_reply->request().url().toString().toStdString() << std::endl;
        QByteArray    response = p_reply->readAll();
        QJsonDocument jsonresp = QJsonDocument::fromJson( response );
        if ( jsonresp.isEmpty() )
        {
            QString errstring = "Unexpected response arrived from web api: " + p_reply->request().url().toString() + ". Reponse body: " + response;
            emit onResponseFailed( reqid, errstring );
        }
        else
        {
            emit onResponse( reqid, jsonresp );
        }
    }

    p_reply->deleteLater();
}

} // namespace webapp
} // namespace m4e
