/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef USER_H
#define USER_H

#include <configuration.h>
#include <webapp/rest-user.h>
#include <webapp/rest-event.h>
#include <data/modeluser.h>
#include <data/modelevent.h>
#include <QObject>


namespace m4e
{
namespace user
{

/**
 * @brief This class provides access to user data.
 *
 * @author boto
 * @date Sep 12, 2017
 */
class User : public QObject
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(User) ";

    Q_OBJECT

    public:

        /**
         * @brief Construct a User instance.
         * @param p_parent Parent object
         */
        explicit                User( QObject* p_parent );

        /**
         * @brief Destruct User instance
         */
        virtual                 ~User();

        /**
         * @brief Set webapp server's URL including port number. Set this URL before using any services below.
         *
         * @param url Server's URL
         */
        void                    setServerURL( const QString& url );

        /**
         * @brief Get webapp's server URL.
         *
         * @return Server URL
         */
        const QString&          getServerURL() const;

        /**
         * @brief Request for getting the user data, the results are emitted by signal 'onResponseUserData'.
         */
        void                    requestUserData( const QString userId );

        /**
         * @brief Request for getting all user events, the results are emitted by signal 'onResponseUserAllEvents'.
         */
        void                    requestAllEvents();

        /**
         * @brief Request for searching for users given the keyword. The hits are emitted by signal 'onResponseUserSearch'.
         *
         * @param keyword  Keyword to search for
         */
        void                    requestUserSearch( const QString& keyword );

    signals:

        /**
         * @brief Results of user data request.
         *
         * @param success  true if user data could successfully be retrieved, otherwise false
         * @param user     User data
         */
        void                    onResponseUserData( bool success, m4e::data::ModelUserPtr user );

        /**
         * @brief Results of user events request.
         *
         * @param success  true if user data could successfully be retrieved, otherwise false
         * @param events   User's events
         */
        void                    onResponseUserAllEvents( bool success, QList< m4e::data::ModelEventPtr > events );

        /**
         * @brief Results of user search request.
         *
         * @param success  true if user data could successfully be retrieved, otherwise false
         * @param users List of user hits
         */
        void                    onResponseUserSearch( bool success, QList< m4e::data::ModelUserInfoPtr > users );

    protected slots:

        /**
         * @brief Receive the results of requestUserData request.
         *
         * @param user           User data
         */
        void                    onRESTUserGetData( m4e::data::ModelUserPtr user );

        /**
         * @brief Signal is received when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                    onRESTUserErrorGetData( QString errorCode, QString reason );

        /**
         * @brief Signal is received when the results of getUserEvents arrive.
         *
         * @param events    User events
         */
        void                    onRESTEventGetAllEvents( QList< m4e::data::ModelEventPtr > events );

        /**
         * @brief Signal is received when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                    onRESTEventErrorGetAllEvents( QString errorCode, QString reason );

        /**
         * @brief Receive the results of searchForUser request.
         *
         * @param users     List of found user candidates
         */
        void                    onRESTUserSearchResults( QList< m4e::data::ModelUserInfoPtr > users );

        /**
         * @brief Signal is received when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                    onRESTUserErrorSearchResults( QString errorCode, QString reason );

    protected:

        m4e::webapp::RESTUser*  _p_restUser  = nullptr;
        m4e::webapp::RESTEvent* _p_restEvent = nullptr;
};

} // namespace user
} // namespace m4e

#endif // USER_H
