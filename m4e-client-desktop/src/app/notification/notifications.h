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
 * @brief This class handles incoming notifications and distributes them in the app.
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

    protected slots:

        /**
         * @brief This signal notifies about a new incoming network packet in channel 'Notify'.
         *
         * @param packet Arrived Notify channel packet
         */
        void                    onChannelNotifyPacket( m4e::comm::PacketPtr packet );

    protected:

        webapp::WebApp*          _p_webApp = nullptr;
};

} // namespace notify
} // namespace m4e

#endif // NOTIFICATIONS_H
