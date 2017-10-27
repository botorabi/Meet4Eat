/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

/**
 * @brief Central place for application configuration
 *
 * @author boto
 * @date Aug 7, 2017
 */

#ifndef CONFIGURATION_H
#define CONFIGURATION_H

/* App name and version */
#define M4E_APP_NAME            "Meet4Eat"
#define M4E_ORGANIZATION_NAME   "vr-fun"
#define M4E_APP_VERSION         "0.8.0"
#define M4E_APP_COPYRIGHT       "(c) 2017"
#define M4E_APP_URL             "http://m4e.org"

/* Default app server address */
#define M4E_DEFAULT_APP_SRV     "https://vr-fun.net:8185"

/* Web site link for registering a new account */
#define M4E_URL_REGISTER_ACC    "https://m4e.org/register"

/* Web site link for resetting password */
#define M4E_URL_FORGOT_PW       "https://m4e.org/request-password"

/*
 * This define is used for accepting insecure SSL connections
 * for REST and WebSocket communication.
 *
 * Set it to 1 in order to validate server's SSL certificate.
 * Set it to 0 in order to accept a self-signed server certificate.
 */
#define M4E_DISALLOW_INSECURE_CONNECTION    0

/* App settings tokens */
#define M4E_SETTINGS_KEY_WIN_GEOM    "WindowsGeom"

/* App settings category for user */
#define M4E_SETTINGS_CAT_USER        "User"
#define M4E_SETTINGS_KEY_USER_LOGIN  "login"
#define M4E_SETTINGS_KEY_USER_PW     "password"
#define M4E_SETTINGS_KEY_USER_PW_REM "rememberpw"

/* App settings category for server */
#define M4E_SETTINGS_CAT_SRV         "Server"
#define M4E_SETTINGS_KEY_SRV_URL     "address"


/* Resource path to base REST services on the web application server */
#define M4E_REST_SRV_RESOURCE_PATH   "/m4e/webresources"

/* WebSocket path for real-time server communication */
#define M4E_WS_SRV_RESOURCE_PATH     "/m4e/ws"

/* Name of local cache directory */
#define M4E_LOCAL_CACHE_DIR          "cache"

/* Cache document expiration in days */
#define M4E_LOCAL_CAHCE_EXPIRE_DAYS  60

/* Default user icon shown if no one is available */
#define M4E_DEFAULT_USER_ICON        ":/icon-user.png"

#endif // CONFIGURATION_H
