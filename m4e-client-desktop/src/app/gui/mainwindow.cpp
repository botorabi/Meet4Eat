/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "mainwindow.h"
#include "mailboxwindow.h"
#include "systemtray.h"
#include <core/log.h>
#include <settings/appsettings.h>
#include <settings/dialogsettings.h>
#include <event/widgeteventlist.h>
#include <event/widgeteventpanel.h>
#include <notification/notifyevent.h>
#include <common/basedialog.h>
#include <common/dialogmessage.h>
#include <common/guiutils.h>
#include <event/dialogeventsettings.h>
#include <user/dialogusersettings.h>
#include <gui/alarmwindow.h>
#include <gui/buzzwindow.h>
#include "ui_mainwindow.h"
#include "ui_widgetabout.h"
#include <QDesktopServices>
#include <QLayout>


namespace m4e
{
namespace gui
{

/** Mail button styling */
const QString MAIL_BTN_STYLE = \
"QPushButton {\
 background-color: transparent;\
 border-radius: 2px;\
 border-image: url(@MAIL_BTN_ICON@);\
}\
QPushButton:hover {\
 background-color: rgb(50,82,95);\
 border-radius: 2px;\
}";
const QString MAIL_BTN_ICON_NONEWMAILS = ":/icon-mail.png";
const QString MAIL_BTN_ICON_NEWMAILS   = ":/icon-mail-notify.png";


MainWindow::MainWindow() :
 QMainWindow( nullptr ),
 _p_ui( new Ui::MainWindow )
{
    setWindowFlags( Qt::Window | Qt::FramelessWindowHint | Qt::CustomizeWindowHint );
    setAttribute( Qt::WA_NoSystemBackground );
    setAttribute( Qt::WA_TranslucentBackground );

    _p_ui->setupUi( this );
    _p_ui->pushButtonResizer->setControlledWidget( this );

    restoreWindowGeometry();

    // prepare the start of webapp, it connects the application to the webapp server
    _p_webApp = new webapp::WebApp( this );
    registerSignals( _p_webApp );

    // create the try icon
    _p_systemTray = new SystemTray( _p_webApp, this );

    _p_initTimer = new QTimer( this );
    _p_initTimer->setSingleShot( true );
    connect( _p_initTimer, SIGNAL( timeout() ), this, SLOT( onTimerInit() ) );
    _p_initTimer->start( 1000 );

    int keepaliveperiod = M4E_PERIOD_SRV_UPDATE_STATUS * 1000 * 60;
    _p_updateTimer = new QTimer( this );
    _p_updateTimer->setSingleShot( false );
    _p_updateTimer->setInterval( keepaliveperiod );
    connect( _p_updateTimer, SIGNAL( timeout() ), this, SLOT( onTimerUpdate() ) );
    _p_updateTimer->start( keepaliveperiod );

    _p_eventTimer = new QTimer( this );
    _p_eventTimer->setSingleShot( true );
    connect( _p_eventTimer, SIGNAL( timeout() ), this, SLOT( onEventRefreshTimer() ) );

    _p_recoveryTimer = new QTimer( this );
    _p_recoveryTimer->setSingleShot( true );
    connect( _p_recoveryTimer, SIGNAL( timeout() ), this, SLOT( onRecoveryTimer() ) );

    setupStatusUI( QApplication::translate( "MainWindow", "No Connection!" ), false );
    _p_ui->pushButtonRefreshEvents->hide();

    setupSoftwareUpdateUI( update::ModelUpdateInfoPtr() );

    clearWidgetClientArea();
}

MainWindow::~MainWindow()
{
    delete _p_ui;
}

void MainWindow::selectEvent( const QString& eventId )
{
    _currentEventSelection = eventId;
    if ( _p_eventList )
    {
        _p_eventList->selectEvent( _currentEventSelection );
    }
}

void MainWindow::terminate()
{
    // first exec the closeEvent handler
    QCloseEvent event;
    closeEvent( &event );

    QApplication::quit();
}

void MainWindow::customEvent( QEvent* p_event )
{
    // this event arrives when the user tries to start another application instance.
    if ( p_event->type() == M4E_APP_INSTANCE_EVENT_TYPE )
    {
        // then bring the main window on top.
        common::GuiUtils::bringWidgetToFront( this );
    }
}

void MainWindow::onMailWindowClosed()
{
    _p_mailWindow = nullptr;
    _p_webApp->getMailBox()->requestCountUnreadMails();
}

void MainWindow::closeEvent( QCloseEvent* p_event )
{
    // hide the window immediately, the dying gasp below takes a few seconds and we don't want a dangling window in that time
    hide();

    if ( _p_mailWindow )
        _p_mailWindow->hide();

    // store back the window gemo
    storeWindowGeometry();
    // accept closing
    p_event->setAccepted( true );

    // shutdown the server connection
    _p_webApp->shutdownConnection();
    // process events to give a chance to finish connection shutdown
    for ( unsigned int i = 0; i < 10; i++ )
    {
        qApp->processEvents( QEventLoop::AllEvents, 100 );
        QThread::msleep( 100 );
    }
    log_verbose << TAG << "shutting down the main window" << std::endl;
}

void MainWindow::onTimerInit()
{
    // if no login exist then show the settings dialog
    QString login = settings::AppSettings::get()->readSettingsValue( M4E_SETTINGS_CAT_USER, M4E_SETTINGS_KEY_USER_LOGIN, "" );
    if ( login.isEmpty() )
    {
        showSettingsDialog();
    }

    QString remember = settings::AppSettings::get()->readSettingsValue( M4E_SETTINGS_CAT_USER, M4E_SETTINGS_KEY_USER_PW_REM, "yes" );
    if ( remember == "yes" )
    {
        _p_webApp->establishConnection();
    }
}

void MainWindow::onLocationVotingStart( event::ModelEventPtr event )
{
    addLogText( "Location voting started for event '" + event->getName() + "'" );

    QString enablealarm = settings::AppSettings::get()->readSettingsValue( M4E_SETTINGS_CAT_NOTIFY, M4E_SETTINGS_KEY_NOTIFY_ALARM, "yes" );
    if ( enablealarm == "yes" )
    {
        gui::AlarmWindow* p_dlg = new gui::AlarmWindow( this );
        p_dlg->setupUI( event );
        p_dlg->show();
        common::GuiUtils::bringWidgetToFront( p_dlg );
    }
}

void MainWindow::onLocationVotingEnd( m4e::event::ModelEventPtr event )
{
    addLogText( "Location voting has ended for event '" + event->getName() + "'" );
}

void MainWindow::onTimerUpdate()
{
    if ( _enableKeepAlive )
    {
        log_verbose << TAG << "sending keepalive" << std::endl;
        _p_webApp->requestAuthState();
        _p_webApp->getMailBox()->requestCountUnreadMails();
    }
}

void MainWindow::onBtnStatusClicked()
{
    if ( _p_webApp->getAuthState() != webapp::WebApp::AuthSuccessful )
    {
        if ( _recoverConnection )
        {
            setupStatusUI( QApplication::translate( "MainWindow", "Connecting..." ), false );
            _p_webApp->requestAuthState();
        }
        else
        {
            showSettingsDialog();
        }
    }
}

void MainWindow::onBtnSoftwareUpdateClicked()
{
    if ( !_updateInfo.valid() )
        return;

    QDesktopServices::openUrl(  QUrl( _updateInfo->getURL() ) );
}

void MainWindow::onBtnLogoClicked()
{
    QDesktopServices::openUrl(  QUrl( M4E_APP_URL ) );
}

void MainWindow::onBtnCollapseLogsClicked()
{
    _p_ui->textLogs->setVisible( !_p_ui->textLogs->isVisible() );
    _p_ui->textLogs->parentWidget()->updateGeometry();
}

void MainWindow::storeWindowGeometry()
{
    QSettings* p_settings = settings::AppSettings::get()->getSettings();
    QByteArray geom = saveGeometry();
    p_settings->setValue( M4E_SETTINGS_KEY_WIN_GEOM, geom );
}

void MainWindow::restoreWindowGeometry()
{
    QSettings* p_settings = settings::AppSettings::get()->getSettings();
    QByteArray geom =  p_settings->value( M4E_SETTINGS_KEY_WIN_GEOM ).toByteArray();
    restoreGeometry( geom );
}

void MainWindow::onBtnCloseClicked()
{
    // before hiding the window check if a system tray is available. if not then just minimize the window, otherwise the app will get inaccessible.
    if ( !SystemTray::isTrayAvailable() )
    {
        onBtnMinimizeClicked();
    }
    else
    {
        QString quitmsg = settings::AppSettings::get()->readSettingsValue( M4E_SETTINGS_CAT_APP, M4E_SETTINGS_KEY_APP_QUIT_MSG, "" );
        if ( quitmsg.isEmpty() )
        {
            common::DialogMessage msg( this );
            QString text = QApplication::translate( "MainWindow", "The application will be running in the background.\nQuit the application by using the system tray menu."
                                                                  "\n\nShould this message be displayed next time?" );
            msg.setupUI( QApplication::translate( "MainWindow", "Quit Application" ),
                         text,
                         common::DialogMessage::BtnYes |  common::DialogMessage::BtnNo );

            if ( msg.exec() == common::DialogMessage::BtnNo )
            {
                settings::AppSettings::get()->writeSettingsValue( M4E_SETTINGS_CAT_APP, M4E_SETTINGS_KEY_APP_QUIT_MSG, "no" );
            }
        }
        hide();
    }
}

void MainWindow::showSettingsDialog()
{
    // don't show the dialog if the main window is minimized
    if ( !isVisible() )
        return;

    if ( !_p_settingsDlg )
        _p_settingsDlg = new settings::DialogSettings( _p_webApp, this );

    if ( !_p_settingsDlg->isVisible() )
        _p_settingsDlg->exec();
    else
        _p_settingsDlg->show();
}

void MainWindow::mouseDoubleClickEvent( QMouseEvent* p_event )
{
    // drag the window only by the means of head-bar
    if ( !_p_ui->widgetHead->geometry().contains( p_event->pos() ) )
        return;

    onBtnMaximizeClicked();
}

void MainWindow::mousePressEvent( QMouseEvent* p_event )
{
    // drag the window only by the means of head-bar
    if ( !_p_ui->widgetHead->geometry().contains( p_event->pos() ) )
        return;

    _draggingPos = p_event->pos();
    _dragging = true;
}

void MainWindow::mouseReleaseEvent( QMouseEvent* /*p_event*/ )
{
    _dragging = false;
}

void MainWindow::mouseMoveEvent( QMouseEvent* p_event )
{
    if ( _dragging )
    {
        if ( ( windowState() & Qt::WindowMaximized ) != 0 )
        {
            setWindowState( windowState() & ~Qt::WindowMaximized );
        }

        move( p_event->globalPos() - _draggingPos );
    }
}

void MainWindow::onBtnMinimizeClicked()
{
    setWindowState( Qt::WindowMinimized );
}

void MainWindow::onBtnMaximizeClicked()
{
    if ( windowState() & Qt::WindowMaximized )
    {
        setWindowState( windowState() & ~Qt::WindowMaximized );
    }
    else
    {
        setWindowState( windowState() | Qt::WindowMaximized );
    }
}

void MainWindow::onBtnUserProfileClicked()
{
    if ( _p_webApp->getAuthState() != webapp::WebApp::AuthSuccessful )
        return;

    user::DialogUserSettings dlg( _p_webApp, this );
    dlg.setupUI( _p_webApp->getUser()->getUserData() );
    dlg.exec();
}

void MainWindow::onBtnMailsClicked()
{
    if ( _p_mailWindow )
    {
        common::GuiUtils::bringWidgetToFront( _p_mailWindow );
    }
    else
    {
        _p_mailWindow = new MailboxWindow( _p_webApp, this );
        connect( _p_mailWindow, SIGNAL( onMailWindowClosed() ), this, SLOT( onMailWindowClosed() ) );
        _p_mailWindow->show();
    }
}

void MainWindow::onBtnSettingsClicked()
{
    _recoverConnection = false;
    showSettingsDialog();
}

void MainWindow::onBtnAboutClicked()
{
    Ui::WidgetAbout about;
    common::BaseDialog* p_dlg = new common::BaseDialog( this );
    p_dlg->decorate( about );
    connect( about.labelText, SIGNAL( linkActivated( QString ) ), this, SLOT( onAboutLinkActivated( QString ) ) );

    QString text = about.labelText->text();
    text.replace( "@APP_NAME@", M4E_APP_NAME );
    text.replace( "@APP_VERSION@", M4E_APP_VERSION );
    text.replace( "@COPYRIGHT@", M4E_APP_COPYRIGHT );
    text.replace( "@URL@", M4E_APP_URL );
    about.labelText->setText( text );

    p_dlg->setTitle( "About " + QString( M4E_APP_NAME ) );
    QString btnok( "Ok" );
    p_dlg->setupButtons( &btnok, nullptr, nullptr );
    p_dlg->setResizable( false );
    p_dlg->exec();
    delete p_dlg;
}

void MainWindow::onAboutLinkActivated( QString link )
{
    if ( link == "LICENSE" )
    {
        QString text;
        QFile data( ":/LICENSE" );
        data.open( QIODevice::ReadOnly );
        text = data.readAll();
        common::DialogMessage msg( this );
        msg.setupUI( QApplication::translate( "MainWindow", "License" ),
                     text,
                     common::DialogMessage::BtnOk );
        msg.exec();
    }
    else if ( link == "WEBSITE" )
    {
        QDesktopServices::openUrl(  QUrl( M4E_APP_URL ) );
    }
}

void MainWindow::onBtnAddEvent()
{
    event::DialogEventSettings* p_dlg = new event::DialogEventSettings( _p_webApp, this , true);
    event::ModelEventPtr event = new event::ModelEvent();
    event->setStartDate( QDateTime::currentDateTime() );

    p_dlg->setupNewEventUI( event );
    p_dlg->exec();
}

void MainWindow::onBtnRefreshEvents()
{
    _p_ui->pushButtonRefreshEvents->hide();
}

void MainWindow::onEventSelection( QString id )
{
    clearWidgetClientArea();
    createWidgetEvent( id );
    _currentEventSelection = id;
}

void MainWindow::onCreateNewLocation( QString eventId )
{
    if ( _p_eventList )
        _p_eventList->createNewLocation( eventId );
}

void MainWindow::onWebServerInfo( bool success, QString /*version*/ )
{
    if ( !success )
    {
        if ( _recoverConnection )
        {
            scheduleConnectionRecovery();
        }
        else
        {
            log_debug << TAG << "the server seems to be unreachable!" << std::endl;
            addLogText( "Application server seems to be unreachable!" );
            showSettingsDialog();
        }
    }
    else
    {
        _p_webApp->getUpdateCheck()->requestGetUpdateInfo();
    }
}

void MainWindow::onAuthState( bool success, bool authenticated )
{
    if ( !success )
    {
        if ( _recoverConnection )
        {
            // schedule a new attempt to sign in
            log_debug << TAG << "cannot reach the app server" << std::endl;
            scheduleConnectionRecovery();
        }
    }
    else if ( !authenticated )
    {
        log_debug << TAG << "attempt to connect the server..." << std::endl;
        _enableKeepAlive = false;
        _p_webApp->establishConnection();
    }
}

void MainWindow::onUserDataReady( user::ModelUserPtr user )
{
    QString text;
    if ( user.valid() )
    {
        text = QApplication::translate( "MainWindow", "User: " ) + user->getName();
    }
    else
    {
        text = QApplication::translate( "MainWindow", "No Connection!" );
    }

    setupStatusUI( text, true );

    // a shutdown may have been done before, so we have to re-register some signals
    registerSignals( _p_webApp );

    _p_webApp->getEvents()->requestGetEvents();
    _p_webApp->getMailBox()->requestCountUnreadMails();
}

void MainWindow::onUserSignedIn( bool success, QString userId )
{
    if ( success )
    {
        log_info << TAG << "user was successfully signed in" << std::endl;
        // start the keep alive updates
        _enableKeepAlive = true;
        _recoverConnection = true;
        addLogText( "Web App Server " + _p_webApp->getWebAppVersion() );
        addLogText( "User has successfully signed in" );
    }
    else
    {
        log_info << TAG << "user could not sign in: " << userId << std::endl;
        setupStatusUI( QApplication::translate( "MainWindow", "Offline!" ), false );
        addLogText( "User failed to sign in!" );

        // show the dialog only on initial sign in
        if ( _initialSignIn )
        {
            common::DialogMessage msg( this );
            msg.setupUI( QApplication::translate( "MainWindow", "Connection Problem" ),
                         QApplication::translate( "MainWindow", "Could not connect the application server. Please check the settings." ),
                         common::DialogMessage::BtnOk );
            msg.exec();

            showSettingsDialog();
        }
    }

    _initialSignIn = false;
    _lastUnreadMails = 0;
}

void MainWindow::onUserSignedOff( bool success )
{
    setupStatusUI( QApplication::translate( "MainWindow", "Offline!" ), false );

    _enableKeepAlive = false;
    _recoverConnection = false;

    if ( success )
    {
        log_info << TAG << "user was successfully signed off" << std::endl;
        clearWidgetMyEvents();
        createWidgetMyEvents();
    }

    addLogText( "User has signed off" );
}

void MainWindow::onServerConnectionClosed()
{
    log_debug << TAG << "server connection was closed" << std::endl;

    setupStatusUI( QApplication::translate( "MainWindow", "Offline!" ), false );
    clearWidgetMyEvents();
    clearWidgetClientArea();

    addLogText( "Server connection was closed" );

    _enableKeepAlive = false;

    // an uncontrolled connection loss should be recovered
    if ( _recoverConnection )
    {
        log_debug << TAG << "lost connection!" << std::endl;
        scheduleConnectionRecovery();
    }
}

void MainWindow::onResponseGetEvents( bool /*success*/, QList< event::ModelEventPtr > /*events*/ )
{
    clearWidgetMyEvents();
    createWidgetMyEvents();
}

void MainWindow::onEventChanged( notify::Notifications::ChangeType changeType, QString eventId )
{
    log_verbose << TAG << "event notification arrived: " << eventId << std::endl;

    QString eventname;
    event::ModelEventPtr event = _p_webApp->getEvents()->getUserEvent( eventId );

    if ( event.valid() )
        eventname = event->getName();

    if ( changeType == notify::Notifications::Added )
        addLogText( QApplication::translate( "MainWindow", "A New event was created" ) );
    else if ( changeType == notify::Notifications::Removed )
        addLogText( QApplication::translate( "MainWindow", "An event was removed" ) );
    else
        addLogText( QApplication::translate( "MainWindow", "Event settings have changed: '" ) + eventname + "'" );

    _p_ui->pushButtonRefreshEvents->show();
    scheduleEventRefreshing();
}

void MainWindow::onEventLocationChanged( notify::Notifications::ChangeType changeType, QString eventId, QString locationId )
{
    log_verbose << TAG << "event location notification arrived: " << eventId << "/" << locationId << std::endl;

    QString eventname;
    event::ModelEventPtr event = _p_webApp->getEvents()->getUserEvent( eventId );

    if ( event.valid() )
        eventname = event->getName();

    if ( changeType == notify::Notifications::Added )
        addLogText( QApplication::translate( "MainWindow", "A New event location was created" ) );
    else if ( changeType == notify::Notifications::Removed )
        addLogText( QApplication::translate( "MainWindow", "An event location was removed" ) );
    else
        addLogText( QApplication::translate( "MainWindow", "Event location settings have changed: " ) + "'" + eventname + "'" );

    _p_ui->pushButtonRefreshEvents->show();
    scheduleEventRefreshing();
}

void MainWindow::onEventMemberChanged( notify::Notifications::ChangeType changeType, QString eventId, QString userId )
{
    log_debug << TAG << "user's event membership changed: " << eventId << "/" << userId << std::endl;
    _p_webApp->getEvents()->updateUserMembership( userId, eventId, changeType == notify::Notifications::Added );
}

void MainWindow::onEventLocationVote( QString senderId, QString senderName, QString eventId, QString locationId, bool vote )
{
    // suppress echo
    QString userid = _p_webApp->getUser()->getUserData()->getId();
    if ( senderId == userid )
        return;

    QString eventname;
    QString locationname;
    event::ModelEventPtr event = _p_webApp->getEvents()->getUserEvent( eventId );

    if ( event.valid() )
    {
        eventname = event->getName();
        event::ModelLocationPtr location = event->getLocation( locationId );
        if ( location.valid() )
            locationname = location->getName();
    }
    addLogText( QApplication::translate( "MainWindow", "Location vote arrived from '" ) + senderName + "': '" + eventname + "' / '" + locationname + "': " + ( vote ? "vote" : "unvote" ) );

    //! TODO play a sound
}

void MainWindow::onEventMessage( QString senderId, QString senderName, QString eventId, notify::NotifyEventPtr notify )
{
    // suppress echo
    if ( senderId == _p_webApp->getUser()->getUserData()->getId() )
        return;

    QString eventname;
    event::ModelEventPtr event = _p_webApp->getEvents()->getUserEvent( eventId );

    if ( event.valid() )
        eventname = event->getName();

    addLogText( QApplication::translate( "MainWindow", "New event message arrived: " ) + "'" + eventname + "', " + notify->getSubject() );

    QString title = QApplication::translate( "MainWindow", "Meet4Eat - @USER@" );
    title.replace( "@USER@", senderName );
    QString text = QApplication::translate( "MainWindow", "Buzzing the members of event" ) + " '" + event->getName() + "'";
    _p_systemTray->showMessage( title, text );


    QString enablealarm = settings::AppSettings::get()->readSettingsValue( M4E_SETTINGS_CAT_NOTIFY, M4E_SETTINGS_KEY_NOTIFY_ALARM, "yes" );
    if ( enablealarm == "yes" )
    {
        gui::BuzzWindow* p_dlg = new gui::BuzzWindow( this );
        p_dlg->setupUI( senderName + " / " + eventname, notify->getSubject(), notify->getText() );
        p_dlg->show();
        common::GuiUtils::bringWidgetToFront( p_dlg );
    }
}

void MainWindow::onResponseCountUnreadMails( bool success, int count )
{
    if ( success )
    {
        QString btnstyle = MAIL_BTN_STYLE;
        btnstyle.replace("@MAIL_BTN_ICON@", ( count > 0 ) ? MAIL_BTN_ICON_NEWMAILS : MAIL_BTN_ICON_NONEWMAILS );
        _p_ui->pushButtonUserMails->setStyleSheet( btnstyle );
        if ( count > _lastUnreadMails )
        {
            log_debug << "user has unread mails: " << count << std::endl;
            addLogText( QApplication::translate( "MainWindow", "New mails have arrived." ) );
        }
        _lastUnreadMails = count;
    }
}

void MainWindow::onResponseGetUpdateInfo( bool success, update::ModelUpdateInfoPtr updateInfo )
{
    if ( success )
    {
        if ( !updateInfo->getVersion().isEmpty() )
        {
            log_info << TAG << " there is a client update: " << updateInfo->getVersion() << std::endl;
        }
        else
        {
            log_debug << TAG << " the client is up to date" << std::endl;
        }
    }
    else
    {
        log_warning << TAG << "could not get client update information, reason: " << _p_webApp->getUpdateCheck()->getLastError() << std::endl;
    }

    _updateInfo = updateInfo;
    setupSoftwareUpdateUI( updateInfo );
}

void MainWindow::onUserOnlineStatusChanged( QString senderId, QString senderName, bool online )
{
    QString text;
    text = QApplication::translate( "MainWindow", "User '@USER@' went @STATUS@" );
    text.replace( "@USER@", senderName );
    text.replace( "@STATUS@", online ? "online" : "offline" );
    addLogText( text );

    _p_webApp->getEvents()->updateUserStatus( senderId, online );
}

void MainWindow::onEventRefreshTimer()
{
    addLogText( QApplication::translate( "MainWindow", "Refreshing all events" ) );
    _p_webApp->getEvents()->requestGetEvents();
}

void MainWindow::onRecoveryTimer()
{
    if ( _p_webApp->getAuthState() != webapp::WebApp::AuthSuccessful )
        _p_webApp->establishConnection();
}

void  MainWindow::reconnectSignal( const QObject* p_sender, const char* p_signal, const QObject* p_receiver, const char* p_member )
{
    disconnect( p_sender, p_signal, p_receiver, p_member );
    connect( p_sender, p_signal, p_receiver, p_member );
}

void MainWindow::registerSignals( webapp::WebApp* p_webApp )
{
    // register for all necessary signals of subsystems

    reconnectSignal( p_webApp, SIGNAL( onWebServerInfo( bool, QString ) ), this, SLOT( onWebServerInfo( bool, QString ) ) );
    reconnectSignal( p_webApp, SIGNAL( onAuthState( bool, bool ) ), this, SLOT( onAuthState( bool, bool ) ) );
    reconnectSignal( p_webApp, SIGNAL( onUserSignedIn( bool, QString ) ), this, SLOT( onUserSignedIn( bool, QString ) ) );
    reconnectSignal( p_webApp, SIGNAL( onUserSignedOff( bool ) ), this, SLOT( onUserSignedOff( bool ) ) );
    reconnectSignal( p_webApp, SIGNAL( onUserDataReady( m4e::user::ModelUserPtr ) ), this, SLOT( onUserDataReady( m4e::user::ModelUserPtr ) ) );
    reconnectSignal( p_webApp, SIGNAL( onServerConnectionClosed() ), this, SLOT( onServerConnectionClosed() ) );
    reconnectSignal( p_webApp->getNotifications(), SIGNAL( onEventChanged( m4e::notify::Notifications::ChangeType, QString ) ), this,
                                                   SLOT( onEventChanged( m4e::notify::Notifications::ChangeType, QString ) ) );
    reconnectSignal( p_webApp->getNotifications(), SIGNAL( onEventLocationChanged( m4e::notify::Notifications::ChangeType, QString, QString ) ), this,
                                                   SLOT( onEventLocationChanged( m4e::notify::Notifications::ChangeType, QString, QString ) ) );
    reconnectSignal( p_webApp->getNotifications(), SIGNAL( onEventMemberChanged( m4e::notify::Notifications::ChangeType, QString, QString ) ), this,
                                                   SLOT( onEventMemberChanged( m4e::notify::Notifications::ChangeType, QString, QString ) ) );
    reconnectSignal( p_webApp->getNotifications(), SIGNAL( onEventLocationVote( QString, QString, QString, QString, bool ) ), this,
                                                   SLOT( onEventLocationVote( QString, QString, QString, QString, bool ) ) );
    reconnectSignal( p_webApp->getNotifications(), SIGNAL( onEventMessage( QString, QString, QString, m4e::notify::NotifyEventPtr ) ), this,
                                                   SLOT( onEventMessage( QString, QString, QString, m4e::notify::NotifyEventPtr ) ) );
    reconnectSignal( p_webApp->getNotifications(), SIGNAL( onUserOnlineStatusChanged( QString, QString, bool ) ), this,
                                                   SLOT( onUserOnlineStatusChanged( QString, QString, bool ) ) );
    reconnectSignal( p_webApp->getEvents(), SIGNAL( onResponseGetEvents( bool, QList< m4e::event::ModelEventPtr > ) ), this,
                                            SLOT( onResponseGetEvents( bool, QList< m4e::event::ModelEventPtr > ) ) );
    reconnectSignal( p_webApp->getEvents(), SIGNAL( onLocationVotingStart( m4e::event::ModelEventPtr ) ), this,
                                            SLOT( onLocationVotingStart( m4e::event::ModelEventPtr ) ) );
    reconnectSignal( p_webApp->getEvents(), SIGNAL( onLocationVotingEnd( m4e::event::ModelEventPtr ) ), this,
                                            SLOT( onLocationVotingEnd( m4e::event::ModelEventPtr ) ) );
    reconnectSignal( p_webApp->getMailBox(), SIGNAL( onResponseCountUnreadMails( bool, int ) ), this,
                                             SLOT( onResponseCountUnreadMails( bool, int ) ) );
    reconnectSignal( p_webApp->getUpdateCheck(), SIGNAL( onResponseGetUpdateInfo( bool, m4e::update::ModelUpdateInfoPtr ) ), this,
                                                 SLOT( onResponseGetUpdateInfo( bool, m4e::update::ModelUpdateInfoPtr ) ) );
}

void MainWindow::setupStatusUI( const QString& text, bool online )
{
    _p_ui->pushButtonStatus->setText( text );
    _p_ui->pushButtonStatus->setEnabled( !online );
    QString tooltip = !online ? QApplication::translate( "MainWindow", "Click to connect the server" ) : "";
    _p_ui->pushButtonStatus->setToolTip( tooltip );
}

void MainWindow::addLogText( const QString& text )
{
    //! NOTE we need a more confortable logs widget, it should support a max length of lines
    //       but for now, the simple output is just ok.

    QDateTime timestamp = QDateTime::currentDateTime();
    QString ts = "[" + timestamp.toString( "yyyy-M-dd HH:mm:ss" ) + "]";
    QString logmsg = "<p>";
    logmsg += "<span style='color: gray;'>" + ts + "</span>";
    logmsg += " <span style='color: white;'>" + text + "</span>";
    logmsg += "</p>";
    _p_ui->textLogs->appendHtml( logmsg );
}

void MainWindow::clearWidgetClientArea()
{
    QLayoutItem* p_item;
    QLayout* p_layout = _p_ui->widgetClientArea->layout();
    while ( ( p_layout->count() > 0 ) && ( nullptr != ( p_item = p_layout->takeAt( 0 ) ) ) )
    {
        p_item->widget()->deleteLater();
        delete p_item;
    }
}

void MainWindow::clearWidgetMyEvents()
{
    if ( _p_eventList )
        _p_eventList->deleteLater();

    _p_eventList = nullptr;
}

void MainWindow::createWidgetMyEvents()
{
    clearWidgetClientArea();
    _p_eventList = new event::WidgetEventList( _p_webApp, this );
    _p_ui->widgetEventItems->layout()->addWidget( _p_eventList );
    connect( _p_eventList, SIGNAL( onEventSelection( QString /*id*/ ) ), this, SLOT( onEventSelection( QString /*id*/ ) ) );
    _p_eventList->selectEvent( _currentEventSelection );
}

void MainWindow::createWidgetEvent( const QString& eventId )
{
    event::WidgetEventPanel* p_eventpanel = new event::WidgetEventPanel( _p_webApp, _p_ui->widgetClientArea );
    connect( p_eventpanel, SIGNAL( onCreateNewLocation( QString /*eventId*/) ), this, SLOT( onCreateNewLocation( QString ) ) );
    p_eventpanel->setupEvent( eventId );

    _p_ui->widgetClientArea->layout()->addWidget( p_eventpanel );
}

void MainWindow::setupSoftwareUpdateUI( update::ModelUpdateInfoPtr updateInfo )
{
    if ( !updateInfo.valid() || updateInfo->getVersion().isEmpty() )
    {
        _p_ui->pushButtonSoftwareUpdate->setVisible( false );
        return;
    }

    QString text = QApplication::translate( "MainWindow", "Software Update Available\nNew Version: " );
    text += updateInfo->getVersion();

    _p_ui->pushButtonSoftwareUpdate->setText( text );
    _p_ui->pushButtonSoftwareUpdate->setVisible( true );
}

void MainWindow::scheduleConnectionRecovery()
{
    // avoid re-scheduling if the timer is already running
    if ( _p_recoveryTimer->remainingTime() > 0 )
        return;

    log_debug << TAG << " schedule a new connection..." << std::endl;
    // callback: onRecoveryTimer
    _p_recoveryTimer->start( M4E_PERIOD_CONN_RECOVERY * 60000 );
}

void MainWindow::scheduleEventRefreshing()
{
    // avoid re-scheduling if the timer is already running
    if ( _p_eventTimer->remainingTime() > 0 )
        return;

    // callback: onEventRefreshTimer
    _p_eventTimer->start( 1000 );
}

} // namespace gui
} // namespace m4e
