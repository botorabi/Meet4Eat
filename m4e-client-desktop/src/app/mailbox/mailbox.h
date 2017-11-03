/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef MAILBOX_H
#define MAILBOX_H

#include <configuration.h>
#include <webapp/request/rest-mailbox.h>
#include <mailbox/modelmail.h>
#include <QObject>


namespace m4e
{
namespace mailbox
{

/**
 * @brief This class provides access to user's mailbox.
 *
 * @author boto
 * @date Nov 1, 2017
 */
class MailBox : public QObject
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(MailBox) ";

    Q_OBJECT

    public:

        /**
         * @brief Construct a mailbox instance.
         *
         * @param p_parent Parent object
         */
        explicit                        MailBox( QObject* p_parent );

        /**
         * @brief Destruct User instance
         */
        virtual                         ~MailBox();

        /**
         * @brief Set mailbox server's URL including port number. Set this URL before using any services below.
         *
         * @param url Server's URL
         */
        void                            setServerURL( const QString& url );

        /**
         * @brief Get mailbox' server URL.
         *
         * @return Server URL
         */
        const QString&                  getServerURL() const;

        /**
         * @brief Get the error which occurred while the last request. Use this if a response delivers a 'success' set to false.
         *
         * @return Last error
         */
        const QString&                  getLastError() const { return _lastError; }

        /**
         * @brief Get the error code set by REST response, see getLastError above.
         *
         * @return Last error code
         */
        const QString&                  getLastErrorCode() const { return _lastErrorCode; }

        /**
         * @brief Get the mail with given ID.
         *
         * @param mailId    Mail ID
         * @return          Return the mail, or an empty object if the ID was not found.
         */
        mailbox::ModelMailPtr           getMail( const QString& mailId );

        /**
         * @brief Get all mails.
         *
         * @return All mails
         */
        QList< mailbox::ModelMailPtr >  getAllMails();

        /**
         * @brief Request for the count of unread mails. The results are emitted by signal 'onReponseCountUnreadMail'.
         */
        void                            requestCountUnreadMails();

        /**
         * @brief Request for getting user's mails in given range. The results are emitted by signal 'onResponseMails'.
         * The user must be authenticated before using this request.
         *
         * @param from  Range begin
         * @param to    Range end
         */
        void                            requestMails( int from, int to );

        /**
         * @brief Request for sending a mail. The results are emitted by signal 'onReponseSendMail'.
         *
         * @param mail  The mail to send.
         */
        void                            requestSendMail( mailbox::ModelMailPtr mail );

        /**
         * @brief Request for trashing a mail. The results are emitted by signal 'onResponsePerformOperation'.
         *
         * @param mailId    ID of the mail to trash
         */
        void                            requestDeleteMail( const QString& mailId );

        /**
         * @brief Request for untrashing a mail. The results are emitted by signal 'onResponsePerformOperation'.
         *
         * @param mailId    ID of the mail to untrash
         */
        void                            requestUndeleteMail( const QString& mailId );

        /**
         * @brief Request for marking a mail as 'read' or 'unread'. The results are emitted by signal 'onResponsePerformOperation'.
         *
         * @param mailId    ID of the mail to mark
         */
        void                            requestMarkMail( const QString& mailId, bool read );

    signals:

        /**
         * @brief Results of unread mails count request.
         *
         * @param success  true if the count of unread mails could successfully be retrieved, otherwise false
         * @param count    Count of unread mails
         */
        void                            onResponseCountUnreadMails( bool success, int count );

        /**
         * @brief Results of user's mails request.
         *
         * @param success  true if user mails could successfully be retrieved, otherwise false
         * @param mails    User's mails
         */
        void                            onResponseMails( bool success, QList< m4e::mailbox::ModelMailPtr > mails );

        /**
         * @brief Results of mail sending
         *
         * @param success   true if the mail was successfully sent, otherwise false
         */
        void                            onResponseSendMail( bool success );

        /**
         * @brief Results of request for performing a mail operation
         *
         * @param success   true if the operation was successfully performed
         * @param mailId    ID of the deleted mail
         * @param operation Performed mail opration
         */
        void                            onResponsePerformOperation( bool success, QString mailId, QString operation );

    protected slots:

        /**
         * @brief Receive the results of requestCountUnreadMails request.
         *
         * @param count     The count of unread mails
         */
        void                            onRESTMailCountUnreadMails( int count );

        /**
         * @brief Signal is received when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                            onRESTMailErrorCountUnreadMails( QString errorCode, QString reason );

        /**
         * @brief Receive the results of requestMails request.
         *
         * @param mails     User's mails
         */
        void                            onRESTMailGetMails( QList< m4e::mailbox::ModelMailPtr > mails );

        /**
         * @brief Signal is received when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                            onRESTMailErrorGetMails( QString errorCode, QString reason );

        /**
         * @brief Receive the results of sendMail request. The mail was successfully sent.
         */
        void                            onRESTMailSendMail();

        /**
         * @brief Signal is emitted when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                            onRESTMailErrorSendMail( QString errorCode, QString reason );

        /**
         * @brief Receive the results of requestMailOperation request.
         *
         * @param mailId    ID of mail which was deleted
         */
        void                            onRESTMailPerformOperation( QString mailId, QString operation );

        /**
         * @brief Signal is received when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                            onRESTMailErrorPerformOperation( QString errorCode, QString reason );

    protected:

        void                            setLastError( const QString& error ="", const QString& errorCode ="" );

        webapp::RESTMailBox*            _p_restMailBox  = nullptr;

        QList< mailbox::ModelMailPtr >  _mails;

        QString                         _lastError;

        QString                         _lastErrorCode;
};

} // namespace mailbox
} // namespace m4e

#endif // MAILBOX_H
