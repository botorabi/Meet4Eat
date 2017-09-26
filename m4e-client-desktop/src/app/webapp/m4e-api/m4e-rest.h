/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef M4E_REST_H
#define M4E_REST_H

#include <configuration.h>
#include <webapp/m4e-api/m4e-restops.h>
#include <webapp/m4e-api/m4e-response.h>
#include <QJsonDocument>
#include <QMap>

namespace m4e
{
namespace webapp
{

/**
 * @brief Base class for accessing to RESTful services of webapp Meet4Eat.
 *
 * @author boto
 * @date Sep 7, 2017
 */
class Meet4EatREST : public QObject
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(Meet4EatREST) ";

    Q_OBJECT

    public:

        /**
         * @brief Construct an instance.
         *
         * @param p_parent   Optional parent object
         */
        explicit                Meet4EatREST( QObject* p_parent );

        /**
         * @brief Destroy instance.
         */
        virtual                 ~Meet4EatREST();

        /**
         * @brief Get the server URL
         *
         * @return Server URL
         */
        const QString&          getServerURL() const;

        /**
         * @brief Set web application's server URL including the port number, e.g. http://myserver:8080
         *
         * @param serverURL  Web application's server URL
         */
        void                    setServerURL( const QString& serverURL );

    protected slots:

        /**
         * @brief This signal is received from WebAppREST when the request response arrives.
         *
         * @param requestId   ID used for requesting
         * @param json        Response results
         */
        void                    onRESTResponse( unsigned int requestId, QJsonDocument json );

        /**
         * @brief This signal is received from WebAppREST when the request could not be performed due to networking problems.
         *
         * @param requestId   ID used for requesting
         * @param reason      Reason string
         */
        void                    onRESTResponseFailed( unsigned int requestId, QString reason );

    protected:

        /**
         * @brief Get the resources path on server.
         *
         * @return            Path to server resources
         */
        const QString&          getResourcePath() const { return _pathResources; }

        /**
         * @brief Get the REST operation object. Use this in derived classes for performing REST interface access.
         * @return            REST operation instance
         */
        Meet4EatRESTOperations* getRESTOps() { return _p_RESTOps; }

        /**
         * @brief Create a new callback entry and return its unique ID.
         *
         * @param p_callback   Callback instance
         * @return             Callback ID
         */
        unsigned int            createResultsCallback( Meet4EatRESTResponse* p_callback );

        /**
         * @brief Internally used to get requester's callback object and remove it from internal lookup.
         * @param requestId   Request ID
         * @return The Callback object, if any exists, otherwise nullptr.
         */
        Meet4EatRESTResponse*    getAndRemoveResultsCallback( unsigned int requestId );

    private:

        Meet4EatRESTOperations* _p_RESTOps = nullptr;

        QString                 _urlServer;

        QString                 _pathResources;

        static unsigned int     _requestId;

        typedef QMap< unsigned int, Meet4EatRESTResponse* > LookupResultsCallbacks;

        LookupResultsCallbacks  _callbacks;
};

} // namespace webapp
} // namespace m4e

#endif // M4E_REST_H
