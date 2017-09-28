/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef REST_USER_H
#define REST_USER_H

#include <configuration.h>
#include <webapp/m4e-api/m4e-rest.h>
#include <user/modeluser.h>


namespace m4e
{
namespace webapp
{

/**
 * @brief Class handling the user related web app interaction
 *
 * @author boto
 * @date Sep 12, 2017
 */
class RESTUser : public Meet4EatREST
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(RESTUser) ";

    Q_OBJECT

    public:

        /**
         * @brief Construct an instance.
         *
         * @param p_parent Parent instance
         */
        explicit                RESTUser( QObject* p_parent );

        /**
         * @brief Destroy instance.
         */
        virtual                 ~RESTUser();

        /**
         * @brief Get user data. The results are emitted by signal 'onRESTUserGetData'.
         *
         * @param userId   User ID
         */
        void                    getUserData( const QString& userId );

        /**
         * @brief Search for a user given a keyword. The results are emitted by signal 'onRESTUserSearchResults'.
         *
         * @param keyword  Keyword to search for.
         */
        void                    searchForUser( const QString& keyword );

    signals:

        /**
         * @brief Emit the results of getUserData request.
         *
         * @param user     User data
         */
        void                    onRESTUserGetData( m4e::user::ModelUserPtr user );

        /**
         * @brief Signal is emitted when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                    onRESTUserErrorGetData( QString errorCode, QString reason );

        /**
         * @brief Emit the results of searchForUser request.
         *
         * @param users     List of found user candidates
         */
        void                    onRESTUserSearchResults( QList< m4e::user::ModelUserInfoPtr > users );

        /**
         * @brief Signal is emitted when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                    onRESTUserErrorSearchResults( QString errorCode, QString reason );
};

} // namespace webapp
} // namespace m4e

#endif // REST_USER_H
