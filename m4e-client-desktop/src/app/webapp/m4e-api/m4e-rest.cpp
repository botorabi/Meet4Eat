/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include <core/log.h>
#include "m4e-rest.h"

namespace m4e
{
namespace webapp
{

unsigned int Meet4EatREST::_requestId = 0;

Meet4EatREST::Meet4EatREST( QObject* p_parent ) :
 QObject( p_parent )
{
    _p_RESTOps = new Meet4EatRESTOperations( this );
    connect( _p_RESTOps, SIGNAL( onResponse( unsigned int, QJsonDocument ) ), this, SLOT( onRESTResponse( unsigned int, QJsonDocument ) ) );
    connect( _p_RESTOps, SIGNAL( onResponseFailed( unsigned int, QString ) ), this, SLOT( onRESTResponseFailed( unsigned int, QString ) ) );
}

Meet4EatREST::~Meet4EatREST()
{
}

const QString& Meet4EatREST::getServerURL() const
{
    return _urlServer;
}

void Meet4EatREST::setServerURL( const QString& serverURL )
{
    _urlServer = serverURL;
    if ( !_urlServer.startsWith( "http://" ) || !_urlServer.startsWith( "https://" ) )
    {
        _urlServer = "http://" + _urlServer;
    }

    _pathResources = _urlServer + M4E_REST_SRV_RESOURCE_PATH;
}

void Meet4EatREST::onRESTResponse( unsigned int requestId, QJsonDocument json )
{
    // if a callback was given then use it now to deliver the resquest results
    Meet4EatRESTResponse* p_callback = getAndRemoveResultsCallback( requestId );
    if ( p_callback )
    {
        p_callback->onRESTResponseSuccess( json );
        // is automatic callback instance deletion active?
        if ( p_callback->getAutoDelete() )
        {
            delete p_callback;
        }
    }
}

void Meet4EatREST::onRESTResponseFailed( unsigned int requestId, QString reason )
{
    Meet4EatRESTResponse* p_callback = getAndRemoveResultsCallback( requestId );
    if ( p_callback )
    {
        p_callback->onRESTResponseError( reason );
        // is automatic callback instance deletion active?
        if ( p_callback->getAutoDelete() )
        {
            delete p_callback;
        }
    }
}

unsigned int Meet4EatREST::createResultsCallback( Meet4EatRESTResponse* p_callback )
{
    ++_requestId;
    _callbacks[ _requestId ] = p_callback;
    return _requestId;
}

Meet4EatRESTResponse* Meet4EatREST::getAndRemoveResultsCallback( unsigned int requestId )
{
    LookupResultsCallbacks::iterator callback = _callbacks.find( requestId );
    if ( callback == _callbacks.end() )
    {
        return nullptr;
    }

    Meet4EatRESTResponse* p_callback = callback.value();
    _callbacks.erase( callback );
    return p_callback;
}

} // namespace webapp
} // namespace m4e
