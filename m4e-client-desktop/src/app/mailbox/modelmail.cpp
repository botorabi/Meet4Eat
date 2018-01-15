/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include "modelmail.h"
#include <QJsonDocument>
#include <QJsonObject>


namespace m4e
{
namespace mailbox
{

QJsonDocument ModelMail::toJSON()
{
    QJsonObject obj;
    obj.insert( "id", getId() );
    obj.insert( "senderId", getSenderId() );
    obj.insert( "senderName", getSenderName() );
    obj.insert( "receiverId", getReceiverId() );
    obj.insert( "receiverName", getReceiverName() );
    obj.insert( "subject", getSubject() );
    obj.insert( "content", getContent() );
    obj.insert( "sendDate", getDate().toMSecsSinceEpoch() );

    QJsonDocument doc( obj );
    return doc;
}

bool ModelMail::fromJSON( const QString& input )
{
    QJsonParseError err;
    QJsonDocument doc = QJsonDocument::fromJson( input.toUtf8(), &err );
    if ( err.error != QJsonParseError::NoError )
        return false;

    return fromJSON( doc );
}

bool ModelMail::fromJSON( const QJsonDocument& input )
{
    QJsonObject data       = input.object();
    bool        unread     = data.value( "unread" ).toBool( true );
    qint64      trashdate  = ( qint64 )data.value( "trashDate" ).toDouble( 0.0 );

    QJsonObject mail       = data.value( "mailContent" ).toObject();
    QString     id         = QString::number( ( qint64 )mail.value( "id" ).toDouble( 0.0 ) );
    QString     senderid   = mail.value( "senderId" ).toString( "" );
    QString     sendername = mail.value( "senderName" ).toString( "" );
    QString     recvid     = mail.value( "receiverId" ).toString( "" );
    QString     recvname   = mail.value( "receiverName" ).toString( "" );
    QString     subject    = mail.value( "subject" ).toString( "" );
    QString     content    = mail.value( "content" ).toString( "" );
    qint64      date       = ( qint64 )mail.value( "sendDate" ).toDouble( 0.0 );

    QDateTime d;
    d.setTime_t( static_cast< uint >( date / 1000 ) );
    setDate( d );

    setId( id );
    setSenderId( senderid );
    setSenderName( sendername );
    setReceiverId( recvid );
    setReceiverName( recvname );
    setSubject( subject );
    setContent( content );

    //! NOTE these two fields are read-only, toJSON does not export them!
    _isUnread = unread;
    _isTrashed = trashdate != 0;

    return true;
}

} // namespace event
} // namespace m4e
