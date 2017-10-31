/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef NOTIFYEVENT_H
#define NOTIFYEVENT_H

#include <configuration.h>
#include <core/smartptr.h>
#include <communication/packet.h>
#include <QObject>
#include <QString>
#include <QJsonDocument>


namespace m4e
{
namespace notify
{

/**
 * @brief This class contains notification information. Notifications
 * are used in real-time communication with the app server and inform
 * the user about news.
 *
 * @author boto
 * @date Aug 13, 2017
 */
class NotifyEvent : public core::RefCount< NotifyEvent >
{
    SMARTPTR_DEFAULTS( NotifyEvent )

    public:
                                NotifyEvent();

        /**
         * @brief Set the notification type.
         *
         * @param type Notification type
         */
        void                    setType( const QString& type )  { _type = type; }

        /**
         * @brief Get the notification type.
         *
         * @return Notification type
         */
        const QString&          getType() const { return _type; }

        /**
         * @brief Set the subject.
         *
         * @param type The subject
         */
        void                    setSubject( const QString& subject )  { _subject = subject; }

        /**
         * @brief Get the subject.
         *
         * @return The subject
         */
        const QString&          getSubject() const { return _subject; }

        /**
         * @brief Set the notification text.
         *
         * @param type The text
         */
        void                    setText( const QString& text )  { _text = text; }

        /**
         * @brief Get the text.
         *
         * @return The text
         */
        const QString&          getText() const { return _text; }

        /**
         * @brief If the notification has any data then set it here.
         *
         * @param data Notification data
         */
        void                    setData( const QJsonDocument& data ) { _data = data; }

        /**
         * @brief Get the notification data.
         *
         * @return Notification data
         */
        const QJsonDocument&    getData() const { return _data; }

        /**
         * @brief Create a JSON string out of the notification instance.
         *
         * @return JSON formatted string representing the document
         */
        QString                 toJSON();

        /**
         * @brief Create a JSON document out of the notification instance.
         *
         * @return JSON document representing this document
         */
        QJsonDocument           toJSONDocument();

        /**
         * @brief Setup the notification given a JSON formatted string.
         *
         * @param input Input string in JSON format
         * @return Return false if the input was not in proper format.
         */
        bool                    fromJSON( const QString& input );

        /**
         * @brief Setup the document given a JSON notification.
         *
         * @param input Input in JSON document
         * @return Return false if the input was not in proper format.
         */
        bool                    fromJSON( const QJsonDocument& input );

        /**
         * @brief Set the original packet which was used for distributing the notification.
         *
         * @param packet Network packet used for distribution
         */
        void                    setPacket( comm::PacketPtr packet ) { _packet = packet; }

        /**
         * @brief Get the network packet used for distributing the notification.
         *
         * @return The network packet.
         */
        comm::PacketPtr         getPacket() { return _packet; }

    protected:

        comm::PacketPtr         _packet;
        QString                 _type;
        QString                 _subject;
        QString                 _text;
        QJsonDocument           _data;
};

typedef m4e::core::SmartPtr< NotifyEvent > NotifyEventPtr;

} // namespace notify
} // namespace m4e

Q_DECLARE_METATYPE( m4e::notify::NotifyEventPtr )

#endif // NOTIFYEVENT_H
