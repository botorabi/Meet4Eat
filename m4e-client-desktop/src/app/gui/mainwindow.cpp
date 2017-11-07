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
#include "ui_mainwindow.h"
#include "ui_widgetabout.h"
#include <QDesktopServices>
#include <QLayout>


namespace m4e
{
namespace gui
{

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
    setWindowFlags( Qt::Window | /*Qt::FramelessWindowHint |*/ Qt::CustomizeWindowHint );
    setAttribute( Qt::WA_NoSystemBackground );
    setAttribute( Qt::WA_TranslucentBackground );

    _p_ui->setupUi( this );
    restoreWindowGeometry();

    // prepare the start of webapp, it connects the application to the webapp server
    _p_webApp = new webapp::WebApp( this );
    connect( _p_webApp, SIGNAL( onAuthState( bool ) ), this, SLOT( onAuthState( bool ) ) );
    connect( _p_webApp, SIGNAL( onUserSignedIn( bool, QString ) ), this, SLOT( onUserSignedIn( bool, QString ) ) );
    connect( _p_webApp, SIGNAL( onUserSignedOff( bool ) ), this, SLOT( onUserSignedOff( bool ) ) );
    connect( _p_webApp, SIGNAL( onUserDataReady( m4e::user::ModelUserPtr ) ), this, SLOT( onUserDataReady( m4e::user::ModelUserPtr ) ) );
    connect( _p_webApp, SIGNAL( onServerConnectionClosed() ), this, SLOT( onServerConnectionClosed() ) );

    // create the try icon
    _p_systemTray = new SystemTray( _p_webApp, this );

    // create the chat system
    _p_chatSystem = new chat::ChatSystem( _p_webApp, this );

    _p_initTimer = new QTimer();
    _p_initTimer->setSingleShot( true );
    connect( _p_initTimer, SIGNAL( timeout() ), this, SLOT( onTimerInit() ) );
    _p_initTimer->start( 1000 );

    int keepaliveperiod = M4E_PERIOD_SRV_UPDATE_STATUS * 1000 * 60;
    _p_updateTimer = new QTimer();
    _p_updateTimer->setSingleShot( false );
    _p_updateTimer->setInterval( keepaliveperiod );
    connect( _p_updateTimer, SIGNAL( timeout() ), this, SLOT( onTimerUpdate() ) );
    _p_updateTimer->start( keepaliveperiod );

    updateStatus( QApplication::translate( "MainWindow", "Connecting..." ), false );
    _p_ui->pushButtonNotification->hide();

    clearWidgetClientArea();
}

MainWindow::~MainWindow()
{
    delete _p_ui;
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
        common::GuiUtils::widgetToFront( this );
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
    QString remember = settings::AppSettings::get()->readSettingsValue( M4E_SETTINGS_CAT_USER, M4E_SETTINGS_KEY_USER_PW_REM, "yes" );
    if ( remember == "yes" )
    {
        _p_webApp->establishConnection();
    }
}

void MainWindow::onTimerUpdate()
{
    if ( _enableKeepAlive )
    {
        _p_webApp->requestAuthState();
        _p_webApp->getMailBox()->requestCountUnreadMails();
    }
}

void MainWindow::onBtnStatusClicked()
{
    if ( _p_webApp->getAuthState() != webapp::WebApp::AuthSuccessful )
    {
        updateStatus( QApplication::translate( "MainWindow", "Connecting..." ), true );
        _p_webApp->establishConnection();
    }
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
        setWindowState( Qt::WindowMaximized );
    }
}

void MainWindow::onBtnUserProfileClicked()
{
    //! TODO
    log_verbose << TAG << "TODO user profile" << std::endl;
}

