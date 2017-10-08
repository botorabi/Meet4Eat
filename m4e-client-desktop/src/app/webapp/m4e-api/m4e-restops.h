/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef M4E_RESTOPS_H
#define M4E_RESTOPS_H

#include <configuration.h>
#include <QJsonDocument>
#include <QtNetwork>

namespace m4e
{
namespace webapp
{

/**
 * @brief Class providing common RESTful services related communication functionality
 *        such as GET, POST, PUT, DELTE.
 *
 * @author boto
 * @date Sep 7, 2017
 */
class Meet4EatRESTOperations : public QObject
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(Meet4EatRESTOperations) ";

    Q_OBJECT

    public:

        /**
         * @brief Construct an instance.
         *
         * @param p_parent   Optional parent object
         */
        explicit                Meet4EatRESTOperations( QObject* p_parent );

        /**
         * @brief Destroy instance.
         */
        virtual                 ~Meet4EatRESTOperations();

        /**
         * @brief Start a GET request.
         *
         * @param url         Service URL
         * @param requestId   Request ID which is delivered when the response arrives (see signal onResponse below)
         */
        void                    GET( const QUrl& url, unsigned int requestId = 0 );

        /**
         * @brief Start a POST request.
         *
         * @param url         Service URL
         * @param json        Request data
         * @param requestId   Request ID which is delivered when the response arrives (see signal onResponse below)
         */
        void                    POST( const QUrl& url, unsigned int requestId = 0, const QJsonDocument& json = QJsonDocument() );

        /**
         * @brief Start a PUT request.
         *
         * @param url         Service URL
         * @param json        Request data
         * @param requestId   Request ID which is delivered when the response arrives (see signal onResponse below)
         */
        void                    PUT( const QUrl& url, unsigned int requestId = 0, const QJsonDocument& json = QJsonDocument() );

        /**
         * @brief Start a DELETE request.
         *
         * @param url         Service URL
         * @param requestId   Request ID which is delivered when the response arrives (see signal onResponse below)
         */
        void                    DELETE( const QUrl& url, unsigned int requestId = 0 );

        /**
         * @brief Get the common cookie which must be used in all server requests. This cookie contains also authentication
         *        and session related data.
         *
         * @return Common cookie used in all server requests
         */
        static QNetworkCookieJar* getCookie();

    signals:

        /**
         * @brief This signal is emitted when the request response arrives.
         *
         * @param requestId   ID used for requesting
         * @param json        Response results
         */
        void                    onResponse( unsigned int requestId, QJsonDocument json );

        /**
         * @brief This signal is emitted when the request could not be performed due to networking problems.
         *
         * @param requestId   ID used for requesting
         * @param reason      Reason string
         */
        void                    onResponseFailed( unsigned int requestId, QString reason );

    protected slots:

        /**
         * @brief Used internally to dispatch network replies.
         * @param p_reply     Network reply
         */
        void                    onReplyFinished( QNetworkReply* p_reply );

    protected:

        /**
         * @brief Start a request
         *
         * @param url
         * @param op
         * @param requestId
         * @param json
         */
        void                    startRequest( const QUrl& url, enum QNetworkAccessManager::Operation op, unsigned int requestId, const QJsonDocument& json );

        QNetworkAccessManager*  _p_nam = nullptr;

        /**
         * One single coolie jar is used for all rest requests.
         */
        static QNetworkCookieJar* _s_cookie;
};

} // namespace webapp
} // namespace m4e

#endif // M4E_RESTOPS_H
