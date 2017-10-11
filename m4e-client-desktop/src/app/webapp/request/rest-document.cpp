/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include <core/log.h>
#include "rest-document.h"
#include <webapp/response/resp-document.h>


namespace m4e
{
namespace webapp
{

RESTDocument::RESTDocument( QObject* p_parent ) :
 Meet4EatREST( p_parent )
{
}

RESTDocument::~RESTDocument()
{
}

void RESTDocument::getDocument( const QString& documentId )
{
    QUrl url( getResourcePath() + "/rest/docs/" + documentId );
    auto p_callback = new ResponseGetDocument( this );
    getRESTOps()->GET( url, createResultsCallback( p_callback ) );
}

} // namespace webapp
} // namespace m4e
