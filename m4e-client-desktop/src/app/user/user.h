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
         * @brief Get the error which occurred while the last request. Use this if a response delivers a 'success' set to false.
         *
         * @return Last error
         */
        const QString&          getLastError() const { return _lastError; }

        /**
         * @brief Get the error code set by REST response, see getLastError above.
         *
         * @return Last error code
         */
        const QString&          getLastErrorCode() const { return _lastErrorCode; }

        /**
         * @brief Get the user data as far as it could be fetched from server successfully. Consider to request the user data before by
         *        using the method 'requestUserData' below.
         *
         * @return User data
         */
        user::ModelUserPtr      getUserData();

        /**
         * @brief Get the user ID, or empty string if no user exists. The user ID can also be retrieved by using getUserData().
         *
         * @return The user ID, or empty string if no user exists now.
         */
        QString                 getUserId();

        /**
         * @brief Given an ID check if it is the ID of authenticated user. This method can be used for checking for resource ownership.
         *
         * @return Return true if the given ID is the same as the user ID.
         */
        bool                    isUserId( const QString& id );

        /**
         * @brief Request for getting the user data, the results are emitted by signal 'onResponseUserData'.
         *
         * @param userId  The user ID
         */
        void                    requestUserData( const QString& userId );

        /**
         * @brief Request for updating the data of current user on server. The results are emitted by signal 'onResponseUpdateUserData'.
         *
         * NOTE: a user must be already authenticated before using this method.
         *
         * @param name      User name, let empty if no change required
         * @param password  Pass the hash of a new password if a password change is required, otherwise let the string be empty.
         * @param photo     New photo, let empty if no change required
         * @return          Return false if no user is currently authenticated.
         */
        bool                    requestUpdateUserData( const QString& name, const QString& password, doc::ModelDocumentPtr photo );

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
         * @brief Results of user data update request.
         *
         * @param success   true if user data could successfully be updated, otherwise false
         * @param userId    ID of the user who was updated on server.
         */
        void                    onResponseUpdateUserData( bool success, QString userId );

        /**
         * @brief Results of user search request.
         *
         * @param success   true if user data could successfully be retrieved, otherwise false
         * @param users     List of user hits
         */
        void                    onResponseUserSearch( bool success, QList< m4e::user::ModelUserInfoPtr > users );

    protected slots:

        /**
         * @brief Receive the results of requestUserData request.
         *
         * @param user      User data
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
         * @brief Receive the results of requestUpdateUserData request.
         *
         * @param userId    The user ID
         */
        void                    onRESTUserUpdateData( QString userId );

        /**
         * @brief Signal is received when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                    onRESTUserErrorUpdateData( QString errorCode, QString reason );

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

        void                    setLastError( const QString& error ="", const QString& errorCode ="" );

        webapp::RESTUser*       _p_restUser  = nullptr;

        user::ModelUserPtr      _userModel;

        QString                 _lastError;

        QString                 _lastErrorCode;
};

} // namespace user
} // namespace m4e

#endif // USER_H
