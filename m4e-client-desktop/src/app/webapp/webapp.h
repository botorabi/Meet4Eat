/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */


#ifndef WEBAPP_H
#define WEBAPP_H


#include <configuration.h>
#include <user/userauth.h>
#include <user/user.h>
#include <event/events.h>
#include <document/modeldocument.h>
#include <document/documentcache.h>
#include <QObject>


namespace m4e
{
namespace webapp
{

/**
 * @brief This class establishes the connection to web application server
 *        and provides access to user data.
 *
 *        The user credentials and server address are expected to be in
 *        application settings.
 *
 * @author boto
 * @date Sep 12, 2017
 */
class WebApp : public QObject
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(WebApp) ";

    Q_OBJECT

    public:

        /**
         * @brief The sever connection states
         */
        enum ConnectionState
        {
            ConnNoConnection,
            ConnConnecting,
            ConnEstablished,
            ConnFail
        };

        /**
         * @brief Construct an instance.
         *
         * @param p_parent Parent instance
         */
        explicit                        WebApp( QObject* p_parent );

        /**
         * @brief Destruct the instance.
         */
        virtual                         ~WebApp();

        /**
         * @brief Establish a connection to server and try to retrieve the user data.
         */
        void                            establishConnection();

        /**
         * @brief Shut down an established connection.
         */
        void                            shutdownConnection();

        /**
         * @brief Get current web app connection state
         * @return Connection state
         */
        enum ConnectionState            getConnectionState() const;

        /**
         * @brief If the connection state is in ConnFail, then this string may help further.
         *
         * @return Connection failure string
         */
        const QString&                  getConnFailReason() const;

        /**
         * @brief If the connection was established successfully then the user data can be retrieved by this method.
         *
         * @return User data
         */
        user::User*                     getUser();

        /**
         * @brief Request for updating user data. If successful then the data will be notified by signal 'onUserDataReady';
         */
        void                            requestUserData();

        /**
         * @brief User this Events instance in order to access the user events.
         *
         * @return Events instance ready for accessing user's events.
         */
        event::Events*                  getEvents();

        /**
         * @brief Request a document from server. If it is available in local cache, then no server request will be performed.
         *        The document is delivered by signal 'onDocumentReady'.
         *
         * @param id    Document ID
         * @param eTag  Document ETag, this is used for caching documents locally
         */
        void                            requestDocument( const QString& id, const QString& eTag );

        /**
         * @brief Request for searching for users given the keyword. The hits are emitted by signal 'onResponseUserSearch'.
         *
         * @param keyword  Keyword to search for
         */
        void                            requestUserSearch( const QString& keyword );

    signals:

        /**
         * @brief This signal is emitted to notify about user authentication results.
         *
         * @param success  true if the user was successfully authenticated, otherwise false
         * @param userId   User ID, valid if success is true
         */
        void                            onUserSignedIn( bool success, QString userId );

        /**
         * @brief This signal is emitted to notify about user authentication results.
         *
         * @param success  true if the user was successfully authenticated, otherwise false
         * @param userId   User ID, valid if success is true
         */
        void                            onUserSignedOff( bool success );

        /**
         * @brief This signal is emitted when an update of user data was arrived.
         *        The user data model can also be empty (e.g. if there were server connection problems).
         *
         * @param user     User data
         */
        void                            onUserDataReady( m4e::user::ModelUserPtr user );

        /**
         * @brief This signal is emitted when a requested document was arrived.
         *
         * @param document   Document
         */
        void                            onDocumentReady( m4e::doc::ModelDocumentPtr document );

        /**
         * @brief This signal is emitted when user search results were arrived.
         *
         * @param users List of user hits
         */
        void                            onUserSearch( QList< m4e::user::ModelUserInfoPtr > users );

    protected slots:

        /**
         * @brief Results of an authentication attempt are emitted by this signal.
         *
         * @param success   True if the authentication was successful, otherwise false.
         * @param userId    User ID if the request was successfull
         * @param code      Results code
         * @param reason    If authentication attempt failed, this string contains a possible reason.
         */
        void                            onResponseSignInResult( bool success, QString userId, enum m4e::user::UserAuthentication::AuthResultsCode code, QString reason );

        /**
         * @brief Results of an sign out attempt are emitted by this signal.
         *
         * @param success   True if the sign-out was successful, otherwise false.
         * @param code      Results code
         * @param reason    If sign-out attempt failed, this string contains a possible reason.
         */
        void                            onResponseSignOutResult( bool success, enum m4e::user::UserAuthentication::AuthResultsCode code, QString reason );

        /**
         * @brief Results of user data request.
         *
         * @param success  true if user data could successfully be retrieved, otherwise false
         * @param user     User data
         */
        void                            onResponseUserData( bool success, m4e::user::ModelUserPtr user );

        /**
         * @brief This signal is received from document cache when a requested document was arrived. It is forwarded to signal 'onDocumentReady' above.
         *
         * @param document   Document
         */
        void                            onCacheDocumentReady( m4e::doc::ModelDocumentPtr document );

        /**
         * @brief This signal is received from user REST api when the results of search request arrive. It is forwarded to 'onUserSearch'.
         *
         * @param success  true if user data could successfully be retrieved, otherwise false
         * @param users List of user hits
         */
        void                            onResponseUserSearch( bool success, QList< m4e::user::ModelUserInfoPtr > users );

    protected:

        user::UserAuthentication*           getOrCreateUserAuth();

        user::User*                         getOrCreateUser();

        event::Events*                      getOrCreateEvent();

        doc::DocumentCache*                 getOrCreateDocumentCache();

        QString                             _userID;

        user::UserAuthentication*           _p_userAuth = nullptr;

        user::User*                         _p_user = nullptr;

        event::Events*                      _p_events = nullptr;

        doc::DocumentCache*                 _p_documentCache = nullptr;

        ConnectionState                     _connState = ConnNoConnection;

        QString                             _connFailReason;
};

} // namespace webapp
} // namespace m4e

#endif // WEBAPP_H