void MainWindow::onBtnMailsClicked()
{
    if ( _p_mailWindow )
    {
        common::GuiUtils::widgetToFront( _p_mailWindow );
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
    settings::DialogSettings* dlg = new settings::DialogSettings( _p_webApp, this );
    dlg->exec();
    delete dlg;
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
    event::DialogEventSettings* p_dlg = new event::DialogEventSettings( _p_webApp, this );
    event::ModelEventPtr event = new event::ModelEvent();
    event->setStartDate( QDateTime::currentDateTime() );

    p_dlg->setupNewEventUI( event );
    p_dlg->exec();
    delete p_dlg;
}

void MainWindow::onBtnNotificationClicked()
{
    addLogText( QApplication::translate( "MainWindow", "Refreshing all events" ) );

    _p_ui->pushButtonNotification->hide();
    _p_webApp->getEvents()->requestGetEvents();
}

void MainWindow::onEventSelection( QString id )
{
    clearWidgetClientArea();
    createWidgetEvent( id );
}

void MainWindow::onAuthState( bool authenticated )
{
    if ( !authenticated )
    {
        log_debug << TAG << "attempt to connect the server..." << std::endl;
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

    updateStatus( text, false );

    connect( _p_webApp->getNotifications(), SIGNAL( onEventChanged( m4e::notify::Notifications::ChangeType, QString ) ), this,
                                            SLOT( onEventChanged( m4e::notify::Notifications::ChangeType, QString ) ) );

    connect( _p_webApp->getNotifications(), SIGNAL( onEventLocationChanged( m4e::notify::Notifications::ChangeType, QString, QString ) ), this,
                                            SLOT( onEventLocationChanged( m4e::notify::Notifications::ChangeType, QString, QString ) ) );

    connect( _p_webApp->getNotifications(), SIGNAL( onEventMessage( QString, QString, m4e::notify::NotifyEventPtr ) ), this,
                                            SLOT( onEventMessage( QString, QString, m4e::notify::NotifyEventPtr ) ) );

    connect( _p_webApp->getEvents(), SIGNAL( onResponseGetEvents( bool, QList< m4e::event::ModelEventPtr > ) ), this,
                                     SLOT( onResponseGetEvents( bool, QList< m4e::event::ModelEventPtr > ) ) );

    connect( _p_webApp->getMailBox(), SIGNAL( onResponseCountUnreadMails( bool, int ) ), this,
                                      SLOT( onResponseCountUnreadMails( bool, int ) ) );

    _p_webApp->getEvents()->requestGetEvents();
    _p_webApp->getMailBox()->requestCountUnreadMails();
}

void MainWindow::onUserSignedIn( bool success, QString userId )
{
    if ( success )
    {
        log_verbose << TAG << "user was successfully signed in: " << userId << std::endl;
        // create the chat system
        _p_chatSystem = new chat::ChatSystem( _p_webApp, this );
        // start the keep alive updates
        _enableKeepAlive = true;
        addLogText( "User has successfully signed in" );
    }
    else
    {
        log_verbose << TAG << "user could not sign in: " << userId << std::endl;
        updateStatus( QApplication::translate( "MainWindow", "Offline!" ), true );
        addLogText( "User failed to sign in!" );

        // show the dialog only on initial sign in
        if ( _initialSignIn )
        {
            common::DialogMessage msg( this );
            msg.setupUI( QApplication::translate( "MainWindow", "Connection Problem" ),
                         QApplication::translate( "MainWindow", "Could not connect the application server. Please check the settings." ),
                         common::DialogMessage::BtnOk );
            msg.exec();

            settings::DialogSettings* dlg = new settings::DialogSettings( _p_webApp, this );
            dlg->exec();
            delete dlg;

            _enableKeepAlive = true;
        }
    }

    _initialSignIn = false;
    _lastUnreadMails = 0;
}

void MainWindow::onUserSignedOff( bool success )
{
    updateStatus( QApplication::translate( "MainWindow", "Offline!" ), true );

    _enableKeepAlive = false;

    delete _p_chatSystem;
    _p_chatSystem = nullptr;

    if ( success )
    {
        clearWidgetMyEvents();
        createWidgetMyEvents();
    }

    addLogText( "User has signed off" );
}

void MainWindow::onServerConnectionClosed()
{
    log_debug << TAG << "server connection was closed" << std::endl;

    updateStatus( QApplication::translate( "MainWindow", "Offline!" ), true );
    delete _p_chatSystem;
    _p_chatSystem = nullptr;
    clearWidgetMyEvents();
    createWidgetMyEvents();

    addLogText( "Server connection was closed" );
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

    _p_ui->pushButtonNotification->show();
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

    _p_ui->pushButtonNotification->show();
}

void MainWindow::onEventMessage( QString /*senderId*/, QString eventId, notify::NotifyEventPtr notify )
{
    QString eventname;
    event::ModelEventPtr event = _p_webApp->getEvents()->getUserEvent( eventId );

    if ( event.valid() )
        eventname = event->getName();

    addLogText( QApplication::translate( "MainWindow", "New event message arrived: " ) + "'" + eventname + "', " + notify->getSubject() );

    //! TODO: on buzz message, bring the application window with an own dialog to front and play a notification sound!
}

void MainWindow::onResponseCountUnreadMails( bool success, int coun )
{
    if ( success )
    {
        QString btnstyle = MAIL_BTN_STYLE;
        btnstyle.replace("@MAIL_BTN_ICON@", ( coun > 0 ) ? MAIL_BTN_ICON_NEWMAILS : MAIL_BTN_ICON_NONEWMAILS );
        _p_ui->pushButtonUserMails->setStyleSheet( btnstyle );
        if ( coun > _lastUnreadMails )
        {
            log_debug << "user has unread mails: " << coun << std::endl;
            addLogText( QApplication::translate( "MainWindow", "New mails have arrived." ) );
        }
        _lastUnreadMails = coun;
    }
}

void MainWindow::updateStatus( const QString& text, bool offline )
{
    _p_ui->pushButtonStatus->setText( text );
    _p_ui->pushButtonStatus->setEnabled( offline );
    QString tooltip = offline ? QApplication::translate( "MainWindow", "Click to connect the server" ) : "";
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
    QLayoutItem* p_item;
    QLayout* p_layout = _p_ui->widgetEventItems->layout();
    while ( ( p_layout->count() > 0 ) && ( nullptr != ( p_item = p_layout->takeAt( 0 ) ) ) )
    {
        p_item->widget()->deleteLater();
        delete p_item;
    }
}

void MainWindow::createWidgetMyEvents()
{
    clearWidgetClientArea();

    event::WidgetEventList* p_widget = new event::WidgetEventList( _p_webApp, this );
    _p_ui->widgetEventItems->layout()->addWidget( p_widget );
    connect( p_widget, SIGNAL( onEventSelection( QString /*id*/ ) ), this, SLOT( onEventSelection( QString /*id*/ ) ) );
    // auto-select the first event
    p_widget->selectFirstEvent();
}

void MainWindow::createWidgetEvent( const QString& eventId )
{
    event::WidgetEventPanel* p_widget = new event::WidgetEventPanel( _p_webApp, _p_ui->widgetClientArea );
    p_widget->setEvent( eventId );

    if ( _p_chatSystem )
        p_widget->setChatSystem( _p_chatSystem );

    _p_ui->widgetClientArea->layout()->addWidget( p_widget );
}

} // namespace gui
} // namespace m4e
