/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */


#include <configuration.h>
#include "core.h"
#include "log.h"
#include "utils.h"
#include "singleproc.h"
#include <settings/appsettings.h>
#include <gui/mainwindow.h>
#include <gui/systemtray.h>
#include <QDebug>
#include <QApplication>
#include <assert.h>


namespace m4e
{
namespace core
{


//! Log stream buffer for QDebug
static class QtOutputStream: public std::basic_streambuf< char >
{
    public:

                                QtOutputStream() {}

        virtual                 ~QtOutputStream() {}

    protected:

        virtual int_type        overflow( int_type c )
                                {
                                    if( !std::char_traits< char >::eq_int_type( c, std::char_traits< char >::eof() ) )
                                    {
                                        _msg += static_cast< char >( c );
                                        if( c == '\n' )
                                        {
                                            _msg.erase( _msg.length() - 2 );
                                            qDebug() << QString::fromStdString( _msg );
                                            _msg = "";
                                        }
                                    }
                                    return std::char_traits< char >::not_eof( c );
                                }

        std::string             _msg;

} qtOutputStream;

static std::basic_ostream< char > QtLogStream( &qtOutputStream );


Core::Core()
{
}

Core::~Core()
{
    shutdown();
}

void Core::initialize( int &argc, char* argv[] )
{
    _p_app = new QApplication( argc, argv );
    _p_app->setApplicationVersion( M4E_APP_VERSION );
    _p_app->setQuitOnLastWindowClosed( false );

    unsigned int loglevel = Log::L_DEBUG;
    QString      logfile;

    // check the cmd line options
    QCommandLineOption optverbose( { "v", "vebose" }, "Enable verbose output." );
    QCommandLineOption optlogfile( { "l", "logfile" }, "Create a log file in given directory <dir>.", "dir" );
    QCommandLineOption optsilent( { "s", "silent" }, "Start the application in minimized mode, used for auto-starting while user logon." );

    QCommandLineParser parser;
    parser.setApplicationDescription( M4E_APP_DESCRIPTION );
    parser.addHelpOption();
    parser.addOption( optverbose );
    parser.addOption( optlogfile );
    parser.addOption( optsilent );

    parser.process( *_p_app );

    _silentStart = parser.isSet( optsilent );

    if ( parser.isSet( optverbose ) )
    {
        loglevel = Log::L_VERBOSE;
    }
    if ( parser.isSet( optlogfile ) )
    {
        logfile = parser.value( "logfile" );
        if ( logfile.isEmpty() )
            logfile = QStandardPaths::writableLocation( QStandardPaths::AppDataLocation ) + QDir::separator() + M4E_APP_NAME ".log";
    }

    if ( !logfile.isEmpty() )
    {
        defaultlog.addSink( "file" , logfile.toStdString(), loglevel );
    }

#ifdef QT_DEBUG
    defaultlog.addSink( "qdebug" , QtLogStream, loglevel );
#else
    if ( parser.isSet( optverbose ) )
    {
        defaultlog.addSink( "qdebug" , QtLogStream, loglevel );
    }
#endif

    defaultlog.enableSeverityLevelPrinting( false );
    defaultlog.enableTimeStamp( false );
    log_info << "*******************************" << std::endl;
    log_info << "All Rights Reserved by A. Botorabi" << std::endl;
    log_info << M4E_APP_NAME << std::endl;
    log_info << "Version: " << M4E_APP_VERSION << std::endl;
    log_info << "Date: " << getFormatedDateAndTime() << std::endl;
    log_info << "*******************************" << std::endl;
    log_info << std::endl;
    defaultlog.enableSeverityLevelPrinting( true );
    defaultlog.enableTimeStamp( true );
    log_info << "Starting the app" << std::endl;

    _p_mainWindow = new m4e::gui::MainWindow();
}

void Core::start()
{
    assert( _p_mainWindow && "core was not initialized before" );
    // in debug build allow multiple instances of the app for debuggin purpose
#ifndef QT_DEBUG
    // check if an app instance is already running
    if ( !checkOrSetupSingleProc( _p_mainWindow ) )
    {
        log_info << "an application instance is already running, quitting..." << std::endl;
        return;
    }
#endif

    // if the app is started in silent mode then check if auto-start is enabled!
    QString autostart = settings::AppSettings::get()->readSettingsValue( M4E_SETTINGS_CAT_APP, M4E_SETTINGS_KEY_APP_AUTOSTART, "yes" );
    bool enableautostart = ( autostart == "yes" );
    if ( _silentStart && ! enableautostart )
    {
        log_info << "Auto-start is disabled, terminating the app" << std::endl;
        return;
    }

    if ( _silentStart || !gui::SystemTray::isTrayAvailable() )
    {
        _p_mainWindow->setWindowState( Qt::WindowMinimized );
    }
    else
    {
        _p_mainWindow->show();
    }

    _p_app->exec();
}

void Core::shutdown()
{
    if ( _p_mainWindow )
    {
        delete _p_mainWindow;
        settings::AppSettings::get()->shutdown();
    }
    _p_mainWindow = nullptr;

    if ( _p_app )
        delete _p_app;
    _p_app = nullptr;

    if ( _p_singleProc )
        delete _p_singleProc;
    _p_singleProc = nullptr;
}

bool Core::checkOrSetupSingleProc( QObject* p_notifyObject )
{
    _p_singleProc = new SingleProc( M4E_APP_INSTANCE_KEY, p_notifyObject );
    if ( _p_singleProc->isAnotherRunning() )
    {
        // notify the running instance to bring its window to front
        _p_singleProc->notifyRunningInstance();
        return false;
    }
    else
    {
        if ( !_p_singleProc->tryToRun() )
            return false;
    }

    return true;
}

} // namespace core
} // namespace m4e
