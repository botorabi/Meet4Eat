/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef CHATSYSTEM_H
#define CHATSYSTEM_H

#include <configuration.h>
#include <communication/connection.h>
#include <document/modeldocument.h>
#include <chat/chatmessage.h>
#include <webapp/webapp.h>
#include <QObject>


namespace m4e
{
namespace chat
{

/**
 * @brief Class handling incoming chat messages.
 *
 * @author boto
 * @date Oct 8, 2017
 */
class ChatSystem : public QObject
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(ChatSystem) ";

    Q_OBJECT

    public:

        /**
         * @brief Create a chat system instance.
         *
         * @param p_webApp  Web application interface
         * @param p_parent  Parent object
         */
                                ChatSystem( webapp::WebApp* p_webApp, QObject* p_parent );


        /**
         * @brief Destroy the ChatSystem instance.
         */
        virtual                 ~ChatSystem();

        /**
         * @brief Send a text message to given user.
         *
         * @param message   Message to send
         * @return          Return true if the message was successfully sent.
         */
        bool                    sendToUser( ChatMessagePtr message );

        /**
         * @brief Send a text message to all event members.
         *
         * @param message   Message to send
         * @return          Return true if the message was successfully sent.
         */
        bool                    sendToEventMembers( ChatMessagePtr message );

    signals:

        /**
         * @brief Notify about a new user chat message.
         *
         * @param msg Chat message
         */
        void                    onReceivedChatMessageUser( m4e::chat::ChatMessagePtr msg );

        /**
         * @brief Notify about a new event chat message.
         *
         * @param msg Chat message
         */
        void                    onReceivedChatMessageEvent( m4e::chat::ChatMessagePtr msg );

    protected slots:

        /**
         * @brief This signal notifies about a new incoming network packet in channel 'Chat'.
         *
         * @param packet Arrived Chat channel packet
         */
        void                    onChannelChatPacket( m4e::comm::PacketPtr packet );

    protected:

        bool                    createAndSendPacket( bool receiverUser, ChatMessagePtr message );

        webapp::WebApp*         _p_webApp = nullptr;
};

} // namespace chat
} // namespace m4e

#endif // CHATSYSTEM_H
