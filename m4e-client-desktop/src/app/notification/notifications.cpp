/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */


#include "notifications.h"
#include <core/log.h>
#include <notification/notifyevent.h>
#include <webapp/webapp.h>


namespace m4e
{
namespace notify
{

Notifications::Notifications( webapp::WebApp* p_webApp, QObject* p_parent ) :
 QObject( p_parent ),
 _p_webApp( p_webApp )
{
    connect( _p_webApp->getConnection(), SIGNAL( onChannelNotifyPacket( m4e::comm::PacketPtr ) ), this, SLOT( onChannelNotifyPacket( m4e::comm::PacketPtr ) ) );
    connect( _p_webApp->getConnection(), SIGNAL( onChannelEventPacket( m4e::comm::PacketPtr ) ), this, SLOT( onChannelEventPacket( m4e::comm::PacketPtr ) ) );
}

Notifications::~Notifications()
{
}

bool Notifications::sendEventMessage( const QString& eventId,  const QString& title, const QString& text )
{
    if ( _p_webApp->getAuthState() != webapp::WebApp::AuthSuccessful )
    {
        log_error << TAG << "cannot send event message, no server connection!" << std::endl;
        return false;
    }

    comm::PacketPtr packet = new comm::Packet();
    packet->setChannel( comm::Packet::CHANNEL_EVENT );
    NotifyEventPtr notify = new NotifyEvent();
    notify->setType( "message" );
    notify->setSubject( title );
    notify->setText( text );
    // the event id is placed into notify data
    QJsonObject obj;
    obj.insert( "eventId", eventId );
    QJsonDocument doc( obj );
    notify->setData( doc );

    packet->setData( notify->toJSONDocument() );

    return _p_webApp->getConnection()->sendPacket( packet );
}

void Notifications::onChannelNotifyPacket( m4e::comm::PacketPtr packet )
{
    notify::NotifyEventPtr notify = new notify::NotifyEvent();
    notify->setPacket( packet );
    notify->fromJSON( packet->getData() );

    log_verbose << TAG << "new notification arrived: " << notify->getSubject() << std::endl;

    const QJsonDocument& doc = notify->getData();
    QJsonObject obj = doc.object();
    QString notifytype = notify->getType();

    if ( ( notifytype == "addevent" ) || ( notifytype == "removeevent"  ) || ( notifytype == "modifyevent"  ) )
    {
        Notifications::ChangeType changetype = ( notifytype == "addevent" ) ? Notifications::Added : ( notifytype == "removeevent" ) ? Notifications::Removed : Notifications::Modified;
        QString eventid = obj.value( "eventId" ).toString( "" );
        emit onEventChanged( changetype, eventid );
    }
    else if ( ( notifytype == "addlocation" ) || ( notifytype == "removelocation"  ) || ( notifytype == "modifylocation"  ) )
    {
        Notifications::ChangeType changetype = ( notifytype == "addlocation" ) ? Notifications::Added : ( notifytype == "removelocation" ) ? Notifications::Removed : Notifications::Modified;
        QString eventid = obj.value( "eventId" ).toString( "" );
        QString locationid = obj.value( "locationId" ).toString( "" );
        emit onEventLocationChanged( changetype, eventid, locationid );
    }
    else if ( notifytype == "modifyvote" )
    {
        QString eventid = obj.value( "eventId" ).toString( "" );
        QString locationid = obj.value( "locationId" ).toString( "" );
        bool    vote = obj.value( "vote" ).toBool( false );
        emit onEventLocationVote( packet->getSourceId(), packet->getSource(), eventid, locationid, vote );
    }
    else if ( notifytype == "onlinestatus" )
    {
        QString status = obj.value( "onlineStatus" ).toString( "" );
        bool online = status == "online";
        emit onUserOnlineStatusChanged( packet->getSourceId(), packet->getSource(), online );
    }
}

void Notifications::onChannelEventPacket( m4e::comm::PacketPtr packet )
{
    notify::NotifyEventPtr notify = new notify::NotifyEvent();
    notify->setPacket( packet );
    notify->fromJSON( packet->getData() );

    log_verbose << TAG << "new notification arrived: " << notify->getSubject() << std::endl;

    const QJsonDocument& doc = notify->getData();
    QJsonObject obj = doc.object();
    QString notifytype = notify->getType();

    if ( ( notifytype == "message" ) )
    {
        QString eventid = obj.value( "eventId" ).toString( "" );
        emit onEventMessage( packet->getSourceId(), packet->getSource(), eventid, notify );
    }
}

} // namespace notify
} // namespace m4e
