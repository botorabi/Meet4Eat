/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef APPSETTINGS_H
#define APPSETTINGS_H

#include <configuration.h>
#include <core/core.h>
#include <QSettings>

namespace m4e
{
namespace settings
{

/**
 * @brief Singleton instance for managing the application settings.
 *
 * @author boto
 * @date Aug 2, 2017
 */
class AppSettings
{
    public:

        /**
         * @brief Access the singleton instance.
         * @return  AppSettings instance.
         */
        static AppSettings*             get();

        /**
         * @brief Return the settings object, which can be used for storing and retrieving the application's persistent data.
         *        Consider to rather use the more convenient methods readSettingsValue and writeSettingsValue.
         * @return QSettings instance used for serializing app settings.
         */
        QSettings*                      getSettings();

        /**
         * Call this method in order to synchronize the settings storage. Usually this will be called on application termination.
         */
        void                            syncSettings();

        /**
         * @brief Write a string to application settings (persistent).
         * @param category      Settings category
         * @param token         Token
         * @param value         Token value
         */
        void                            writeSettingsValue( const QString& category, const QString& token, const QString& value );

        /**
         * @brief Read a string from application settings (persistent).
         * @param category      Settings category
         * @param token         Token
         * @param defaultValue  A value which is returned for the case that the token does not exist.
         * @return
         */
        QString                         readSettingsValue( const QString& category, const QString& token, const QString& defaultValue );

    protected:

                                        AppSettings() {}

                                        ~AppSettings();

                                        AppSettings( const AppSettings& );

        /**
         * @brief This method can be called from Core class in order to shutdown the singleton.
         */
        void                            shutdown();

        static AppSettings*             _s_p_appSettings;

        QSettings*                      _p_settings = nullptr;

        friend class m4e::core::Core;
};

} // namespace settings
} // namespace m4e

#endif // APPSETTINGS_H
