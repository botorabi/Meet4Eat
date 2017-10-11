/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef REST_AUTHENTICATION_H
#define REST_AUTHENTICATION_H

#include <configuration.h>
#include <webapp/m4e-api/m4e-rest.h>
#include <QJsonDocument>

namespace m4e
{
namespace webapp
{

/**
 * @brief Class handling the authentication tasks for Meet4Eat web app
 *
 * @author boto
 * @date Sep 8, 2017
 */
class RESTAuthentication : public Meet4EatREST
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(RESTAuthentication) ";

    Q_OBJECT

    public:

        /**
         * @brief Construct an instance.
         *
         * @param p_parent Parent instance
         */
        explicit                RESTAuthentication( QObject* p_parent );

        /**
         * @brief Destroy instance.
         */
        virtual                 ~RESTAuthentication();

        /**
         * @brief Get authentication state. The results are emitted by signal 'onRESTAuthenticationAuthState'.
         */
        void                    getAuthState();

        /**
         * @brief Create a hash out of given input. This is used for hashing the plain password.
         * @param input     Hash input
         * @return          Hashed input
         */
        static QString          createHash( const QString& input );

        /**
         * @brief Try to authenticate a user.
         *
         * @param sid         Session ID, it can be retrieved by a previous request to "Authentication State" (see getAuthState above).
         * @param userName    User name
         * @param password    User's password hash. Use method 'createHash' above to create the password hash.
         */
        void                    login( const QString& sid, const QString& userName, const QString& password );

        /**
         * @brief Logout an authenticated user.
         */
        void                    logout();

    signals:

        /**
         * @brief Emit the results of getAuthState request.
         *
         * @param authenticated  true if the user is authenticated, otherwise false
         * @param sid            Session ID
         * @param userId         User ID
         */
        void                    onRESTAuthenticationAuthState( bool authenticated, QString sid, QString userId );

        /**
         * @brief Signal is emitted when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                    onRESTAuthenticationErrorAuthState( QString errorCode, QString reason );

        /**
         * @brief Emit the results of login request.
         *
         * @param userId         User ID
         */
        void                    onRESTAuthenticationLogin( QString userId );

        /**
         * @brief Signal is emitted when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                    onRESTAuthenticationErrorLogin( QString errorCode, QString reason );

        /**
         * @brief Emit this signal if the logout request was successful.
         */
        void                    onRESTAuthenticationLogout();

        /**
         * @brief Signal is emitted when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                    onRESTAuthenticationErrorLogout( QString errorCode, QString reason );
};

} // namespace webapp
} // namespace m4e

#endif // REST_AUTHENTICATION_H
