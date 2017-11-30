/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef M4E_WS_H
#define M4E_WS_H

#include <configuration.h>
#include <communication/packet.h>
#include <QtWebSockets/QtWebSockets>


namespace m4e
{
namespace webapp
{

/**
 * @brief This class handles the WebSocket based communication with server.
 *
 * @author boto
 * @date Oct 7, 2017
 */
class Meet4EatWebSocket : public QObject
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(WebSocket) ";

    Q_OBJECT

    public:

        /**
         * @brief Construct an instance.
         *
         * @param p_parent   Optional parent object
         */
        explicit                Meet4EatWebSocket( QObject* p_parent );

        /**
         * @brief Destroy instance.
         */
        virtual                 ~Meet4EatWebSocket();

        /**
         * @brief Get the WebSocket URL.
         *
         * @return WebSocket URL
         */
        const QString&          getWsURL() const;

        /**
         * @brief Set web application's WebSocket URL including the port number, e.g. ws://myserver:8080/ws
         *
         * @param wsURL  WebSocket URL
         */
        void                    setWsURL( const QString& wsURL );

        /**
         * @brief Enable/disable periodic WebSocket server pings. The Internval is defined by M4E_PERIOD_SRV_UPDATE_STATUS
         *
         * @param enable    Pass true to enable, false for disable
         */
        void                    enablePing( bool enable );

        /**
         * @brief Establish a WebSocket connection to server. If there is already a connection then it will be closed first.
         * The results are delivered by signals 'onConnectionEstablished'.
         *
         * @return Return true if the request for connection was successful.
         */
        bool                    establishConnection();

        /**
         * @brief Get the web app protocol version. Only valid after a successful connection to server.
         *
         * @return Web app protocol version
         */
        const QString&          getWebAppProtocolVersion() const;

        /**
         * @brief Close the connection.
         */
        void                    shutdownConnection();

        /**
         * @brief Send the given packet.
         *
         * @param packet Network packet to send
         * @return Return false if something went wrong.
         */
        bool                    sendPacket( comm::PacketPtr packet );

    protected slots:

        void                    onConnected();

        void                    onDisconnected();

        void                    onError( QAbstractSocket::SocketError error );

        void                    onTextMessageReceived( QString message );

        void                    onPingTimer();

        void                    onPongReceived( quint64 elapsedTime, const QByteArray& payload );

    signals:

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

        /**
         * @brief Setup a network request by re-using the session cookie already created
         * during sign-in process (by REST services).
         *
         * @param request   Network request to setup
         * @return          Return true if successful, otherwise false.
         */
        bool                    setupNetworkRequest( QNetworkRequest& request );

        /**
         * @brief Given the connection response packet from server, extract the web app protocol version.
         *
         * @param packet    First packet received after connection
         * @return          Web app protocol version
         */
        QString                 getProtocolVersion( comm::PacketPtr packet );

        /**
         * @brief Send a text message to server.
         *
         * @param message Message to send
         * @return Return false if not successful.
         */
        bool                    sendMessage( const QString& message );

        QString                 _wsURL;

        QWebSocket*             _p_webSocket    = nullptr;

        QTimer*                 _p_pingTimer    = nullptr;

        bool                    _enablePing     = false;

        QString                 _webAppProtVersion;
};

} // namespace webapp
} // namespace m4e

#endif // M4E_WS_H
