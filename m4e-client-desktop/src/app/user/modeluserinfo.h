/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef MODELUSERINFO_H
#define MODELUSERINFO_H

#include <configuration.h>
#include <core/smartptr.h>
#include <common/modelbase.h>
#include <QMetaType>


namespace m4e
{
namespace user
{

/**
 * @brief Class describing a subset (public part) of user data
 *
 * @author boto
 * @date Sep 25, 2017
 */
class ModelUserInfo : public common::ModelBase, public m4e::core::RefCount< ModelUserInfo >
{
    DECLARE_SMARTPTR_ACCESS( ModelUserInfo )

    public:

        /**
         * @brief Construct an instance.
         */
                                        ModelUserInfo() {}

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
         * @return JSON formatted string representing the user
         */
        QString                         toJSON();

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

    protected:

        virtual                         ~ModelUserInfo() {}

        //! Omit copy construction!
                                        ModelUserInfo( const ModelUserInfo& );

        QString                         _status;
};

typedef m4e::core::SmartPtr< ModelUserInfo > ModelUserInfoPtr;

} // namespace user
} // namespace m4e

Q_DECLARE_METATYPE( m4e::user::ModelUserInfoPtr )

#endif // MODELUSERINFO_H
