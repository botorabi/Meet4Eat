/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef MODELREQUPDATEINFO_H
#define MODELREQUPDATEINFO_H

#include <configuration.h>
#include <core/smartptr.h>
#include <QDateTime>
#include <QMetaType>
#include <QString>


namespace m4e
{
namespace update
{

/**
 * @brief Class used while requesting for client update information
 *
 * @author boto
 * @date Dec 6, 2017
 */
class ModelRequestUpdateInfo: public m4e::core::RefCount< ModelRequestUpdateInfo >
{
    SMARTPTR_DEFAULTS( ModelRequestUpdateInfo )

    public:

        /**
         * @brief Construct an instance.
         */
                                        ModelRequestUpdateInfo() {}

        /**
         * @brief Set the client name requesting for update information.
         *
         * @param name The name of the client
         */
        void                            setName( const QString& name ) { _name = name; }

        /**
         * @brief Get the client name.
         *
         * @return The client name
         */
        const QString&                  getName() const { return _name; }

        /**
         * @brief Set the client operation system.
         *
         * @param name The client OS
         */
        void                            setOS( const QString& os ) { _os = os; }

        /**
         * @brief Get the client operation system.
         *
         * @return The client OS
         */
        const QString&                  getOS() const { return _os; }

        /**
         * @brief Set the client flavor.
         *
         * @param name The client flavor
         */
        void                            setFlavor( const QString& flavor ) { _flavor = flavor; }

        /**
         * @brief Get the client flavor.
         *
         * @return The client flavor
         */
        const QString&                  getFlavor() const { return _flavor; }

        /**
         * @brief Set the client version.
         *
         * @param name The client version
         */
        void                            setVersion( const QString& version ) { _version = version; }

        /**
         * @brief Get the client version.
         *
         * @return The client version
         */
        const QString&                  getVersion() const { return _version; }

        /**
         * @brief Create a JSON string out of the model.
         *
         * @return JSON document representing the model
         */
        QJsonDocument                   toJSON();

    protected:

        QString                         _name;
        QString                         _os;
        QString                         _flavor;
        QString                         _version;
};

typedef m4e::core::SmartPtr< ModelRequestUpdateInfo > ModelRequestUpdateInfoPtr;

} // namespace update
} // namespace m4e

Q_DECLARE_METATYPE( m4e::update::ModelRequestUpdateInfoPtr )

#endif // MODELREQUPDATEINFO_H
