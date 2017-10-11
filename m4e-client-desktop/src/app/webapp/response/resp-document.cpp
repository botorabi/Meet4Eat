/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include <core/log.h>
#include "resp-document.h"
#include <webapp/request/rest-document.h>
#include <document/modeldocument.h>
#include <QJsonObject>
#include <QJsonArray>


namespace m4e
{
namespace webapp
{

ResponseGetDocument::ResponseGetDocument( RESTDocument* p_requester ) :
 _p_requester( p_requester )
{}

void ResponseGetDocument::onRESTResponseSuccess( const QJsonDocument& results )
{
    QJsonDocument datadoc;
    QString       errstring;
    QString       errcode;
    bool res = checkStatus( results, datadoc, errcode, errstring );
    if ( !res )
    {
        emit _p_requester->onRESTDocumentErrorGet( errcode, errstring );
        return;
    }

    doc::ModelDocumentPtr document = new doc::ModelDocument();
    res = document->fromJSON( datadoc );
    if ( !res )
    {
        emit _p_requester->onRESTDocumentErrorGet( "", "invalid input format, JSON was expected" );
    }
    else
    {
        emit _p_requester->onRESTDocumentGet( document );
    }
}

void ResponseGetDocument::onRESTResponseError( const QString& reason )
{
    emit _p_requester->onRESTDocumentErrorGet( "", reason );
}

} // namespace webapp
} // namespace m4e
