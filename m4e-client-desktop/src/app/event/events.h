/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef EVENT_H
#define EVENT_H

#include <configuration.h>
#include <webapp/request/rest-event.h>
#include <event/modelevent.h>
#include <QObject>


namespace m4e
{
namespace event
{

/**
 * @brief This class provides access to event data.
 *
 * @author boto
 * @date Sep 28, 2017
 */
class Events : public QObject
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(Events) ";

    Q_OBJECT

    public:

        /**
         * @brief Construct an Event instance.
         *
         * @param p_parent Parent object
         */
        explicit                        Events( QObject* p_parent );

        /**
         * @brief Destruct User instance
         */
        virtual                         ~Events();

        /**
         * @brief Set webapp server's URL including port number. Set this URL before using any services below.
         *
         * @param url Server's URL
         */
        void                                setServerURL( const QString& url );

        /**
         * @brief Get webapp's server URL.
         *
         * @return Server URL
         */
        const QString&                      getServerURL() const;

        /**
         * @brief Get all events the user is part of. Consider to request it before via 'requestGetEvents'.
         *
         * @return User events
         */
        QList< m4e::event::ModelEventPtr >   getUserEvents();

        /**
         * @brief Request for getting all user events, the results are emitted by signal 'onResponseGetEvents'.
         */
        void                                requestGetEvents();

        /**
         * @brief Request for a new member to event, the results are emitted by signal 'onResponseAddMember'.
         *
         * @param eventId  ID of event which should get a new member
         * @param memberId ID of new member to add to given event
         */
        void                                requestAddMember( const QString& eventId, const QString& memberId );

    signals:

        /**
         * @brief Results of user events request.
         *
         * @param success  true if user data could successfully be retrieved, otherwise false
         * @param events   User's events
         */
        void                                onResponseGetEvents( bool success, QList< m4e::event::ModelEventPtr > events );

        /**
         * @brief Results of add member request.
         *
         * @param success  true if user data could successfully be retrieved, otherwise false
         * @param eventId  ID of event with new member added
         * @param memberId ID of new added member
         */
        void                                onResponseAddMember( bool success, QString eventId, QString memberId );

    protected slots:

        /**
         * @brief Signal is received when the results of getEvents arrive.
         *
         * @param events    User events
         */
        void                                onRESTEventGetEvents( QList< m4e::event::ModelEventPtr > events );

        /**
         * @brief Signal is received when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                                onRESTEventErrorGetEvents( QString errorCode, QString reason );

        /**
         * @brief Signal is received when the results of addMember request arrive.
         *
         * @param eventId   ID of event the user was added to
         * @param memberId  User ID of new member
         */
        void                                onRESTEventAddMember( QString eventId, QString memberId );

        /**
         * @brief Signal is emitted when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                                onRESTEventErrorAddMember( QString errorCode, QString reason );

    protected:

        webapp::RESTEvent*                  _p_restEvent = nullptr;

        QList< m4e::event::ModelEventPtr >  _events;
};

} // namespace event
} // namespace m4e

#endif // EVENT_H
