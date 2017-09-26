/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "appsettings.h"

namespace m4e
{
namespace data
{

AppSettings* AppSettings::_s_p_appSettings = nullptr;


AppSettings::~AppSettings()
{
    if ( _p_settings )
    {
        syncSettings();
        delete _p_settings;
    }
}

AppSettings* AppSettings::get()
{
    if ( !_s_p_appSettings )
    {
        _s_p_appSettings = new AppSettings();
        _s_p_appSettings->_p_settings = new QSettings( QSettings::NativeFormat, QSettings::UserScope, M4E_ORGANIZATION_NAME, M4E_APP_NAME );
    }
    return _s_p_appSettings;
}

QSettings *AppSettings::getSettings()
{
    return _s_p_appSettings->_p_settings;
}

void AppSettings::syncSettings()
{
    _p_settings->sync();
}

void AppSettings::writeSettingsValue( const QString& category, const QString& token, const QString& value )
{
    QString cat = category;
    if ( !cat.isEmpty() && !cat.endsWith( "/" ) )
        cat += "/";

    _p_settings->setValue( cat + token, value );
}

QString AppSettings::readSettingsValue( const QString& category, const QString& token, const QString& defaultValue )
{
    QString cat = category;
    if ( !cat.isEmpty() && !cat.endsWith( "/" ) )
        cat += "/";

    return _p_settings->value( cat + token, defaultValue ).toString();
}

void AppSettings::shutdown()
{
    if ( _s_p_appSettings )
        delete _s_p_appSettings;
    _s_p_appSettings = nullptr;
}

} // namespace data
} // namespace m4e
