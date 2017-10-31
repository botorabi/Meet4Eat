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
        explicit                            Events( QObject* p_parent );

        /**
         * @brief Destruct User instance
         */
        virtual                             ~Events();

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
         * @brief Get the error which occurred while the last request. Use this if a response delivers a 'success' set to false.
         *
         * @return Last error
         */
        const QString&                      getLastError() const { return _lastError; }

        /**
         * @brief Get the error code set by REST response, see getLastError above.
         *
         * @return Last error code
         */
        const QString&                      getLastErrorCode() const { return _lastErrorCode; }

        /**
         * @brief Get all events the user is part of. Consider to request it before via 'requestGetEvents'.
         *
         * @return User events
         */
        QList< m4e::event::ModelEventPtr >   getUserEvents();

        /**
         * @brief Get the event with given ID.
         *
         * @param id The event ID
         * @return User event, or an invalid event object if the ID was not found.
         */
        m4e::event::ModelEventPtr           getUserEvent( const QString& id );

        /**
         * @brief Request for getting all user events, the results are emitted by signal 'onResponseGetEvents'.
         */
        void                                requestGetEvents();

        /**
         * @brief Request for getting a user event, the results are emitted by signal 'onResponseGetEvent'.
         *
         * @param eventID The event ID
         */
        void                                requestGetEvent( const QString& eventId );

        /**
         * @brief Request for deleting a user event, the results are emitted by signal 'onResponseDeleteEvent'.
         *
         * @param eventID The event ID
         */
        void                                requestDeleteEvent( const QString& eventId );

        /**
         * @brief Request for update an existing event, the results are emitted by signal 'onResponseUpdateEvent'.
         *
         * @param event  The event to update
         */
        void                                requestUpdateEvent( m4e::event::ModelEventPtr event );

        /**
         * @brief Request for creating a new event, the results are emitted by signal 'onResponseNewEvent'.
         *
         * @param event  The event to create
         */
        void                                requestNewEvent( m4e::event::ModelEventPtr event );

        /**
         * @brief Request for a new member to event, the results are emitted by signal 'onResponseAddMember'.
         *
         * @param eventId  ID of event which should get a new member
         * @param memberId ID of new member to add to given event
         */
        void                                requestAddMember( const QString& eventId, const QString& memberId );

        /**
         * @brief Request for removing a member from event, the results are emitted by signal 'onResponseRemoveMember'.
         *
         * @param eventId  ID of event
         * @param memberId ID of member to remove from given event
         */
        void                                requestRemoveMember( const QString& eventId, const QString& memberId );

        /**
         * @brief Request for getting location data, the results are emitted by signal 'onResponseGetLocation'.
         *
         * @param eventId    ID of event
         * @param locationId ID of location
         */
        void                                requestGetLocation( const QString& eventId, const QString& locationId );

        /**
         * @brief Request for adding the given location to an event, the results are emitted by signal 'onResponseAddLocation'.
         *
         * @param eventId   ID of event getting the new location
         * @param location  Location to add
         */
        void                                requestAddLocation( const QString& eventId, ModelLocationPtr location );

        /**
         * @brief Request for removing the given location from an event, the results are emitted by signal 'onResponseRemoveLocation'.
         *
         * @param eventId       ID of event removing the location from
         * @param locationId    ID of location to remove
         */
        void                                requestRemoveLocation( const QString& eventId, const QString& locationId );

        /**
         * @brief Request for update an existing evebt location, the results are emitted by signal 'onResponseUpdateLocation'.
         *
         * @param eventId   ID of event containing the location
         * @param location  The event location to update
         */
        void                                requestUpdateLocation( const QString& eventId, m4e::event::ModelLocationPtr location );

    signals:

        /**
         * @brief Results of user events request.
         *
         * @param success  true if user events could successfully be retrieved, otherwise false
         * @param events   User's events
         */
        void                                onResponseGetEvents( bool success, QList< m4e::event::ModelEventPtr > events );

        /**
         * @brief Results of user event request.
         *
         * @param success  true if user event could successfully be retrieved, otherwise false
         * @param event    A user event
         */
        void                                onResponseGetEvent( bool success, m4e::event::ModelEventPtr event );

        /**
         * @brief Results of event update request.
         *
         * @param success  true if event data could successfully be updated, otherwise false
         * @param eventId  ID of event which was updated
         */
        void                                onResponseUpdateEvent( bool success, QString eventId );

        /**
         * @brief Results of event creation request.
         *
         * @param success  true if the event could successfully be created, otherwise false
         * @param eventId  ID of event which was created
         */
        void                                onResponseNewEvent( bool success, QString eventId );

        /**
         * @brief Results of deleting a user event request.
         *
         * @param success  true if user event could successfully be retrieved, otherwise false
         * @param eventId  User event which was deleted
         */
        void                                onResponseDeleteEvent( bool success, QString eventId );

        /**
         * @brief Results of add member request.
         *
         * @param success  true if user data could successfully be retrieved, otherwise false
         * @param eventId  ID of event with new member added
         * @param memberId ID of new added member
         */
        void                                onResponseAddMember( bool success, QString eventId, QString memberId );

        /**
         * @brief Results of remove member request.
         *
         * @param success  true if user data could successfully be retrieved, otherwise false
         * @param eventId  ID of event
         * @param memberId ID of member to remove
         */
        void                                onResponseRemoveMember( bool success, QString eventId, QString memberId );

        /**
         * @brief Results of getting event location data request.
         *
         * @param success    true if user data could successfully be retrieved, otherwise false
         * @param location   The location
         */
        void                                onResponseGetLocation( bool success, m4e::event::ModelLocationPtr location );

        /**
         * @brief Results of add event location request.
         *
         * @param success    true if user data could successfully be retrieved, otherwise false
         * @param eventId    ID of event with new location added
         * @param locationId ID of new location
         */
        void                                onResponseAddLocation( bool success, QString eventId, QString locationId );

        /**
         * @brief Results of remove event location request.
         *
         * @param success    true if user data could successfully be retrieved, otherwise false
         * @param eventId    ID of event
         * @param locationId ID of location to remove
         */
        void                                onResponseRemoveLocation( bool success, QString eventId, QString locationId );

        /**
         * @brief Results of update event location request.
         *
         * @param success    true if user data could successfully be retrieved, otherwise false
         * @param eventId    ID of event
         * @param locationId ID of location to update
         */
        void                                onResponseUpdateLocation( bool success, QString eventId, QString locationId );

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
         * @brief Signal is received when the results of getEvent arrive.
         *
         * @param event     User event
         */
        void                                onRESTEventGetEvent( m4e::event::ModelEventPtr event );

        /**
         * @brief Signal is received when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                                onRESTEventErrorGetEvent( QString errorCode, QString reason );

        /**
         * @brief Signal is received when the results of deleteEvent arrive.
         *
         * @param eventId    ID of event which was deleted
         */
        void                                onRESTEventDeleteEvent( QString eventId );

        /**
         * @brief Signal is received when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                                onRESTEventErrorDeleteEvent( QString errorCode, QString reason );

        /**
         * @brief Signal is received when the results of newEvent arrive.
         *
         * @param eventId   ID of new event
         */
        void                                onRESTEventNewEvent( QString eventId );

        /**
         * @brief Signal is emitted when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                                onRESTEventErrorNewEvent( QString errorCode, QString reason );

        /**
         * @brief Signal is received when the results of updating event arrive.
         *
         * @param eventId    ID of event which was updated
         */
        void                                onRESTEventUpdateEvent( QString eventId );

        /**
         * @brief Signal is received when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                                onRESTEventErrorUpdateEvent( QString errorCode, QString reason );

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

        /**
         * @brief Signal is received when the results of removeMember request arrive.
         *
         * @param eventId   ID of event the user was removed from
         * @param memberId  User ID of removed member
         */
        void                                onRESTEventRemoveMember( QString eventId, QString memberId );

        /**
         * @brief Signal is emitted when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                                onRESTEventErrorRemoveMember( QString errorCode, QString reason );

        /**
         * @brief Signal is received when the results of getLocation request arrive.
         *
         * @param location    The location
         */
        void                                onRESTEventGetLocation( m4e::event::ModelLocationPtr location );

        /**
         * @brief Signal is emitted when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                                onRESTEventErrorGetLocation( QString errorCode, QString reason );

        /**
         * @brief Signal is received when the results of addLocation request arrive.
         *
         * @param eventId     ID of event the location was added to
         * @param locationId  ID of new location
         */
        void                                onRESTEventAddLocation( QString eventId, QString locationId );

        /**
         * @brief Signal is emitted when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                                onRESTEventErrorAddLocation( QString errorCode, QString reason );

        /**
         * @brief Signal is received when the results of removeLocation request arrive.
         *
         * @param eventId     ID of event the location was removed from
         * @param locationId  ID of removed location
         */
        void                                onRESTEventRemoveLocation( QString eventId, QString locationId );

        /**
         * @brief Signal is emitted when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                                onRESTEventErrorRemoveLocation( QString errorCode, QString reason );

        /**
         * @brief Signal is received when the results of updateLocation request arrive.
         *
         * @param eventId     ID of event containing the location
         * @param locationId  ID of updated location
         */
        void                                onRESTEventUpdateLocation( QString eventId, QString locationId );

        /**
         * @brief Signal is emitted when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                                onRESTEventErrorUpdateLocation( QString errorCode, QString reason );

    protected:

        void                                setLastError( const QString& error ="", const QString& errorCode ="" );

        webapp::RESTEvent*                  _p_restEvent = nullptr;

        QList< m4e::event::ModelEventPtr >  _events;

        QString                             _lastError;

        QString                             _lastErrorCode;
};

} // namespace event
} // namespace m4e

#endif // EVENT_H
