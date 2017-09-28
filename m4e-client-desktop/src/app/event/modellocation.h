/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef MODELLOCATION_H
#define MODELLOCATION_H

#include <configuration.h>
#include <core/smartptr.h>
#include <common/modelbase.h>
#include <QString>
#include <QList>

namespace m4e
{
namespace event
{

/**
 * @brief Class for holding location information
 *
 * @author boto
 * @date Sep 8, 2017
 */
class ModelLocation : public common::ModelBase, public m4e::core::RefCount< ModelLocation >
{
    DECLARE_SMARTPTR_ACCESS( ModelLocation )

    public:

        /**
         * @brief Construct an instance.
         */
                                        ModelLocation() {}

        /**
         * @brief Set the name of members who voted for this location.
         * @return List of voted Members
         */
        const QList< QString >&         getVotedMembers() const { return _votedMembers; }

        /**
         * @brief Get the name of members who voted for this location.
         * @param votedMembers  List of voted members
         */
        void                            setVotedMembers( const QList< QString >& votedMembers ) { _votedMembers = votedMembers; }

        /**
         * @brief Comparison operator which considers the location ID.
         * @param right     Right hand of operation.
         * @return true if both locations have the same ID, otherwise false.
         */
        bool                            operator == ( const ModelLocation& right ) { return _id == right.getId(); }

        /**
         * @brief Unequal operator which considers the location ID.
         * @param right     Right hand of operation.
         * @return true if both locations have the same ID, otherwise false.
         */
        bool                            operator != ( const ModelLocation& right ) { return _id != right.getId(); }

    protected:

        virtual                         ~ModelLocation() {}

        //! Omit copy construction!
                                        ModelLocation( const ModelLocation& );

        QList< QString >                _votedMembers;
};

typedef m4e::core::SmartPtr< ModelLocation > ModelLocationPtr;

} // namespace event
} // namespace m4e

Q_DECLARE_METATYPE( m4e::event::ModelLocationPtr )

#endif // MODELLOCATION_H
