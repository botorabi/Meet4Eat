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
#include <data/modelevent.h>
#include <QString>
#include <QList>

namespace m4e
{
namespace data
{

/**
 * @brief Class describing a user
 *
 * @author boto
 * @date Sep 8, 2017
 */
class ModelUser : public m4e::core::RefCount< ModelUser >
{
    DECLARE_SMARTPTR_ACCESS( ModelUser )

    public:

        /**
         * @brief Construct an instance.
         */
                                        ModelUser() {}
        /**
         * @brief Get the unique user ID.
         * @return The user ID
         */
        const QString&                  getId() const { return _id; }

        /**
         * @brief Set the unique user ID.
         * @param id    The user ID
         */
        void                            setId( const QString &id ) { _id = id; }

        /**
         * @brief Get the user name.
         * @return The user name
         */
        const QString&                  getName() const { return _name; }

        /**
         * @brief Set the user name.
         * @param name  The user name
         */
        void                            setName( const QString& name ) { _name = name; }

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

        /**
         * @brief Get user's events.
         *
         * @return User's events
         */
        QList< ModelEventPtr >          getEvents() const { return _events; }

        /**
         * @brief Set user's events.
         *
         * @param events  User's events
         */
        void                            setEvents( const QList< ModelEventPtr >& events ) { _events = events; }

    protected:

        virtual                         ~ModelUser() {}

        //! Omit copy construction!
                                        ModelUser( const ModelUser& );

        QString                         _id;
        QString                         _name;
        QString                         _email;
        QList< ModelEventPtr >          _events;
};

typedef m4e::core::SmartPtr< ModelUser > ModelUserPtr;

} // namespace data
} // namespace m4e

#endif // MODELUSER_H
