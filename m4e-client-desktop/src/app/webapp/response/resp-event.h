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
class ResponseGetEvents: public Meet4EatRESTResponse
{
    public:

        explicit    ResponseGetEvents( RESTEvent* p_requester );

        void        onRESTResponseSuccess( const QJsonDocument& results );

        void        onRESTResponseError( const QString& reason );

    protected:

        RESTEvent*  _p_requester;
};

/**
 * @brief Response handler for NewEvent
 *
 * @author boto
 * @date Oct 16, 2017
 */
class ResponseNewEvent: public Meet4EatRESTResponse
{
    public:

        explicit    ResponseNewEvent( RESTEvent* p_requester );

        void        onRESTResponseSuccess( const QJsonDocument& results );

        void        onRESTResponseError( const QString& reason );

    protected:

        RESTEvent*  _p_requester;
};

/**
 * @brief Response handler for DeleteEvent
 *
 * @author boto
 * @date Oct 17, 2017
 */
class ResponseDeleteEvent: public Meet4EatRESTResponse
{
    public:

        explicit    ResponseDeleteEvent( RESTEvent* p_requester );

        void        onRESTResponseSuccess( const QJsonDocument& results );

        void        onRESTResponseError( const QString& reason );

    protected:

        RESTEvent*  _p_requester;
};

/**
 * @brief Response handler for UpdateEvent
 *
 * @author boto
 * @date Sep 29, 2017
 */
class ResponseUpdateEvent: public Meet4EatRESTResponse
{
    public:

        explicit    ResponseUpdateEvent( RESTEvent* p_requester );

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

/**
 * @brief Response handler for AddMember
 *
 * @author boto
 * @date Sep 28, 2017
 */
class ResponseEventAddMember: public Meet4EatRESTResponse
{
    public:

        explicit    ResponseEventAddMember( RESTEvent* p_requester );

        void        onRESTResponseSuccess( const QJsonDocument& results );

        void        onRESTResponseError( const QString& reason );

    protected:

        RESTEvent*  _p_requester;
};

/**
 * @brief Response handler for RemoveMember
 *
 * @author boto
 * @date Sep 29, 2017
 */
class ResponseEventRemoveMember: public Meet4EatRESTResponse
{
    public:

        explicit    ResponseEventRemoveMember( RESTEvent* p_requester );

        void        onRESTResponseSuccess( const QJsonDocument& results );

        void        onRESTResponseError( const QString& reason );

    protected:

        RESTEvent*  _p_requester;
};

/**
 * @brief Response handler for GetLocation
 *
 * @author boto
 * @date Oct 13, 2017
 */
class ResponseEventGetLocation: public Meet4EatRESTResponse
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(ResponseEventGetLocation) ";

    public:

        explicit    ResponseEventGetLocation( RESTEvent* p_requester );

        void        onRESTResponseSuccess( const QJsonDocument& results );

        void        onRESTResponseError( const QString& reason );

    protected:

        RESTEvent*  _p_requester;
};

/**
 * @brief Response handler for AddLocation
 *
 * @author boto
 * @date Oct 11, 2017
 */
class ResponseEventAddLocation: public Meet4EatRESTResponse
{
    public:

        explicit    ResponseEventAddLocation( RESTEvent* p_requester );

        void        onRESTResponseSuccess( const QJsonDocument& results );

        void        onRESTResponseError( const QString& reason );

    protected:

        RESTEvent*  _p_requester;
};

/**
 * @brief Response handler for RemoveLocation
 *
 * @author boto
 * @date Oct 11, 2017
 */
class ResponseEventRemoveLocation: public Meet4EatRESTResponse
{
    public:

        explicit    ResponseEventRemoveLocation( RESTEvent* p_requester );

        void        onRESTResponseSuccess( const QJsonDocument& results );

        void        onRESTResponseError( const QString& reason );

    protected:

        RESTEvent*  _p_requester;
};

} // namespace webapp
} // namespace m4e

#endif // RESP_EVENT_H
