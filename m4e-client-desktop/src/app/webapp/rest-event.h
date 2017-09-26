/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef REST_EVENT_H
#define REST_EVENT_H

#include <configuration.h>
#include <webapp/m4e-api/m4e-rest.h>
#include <data/modeluser.h>


namespace m4e
{
namespace webapp
{

/**
 * @brief Class handling the event related web app interaction
 *
 * @author boto
 * @date Sep 13, 2017
 */
class RESTEvent : public Meet4EatREST
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(RESTEvent) ";

    Q_OBJECT

    public:

        /**
         * @brief Construct an instance.
         *
         * @param p_parent Parent instance
         */
        explicit                RESTEvent( QObject* p_parent );

        /**
         * @brief Destroy instance.
         */
        virtual                 ~RESTEvent();

        /**
         * @brief Get all user events. The results are emitted by signal 'onRESTEventGetAllEvents'.
         *
         * @param userId   User ID
         */
        void                    getAllEvents();

        /**
         * @brief Get user's event with given ID. The results are emitted by signal 'onRESTEventGetEvent'.
         *
         * @param eventId  Event ID
         */
        void                    getEvent( const QString& eventId );

    signals:

        /**
         * @brief Emit the results of getUserEvents request.
         *
         * @param events    User events
         */
        void                    onRESTEventGetAllEvents( QList< m4e::data::ModelEventPtr > events );

        /**
         * @brief Signal is emitted when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                    onRESTEventErrorGetAllEvents( QString errorCode, QString reason );

        /**
         * @brief Emit the results of getUserEvent request.
         *
         * @param event    User event
         */
        void                    onRESTEventGetEvent( m4e::data::ModelEventPtr events );

        /**
         * @brief Signal is emitted when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                    onRESTEventErrorGetEvent( QString errorCode, QString reason );
};

} // namespace webapp
} // namespace m4e

#endif // REST_EVENT_H
