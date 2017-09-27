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
#include <data/modelbase.h>
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
class ModelUser : public ModelBase, public m4e::core::RefCount< ModelUser >
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

        QString                         _email;
        QList< ModelEventPtr >          _events;
};

typedef m4e::core::SmartPtr< ModelUser > ModelUserPtr;

} // namespace data
} // namespace m4e

Q_DECLARE_METATYPE( m4e::data::ModelUserPtr )

#endif // MODELUSER_H
