/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef REST_APPINFO_H
#define REST_APPINFO_H

#include <configuration.h>
#include <webapp/m4e-api/m4e-rest.h>
#include <QJsonDocument>

namespace m4e
{
namespace webapp
{

/**
 * @brief Class handling the app info REST API
 *
 * @author boto
 * @date Nov 21, 2017
 */
class RESTAppInfo : public Meet4EatREST
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(RESTAppInfo) ";

    Q_OBJECT

    public:

        /**
         * @brief Construct an instance.
         *
         * @param p_parent Parent instance
         */
        explicit                RESTAppInfo( QObject* p_parent );

        /**
         * @brief Destroy instance.
         */
        virtual                 ~RESTAppInfo();

        /**
         * @brief Get the web app information. The results are emitted by signal 'onRESTAppInfo'.
         */
        void                    getAppInfo();

    signals:

        /**
         * @brief Emit the results of getAppInfo request.
         *
         * @param version   The web application version
         */
        void                    onRESTAppInfo( QString version );

        /**
         * @brief Signal is emitted when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                    onRESTAppInfoError( QString errorCode, QString reason );
};

} // namespace webapp
} // namespace m4e

#endif // REST_APPINFO_H
