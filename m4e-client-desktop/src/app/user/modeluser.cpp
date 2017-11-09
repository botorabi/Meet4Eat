/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include <core/log.h>
#include "modeluser.h"
#include <QJsonObject>


namespace m4e
{
namespace user
{

QJsonDocument ModelUser::toJSON()
{
    QJsonObject obj;
    obj.insert( "id", getId() );
    obj.insert( "name", getName() );
    obj.insert( "description", getDescription() );
    obj.insert( "email", getEMail() );
    obj.insert( "photoId", getPhotoId() );
    obj.insert( "photoETag", getPhotoETag() );
    obj.insert( "status", getStatus() );

    QJsonDocument doc( obj );
    return doc;
}

bool ModelUser::fromJSON( const QString& input )
{
    QJsonParseError err;
    QJsonDocument doc = QJsonDocument::fromJson( input.toUtf8(), &err );
    if ( err.error != QJsonParseError::NoError )
        return false;

    return fromJSON( doc );
}

bool ModelUser::fromJSON( const QJsonDocument& input )
{
    QJsonObject data      = input.object();
    QString     id        = data.value( "id" ).toString( "" );
    QString     name      = data.value( "name" ).toString( "" );
    QString     desc      = data.value( "description" ).toString( "" );
    QString     email     = data.value( "email" ).toString( "" );
    QString     photoid   = data.value( "photoId" ).toString( "" );
    QString     photoetag = data.value( "photoETag" ).toString( "" );
    QString     status    = data.value( "status" ).toString( "" );

    setId( id );
    setName( name );
    setDescription( desc );
    setEMail( email );
    setPhotoId( photoid );
    setPhotoETag( photoetag );
    setStatus( status );

    return true;
}

} // namespace user
} // namespace m4e
