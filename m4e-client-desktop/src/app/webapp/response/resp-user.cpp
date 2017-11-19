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
#include <webapp/request/rest-user.h>
#include <user/modeluser.h>


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

    user::ModelUserPtr user = new user::ModelUser();
    if ( !user->fromJSON( datadoc ) )
    {
        log_warning << TAG << "invalid JSON format detected, ignoring user data!" << std::endl;
        emit _p_requester->onRESTUserErrorGetData( "", "Invalid user data format" );
    }
    else
    {
        emit _p_requester->onRESTUserGetData( user );
    }
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
    QList< user::ModelUserInfoPtr > hits;
    for ( int i = 0; i < users.size(); i++ )
    {
        QJsonObject obj = users.at( i ).toObject();
        user::ModelUserInfoPtr u = new user::ModelUserInfo();
        if ( !u->fromJSON( QJsonDocument( obj ) ) )
        {
            log_warning << TAG << "invalid JSON format detected, ignoring search result!" << std::endl;
        }
        else
        {
            hits.append( u );
        }
    }

    emit _p_requester->onRESTUserSearchResults( hits );
}

void ResponseGetUserSearch::onRESTResponseError( const QString& reason )
{
    emit _p_requester->onRESTUserErrorSearchResults( "", reason );
}

/******************************************************/
/************* ResponseUpdateUserData *****************/
/******************************************************/

ResponseUpdateUserData::ResponseUpdateUserData( RESTUser* p_requester ) :
 _p_requester( p_requester )
{}

void ResponseUpdateUserData::onRESTResponseSuccess( const QJsonDocument& results )
{
    QJsonDocument datadoc;
    QString       errstring;
    QString       errcode;
    bool res = checkStatus( results, datadoc, errcode, errstring );
    if ( !res )
    {
        emit _p_requester->onRESTUserErrorUpdateData( errcode, errstring );
        return;
    }

    QString userid = datadoc.object().value( "userId" ).toString( "" );
    emit _p_requester->onRESTUserUpdateData( userid );
}

void ResponseUpdateUserData::onRESTResponseError( const QString& reason )
{
    emit _p_requester->onRESTUserErrorUpdateData( "", reason );
}

} // namespace webapp
} // namespace m4e
