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
#include <data/events.h>
#include <data/modeldocument.h>
#include <data/documentcache.h>
#include <QObject>


namespace m4e
{
namespace data
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
        data::ModelUserPtr              getUserData() const;

        /**
         * @brief Request for updating user data. If successful then the data will be notified by signal 'onUserDataReady';
         */
        void                            requestUserData();

        /**
         * @brief Get user events. The Events class is a helper for convenient event related operations.
         *
         * @return User events
         */
        EventsPtr                       getUserEvents();

        /**
         * @brief Request for updating user events. If successful then the data will be notified by signal 'onUserEventsReady';
         */
        void                            requestUserEvents();

        /**
         * @brief Request a document from server. If it is available in local cache, then no server request will be performed.
         *        The document is delivered by signal 'onDocumentReady'.
         *
         * @param id    Document ID
         * @param eTag  Document ETag, this is used for caching documents locally
         */
        void                            requestDocument( const QString& id, const QString& eTag );

    signals:

        /**
         * @brief This signal is emitted when an update of user data was arrived.
         *        The user data model can also be empty (e.g. if there were server connection problems).
         *
         * @param user     User data
         */
        void                            onUserDataReady( m4e::data::ModelUserPtr user );

        /**
         * @brief This signal is emitted when user events were arrived.
         *
         * @param events   User events
         */
        void                            onUserEventsReady( QList< m4e::data::ModelEventPtr > events );

        /**
         * @brief This signal is emitted when a requested document was arrived.
         *
         * @param document   Document
         */
        void                            onDocumentReady( m4e::data::ModelDocumentPtr document );

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
         * @brief Results of user data request.
         *
         * @param success  true if user data could successfully be retrieved, otherwise false
         * @param user     User data
         */
        void                            onResponseUserData( bool success, m4e::data::ModelUserPtr user );

        /**
         * @brief Results of user events request.
         *
         * @param success  true if user data could successfully be retrieved, otherwise false
         * @param events   User's events
         */
        void                            onResponseUserAllEvents( bool success, QList< m4e::data::ModelEventPtr > events );

        /**
         * @brief This signal is received from document cache when a requested document was arrived. It is forwarded to signal 'onDocumentReady' above.
         *
         * @param document   Document
         */
        void                            onCacheDocumentReady( m4e::data::ModelDocumentPtr document );

    protected:

        user::UserAuthentication*       getOrCreateUserAuth();

        user::User*                     getOrCreateUser();

        DocumentCache*                  getOrCreateDocumentCache();

        QString                         _userID;

        user::UserAuthentication*       _p_userAuth = nullptr;

        user::User*                     _p_user     = nullptr;

        ModelUserPtr                    _userModel;

        QList< ModelEventPtr >          _events;

        DocumentCache*                  _p_documentCache = nullptr;

        ConnectionState                 _connState  = ConnNoConnection;

        QString                         _connFailReason;
};

} // namespace data
} // namespace m4e

#endif // WEBAPP_H
