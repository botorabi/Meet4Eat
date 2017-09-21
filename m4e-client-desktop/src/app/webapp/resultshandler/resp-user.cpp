/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include <core/log.h>
#include "resp-user.h"
#include "../rest-user.h"
#include <data/modeluser.h>


namespace m4e
{
namespace webapp
{

/******************************************************/
/*************** ResponseGetUserData ******************/
/******************************************************/

ResponseGetUserData::ResponseGetUserData( RESTUser* p_requester ) :
 _p_requester( p_requester )
{}

void ResponseGetUserData::onRESTResponseSuccess( const QJsonDocument& results )
{
    QJsonDocument datadoc;
    QString       errstring;
    QString       errcode;
    bool res = checkStatus( results, datadoc, errcode, errstring );
    if ( !res )
    {
        emit _p_requester->onRESTUserErrorGetData( errcode, errstring );
        return;
    }

    QJsonObject data  = datadoc.object();
    QString     id    = data.value( "id" ).toString( "" );
    QString     name  = data.value( "name" ).toString( "" );
    QString     email = data.value( "email" ).toString( "" );
    m4e::data::ModelUserPtr user = new m4e::data::ModelUser();
    user->setId( id );
    user->setName( name );
    user->setEMail( email );

    emit _p_requester->onRESTUserGetData( user );
}

void ResponseGetUserData::onRESTResponseError( const QString& reason )
{
    emit _p_requester->onRESTUserErrorGetData( "", reason );
}

} // namespace webapp
} // namespace m4e
