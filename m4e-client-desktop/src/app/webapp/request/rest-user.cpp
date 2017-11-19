/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include <core/log.h>
#include "rest-user.h"
#include <webapp/response/resp-user.h>


namespace m4e
{
namespace webapp
{

RESTUser::RESTUser( QObject* p_parent ) :
 Meet4EatREST( p_parent )
{
}

RESTUser::~RESTUser()
{
}

void RESTUser::getUserData( const QString& userId )
{
    QUrl url( getResourcePath() + "/rest/users/" + userId );
    //! NOTE the callback object will be automatically removed once the response arrives, see method 'Meet4EatRESTCallback::getAutoDelete'
    auto p_callback = new ResponseGetUserData( this );
    getRESTOps()->GET( url, createResultsCallback( p_callback ) );
}

void RESTUser::updateUserData( const QString& userId, const QString& name, const QString& password, doc::ModelDocumentPtr photo )
{
    QUrl url( getResourcePath() + "/rest/users/" + userId );
    auto p_callback = new ResponseUpdateUserData( this );
    getRESTOps()->PUT( url, createResultsCallback( p_callback ), user::ModelUser::toJSONForUpdate( name, password, photo ) );
}

void RESTUser::searchForUser( const QString& keyword )
{
    QUrl url( getResourcePath() + "/rest/users/search/" + keyword );
    auto p_callback = new ResponseGetUserSearch( this );
    getRESTOps()->GET( url, createResultsCallback( p_callback ) );
}

} // namespace webapp
} // namespace m4e
