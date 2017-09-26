/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef RESP_EVENT_H
#define RESP_EVENT_H

#include <configuration.h>
#include <webapp/m4e-api/m4e-response.h>
#include <QJsonDocument>


namespace m4e
{
namespace webapp
{

class RESTEvent;

/**
 * @brief Response handler for GetAllEvents
 *
 * @author boto
 * @date Sep 13, 2017
 */
class ResponseGetAllEvents: public Meet4EatRESTResponse
{
    public:

        explicit    ResponseGetAllEvents( RESTEvent* p_requester );

        void        onRESTResponseSuccess( const QJsonDocument& results );

        void        onRESTResponseError( const QString& reason );

    protected:

        RESTEvent*  _p_requester;
};

/**
 * @brief Response handler for GetEvent
 *
 * @author boto
 * @date Sep 13, 2017
 */
class ResponseGetEvent: public Meet4EatRESTResponse
{
    public:

        explicit    ResponseGetEvent( RESTEvent* p_requester );

        void        onRESTResponseSuccess( const QJsonDocument& results );

        void        onRESTResponseError( const QString& reason );

    protected:

        RESTEvent*  _p_requester;
};

} // namespace webapp
} // namespace m4e

#endif // RESP_EVENT_H
