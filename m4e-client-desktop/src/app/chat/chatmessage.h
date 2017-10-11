/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef CHATMESSAGE_H
#define CHATMESSAGE_H

#include <configuration.h>
#include <core/smartptr.h>
#include <document/modeldocument.h>
#include <QDateTime>


namespace m4e
{
namespace chat
{

class ChatMessage : public core::RefCount< ChatMessage >
{
    DECLARE_SMARTPTR_ACCESS( ChatMessage )

    public:

                                ChatMessage();

        /**
         * @brief Get the packet sender name.
         *
         * @return Packet sender
         */
        const QString&          getSender() const { return _sender; }

        /**
         * @brief Set the packet sender name.
         *
         * @param sender Packet sender
         */
        void                    setSender( const QString& sender ) { _sender = sender; }

        /**
         * @brief Get the packet receiver ID.
         *
         * @return Packet receiver ID
         */
        const QString&          getReceiverId() const { return _receiverId; }

        /**
         * @brief Set the packet receiver ID.
         *
         * @param sender Packet receiver ID
         */
        void                    setReceiverId( const QString& receiverId ) { _receiverId = receiverId; }

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
         * @brief Set the message text.
         *
         * @param text Message text
         */
        void                    setText( const QString& text ) { _text = text; }

        /**
         * @brief Get the message text.
         *
         * @return Message text
         */
        const QString&          getText() const { return _text; }

        /**
         * @brief Set the message document such as an image or file.
         *
         * @param document Message document
         */
        void                    setDocument( doc::ModelDocumentPtr document ) { _document = document; }

        /**
         * @brief Get the message document.
         *
         * @return Message document
         */
        doc::ModelDocumentPtr   getDocument() const { return _document; }

    protected:

        virtual                 ~ChatMessage() {}

        //! Omit copy construction!
                                ChatMessage( const ChatMessage& );

        QString                 _sender;
        QString                 _receiverId;
        QDateTime               _time;
        QString                 _text;
        doc::ModelDocumentPtr   _document;
};

typedef m4e::core::SmartPtr< ChatMessage > ChatMessagePtr;

} // namespace chat
} // namespace m4e

#endif // CHATMESSAGE_H
