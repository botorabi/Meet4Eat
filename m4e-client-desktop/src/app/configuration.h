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
#define M4E_APP_VERSION         "0.8.5"
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
#define M4E_SETTINGS_KEY_WIN_GEOM     "WindowsGeom"
#define M4E_SETTINGS_KEY_MAILBOX_GEOM "MailBoxGeom2"

/* App settings category for user */
#define M4E_SETTINGS_CAT_USER        "User"
#define M4E_SETTINGS_KEY_USER_LOGIN  "login"
#define M4E_SETTINGS_KEY_USER_PW     "password"
#define M4E_SETTINGS_KEY_USER_PW_REM "rememberpw"

/* App settings category for server */
#define M4E_SETTINGS_CAT_SRV         "Server"
#define M4E_SETTINGS_KEY_SRV_URL     "address"

/* App settings category for server */
#define M4E_SETTINGS_CAT_APP          "App"
#define M4E_SETTINGS_KEY_APP_QUIT_MSG "quitmsg"

/* App settings category for notifications */
#define M4E_SETTINGS_CAT_NOTIFY       "Notify"
#define M4E_SETTINGS_KEY_NOTIFY_ALARM "alarm"
#define M4E_SETTINGS_KEY_NOTIFY_EVENT "event"

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

/* Some mails come from system, define the sender name for those mails */
#define M4E_MAIL_SENDER_SYSTEM_NAME "[Meet4Eat Team]"

/* The period (in minutes) for updating the server status.
 * The periodic server status update is used to avoid a server session
 * expiration, so it must be less than the defined session expiration
 * period on app server.
 */
#define M4E_PERIOD_SRV_UPDATE_STATUS 15

/* Used for assuring one running instance of the application per user */
#define  M4E_APP_INSTANCE_KEY        M4E_APP_NAME

/* Qt custom event type for notifying about new application instances */
#define  M4E_APP_INSTANCE_EVENT_TYPE 2103

/* Alarm animation period in seconds */
#define M4E_ALARM_ANIM_PERIOD       20

#endif // CONFIGURATION_H
