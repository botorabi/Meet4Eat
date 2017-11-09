/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef CONNECTION_H
#define CONNECTION_H

#include <configuration.h>
#include <communication/packet.h>
#include <webapp/m4e-api/m4e-ws.h>
#include <QObject>


namespace m4e
{
namespace comm
{

/**
 * @brief This class provides real-time communication with the app server.
 *
 * @author boto
 * @date Oct 8, 2017
 */
class Connection : public QObject
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(Connection) ";

    Q_OBJECT

    public:

        /**
         * @brief Construct a Connection instance.
         *
         * @param p_parent Parent object
         */
        explicit                Connection( QObject* p_parent );

        /**
         * @brief Destruct Connection instance
         */
        virtual                 ~Connection();

        /**
         * @brief Set webapp server's URL including port number. Set this URL before using any services below.
         *
         * @param url Server's URL
         */
        void                    setServerURL( const QString& url );

        /**
         * @brief Get webapp's server URL.
         *
         * @return Server URL
         */
        const QString&          getServerURL() const;

        /**
         * @brief Try to connect the web app server. The result will be notified by signal 'onConnection'.
         */
        void                    connectServer();

        /**
         * @brief Close the connection to web app server.
         */
        void                    closeConnection();

        /**
         * @brief Send the given packet.
         *
         * @param packet Network packet to send
         * @return Return false if something went wrong.
         */
        bool                    sendPacket( m4e::comm::PacketPtr packet );

    signals:

        /**
         * @brief This signal is emitted once the WebSocket connection was established.
         *
         * @param success True if the connection was successful, otherwise false
         * @param reason  A reason string for a failed connection
         */
        void                    onConnection( bool success, QString reason );

        /**
         * @brief Notify about closing the connection.
         */
        void                    onClosedConnection();

        /**
         * @brief This signal notifies about a new incoming network packet in channel 'System'.
         *
         * @param packet Arrived System channel packet
         */
        void                    onChannelSystemPacket( m4e::comm::PacketPtr packet );

        /**
         * @brief This signal notifies about a new incoming network packet in channel 'Chat'.
         *
         * @param packet Arrived Chat channel packet
         */
        void                    onChannelChatPacket( m4e::comm::PacketPtr packet );

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

    protected slots:

        /**
         * @brief This signal is emitted once the WebSocket connection was established.
         */
        void                    onConnectionEstablished();

        /**
         * @brief Notify about disconnection
         */
        void                    onConnectionClosed();

        /**
         * @brief This signal notifies about a new incoming network packet.
         *
         * @param packet Arrived network packet
         */
        void                    onReceivedPacket( m4e::comm::PacketPtr packet );

    protected:

        webapp::Meet4EatWebSocket*  _p_ws = nullptr;
};

} // namespace comm
} // namespace m4e

#endif // CONNECTION_H
