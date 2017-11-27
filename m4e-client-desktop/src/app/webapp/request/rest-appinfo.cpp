/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include "rest-appinfo.h"
#include <webapp/m4e-api/m4e-response.h>
#include <webapp/response/resp-appinfo.h>


namespace m4e
{
namespace webapp
{

RESTAppInfo::RESTAppInfo( QObject* p_parent ) :
 Meet4EatREST( p_parent )
{
}

RESTAppInfo::~RESTAppInfo()
{
}

void RESTAppInfo::requestAppInfo()
{
    QUrl url( getResourcePath() + "/rest/appinfo" );
    auto p_callback = new ResponseAppInfo( this );
    getRESTOps()->GET( url, createResultsCallback( p_callback ) );
}

} // namespace webapp
} // namespace m4e
