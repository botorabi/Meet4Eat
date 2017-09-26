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
#include "../rest-document.h"
#include <data/modeldocument.h>
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

    QJsonObject data      = datadoc.object();
    QString     id        = QString::number( data.value( "id" ).toInt() );
    QString     name      = data.value( "name" ).toString( "" );
    QString     encoding  = data.value( "encoding" ).toString( "" );
    QString     type      = data.value( "type" ).toString( "" );
    QString     etag      = data.value( "eTag" ).toString( "" );
    QByteArray  content   = data.value( "content" ).toString( "" ).toUtf8();

    m4e::data::ModelDocumentPtr document = new m4e::data::ModelDocument();

    document->setId( id );
    document->setName( name );
    document->setEncoding( encoding );
    document->setType( type );
    document->setETag( etag );
    document->setContent( content );

    emit _p_requester->onRESTDocumentGet( document );
}

void ResponseGetDocument::onRESTResponseError( const QString& reason )
{
    emit _p_requester->onRESTDocumentErrorGet( "", reason );
}

} // namespace webapp
} // namespace m4e
