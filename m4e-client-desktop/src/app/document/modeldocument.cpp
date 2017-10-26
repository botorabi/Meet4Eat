/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include "modeldocument.h"
#include <QJsonObject>
#include <QList>


namespace m4e
{
namespace doc
{

void ModelDocument::setContent( const QByteArray& payload, const QString& mimeType, const QString& format )
{
    QByteArray content = payload.toBase64( QByteArray::Base64Encoding );
    QString head = "data:" + mimeType + "/" + format + ";base64,";
    content.insert( 0, head );
    setContent( content );
}

QString ModelDocument::toJSON()
{
    QJsonObject obj;
    obj.insert( "id", getId() );
    obj.insert( "name", getName() );
    obj.insert( "encoding", getEncoding() );
    obj.insert( "type", getType() );
    obj.insert( "eTag", getETag() );
    obj.insert( "content", QString( getContent() ) );

    QJsonDocument doc( obj );
    return QString( doc.toJson() );
}

bool ModelDocument::fromJSON( const QString& input )
{
    QJsonParseError err;
    QJsonDocument doc = QJsonDocument::fromJson( input.toUtf8(), &err );
    if ( err.error != QJsonParseError::NoError )
        return false;

    return fromJSON( doc );
}

bool ModelDocument::fromJSON( const QJsonDocument& input )
{
    QJsonObject data      = input.object();
    QString     id        = QString::number( data.value( "id" ).toInt() );
    QString     name      = data.value( "name" ).toString( "" );
    QString     encoding  = data.value( "encoding" ).toString( "" );
    QString     type      = data.value( "type" ).toString( "" );
    QString     etag      = data.value( "eTag" ).toString( "" );
    QByteArray  content   = data.value( "content" ).toString( "" ).toUtf8();

    setId( id );
    setName( name );
    setEncoding( encoding );
    setType( type );
    setETag( etag );
    setContent( content );

    return true;
}

bool ModelDocument::extractImageData( QByteArray& data, QString& format )
{
    QString mimeType, encoding;
    int headerLength;

    if ( !getHeaderInfo( _content, mimeType, format, encoding, headerLength ) )
        return false;

    if ( mimeType != "image" )
        return false;

    data = _content;
    data.remove( 0, headerLength + 1 );
    if ( encoding == "base64")
    {
        data = QByteArray::fromBase64( data );
    }

    return true;
}

bool ModelDocument::getHeaderInfo( QByteArray& content, QString& mimeType, QString& format, QString& encoding, int& headerLength )
{
    // the first 64 bytes should be sufficient to extract header info
    QByteArray header = content.mid( 0, 64 );
    int type = header.indexOf( ';' );
    if ( type < 0 )
        return false;

    // if a charset is given then skip it, we don't need it
    int charset = header.indexOf( ';', type + 1 );
    if ( charset > 0 )
        type = charset;

    headerLength = header.indexOf( ',', type + 1 );
    if ( headerLength < 0 )
        return false;

    // example header: "data:image/png"
    QString datatype = header.mid( 0, type );
    if ( !datatype.startsWith( "data:") )
        return false;

    datatype = datatype.remove( 0, QString( "data:" ).size() );
    QList< QString > parts = datatype.split( "/" );
    if ( parts.size() < 2 )
        return false;

    // pick the data type
    mimeType = parts.at( 0 );
    // pick the format
    format = parts.at( 1 );
    // pick the encoding, e.g. "base64"
    encoding = header.mid( type + 1, headerLength - type - 1 );

    return true;
}

} // namespace doc
} // namespace m4e
