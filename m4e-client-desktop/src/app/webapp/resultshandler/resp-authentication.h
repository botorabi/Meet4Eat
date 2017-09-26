/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef RESP_AUTHENTICATION_H
#define RESP_AUTHENTICATION_H

#include <configuration.h>
#include <webapp/m4e-api/m4e-response.h>
#include <QJsonDocument>


namespace m4e
{
namespace webapp
{

class RESTAuthentication;

/**
 * @brief Response handler for AuthState
 *
 * @author boto
 * @date Sep 8, 2017
 */
class ResponseAuthState: public Meet4EatRESTResponse
{
    public:

        explicit    ResponseAuthState( RESTAuthentication* p_requester );

        void        onRESTResponseSuccess( const QJsonDocument& results );

        void        onRESTResponseError( const QString& reason );

    protected:

        RESTAuthentication* _p_requester;
};

/**
 * @brief Response handler for Login
 *
 * @author boto
 * @date Sep 8, 2017
 */
class ResponseLogin: public Meet4EatRESTResponse
{
    public:

        explicit    ResponseLogin( RESTAuthentication* p_requester );

        void        onRESTResponseSuccess( const QJsonDocument& results );

        void        onRESTResponseError( const QString& reason );

    protected:

        RESTAuthentication* _p_requester;
};

/**
 * @brief Response handler for Logout
 *
 * @author boto
 * @date Sep 8, 2017
 */
class ResponseLogout: public Meet4EatRESTResponse
{
    public:

        explicit    ResponseLogout( RESTAuthentication* p_requester );

        void        onRESTResponseSuccess( const QJsonDocument& results );

        void        onRESTResponseError( const QString& reason );

    protected:

        RESTAuthentication* _p_requester;
};

} // namespace webapp
} // namespace m4e

#endif // RESP_AUTHENTICATION_H
