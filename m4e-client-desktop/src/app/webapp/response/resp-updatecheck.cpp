/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include <core/log.h>
#include "resp-updatecheck.h"
#include <update/modelupdateinfo.h>
#include <webapp/request/rest-updatecheck.h>


namespace m4e
{
namespace webapp
{

ResponseUpdateCheck::ResponseUpdateCheck( RESTUpdateCheck* p_requester ) :
 _p_requester( p_requester )
{}

void ResponseUpdateCheck::onRESTResponseSuccess( const QJsonDocument& results )
{
    QJsonDocument datadoc;
    QString       errstring;
    QString       errcode;
    bool res = checkStatus( results, datadoc, errcode, errstring );
    if ( !res )
    {
        emit _p_requester->onRESTUpdateErrorGetInfo( errcode, errstring );
        return;
    }

    update::ModelUpdateInfoPtr info = new update::ModelUpdateInfo();
    if ( !info->fromJSON( datadoc ) )
    {
        log_warning << TAG << "could not get update info, invalid format" << std::endl;
        emit _p_requester->onRESTUpdateErrorGetInfo( "", "Invalid format" );
    }
    else
    {
        emit _p_requester->onRESTUpdatetGetInfo( info );
    }
}

void ResponseUpdateCheck::onRESTResponseError( const QString& reason )
{
    emit _p_requester->onRESTUpdateErrorGetInfo( "", reason );
}

} // namespace webapp
} // namespace m4e
