/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef USERAUTH_H
#define USERAUTH_H

#include <configuration.h>
#include <webapp/rest-authentication.h>
#include <QObject>


namespace m4e
{
namespace user
{

/**
 * @brief This class provides functionality for user authentication.
 *
 * @author boto
 * @date Sep 10, 2017
 */
class UserAuthentication : public QObject
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(UserAuthentication) ";

    Q_OBJECT

    public:

        /**
         * @brief Results code used in 'onAuthenticationResult'
         */
        enum AuthResultsCode
        {
            AuthCodeSuccess,
            AuthCodeMissingServerAddress,
            AuthCodeMissingCredentials,
            AuthCodeInvalidCredentials,
            AuthCodeServerNotReachable,
            AuthCodeOtherReason
        };

        /**
         * @brief Construct a User instance.
         * @param p_parent Parent object
         */
        explicit                UserAuthentication( QObject* p_parent );

        /**
         * @brief Destruct User instance
         */
        virtual                 ~UserAuthentication();

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
         * @brief Request for getting the authentication state. The results are emitted by signal 'onResponseAuthState'.
         */
        void                    requestAuthState();

        /**
         * @brief Try to sign in the user. The authentication results are emitted by signal 'onResponseSignInResult'.
         *
         * @param userName  User name
         * @param password  User's password hash (use RESTAuthentication::createHash for creating a password hash)
         */
        void                    requestSignIn( const QString& userName, const QString& password );

        /**
         * @brief Request for signing out the user. The results are emitted by signal 'onResponseSignOutResult'.
         */
        void                    requestSignOut();

    signals:

        /**
         * @brief Results of authentication state request.
         *
         * @param authenticated true if the user is already authenticated, otherwise false.
         * @param userId        User ID, if the user is already authenticated, otherwise 0
         */
        void                    onResponseAuthState( bool authenticated, QString userId );

        /**
         * @brief Results of an authentication attempt are emitted by this signal.
         *
         * @param success   True if the authentication was successful, otherwise false.
         * @param userId    User ID if the request was successfull
         * @param code      Results code
         * @param reason    If authentication attempt failed, this string contains a possible reason.
         */
        void                    onResponseSignInResult( bool success, QString userId, enum m4e::user::UserAuthentication::AuthResultsCode code, QString reason );

        /**
         * @brief Results of an sign out attempt are emitted by this signal.
         *
         * @param success   True if the sign-out was successful, otherwise false.
         * @param code      Results code
         * @param reason    If sign-out attempt failed, this string contains a possible reason.
         */
        void                    onResponseSignOutResult( bool success, enum m4e::user::UserAuthentication::AuthResultsCode code, QString reason );

    protected slots:

        /**
         * @brief Receive the results of getAuthState request.
         *
         * @param authenticated  true if the user is authenticated, otherwise false
         * @param sid            Session ID
         * @param userId         User ID
         */
        void                    onRESTAuthenticationAuthState( bool authenticated, QString sid, QString userId );

        /**
         * @brief Signal is received when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                    onRESTAuthenticationErrorAuthState( QString errorCode, QString reason );

        /**
         * @brief Receive the results of login request.
         *
         * @param userId         User ID
         */
        void                    onRESTAuthenticationLogin( QString userId );

        /**
         * @brief Signal is received when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                    onRESTAuthenticationErrorLogin( QString errorCode, QString reason );

        /**
         * @brief Receive this signal on a successful logout.
         */
        void                    onRESTAuthenticationLogout();

        /**
         * @brief Signal is received when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                    onRESTAuthenticationErrorLogout( QString errorCode, QString reason );

    protected:

        m4e::webapp::RESTAuthentication*  _p_restAuth = nullptr;
        QString                           _userName;
        QString                           _password;
};

} // namespace user
} // namespace m4e

#endif // USERAUTH_H
