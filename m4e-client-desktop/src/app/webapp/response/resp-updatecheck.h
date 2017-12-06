/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef RESP_UPDATECHECK_H
#define RESP_UPDATECHECK_H

#include <configuration.h>
#include <webapp/m4e-api/m4e-response.h>
#include <QJsonDocument>


namespace m4e
{
namespace webapp
{

class RESTUpdateCheck;

/**
 * @brief Response handler for UpdateCheck
 *
 * @author boto
 * @date Dec 6, 2017
 */
class ResponseUpdateCheck: public Meet4EatRESTResponse
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(ResponseUpdateCheck) ";

    public:

        explicit    ResponseUpdateCheck( RESTUpdateCheck* p_requester );

        void        onRESTResponseSuccess( const QJsonDocument& results );

        void        onRESTResponseError( const QString& reason );

    protected:

        RESTUpdateCheck* _p_requester;
};

} // namespace webapp
} // namespace m4e

#endif // RESP_UPDATECHECK_H
