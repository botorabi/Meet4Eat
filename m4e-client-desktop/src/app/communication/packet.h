/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef PACKET_H
#define PACKET_H

#include <configuration.h>
#include <core/smartptr.h>
#include <QJsonDocument>
#include <QDateTime>


namespace m4e
{
namespace comm
{

/**
 * @brief A class describing a network packet used for communication with server.
 *        JSON format is used for exchanging data with the web app server.
 *
 * @author boto
 * @date Oct 8, 2017
 */
class Packet : public core::RefCount< Packet >
{
    DECLARE_SMARTPTR_ACCESS( Packet )

    public:

        //! The communication channel IDs
        static const QString CHANNEL_SYSTEM;
        static const QString CHANNEL_NOTIFY;
        static const QString CHANNEL_CHAT;
        static const QString CHANNEL_EVENT;


        /**
         * @brief Construct a Packet instance.
         */
                                Packet();

        /**
         * @brief Get the communication channel ID, one of CHANNEL_xx strings.
         *
         * @return Communication channel
         */
        const QString&          getChannel() const { return _channel; }

        /**
         * @brief Set the communication channel ID, one of CHANNEL_xx strings.
         *
         * @param channel Communication channel
         */
        void                    setChannel( const QString& channel ) { _channel = channel; }

        /**
         * @brief Get the packet sender.
         *
         * @return Packet sender
         */
        const QString&          getSender() const { return _sender; }

        /**
         * @brief Set the packet sender.
         *
         * @param sender Packet sender
         */
        void                    setSender( const QString& sender ) { _sender = sender; }

        /**
         * @brief Get the packet send/receive time.
         *
         * @return Packet's send/receive timestamp
         */
        const QDateTime&        getTime() const { return _time; }

        /**
         * @brief Set the packet send/receive time.
         *
         * @param time Packet's send/receive timestamp
         */
        void                    setTime( const QDateTime& time ) { _time = time; }

        /**
         * @brief Get the packet payload.
         *
         * @return Packet data
         */
        const QJsonDocument&    getData() const { return _data; }

        /**
         * @brief Set the packet payload.
         *
         * @param data Packet data
         */
        void                    setData( const QJsonDocument& data ) { _data = data; }

        /**
         * @brief Create a JSON string representing the packet.
         *
         * @return Packet in JSON format
         */
        QString                 toJSON();

        /**
         * @brief Setup the packet given a JSON string.
         *
         * @param input JSON string
         * @return Return false if an invalid JSON string was passed, otherwise true.
         */
        bool                    fromJSON( const QString& input );

    protected:

        virtual                 ~Packet() {}

        //! Omit copy construction!
                                Packet( const Packet& );

        QString                 _channel;
        QString                 _sender;
        QDateTime               _time;
        QJsonDocument           _data;
};

typedef m4e::core::SmartPtr< Packet > PacketPtr;

} // namespace webapp
} // namespace m4e

#endif // PACKET_H
