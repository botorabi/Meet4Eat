/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include <configuration.h>
#include <webapp/webapp.h>
#include <chat/chatsystem.h>
#include <event/events.h>
#include <event/widgeteventlist.h>
#include <notification/notifications.h>
#include <QMainWindow>
#include <QMouseEvent>
#include <QTimer>


namespace Ui {
  class MainWindow;
}

namespace m4e
{
namespace gui
{

class MailboxWindow;
class SystemTray;

/**
 * @brief Main application window class
 *
 * @author boto
 * @date Aug 2, 2017
 */
class MainWindow : public QMainWindow
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(MainWindow) ";

    Q_OBJECT

    public:

        /**
         * @brief Create the main window instance.
         */
                                    MainWindow();

        /**
         * @brief Destroy the instance.
         */
        virtual                     ~MainWindow();

        /**
         * @brief Select the given event in event list.
         *
         * @param eventId ID of event to select
         */
        void                        selectEvent( const QString& eventId );

        /**
         * @brief Request for teminating the application.
         */
        void                        terminate();

    protected slots:

        void                        onMailWindowClosed();

        /**
         * @brief Initial webapp setup happens in this slot.
         */
        void                        onTimerInit();

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
         * @brief Periodic update timer
         */
        void                        onTimerUpdate();

        void                        onBtnStatusClicked();

        void                        onBtnLogoClicked();

        void                        onBtnCollapseLogsClicked();

        void                        onBtnCloseClicked();

        void                        onBtnMinimizeClicked();

        void                        onBtnMaximizeClicked();

        void                        onBtnUserProfileClicked();

        void                        onBtnMailsClicked();

        void                        onBtnSettingsClicked();

        void                        onBtnAboutClicked();

        void                        onAboutLinkActivated( QString link );

        void                        onBtnAddEvent();

        void                        onBtnRefreshEvents();

        void                        onEventSelection( QString id );

        void                        onCreateNewLocation( QString eventId );

        /**
         * @brief This signal is emitted to inform about the current authentication state.
         *
         * @param authenticated  True if the user is authenticated, otherwise false
         */
        void                        onAuthState( bool authenticated );

        /**
         * @brief This signal is emitted when an update of user data was arrived.
         *        The user data model can also be empty (e.g. if there were server connection problems).
         *
         * @param user     User data
         */
        void                        onUserDataReady( m4e::user::ModelUserPtr user );

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
         * @brief This signal is received when user events were arrived.
         *
         * @param success  true for successful access
         * @param events   User events
         */
        void                        onResponseGetEvents( bool success, QList< m4e::event::ModelEventPtr > events );

        /**
         * @brief This signal is emitted when an event was changed.
         *
         * @param changeType One of ChangeType enums
         * @param eventId    Event ID
         */
        void                        onEventChanged( m4e::notify::Notifications::ChangeType changeType, QString eventId );

        /**
         * @brief This signal is emitted when an event location was changed.
         *
         * @param changeType One of ChangeType enums
         * @param eventId    Event ID
         * @param loactionId Event location ID
         */
        void                        onEventLocationChanged( m4e::notify::Notifications::ChangeType changeType, QString eventId, QString locationId );

        /**
         * @brief This signal is emitted when an event location vote arrives.
         *
         * @param senderId   User ID of the voter
         * @param eventId    Event ID
         * @param loactionId Event location ID
         * @param vote       true for vote and false for unvote the given location
         */
        void                        onEventLocationVote( QString senderId, QString eventId, QString locationId, bool vote );

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
         * @brief This signal is emitted when the results of unread mails count request arrive.
         *
         * @param success  true if the count of unread mails could successfully be retrieved, otherwise false
         * @param count     Count of unread mails
         */
        void                        onResponseCountUnreadMails( bool success, int count );

    protected:

        void                        customEvent( QEvent* p_event );

        void                        updateStatus( const QString& text, bool offline );

        void                        addLogText( const QString& text );

        void                        closeEvent( QCloseEvent* p_event );

        void                        storeWindowGeometry();

        void                        restoreWindowGeometry();

        void                        mouseDoubleClickEvent( QMouseEvent* p_event );

        void                        mousePressEvent( QMouseEvent* p_event );

        void                        mouseReleaseEvent( QMouseEvent* p_event );

        void                        mouseMoveEvent( QMouseEvent* p_event );

        void                        clearWidgetClientArea();

        void                        createWidgetMyEvents();

        void                        clearWidgetMyEvents();

        void                        createWidgetEvent( const QString& eventId );

        Ui::MainWindow*             _p_ui            = nullptr;

        QTimer*                     _p_initTimer     = nullptr;

        QTimer*                     _p_updateTimer   = nullptr;

        webapp::WebApp*             _p_webApp        = nullptr;

        MailboxWindow*              _p_mailWindow    = nullptr;

        SystemTray*                 _p_systemTray    = nullptr;

        event::WidgetEventList*     _p_eventList     = nullptr;

        bool                        _dragging        = false;

        QPoint                      _draggingPos;

        bool                        _initialSignIn   = true;

        bool                        _enableKeepAlive = false;

        int                         _lastUnreadMails = 0;

        QString                     _currentEventSelection;
};

} // namespace gui
} // namespace m4e

#endif // MAINWINDOW_H
