/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef RESP_USER_H
#define RESP_USER_H

#include <configuration.h>
#include <webapp/m4e-api/m4e-response.h>
#include <QJsonDocument>


namespace m4e
{
namespace webapp
{

class RESTUser;

/**
 * @brief Response handler for GetUserData
 *
 * @author boto
 * @date Sep 12, 2017
 */
class ResponseGetUserData: public Meet4EatRESTResponse
{
    public:

        explicit    ResponseGetUserData( RESTUser* p_requester );

        void        onRESTResponseSuccess( const QJsonDocument& results );

        void        onRESTResponseError( const QString& reason );

    protected:

        RESTUser*   _p_requester;
};

} // namespace webapp
} // namespace m4e

#endif // RESP_USER_H
