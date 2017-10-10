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
#include <user/modeluser.h>
#include <event/modelevent.h>


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
         * @brief Get all user events. The results are emitted by signal 'onRESTEventGetEvents'.
         *
         * @param userId   User ID
         */
        void                    getEvents();

        /**
         * @brief Get user's event with given ID. The results are emitted by signal 'onRESTEventGetEvent'.
         *
         * @param eventId  Event ID
         */
        void                    getEvent( const QString& eventId );

        /**
         * @brief Update an existing event. The results are emitted by signal 'onRESTEventUpdateEvent'.
         *
         * @param event Event to upate
         */
        void                    updateEvent( m4e::event::ModelEventPtr event );

        /**
         * @brief Add a new member to event.
         *
         * @param eventId   Event ID
         * @param memberId  User ID
         */
        void                    addMember( const QString& eventId, const QString& memberId );

        /**
         * @brief Remove a member from event.
         *
         * @param eventId   Event ID
         * @param memberId  User ID
         */
        void                    removeMember( const QString& eventId, const QString& memberId );

    signals:

        /**
         * @brief Emit the results of getUserEvents request.
         *
         * @param events    User events
         */
        void                    onRESTEventGetEvents( QList< m4e::event::ModelEventPtr > events );

        /**
         * @brief Signal is emitted when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                    onRESTEventErrorGetEvents( QString errorCode, QString reason );

        /**
         * @brief Emit the results of getEvent request.
         *
         * @param event    User event
         */
        void                    onRESTEventGetEvent( m4e::event::ModelEventPtr events );

        /**
         * @brief Signal is emitted when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                    onRESTEventErrorGetEvent( QString errorCode, QString reason );

        /**
         * @brief Emit the results of updateEvent request.
         *
         * @param eventId   ID of updated event
         */
        void                    onRESTEventUpdateEvent( QString eventId );

        /**
         * @brief Signal is emitted when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                    onRESTEventErrorUpdateEvent( QString errorCode, QString reason );

        /**
         * @brief Emit the results of addMember request.
         *
         * @param eventId   ID of event the user was added to
         * @param memberId  User ID of new member
         */
        void                    onRESTEventAddMember( QString eventId, QString memberId );

        /**
         * @brief Signal is emitted when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                    onRESTEventErrorAddMember( QString errorCode, QString reason );

        /**
         * @brief Emit the results of removeMember request.
         *
         * @param eventId   ID of event the user removed from
         * @param memberId  User ID of removed member
         */
        void                    onRESTEventRemoveMember( QString eventId, QString memberId );

        /**
         * @brief Signal is emitted when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                    onRESTEventErrorRemoveMember( QString errorCode, QString reason );
};

} // namespace webapp
} // namespace m4e

#endif // REST_EVENT_H
