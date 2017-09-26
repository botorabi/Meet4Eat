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
#include "resultshandler/resp-user.h"


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

} // namespace webapp
} // namespace m4e
