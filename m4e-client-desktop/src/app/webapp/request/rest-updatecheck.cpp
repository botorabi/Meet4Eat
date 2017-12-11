/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include "rest-updatecheck.h"
#include <webapp/m4e-api/m4e-response.h>
#include <webapp/response/resp-updatecheck.h>


namespace m4e
{
namespace webapp
{

RESTUpdateCheck::RESTUpdateCheck( QObject* p_parent ) :
 Meet4EatREST( p_parent )
{
}

RESTUpdateCheck::~RESTUpdateCheck()
{
}

void RESTUpdateCheck::requestUpdateInfo( m4e::update::ModelRequestUpdateInfoPtr request )
{
    QUrl url( getResourcePath() + "/rest/update/check" );
    auto p_callback = new ResponseUpdateCheck( this );
    getRESTOps()->POST( url, createResultsCallback( p_callback ), request->toJSON() );
}

} // namespace webapp
} // namespace m4e
