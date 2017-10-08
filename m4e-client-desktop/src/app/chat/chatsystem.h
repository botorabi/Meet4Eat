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
         * @param userId    The recipient ID
         * @param message   Message to send
         * @param doc       Optional document to send, e.g. an image
         * @return          Return true if the message was successfully sent.
         */
        bool                    sendToUser( const QString& userId, const QString& message, doc::ModelDocumentPtr doc = doc::ModelDocumentPtr() );

        /**
         * @brief Send a text message to all event members.
         *
         * @param eventId   The event ID
         * @param message   Message to send
         * @param doc       Optional document to send, e.g. an image
         * @return          Return true if the message was successfully sent.
         */
        bool                    sendToEventMembers( const QString& eventId, const QString& message, doc::ModelDocumentPtr doc = doc::ModelDocumentPtr() );

    signals:

        //! TODO

    protected slots:

        //! TODO

    protected:

        bool                    createAndSendPacket( bool receiverUser, const QString& receiverId, const QString& message, doc::ModelDocumentPtr doc );

        webapp::WebApp*         _p_webApp = nullptr;
};

} // namespace chat
} // namespace m4e

#endif // CHATSYSTEM_H
