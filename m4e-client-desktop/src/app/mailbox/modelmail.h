/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef MODELMAIL_H
#define MODELMAIL_H

#include <configuration.h>
#include <core/smartptr.h>
#include <QDateTime>
#include <QString>
#include <QList>


namespace m4e
{
namespace mailbox
{

/**
 * @brief Class for holding mail data
 *
 * @author boto
 * @date Nov 1, 2017
 */
class ModelMail : public m4e::core::RefCount< ModelMail >
{
    SMARTPTR_DEFAULTS( ModelMail )

    public:

        /**
         * @brief Construct an instance.
         */
                                        ModelMail() {}

        /**
         * @brief Get the unique ID.
         *
         * @return The ID
         */
        const QString&                  getId() const { return _id; }

        /**
         * @brief Set the unique ID.
         *
         * @param id    The ID
         */
        void                            setId( const QString& id ) { _id = id; }

        /**
         * @brief Get the sender ID.
         *
         * @return The sender ID
         */
        const QString&                  getSenderId() const { return _senderId; }

        /**
         * @brief Set the sender ID.
         *
         * @param senderId   The sender ID
         */
        void                            setSenderId( const QString& senderId ) { _senderId = senderId; }

        /**
         * @brief Get the sender name.
         *
         * @return The sender name
         */
        const QString&                  getSenderName() const { return _senderName; }

        /**
         * @brief Set the sender name.
         *
         * @param senderName   The sender name
         */
        void                            setSenderName( const QString& senderName ) { _senderName = senderName; }

        /**
         * @brief Get the receiver ID.
         *
         * @return The receiver ID
         */
        const QString&                  getReceiverId() const { return _receiverId; }

        /**
         * @brief Set the receiver ID.
         *
         * @param receiverId   The receiver ID
         */
        void                            setReceiverId( const QString& receiverId ) { _receiverId = receiverId; }

        /**
         * @brief Get the receiver name.
         *
         * @return The receiver name
         */
        const QString&                  getReceiverName() const { return _receiverName; }

        /**
         * @brief Set the receiver name.
         *
         * @param receiverName   The receiver name
         */
        void                            setReceiverName( const QString& receiverName ) { _receiverName = receiverName; }

        /**
         * @brief Get the mail subject.
         *
         * @return The subject
         */
        const QString&                  getSubject() const { return _subject; }

        /**
         * @brief Set the mail subject.
         *
         * @param subject  The subject
         */
        void                            setSubject( const QString& subject ) { _subject = subject; }

        /**
         * @brief Get the mail content.
         *
         * @return The mail content
         */
        const QString&                  getContent() const { return _content; }

        /**
         * @brief Set the mail content.
         *
         * @param content  The content
         */
        void                            setContent( const QString& content ) { _content = content; }

        /**
         * @brief Get the mail delivery date.
         *
         * @return Delivery date
         */
        QDateTime                       getDate() const { return _date; }

        /**
         * @brief Set the mail delivery date.
         *
         * @param date The delivery date
         */
        void                            setDate( const QDateTime& date ) { _date = date; }

        /**
         * @brief Set the read/unread state.
         *
         * @param  unread  Unread flag
         */
        void                            setUnread( bool unread ) { _isUnread = unread; }

        /**
         * @brief Is the mail unread?
         *
         * @return Return true if the mail is unread.
         */
        bool                            isUnread() const { return _isUnread; }

        /**
         * @brief Is the mail trashed?
         *
         * @return Return true if the mail is trashed.
         */
        bool                            isTrashed() const { return _isTrashed; }

        /**
         * @brief Create a JSON string out of the mail model.
         *
         * @return JSON document representing the mail
         */
        QJsonDocument                   toJSON();

        /**
         * @brief Setup the mail given a JSON formatted string.
         *
         * @param input Input string in JSON format
         * @return Return false if the input was not in proper format.
         */
        bool                            fromJSON( const QString& input );

        /**
         * @brief Setup the mail given a JSON document.
         *
         * @param input Input in JSON document
         * @return Return false if the input was not in proper format.
         */
        bool                            fromJSON( const QJsonDocument& input );

        /**
         * @brief Comparison operator which considers the mail ID.
         * @param right     Right hand of operation.
         * @return true if both mails have the same ID, otherwise false.
         */
        bool                            operator == ( const ModelMail& right ) { return _id == right.getId(); }

        /**
         * @brief Unequal operator which considers the mail ID.
         * @param right     Right hand of operation.
         * @return true if both mails have the same ID, otherwise false.
         */
        bool                            operator != ( const ModelMail& right ) { return _id != right.getId(); }

    protected:

        QString                         _id;
        QDateTime                       _date;
        QString                         _senderId;
        QString                         _senderName;
        QString                         _receiverId;
        QString                         _receiverName;
        QString                         _subject;
        QString                         _content;
        bool                            _isUnread  = true;
        bool                            _isTrashed = false;
};

typedef m4e::core::SmartPtr< ModelMail > ModelMailPtr;

} // namespace mailbox
} // namespace m4e

Q_DECLARE_METATYPE( m4e::mailbox::ModelMailPtr )

#endif // MODELMAIL_H
