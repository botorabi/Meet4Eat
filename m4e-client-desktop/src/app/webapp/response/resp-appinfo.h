/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef RESP_APPINFO_H
#define RESP_APPINFO_H

#include <configuration.h>
#include <webapp/m4e-api/m4e-response.h>
#include <QJsonDocument>


namespace m4e
{
namespace webapp
{

class RESTAppInfo;

/**
 * @brief Response handler for AppInfo
 *
 * @author boto
 * @date Nov 21, 2017
 */
class ResponseAppInfo: public Meet4EatRESTResponse
{
    public:

        explicit    ResponseAppInfo( RESTAppInfo* p_requester );

        void        onRESTResponseSuccess( const QJsonDocument& results );

        void        onRESTResponseError( const QString& reason );

    protected:

        RESTAppInfo* _p_requester;
};

} // namespace webapp
} // namespace m4e

#endif // RESP_APPINFO_H
