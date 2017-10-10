/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include <core/log.h>
#include "connection.h"
#include <QApplication>


namespace m4e
{
namespace comm
{

Connection::Connection( QObject* p_parent ) :
 QObject( p_parent )
{
    _p_ws  = new webapp::Meet4EatWebSocket( this );
    connect( _p_ws, SIGNAL( onReceivedPacket( m4e::comm::PacketPtr ) ), this, SLOT( onReceivedPacket( m4e::comm::PacketPtr ) ) );
    connect( _p_ws, SIGNAL( onConnectionEstablished() ), this, SLOT( onConnectionEstablished() ) );
    connect( _p_ws, SIGNAL( onConnectionClosed() ), this, SLOT( onConnectionClosed() ) );
}

Connection::~Connection()
{
}

void Connection::setServerURL( const QString& url )
{
    _p_ws->setWsURL( url );
}

const QString& Connection::getServerURL() const
{
    return _p_ws->getWsURL();
}

void Connection::connectServer()
{
    _p_ws->establishConnection();
}

void Connection::closeConnection()
{
    _p_ws->shutdownConnection();
}

bool Connection::sendPacket( PacketPtr packet )
{
    return _p_ws->sendPacket( packet );
}

void Connection::onConnectionEstablished()
{
    emit onConnection( true, "" );
}

void Connection::onConnectionClosed()
{
    emit onClose();
}

void Connection::onReceivedPacket( PacketPtr packet )
{
    if ( packet->getChannel() == Packet::CHANNEL_SYSTEM ) {
        emit onChannelSystemPacket( packet );
    }
    else if ( packet->getChannel() == Packet::CHANNEL_CHAT ) {
        emit onChannelChatPacket( packet );
    }
    else if ( packet->getChannel() == Packet::CHANNEL_NOTIFY ) {
        emit onChannelNotifyPacket( packet );
    }
    else if ( packet->getChannel() == Packet::CHANNEL_EVENT ) {
        emit onChannelEventPacket( packet );
    }
}

} // namespace comm
} // namespace m4e
