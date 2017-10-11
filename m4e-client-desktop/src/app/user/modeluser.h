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
    DECLARE_SMARTPTR_ACCESS( ModelUser )

    public:

        /**
         * @brief Construct an instance.
         */
                                        ModelUser() {}

        /**
         * @brief Get user's email.
         * @return User's email
         */
        const QString&                  getEMail() const { return _email; }

        /**
         * @brief Set user's email.
         * @param email  User's email
         */
        void                            setEMail( const QString& email ) { _email = email; }

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

        virtual                         ~ModelUser() {}

        //! Omit copy construction!
                                        ModelUser( const ModelUser& );

        QString                         _email;

        QString                         _status;
};

typedef m4e::core::SmartPtr< ModelUser > ModelUserPtr;

} // namespace user
} // namespace m4e

Q_DECLARE_METATYPE( m4e::user::ModelUserPtr )

#endif // MODELUSER_H
