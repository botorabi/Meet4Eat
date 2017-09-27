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
#include <data/modelbase.h>
#include <QMetaType>

namespace m4e
{
namespace data
{

/**
 * @brief Class describing a subset (public part) of user data
 *
 * @author boto
 * @date Sep 25, 2017
 */
class ModelUserInfo : public ModelBase, public m4e::core::RefCount< ModelUserInfo >
{
    DECLARE_SMARTPTR_ACCESS( ModelUserInfo )

    public:

        /**
         * @brief Construct an instance.
         */
                                        ModelUserInfo() {}

    protected:

        virtual                         ~ModelUserInfo() {}

        //! Omit copy construction!
                                        ModelUserInfo( const ModelUserInfo& );
};

typedef m4e::core::SmartPtr< ModelUserInfo > ModelUserInfoPtr;

} // namespace data
} // namespace m4e

Q_DECLARE_METATYPE( m4e::data::ModelUserInfoPtr )

#endif // MODELUSERINFO_H
