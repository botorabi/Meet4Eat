/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "chatsystem.h"
#include <core/log.h>


namespace m4e
{
namespace chat
{

ChatSystem::ChatSystem( webapp::WebApp* p_webApp, QObject* p_parent ) :
 QObject( p_parent ),
 _p_webApp( p_webApp )
{

}

ChatSystem::~ChatSystem()
{
}

bool ChatSystem::sendToUser( const QString& userId, const QString& message, doc::ModelDocumentPtr doc )
{
    return createAndSendPacket( true, userId, message, doc );
}

bool ChatSystem::sendToEventMembers( const QString& eventId, const QString& message, doc::ModelDocumentPtr doc )
{
    return createAndSendPacket( false, eventId, message, doc );
}

bool ChatSystem::createAndSendPacket( bool receiverUser, const QString& receiverId, const QString& message, doc::ModelDocumentPtr doc )
{
    user::ModelUserPtr user = _p_webApp->getUser()->getUserData();

    if ( !user.valid() )
        return false;

    QJsonObject obj;
    obj.insert( receiverUser ? "receiverUser" : "receiverEvent", receiverId );
    obj.insert( "text" , message );
    if ( doc.valid() )
        obj.insert( "document", doc->toJSON() );

    QJsonDocument chatdata ( obj );

    comm::PacketPtr packet = new comm::Packet();
    packet->setChannel( comm::Packet::CHANNEL_CHAT );
    packet->setSender( user->getName() );
    packet->setData( chatdata );

    return _p_webApp->getConnection()->sendPacket( packet );
}

} // namespace chat
} // namespace m4e
