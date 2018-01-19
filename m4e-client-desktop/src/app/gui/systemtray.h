/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef SYSTEMTRAY_H
#define SYSTEMTRAY_H

#include <configuration.h>
#include <webapp/webapp.h>
#include <QSystemTrayIcon>
#include <QObject>


namespace m4e
{
namespace gui
{

class MainWindow;

/**
 * @brief Class handling the system try menu
 *
 * @author boto
 * @date Nov 6, 2017
 */
class SystemTray : QObject
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(SystemTray) ";

    Q_OBJECT

    public:

        /**
         * @brief Create the tray instance.
         */
                                    SystemTray( webapp::WebApp* p_webApp, MainWindow* p_parent );

        /**
         * @brief Destroy the instance.
         */
        virtual                     ~SystemTray();

        /**
         * @brief Post a message to show pop-up in sys tray. Note that the actual appearance of the pup-up depends on the OS.
         *
         * @param title     The title
         * @param message   The message text
         * @param warning   Pass true in order to display a warning icon for the message.
         * @param duration  Duration in milliseconds for displaying the pop-up
         */
        void                        showMessage( const QString& title, const QString& message, bool warning = false, int duration = 2000 );

        /**
         * @brief Use this method in order to change the system try icon and notify the user about news while the main window is minimized.
         *
         * @param show      Pass true to change the icon to notification style, pass false to show the icon in normal style.
         * @param toolTip   An optional tooltip text
         */
        void                        showIconNotify( bool show, const QString& toolTip = QString() );

        /**
         * @brief Check if the system tray is available. Some platforms may not support it.
         *
         * @return Return true if the system tray is available.
         */
        static bool                 isTrayAvailable();

    protected slots:

        /**
         * @brief Called when the try icon was activated.
         *
         * @param reason    Reason of activation
         */
        void                        onActivated( QSystemTrayIcon::ActivationReason reason );

        /**
         * @brief Called when a menu icon was selected.
         *
         * @param p_action  The triggered action
         */
        void                        onMenuTriggert( QAction* p_action );

        /**
         * @brief Called when the menu is about to be shown.
         */
        void                        onMenuAboutToShow();

        /**
         * @brief This method is called when the tray message was clicked.
         */
        void                        onMessageClicked();

        /**
         * @brief This signal is emitted to notify about user authentication results.
         *
         * @param success  true if the user was successfully authenticated, otherwise false
         * @param userId   User ID, valid if success is true
         */
        void                        onUserSignedIn( bool success, QString userId );

        /**
         * @brief This signal is emitted to notify about user authentication results.
         *
         * @param success  true if the user was successfully authenticated, otherwise false
         * @param userId   User ID, valid if success is true
         */
        void                        onUserSignedOff( bool success );

        /**
         * @brief This signal is emitted when the connection to server was closed.
         */
        void                        onServerConnectionClosed();

        /**
         * @brief This signal is emitted  when an event message was arrived. An event message can be used to buzz all event members.
         *
         * @param senderId      Message sender Id (usually an user ID)
         * @param senderName    Message sender's name
         * @param eventId       ID of receiving event
         * @param notify        Notification object containing the message content
         */
        void                        onEventMessage( QString senderId, QString senderName, QString eventId, m4e::notify::NotifyEventPtr notify );

        /**
         * @brief This signal is emitted when an event location vote arrives.
         *
         * @param senderId   User ID of the voter
         * @param senderName User name of the voter
         * @param eventId    Event ID
         * @param loactionId Event location ID
         * @param vote       true for vote and false for unvote the given location
         */
        void                        onEventLocationVote( QString senderId, QString senderName, QString eventId, QString locationId, bool vote );

        /**
         * @brief This signal is emitted when the results of unread mails count request arrive.
         *
         * @param success  true if the count of unread mails could successfully be retrieved, otherwise false
         * @param count     Count of unread mails
         */
        void                        onResponseCountUnreadMails( bool success, int count );

        /**
         * @brief Received when an event voting has started.
         *
         * @param event     The event which triggered its voting alarm
         */
        void                        onLocationVotingStart( m4e::event::ModelEventPtr event );

        /**
         * @brief End of an event voting time.
         *
         * @param event  The event
         */
        void                        onLocationVotingEnd( m4e::event::ModelEventPtr event );

        /**
         * @brief Notify about a new user chat message.
         *
         * @param msg Chat message
         */
        void                        onReceivedChatMessageUser( m4e::chat::ChatMessagePtr msg );

        /**
         * @brief Notify about a new event chat message.
         *
         * @param msg Chat message
         */
        void                        onReceivedChatMessageEvent( m4e::chat::ChatMessagePtr msg );

    protected:

        void                        setupSystemTray();

        webapp::WebApp*             _p_webApp     = nullptr;

        MainWindow*                 _p_mainWindow = nullptr;

        QSystemTrayIcon*            _p_systemTray = nullptr;

        bool                        _enableNotification = true;

        bool                        _enableAlarm  = true;
};

} // namespace gui
} // namespace m4e

#endif // SYSTEMTRAY_H
