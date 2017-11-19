/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef MODELUSER_H
#define MODELUSER_H

#include <configuration.h>
#include <core/smartptr.h>
#include <common/modelbase.h>
#include <QJsonDocument>
#include <QString>
#include <QList>


namespace m4e
{
namespace user
{

/**
 * @brief Class describing a user
 *
 * @author boto
 * @date Sep 8, 2017
 */
class ModelUser : public common::ModelBase, public m4e::core::RefCount< ModelUser >
{
    SMARTPTR_DEFAULTS( ModelUser )

    public:

        /**
         * @brief Construct an instance.
         */
                                        ModelUser() {}

        /**
         * @brief Get user's login.
         *
         * @return User's login
         */
        const QString&                  getLogin() const { return _login; }

        /**
         * @brief Set user's login.
         * @param email  User's login
         */
        void                            setLogin( const QString& login ) { _login = login; }

        /**
         * @brief Get user's email.
         * @return User's email
         */
        const QString&                  getEmail() const { return _email; }

        /**
         * @brief Set user's email.
         * @param email  User's email
         */
        void                            setEmail( const QString& email ) { _email = email; }

        /**
         * @brief Get user's status such as 'online' or 'offline'.
         *
         * @return User status
         */
        const QString&                  getStatus() const { return _status; }

        /**
         * @brief Set user's status.
         *
         * @param status User status (e.g. 'online' or 'offline')
         */
        void                            setStatus( const QString& status ) { _status = status; }

        /**
         * @brief Create a JSON string out of the user model.
         *
         * @return JSON document representing the user
         */
        QJsonDocument                   toJSON();

        /**
         * @brief Create a JSON string out of given input which is used for updating the user data on server.
         *
         * NOTE: only the following subset of user data can be changed: name, password, photo
         *
         * @param name      User name, let empty if no change required
         * @param password  Pass the hash of a new password if a password change is required, otherwise let the string be empty.
         * @param photo     New photo, let empty if no change required
         */
        static QJsonDocument            toJSONForUpdate( const QString& name, const QString& password, doc::ModelDocumentPtr photo );

        /**
         * @brief Setup the user given a JSON formatted string.
         *
         * @param input Input string in JSON format
         * @return Return false if the input was not in proper format.
         */
        bool                            fromJSON( const QString& input );

        /**
         * @brief Setup the user given a JSON document.
         *
         * @param input Input in JSON document
         * @return Return false if the input was not in proper format.
         */
        bool                            fromJSON( const QJsonDocument& input );

        /**
         * @brief Comparison operator which considers the user ID.
         * @param right     Right hand of operation.
         * @return true if both users have the same ID, otherwise false.
         */
        bool                            operator == ( const ModelUser& right ) { return _id == right.getId(); }

        /**
         * @brief Unequal operator which considers the user ID.
         * @param right     Right hand of operation.
         * @return true if both users have the same ID, otherwise false.
         */
        bool                            operator != ( const ModelUser& right ) { return _id != right.getId(); }

    protected:

        QString                         _login;

        QString                         _email;

        QString                         _status;
};

typedef m4e::core::SmartPtr< ModelUser > ModelUserPtr;

} // namespace user
} // namespace m4e

Q_DECLARE_METATYPE( m4e::user::ModelUserPtr )

#endif // MODELUSER_H
