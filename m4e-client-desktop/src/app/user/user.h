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
#include <webapp/request/rest-user.h>
#include <webapp/request/rest-event.h>
#include <user/modeluser.h>
#include <event/modelevent.h>
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
         *
         * @param p_parent Parent object
         */
        explicit                User( QObject* p_parent );

        /**
         * @brief Destruct User instance
         */
        virtual                 ~User();

        /**
         * @brief Set the server URL including port number. Set this URL before using any services below.
         *
         * @param url Server's URL
         */
        void                    setServerURL( const QString& url );

        /**
         * @brief Get server's URL.
         *
         * @return Server URL
         */
        const QString&          getServerURL() const;

        /**
         * @brief Get the user data as far as it could be fetched from server successfully. Consider to request the user data before by
         *        using the method 'requestUserData' below.
         *
         * @return User data
         */
        user::ModelUserPtr      getUserData();

        /**
         * @brief Request for getting the user data, the results are emitted by signal 'onResponseUserData'.
         */
        void                    requestUserData( const QString userId );

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
        void                    onResponseUserData( bool success, m4e::user::ModelUserPtr user );

        /**
         * @brief Results of user search request.
         *
         * @param success  true if user data could successfully be retrieved, otherwise false
         * @param users List of user hits
         */
        void                    onResponseUserSearch( bool success, QList< m4e::user::ModelUserInfoPtr > users );

    protected slots:

        /**
         * @brief Receive the results of requestUserData request.
         *
         * @param user           User data
         */
        void                    onRESTUserGetData( m4e::user::ModelUserPtr user );

        /**
         * @brief Signal is received when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                    onRESTUserErrorGetData( QString errorCode, QString reason );

        /**
         * @brief Receive the results of searchForUser request.
         *
         * @param users     List of found user candidates
         */
        void                    onRESTUserSearchResults( QList< m4e::user::ModelUserInfoPtr > users );

        /**
         * @brief Signal is received when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                    onRESTUserErrorSearchResults( QString errorCode, QString reason );

    protected:

        webapp::RESTUser*       _p_restUser  = nullptr;

        user::ModelUserPtr      _userModel;
};

} // namespace user
} // namespace m4e

#endif // USER_H
