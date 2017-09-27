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

    QJsonObject data      = datadoc.object();
    QString     id        = QString::number( data.value( "id" ).toInt() );
    QString     name      = data.value( "name" ).toString( "" );
    QString     email     = data.value( "email" ).toString( "" );
    QString     photoid   = QString::number( data.value( "photoId" ).toInt() );
    QString     photoetag = data.value( "photoETag" ).toString( "" );

    m4e::data::ModelUserPtr user = new m4e::data::ModelUser();
    user->setId( id );
    user->setName( name );
    user->setEMail( email );
    user->setPhotoId( photoid );
    user->setPhotoETag( photoetag );

    emit _p_requester->onRESTUserGetData( user );
}

void ResponseGetUserData::onRESTResponseError( const QString& reason )
{
    emit _p_requester->onRESTUserErrorGetData( "", reason );
}

/******************************************************/
/************** ResponseGetUserSearch *****************/
/******************************************************/

ResponseGetUserSearch::ResponseGetUserSearch( RESTUser* p_requester ) :
 _p_requester( p_requester )
{}

void ResponseGetUserSearch::onRESTResponseSuccess( const QJsonDocument& results )
{
    QJsonDocument datadoc;
    QString       errstring;
    QString       errcode;
    bool res = checkStatus( results, datadoc, errcode, errstring );
    if ( !res )
    {
        emit _p_requester->onRESTUserErrorSearchResults( errcode, errstring );
        return;
    }
    QJsonArray users = datadoc.array();
    QList< data::ModelUserInfoPtr > hits;
    for ( int i = 0; i < users.size(); i++ )
    {
        QJsonObject obj = users.at( i ).toObject();
        QString id        = QString::number( obj.value( "id" ).toInt() );
        QString name      = obj.value( "name" ).toString( "" );
        QString photoid   = QString::number( obj.value( "photoId" ).toInt() );
        QString photoetag = obj.value( "photoETag" ).toString( "" );

        data::ModelUserInfoPtr u = new data::ModelUserInfo();
        u->setId( id );
        u->setName( name );
        u->setPhotoId( photoid );
        u->setPhotoETag( photoetag );

        hits.append( u );
    }

    emit _p_requester->onRESTUserSearchResults( hits );
}

void ResponseGetUserSearch::onRESTResponseError( const QString& reason )
{
    emit _p_requester->onRESTUserErrorSearchResults( "", reason );
}
} // namespace webapp
} // namespace m4e
