/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef NOTIFICATIONS_H
#define NOTIFICATIONS_H

#include <configuration.h>
#include <core/smartptr.h>
#include <communication/packet.h>
#include <notification/notifyevent.h>
#include <QObject>
#include <QString>


namespace m4e
{
namespace webapp
{
 class WebApp;
}
namespace notify
{

/**
 * @brief This class handles incoming notifications and distributes them in the app. It also provides functionality to send notifications.
 *
 * @author boto
 * @date Aug 13, 2017
 */
class Notifications : public QObject
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(Notifications) ";

    Q_OBJECT

    public:

        /**
         * @brief Resource change types
         */
        enum ChangeType
        {
            Added,
            Removed,
            Modified
        };

        /**
         * @brief Create a notifications instance.
         *
         * @param p_webApp  Web application interface
         * @param p_parent  Parent object
         */
                                Notifications( webapp::WebApp* p_webApp, QObject* p_parent );

        /**
         * @brief Destroy the Notifications instance.
         */
        virtual                 ~Notifications();

        /**
         * @brief Send a message to all event members. This can be used e.g. to buzz event members.
         *
         * @param eventId   ID of receiving event
         * @param title     Message title
         * @param text      Message text
         * @return          Return true if the message was successfully sent, otherwise false
         */
        bool                    sendEventMessage( const QString& eventId,  const QString& title, const QString& text );

    signals:

        /**
         * @brief This signal is emitted when an event was changed.
         *
         * @param changeType One of ChangeType enums
         * @param eventId    Event ID
         */
        void                    onEventChanged( m4e::notify::Notifications::ChangeType changeType, QString eventId );

        /**
         * @brief This signal is emitted when an event location was changed.
         *
         * @param changeType One of ChangeType enums
         * @param eventId    Event ID
         * @param loactionId Event location ID
         */
        void                    onEventLocationChanged( m4e::notify::Notifications::ChangeType changeType, QString eventId, QString locationId );

        /**
         * @brief This signal is emitted when an event location vote arrives.
         *
         * @param senderId   User ID of the voter
         * @param senderName User name of the voter
         * @param eventId    Event ID
         * @param loactionId Event location ID
         * @param vote       true for vote and false for unvote the given location
         */
        void                    onEventLocationVote( QString senderId, QString senderName, QString eventId, QString locationId, bool vote );

        /**
         * @brief Notify about a user's online status.
         *
         * @param senderId      User ID
         * @param senderName    User Name
         * @param online        true if the user went online, otherwise false for user going offline
         */
        void                    onUserOnlineStatusChanged( QString senderId, QString senderName, bool online );

        /**
         * @brief This signal is emitted  when an event message was arrived. An event message can be used to buzz all event members.
         *
         * @param senderId      Message sender Id (usually an user ID)
         * @param senderName    Message sender's name
         * @param eventId       ID of receiving event
         * @param notify        Notification object containing the message content
         */
        void                    onEventMessage( QString senderId, QString senderName, QString eventId, m4e::notify::NotifyEventPtr notify );

    protected slots:

        /**
         * @brief This signal notifies about a new incoming network packet in channel 'Notify'.
         *
         * @param packet Arrived Notify channel packet
         */
        void                    onChannelNotifyPacket( m4e::comm::PacketPtr packet );

        /**
         * @brief This signal notifies about a new incoming network packet in channel 'Event'.
         *
         * @param packet Arrived Event channel packet
         */
        void                    onChannelEventPacket( m4e::comm::PacketPtr packet );

    protected:

        webapp::WebApp*          _p_webApp = nullptr;
};

} // namespace notify
} // namespace m4e

#endif // NOTIFICATIONS_H
