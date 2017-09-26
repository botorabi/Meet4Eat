/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef M4E_RESPONSE_H
#define M4E_RESPONSE_H

#include <configuration.h>
#include <QJsonDocument>


namespace m4e
{
namespace webapp
{

/**
 * @brief This base class is used for handling the response of an asynchronous REST requests.
 *
 * @author boto
 * @date Sep 7, 2017
 */
class Meet4EatRESTResponse
{
    public:

        /**
         * @brief Construct an instance of RESTResultsCallback
         */
                        Meet4EatRESTResponse() {}

        /**
         * @brief Destroy the callback instance.
         */
        virtual         ~Meet4EatRESTResponse() {}

        /**
         * @brief If this method returns true then the instance must be automatically deleted
         *        right after one of following callback methods was called. Override and return
         *        false in order to take control on the instance lifecycle yourself.
         *
         * @return Return true for automatic deletion of the response handler.
         */
        virtual bool    getAutoDelete() { return true; }

        /**
         * @brief Called on success this method delivers the request results in given JSON document.
         *
         * @param results The JSON document contains the response results.
         */
        virtual void    onRESTResponseSuccess( const QJsonDocument& /*results*/ ) {}

        /**
         * @brief This method is called if the server could not be contacted.
         *
         * @param reason The reason for the error.
         */
        virtual void    onRESTResponseError( const QString& /*reason*/ ) {}

        /**
         * @brief Check the request results.
         *
         * @param results     Request results as received e.g. in method onRESTResultsSuccess
         * @param data        Extracted data from results
         * @param errorCode   Error code if the results status was not ok.
         * @param errorString Error string if the results status was not ok.
         * @return            Return false if the results status was not ok.
         */
        bool            checkStatus( const QJsonDocument& results, QJsonDocument& data, QString& errorCode, QString& errorString );
};

} // namespace webapp
} // namespace m4e

#endif // M4E_RESPONSE_H
