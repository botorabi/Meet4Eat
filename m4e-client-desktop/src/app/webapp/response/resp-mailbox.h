/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef RESP_MAILBOX_H
#define RESP_MAILBOX_H

#include <configuration.h>
#include <webapp/m4e-api/m4e-response.h>
#include <QJsonDocument>


namespace m4e
{
namespace webapp
{

class RESTMailBox;

/**
 * @brief Response handler for GetMails
 *
 * @author boto
 * @date Nov 1, 2017
 */
class ResponseGetMails: public Meet4EatRESTResponse
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(ResponseGetMails) ";

    public:

        explicit        ResponseGetMails( RESTMailBox* p_requester );

        void            onRESTResponseSuccess( const QJsonDocument& results );

        void            onRESTResponseError( const QString& reason );

    protected:

        RESTMailBox*    _p_requester;
};

/**
 * @brief Response handler for SendMail
 *
 * @author boto
 * @date Nov 1, 2017
 */
class ResponseSendMail: public Meet4EatRESTResponse
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(ResponseSendMail) ";

    public:

        explicit        ResponseSendMail( RESTMailBox* p_requester );

        void            onRESTResponseSuccess( const QJsonDocument& results );

        void            onRESTResponseError( const QString& reason );

    protected:

        RESTMailBox*    _p_requester;
};

/**
 * @brief Response handler for PerformMailOperation
 *
 * @author boto
 * @date Nov 1, 2017
 */
class PerformMailOperation: public Meet4EatRESTResponse
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(ResponseDeleteMail) ";

    public:

        explicit        PerformMailOperation( RESTMailBox* p_requester );

        void            onRESTResponseSuccess( const QJsonDocument& results );

        void            onRESTResponseError( const QString& reason );

    protected:

        RESTMailBox*    _p_requester;
};

/**
 * @brief Response handler for CountMails
 *
 * @author boto
 * @date Nov 3, 2017
 */
class ResponseCountUnreadMails: public Meet4EatRESTResponse
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(ResponseCountUnreadMails) ";

    public:

        explicit        ResponseCountUnreadMails( RESTMailBox* p_requester );

        void            onRESTResponseSuccess( const QJsonDocument& results );

        void            onRESTResponseError( const QString& reason );

    protected:

        RESTMailBox*    _p_requester;
};

/**
 * @brief Response handler for CountMails
 *
 * @author boto
 * @date Nov 7, 2017
 */
class ResponseCountMails: public Meet4EatRESTResponse
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(ResponseCountMails) ";

    public:

        explicit        ResponseCountMails( RESTMailBox* p_requester );

        void            onRESTResponseSuccess( const QJsonDocument& results );

        void            onRESTResponseError( const QString& reason );

    protected:

        RESTMailBox*    _p_requester;
};


} // namespace webapp
} // namespace m4e

#endif // RESP_MAILBOX_H
