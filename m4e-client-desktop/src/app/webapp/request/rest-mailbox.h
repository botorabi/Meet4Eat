/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef REST_MAILBOX_H
#define REST_MAILBOX_H

#include <configuration.h>
#include <webapp/m4e-api/m4e-rest.h>
#include <mailbox/modelmail.h>


namespace m4e
{
namespace webapp
{

/**
 * @brief Class handling the mailbox related web app interaction
 *
 * @author boto
 * @date Nov 1, 2017
 */
class RESTMailBox : public Meet4EatREST
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(RESTMailBox) ";

    Q_OBJECT

    public:

        /**
         * @brief Construct an instance.
         *
         * @param p_parent Parent instance
         */
        explicit                RESTMailBox( QObject* p_parent );

        /**
         * @brief Destroy instance.
         */
        virtual                 ~RESTMailBox();

        /**
         * @brief Get the count of user's unread mails. The results are emitted by signal 'onRESTMailCountUnreadMails'.
         */
        void                    getCountUnreadMails();

        /**
         * @brief Get user's mails in a given range. Pass 0/0 to get all mails.
         * The results are emitted by signal 'onRESTMailGetMails'.
         *
         * @param from  Range begin
         * @param to    Range end
         */
        void                    getMails( int from, int to );

        /**
         * @brief Send a mail to another user. The results are emitted by signal 'onRESTMailSendMail'.
         *
         * @param mail  The mail to send
         */
        void                    sendMail( m4e::mailbox::ModelMailPtr mail );

        /**
         * @brief Perform a mail operation. The results are emitted by signal 'onRESTMailPerformOperation'.
         *
         * @param mailId    ID of mail to operate on
         * @param operation One of supported operations
         */
        void                    performMailOperation( const QString& mailId, const QString& operation );

    signals:

        /**
         * @brief Emit the results of getCountUnreadMails.
         *
         * @param count     The count of unread mails
         */
        void                    onRESTMailCountUnreadMails( int count );

        /**
         * @brief Signal is emitted when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                    onRESTMailErrorCountUnreadMails( QString errorCode, QString reason );

        /**
         * @brief Emit the results of getMails request.
         *
         * @param mails    User's mails
         */
        void                    onRESTMailGetMails( QList< m4e::mailbox::ModelMailPtr > mails );

        /**
         * @brief Signal is emitted when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                    onRESTMailErrorGetMails( QString errorCode, QString reason );

        /**
         * @brief Emit the results of sendMail request. The mail was successfully sent.
         */
        void                    onRESTMailSendMail();

        /**
         * @brief Signal is emitted when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                    onRESTMailErrorSendMail( QString errorCode, QString reason );

        /**
         * @brief Emit the results of a mail operation request.
         *
         * @param mailId     ID of the mail
         * @param operation  The requested mail operation
         */
        void                    onRESTMailPerformOperation( QString mailId, QString operation );

        /**
         * @brief Signal is emitted when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                    onRESTMailErrorPerformOperation( QString errorCode, QString reason );
};

} // namespace webapp
} // namespace m4e

#endif // REST_MAILBOX_H
