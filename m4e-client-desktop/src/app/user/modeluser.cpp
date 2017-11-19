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
    obj.insert( "login", getLogin() );
    obj.insert( "email", getEmail() );
    obj.insert( "photoId", getPhotoId() );
    obj.insert( "photoETag", getPhotoETag() );
    obj.insert( "status", getStatus() );

    // is there a photo update?
    if ( getUpdatedPhoto().valid() )
    {
        obj.insert( "photo", QString( getUpdatedPhoto()->getContent() ) );
    }

    QJsonDocument doc( obj );
    return doc;
}

QJsonDocument ModelUser::toJSONForUpdate( const QString& name, const QString& password, doc::ModelDocumentPtr photo )
{
    //! NOTE for an user data update request only the following fields are relevant. All other fields cannot be changed.
    QJsonObject obj;
    if ( name.length() > 0 )
        obj.insert( "name", name );

    if ( password.length() > 0 )
        obj.insert( "password", password );

    if ( photo.valid() )
        obj.insert( "photo", QString( photo->getContent() ) );

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
    QString     login     = data.value( "login" ).toString( "" );
    QString     email     = data.value( "email" ).toString( "" );
    QString     photoid   = data.value( "photoId" ).toString( "" );
    QString     photoetag = data.value( "photoETag" ).toString( "" );
    QString     status    = data.value( "status" ).toString( "" );

    setId( id );
    setName( name );
    setLogin( login );
    setEmail( email );
    setPhotoId( photoid );
    setPhotoETag( photoetag );
    setStatus( status );

    return true;
}

} // namespace user
} // namespace m4e
