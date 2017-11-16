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
    SMARTPTR_DEFAULTS( ModelLocation )

    public:

        /**
         * @brief Construct an instance.
         */
                                        ModelLocation() {}

        /**
         * @brief Create a JSON string out of the location model.
         *
         * @return JSON document representing the event location
         */
        QJsonDocument                   toJSON();

        /**
         * @brief Setup the location given a JSON formatted string.
         *
         * @param input Input string in JSON format
         * @return Return false if the input was not in proper format.
         */
        bool                            fromJSON( const QString& input );

        /**
         * @brief Setup the location given a JSON document.
         *
         * @param input Input in JSON document
         * @return Return false if the input was not in proper format.
         */
        bool                            fromJSON( const QJsonDocument& input );

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
};

typedef m4e::core::SmartPtr< ModelLocation > ModelLocationPtr;

} // namespace event
} // namespace m4e

Q_DECLARE_METATYPE( m4e::event::ModelLocationPtr )

#endif // MODELLOCATION_H
