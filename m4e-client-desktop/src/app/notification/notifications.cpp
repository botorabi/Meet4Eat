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
}

Notifications::~Notifications()
{
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
        QString eventid = QString::number( obj.value( "eventId" ).toInt( 0 ) );
        emit onEventChanged( changetype, eventid );
    }
    else if ( ( notifytype == "addlocation" ) || ( notifytype == "removelocation"  ) || ( notifytype == "modifylocation"  ) )
    {
        Notifications::ChangeType changetype = ( notifytype == "addlocation" ) ? Notifications::Added : ( notifytype == "removelocation" ) ? Notifications::Removed : Notifications::Modified;
        QString eventid = QString::number( obj.value( "eventId" ).toInt( 0 ) );
        QString locationid = QString::number(  obj.value( "locationId" ).toInt( 0 ) );
        emit onEventLocationChanged( changetype, eventid, locationid );
    }
}

} // namespace notify
} // namespace m4e
