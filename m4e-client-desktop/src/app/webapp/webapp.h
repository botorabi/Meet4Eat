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
#include <communication/connection.h>
#include <notification/notifications.h>
#include <mailbox/mailbox.h>
#include <chat/chatsystem.h>
#include <update/updatecheck.h>
#include <QObject>


namespace m4e
{
namespace webapp
{

class RESTAppInfo;

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
         * @brief The sever authentication states
         */
        enum AuthState
        {
            AuthNoConnection,
            AuthConnecting,
            AuthSuccessful,
            AuthFail
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
         * @brief Get the web application version retrieved from server.
         *
         * @return Web application version
         */
        const QString&                  getWebAppVersion() const;

        /**
         * @brief Get the client update checker.
         *
         * @return The update checker
         */
        update::UpdateCheck*            getUpdateCheck();

        /**
         * @brief Get current web app authentication state
         *
         * @return Authentication state
         */
        enum AuthState                  getAuthState() const;

        /**
         * @brief If the connection state is in AuthFail, then this string may help further.
         *
         * @return Authentication failure string
         */
        const QString&                  getAuthFailReason() const;

        /**
         * @brief If the connection was established successfully then the user data can be retrieved by this method.
         *
         * @return User data
         */
        user::User*                     getUser();

        /**
         * @brief Get the connection instance, it handles the real-time communication with the server.
         *
         * @return Connection instance
         */
        comm::Connection*               getConnection();

        /**
         * @brief Get the Notifications instance, it handles all incoming and outgoing notifications.
         *
         * @return Notifications instance
         */
        notify::Notifications*          getNotifications();

        /**
         * @brief User this Events instance in order to access the user events.
         *
         * @return Events instance ready for accessing user's events.
         */
        event::Events*                  getEvents();

        /**
         * @brief Get user's mailbox.
         *
         * @return The mailbox
         */
        mailbox::MailBox*               getMailBox();

        /**
         * @brief Get the chat system
         *
         * @return The chat system
         */
        chat::ChatSystem*               getChatSystem();

        /**
         * @brief Request for updating the authentication state. If successful then the state will be notified by signal 'onAuthState';
         */
        void                            requestAuthState();

        /**
         * @brief Request for updating user data. If successful then the data will be notified by signal 'onUserDataReady';
         */
        void                            requestUserData();

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
         * @brief On start of server connection establishing the web server information is fetched as first step.
         * This signal notifies about the reachablity of the server and its version.
         *
         * @param success   true if the server was reachable, otherwise false
         * @param version   The web app server version.
         */
        void                            onWebServerInfo( bool success, QString version );

        /**
         * @brief This signal is emitted to inform about the current authentication state.
         *
         * @param success        true if the authentication state could be determined, otherwise false if a connection problem exists.
         * @param authenticated  True if the user is authenticated, otherwise false
         */
        void                            onAuthState( bool success, bool authenticated );

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

        /**
         * @brief This signal is emitted when the connection to server was closed.
         */
        void                            onServerConnectionClosed();

    protected slots:

        /**
         * @brief Periodic update timer used for keeping the connection alive.
         */
        void                            onTimerUpdate();

        /**
         * @brief This signal is emitted when the server info arrives.
         *
         * @param version   The web application version
         */
        void                            onRESTAppInfo( QString version );

        /**
         * @brief Signal is emitted when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                            onRESTAppInfoError( QString errorCode, QString reason );

        /**
         * @brief Results of authentication state request.
         *
         * @param success       True if the authentication state retrieval was successful, otherwise false.
         * @param authenticated true if the user is already authenticated, otherwise false.
         * @param userId        User ID, if the user is already authenticated, otherwise 0
         */
        void                            onResponseAuthState( bool success, bool authenticated, QString userId );

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

        /**
         * @brief This signal is emitted when the WebSocket connection was closed.
         */
        void                            onClosedConnection();

        /**
         * @brief This signal notifies about a new incoming network packet in channel 'System'.
         *
         * @param packet Arrived System channel packet
         */
        void                            onChannelSystemPacket( m4e::comm::PacketPtr packet );

    protected:

        template< class T >
        void                            setupServerURL( T* p_inst ) const;

        RESTAppInfo*                    getOrCreateAppInfo();

        update::UpdateCheck*            getOrCreateUpdateCheck();

        user::UserAuthentication*       getOrCreateUserAuth();

        comm::Connection*               getOrCreateConnection();

        notify::Notifications*          getOrCreateNotifications();

        user::User*                     getOrCreateUser();

        event::Events*                  getOrCreateEvent();

        doc::DocumentCache*             getOrCreateDocumentCache();

        mailbox::MailBox*               getOrCreateMailBox();

        chat::ChatSystem*               getOrCreateChatSystem();

        void                            resetAllResources();

        QString                         _userID;

        QString                         _webAppVersion;

        QString                         _lastServerURL;

        QTimer*                         _p_connTimer     = nullptr;

        RESTAppInfo*                    _p_restAppInfo   = nullptr;

        update::UpdateCheck*            _p_updateCheck   = nullptr;

        user::UserAuthentication*       _p_userAuth      = nullptr;

        comm::Connection*               _p_connection    = nullptr;

        notify::Notifications*          _p_notifications = nullptr;

        user::User*                     _p_user          = nullptr;

        event::Events*                  _p_events        = nullptr;

        doc::DocumentCache*             _p_documentCache = nullptr;

        mailbox::MailBox*               _p_mailBox       = nullptr;

        chat::ChatSystem*               _p_chatSystem    = nullptr;

        AuthState                       _authState = AuthNoConnection;

        QString                         _authFailReason;
};

} // namespace webapp
} // namespace m4e

#endif // WEBAPP_H
