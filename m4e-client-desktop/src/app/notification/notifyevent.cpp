/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */


#include "notifyevent.h"
#include <QJsonDocument>
#include <QJsonObject>


namespace m4e
{
namespace notify
{

NotifyEvent::NotifyEvent()
{
}

QString NotifyEvent::toJSON()
{
    return QString( toJSONDocument().toJson() );
}

QJsonDocument NotifyEvent::toJSONDocument()
{
    QJsonObject obj;
    obj.insert( "type", getType() );
    obj.insert( "subject", getSubject() );
    obj.insert( "text", getText() );
    obj.insert( "data",  getData().object() );

    QJsonDocument doc( obj );
    return doc;
}

bool NotifyEvent::fromJSON( const QString& input )
{
    QJsonParseError err;
    QJsonDocument doc = QJsonDocument::fromJson( input.toUtf8(), &err );
    if ( err.error != QJsonParseError::NoError )
        return false;

    return fromJSON( doc );
}

bool NotifyEvent::fromJSON( const QJsonDocument& input )
{
    QJsonObject obj      = input.object();
    QString     type     = obj.value( "type" ).toString( "" );
    QString     subject  = obj.value( "subject" ).toString( "" );
    QString     text     = obj.value( "text" ).toString( "" );
    QJsonObject data     = obj.value( "data" ).toObject();

    setType( type );
    setSubject( subject );
    setText( text );
    setData( QJsonDocument( data ) );

    return true;
}

} // namespace notify
} // namespace m4e
