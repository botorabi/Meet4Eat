/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include "modellocation.h"
#include <QJsonDocument>
#include <QJsonObject>


namespace m4e
{
namespace event
{

QJsonDocument ModelLocation::toJSON()
{
    QJsonObject obj;
    obj.insert( "id", getId() );
    obj.insert( "name", getName() );
    obj.insert( "description", getDescription() );
    obj.insert( "photoId", getPhotoId() );
    obj.insert( "photoETag", getPhotoETag() );
    if ( getUpdatedPhoto().valid() )
    {
        obj.insert( "photo", QString( getUpdatedPhoto()->getContent() ) );
    }

    QJsonDocument doc( obj );
    return doc;
}

bool ModelLocation::fromJSON( const QString& input )
{
    QJsonParseError err;
    QJsonDocument doc = QJsonDocument::fromJson( input.toUtf8(), &err );
    if ( err.error != QJsonParseError::NoError )
        return false;

    return fromJSON( doc );
}

bool ModelLocation::fromJSON( const QJsonDocument& input )
{
    QJsonObject data      = input.object();
    QString     id        = data.value( "id" ).toString( "" );
    QString     name      = data.value( "name" ).toString( "" );
    QString     desc      = data.value( "description" ).toString( "" );
    QString     photoid   = data.value( "photoId" ).toString( "" );
    QString     photoetag = data.value( "photoETag" ).toString( "" );

    setId( id );
    setName( name );
    setDescription( desc );
    setPhotoId( photoid );
    setPhotoETag( photoetag );

    return true;
}

} // namespace event
} // namespace m4e
