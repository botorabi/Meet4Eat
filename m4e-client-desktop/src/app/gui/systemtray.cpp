/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "systemtray.h"
#include "mainwindow.h"
#include <core/log.h>
#include <common/guiutils.h>
#include <QApplication>
#include <QSystemTrayIcon>
#include <QMenu>


namespace m4e
{
namespace gui
{

enum MenuIDs
{
    MenuOpen                = 100,
    MenuQuit                = 101,
    MenuEnableNotification  = 102,
    MenuEnableAlarm         = 103
};


SystemTray::SystemTray( webapp::WebApp* p_webApp, MainWindow* p_parent ) :
 QObject( p_parent ),
 _p_webApp( p_webApp ),
 _p_mainWindow( p_parent )
{
    setupSystemTray();

    connect( _p_webApp, SIGNAL( onUserSignedIn( bool, QString ) ), this, SLOT( onUserSignedIn( bool, QString ) ) );
    connect( _p_webApp, SIGNAL( onUserSignedOff( bool ) ), this, SLOT( onUserSignedOff( bool ) ) );
    connect( _p_webApp, SIGNAL( onServerConnectionClosed() ), this, SLOT( onServerConnectionClosed() ) );

    connect( _p_webApp->getNotifications(), SIGNAL( onEventMessage( QString, QString, m4e::notify::NotifyEventPtr ) ), this,
                                            SLOT( onEventMessage( QString, QString, m4e::notify::NotifyEventPtr ) ) );

    connect( _p_webApp->getMailBox(), SIGNAL( onResponseCountMails( bool, int, int ) ), this,
                                      SLOT( onResponseCountMails( bool, int, int ) ) );
}

SystemTray::~SystemTray()
{
}

void SystemTray::onActivated( QSystemTrayIcon::ActivationReason reason )
{
    switch ( reason )
    {
        case QSystemTrayIcon::DoubleClick:
            common::GuiUtils::widgetToFront( _p_mainWindow );
        break;

        default:
            break;
    }
}

void SystemTray::onMenuTriggert( QAction* p_action )
{
    int id = p_action->data().toInt();
    switch( id )
    {
        case MenuOpen:
            common::GuiUtils::widgetToFront( _p_mainWindow );
        break;

        case MenuQuit:
            _p_mainWindow->terminate();
        break;

        case MenuEnableNotification:
            _enableNotification = p_action->isChecked();
        break;

        case MenuEnableAlarm:
            _enableAlarm = p_action->isChecked();
        break;

        default:
            log_warning << TAG << "unsupported tray menu option: " << id << std::endl;
    }
}

void SystemTray::onMessageClicked()
{
    common::GuiUtils::widgetToFront( _p_mainWindow );
}

void SystemTray::setupSystemTray()
{
    _p_systemTray = new QSystemTrayIcon( QIcon( ":/icon.ico" ), this );
    connect( _p_systemTray, SIGNAL( activated( QSystemTrayIcon::ActivationReason ) ), this, SLOT( onActivated( QSystemTrayIcon::ActivationReason ) ) );
    connect( _p_systemTray, SIGNAL( messageClicked() ), this, SLOT( onMessageClicked() ) );

    QMenu* p_menu = new QMenu();
    connect( p_menu, SIGNAL( triggered( QAction* ) ), this, SLOT( onMenuTriggert( QAction* ) ) );

    QAction* p_action;

    p_action = new QAction();
    p_action->setSeparator( true );
    p_menu->addAction( p_action );

    p_action = new QAction( "Open Meet4Eat" );
    p_action->setData( QVariant( MenuOpen ) );
    p_menu->addAction( p_action );

    p_action = new QAction();
    p_action->setSeparator( true );
    p_menu->addAction( p_action );

    p_action = new QAction( "Enable Notification" );
    p_action->setCheckable( true );
    p_action->setChecked( true );
    p_action->setData( QVariant( MenuEnableNotification ) );
    p_menu->addAction( p_action );

    p_action = new QAction( "Enable Alarm" );
    p_action->setCheckable( true );
    p_action->setChecked( true );
    p_action->setData( QVariant( MenuEnableAlarm ) );
    p_menu->addAction( p_action );

    p_action = new QAction();
    p_action->setSeparator( true );
    p_menu->addAction( p_action );

    p_action = new QAction( "Quit" );
    p_action->setData( QVariant( MenuQuit ) );
    p_menu->addAction( p_action );

    _p_systemTray->setContextMenu( p_menu );
    _p_systemTray->show();
}

void SystemTray::showMessage( const QString& title, const QString& message, bool warning, int duration )
{
    // are notifications enabled?
    if ( !_enableNotification )
        return;

    _p_systemTray->showMessage( title, message, warning ? QSystemTrayIcon::Warning : QSystemTrayIcon::Information, duration );
}

void SystemTray::onUserSignedIn( bool success, QString /*userId*/ )
{
    QString title = QApplication::translate( "SystemTray", "Meet4Eat - User Sign In" );
    QString text;
    bool    warning = false;
    if ( !success )
    {
        warning = true;
        text =  QApplication::translate( "SystemTray", "User could not sign in!" );
    }
    else
    {
        text = QApplication::translate( "SystemTray", "User was successfully signed in." );
    }
    showMessage( title, text, warning );
}

void SystemTray::onUserSignedOff( bool /*success*/ )
{
}

void SystemTray::onServerConnectionClosed()
{
}

void SystemTray::onEventMessage( QString senderId, QString eventId, notify::NotifyEventPtr notify )
{
    QString eventname;
    QString userid;
    event::ModelEventPtr event = _p_webApp->getEvents()->getUserEvent( eventId );
    user::ModelUserPtr   user  = _p_webApp->getUser()->getUserData();

    if ( event.valid() )
        eventname = event->getName();

    if ( user.valid() )
        userid = user->getId();

    // suppress echo
    if ( userid == senderId )
        return;

    QString title = QApplication::translate( "SystemTray", "Meet4Eat - Event Notification" );
    QString text = notify->getSubject();

    showMessage( title, text, false );
}

void SystemTray::onResponseCountMails( bool success, int /*countTotal*/, int countUnread )
{
    if ( success && ( countUnread > 0 ) )
    {
        QString title = QApplication::translate( "SystemTray", "Meet4Eat - New Mails" );
        QString text = QApplication::translate( "SystemTray", "You have received new mails." );
        showMessage( title, text, false );
    }
}

} // namespace gui
} // namespace m4e
