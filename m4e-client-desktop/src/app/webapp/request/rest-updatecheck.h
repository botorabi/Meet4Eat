/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef REST_UPDATECHECK_H
#define REST_UPDATECHECK_H

#include <configuration.h>
#include <update/modelupdateinfo.h>
#include <update/modelrequpdateinfo.h>
#include <webapp/m4e-api/m4e-rest.h>


namespace m4e
{
namespace webapp
{

/**
 * @brief Class handling the update check REST API
 *
 * @author boto
 * @date Dec 6, 2017
 */
class RESTUpdateCheck : public Meet4EatREST
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(RESTUpdateCheck) ";

    Q_OBJECT

    public:

        /**
         * @brief Construct an instance.
         *
         * @param p_parent Parent instance
         */
        explicit                RESTUpdateCheck( QObject* p_parent );

        /**
         * @brief Destroy instance.
         */
        virtual                 ~RESTUpdateCheck();

        /**
         * @brief Request for client update check. The results are emitted by signal 'onRESTUpdateInfo'.
         *
         * @param request Model containing information about requesting client
         */
        void                    requestUpdateInfo( m4e::update::ModelRequestUpdateInfoPtr request );

    signals:

        /**
         * @brief Emit the results of requestUpdateInfo request.
         *
         * @param info   Update information
         */
        void                    onRESTUpdateInfo( m4e::update::ModelUpdateInfoPtr info );

        /**
         * @brief Signal is emitted when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                    onRESTUpdateInfoError( QString errorCode, QString reason );
};

} // namespace webapp
} // namespace m4e

#endif // REST_UPDATECHECK_H
