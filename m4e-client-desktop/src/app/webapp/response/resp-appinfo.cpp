/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include "resp-appinfo.h"
#include <webapp/request/rest-appinfo.h>


namespace m4e
{
namespace webapp
{

/******************************************************/
/****************** ResponseAppInfo *******************/
/******************************************************/

ResponseAppInfo::ResponseAppInfo( RESTAppInfo* p_requester ) :
 _p_requester( p_requester )
{}

void ResponseAppInfo::onRESTResponseSuccess( const QJsonDocument& results )
{
    QJsonDocument datadoc;
    QString       errstring;
    QString       errcode;
    bool res = checkStatus( results, datadoc, errcode, errstring );
    if ( !res )
    {
        emit _p_requester->onRESTAppInfoError( errcode, errstring );
        return;
    }

    QJsonObject data = datadoc.object();
    QString version = data.value( "version" ).toString( "" );
    emit _p_requester->onRESTAppInfo( version );
}

void ResponseAppInfo::onRESTResponseError( const QString& reason )
{
    emit _p_requester->onRESTAppInfoError( "", reason );
}

} // namespace webapp
} // namespace m4e
