/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef MODELUPDATEINFO_H
#define MODELUPDATEINFO_H

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
 * @brief Class representing client update information
 *
 * @author boto
 * @date Dec 6, 2017
 */
class ModelUpdateInfo: public m4e::core::RefCount< ModelUpdateInfo >
{
    SMARTPTR_DEFAULTS( ModelUpdateInfo )

    public:

        /**
         * @brief Construct an instance.
         */
                                        ModelUpdateInfo() {}

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
         * @brief Set the URL to the update.
         *
         * @param name The URL to grab the update
         */
        void                            setURL( const QString& url ) { _url = url; }

        /**
         * @brief Get the URL to the update.
         *
         * @return The URL to grab the update
         */
        const QString&                  getURL() const { return _url; }

        /**
         * @brief Set the update release date.
         *
         * @param name The release date
         */
        void                            setReleaseDate( const QDateTime& date ) { _releaseDate = date; }

        /**
         * @brief Get the update release date.
         *
         * @return The release date
         */
        const QDateTime&                getReleaseDate() const { return _releaseDate; }

        /**
         * @brief Setup the update info given a JSON formatted string.
         *
         * @param input Input string in JSON format
         * @return Return false if the input was not in proper format.
         */
        bool                            fromJSON( const QString& input );

        /**
         * @brief Setup the update info given a JSON document.
         *
         * @param input Input in JSON document
         * @return Return false if the input was not in proper format.
         */
        bool                            fromJSON( const QJsonDocument& input );

    protected:

        QString                         _os;
        QString                         _version;
        QString                         _url;
        QDateTime                       _releaseDate;
};

typedef m4e::core::SmartPtr< ModelUpdateInfo > ModelUpdateInfoPtr;

} // namespace update
} // namespace m4e

Q_DECLARE_METATYPE( m4e::update::ModelUpdateInfoPtr )

#endif // MODELUPDATEINFO_H
