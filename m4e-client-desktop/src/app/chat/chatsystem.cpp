/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "chatsystem.h"
#include <core/log.h>
#include "chatmessage.h"
#include <QJsonObject>


namespace m4e
{
namespace chat
{

/* Chat message fields in network packet */
static const QString PACKET_FIELD_TEXT       = "text";
static const QString PACKET_FIELD_DOC        = "document";
static const QString PACKET_FIELD_RECV_USER  = "receiverUser";
static const QString PACKET_FIELD_RECV_EVENT = "receiverEvent";


ChatSystem::ChatSystem( webapp::WebApp* p_webApp, QObject* p_parent ) :
 QObject( p_parent ),
 _p_webApp( p_webApp )
{
    connect( _p_webApp->getConnection(), SIGNAL( onChannelChatPacket( m4e::comm::PacketPtr ) ), this, SLOT( onChannelChatPacket( m4e::comm::PacketPtr ) ) );
}

ChatSystem::~ChatSystem()
{
}

bool ChatSystem::sendToUser( ChatMessagePtr message )
{
    return createAndSendPacket( true, message );
}

bool ChatSystem::sendToEventMembers( ChatMessagePtr message )
{
    return createAndSendPacket( false, message );
}

void ChatSystem::onChannelChatPacket( comm::PacketPtr packet )
{
    log_verbose << TAG << "channel Chat received new packet" << std::endl;

    const QJsonDocument& doc = packet->getData();
    QJsonObject obj = doc.object();

    ChatMessagePtr msg = new ChatMessage();
    msg->setSender( packet->getSource() );
    msg->setTime( packet->getTime() );
    msg->setText( obj.value( PACKET_FIELD_TEXT ).toString( "" ) );
    QString document = obj.value( PACKET_FIELD_DOC ).toString( "" );
    if ( !document.isEmpty() )
    {
        doc::ModelDocumentPtr d = new doc::ModelDocument();
        if ( !d->fromJSON( document ) )
        {
            log_warning << TAG << "invalid document format detected, ignoring it" << std::endl;
        }
        else
        {
            msg->setDocument( d );
        }
    }

    QString recvuser  = obj.value( PACKET_FIELD_RECV_USER ).toString( "" );
    QString recvevent = obj.value( PACKET_FIELD_RECV_EVENT ).toString( "" );
    if ( !recvuser.isEmpty() )
    {
        msg->setReceiverId( recvuser );
        emit onReceivedChatMessageUser( msg );
    }
    else if ( !recvevent.isEmpty() )
    {
        msg->setReceiverId( recvevent );
        emit onReceivedChatMessageEvent( msg );
    }
    else
    {
        log_warning << TAG << "invalid recipient in chat message, ignore it" << std::endl;
    }
}

bool ChatSystem::createAndSendPacket( bool receiverUser, ChatMessagePtr message )
{
    user::ModelUserPtr user = _p_webApp->getUser()->getUserData();

    if ( !user.valid() )
        return false;

    QJsonObject obj;
    obj.insert( receiverUser ? PACKET_FIELD_RECV_USER : PACKET_FIELD_RECV_EVENT, message->getReceiverId() );
    obj.insert( PACKET_FIELD_TEXT , message->getText() );
    if ( message->getDocument().valid() )
        obj.insert( PACKET_FIELD_DOC, message->getDocument()->toJSON() );

    QJsonDocument chatdata ( obj );

    comm::PacketPtr packet = new comm::Packet();
    packet->setChannel( comm::Packet::CHANNEL_CHAT );
    packet->setSource( user->getName() );
    packet->setTime( message->getTime().isValid() ? message->getTime() : QDateTime::currentDateTime() );
    packet->setData( chatdata );

    return _p_webApp->getConnection()->sendPacket( packet );
}

} // namespace chat
} // namespace m4e
