/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "packet.h"
#include <QJsonObject>


namespace m4e
{
namespace comm
{

const QString Packet::CHANNEL_SYSTEM = "system";
const QString Packet::CHANNEL_NOTIFY = "notify";
const QString Packet::CHANNEL_CHAT   = "chat";
const QString Packet::CHANNEL_EVENT  = "event";


Packet::Packet()
{
}

QJsonDocument Packet::toJSON()
{
    QJsonObject obj;
    obj.insert( "channel", _channel );
    obj.insert( "source", _source );
    obj.insert( "time", _time.toMSecsSinceEpoch() );
    obj.insert( "data", _data.object() );

    QJsonDocument doc( obj );
    return doc;
}

bool Packet::fromJSON( const QString& input )
{
    QJsonParseError err;
    QJsonDocument doc = QJsonDocument::fromJson( input.toUtf8(), &err );

    if ( err.error != QJsonParseError::NoError )
        return false;

    QJsonObject obj = doc.object();

    // the data (payload) is also expected to be in JSON format
    _data    = QJsonDocument( obj.value( "data" ).toObject( QJsonObject() ) );
    _channel = obj.value( "channel" ).toString( "" );
    _source  = obj.value( "source" ).toString( "" );
    qint64 time = ( qint64 )obj.value( "time" ).toDouble( 0.0 );
    _time = QDateTime::fromMSecsSinceEpoch( time );

    return true;
}


} // namespace webapp
} // namespace m4e
