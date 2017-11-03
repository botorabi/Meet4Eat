/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */


#include "singleproc.h"
#include "log.h"
#include <QApplication>
#include <QCryptographicHash>
#include <QStandardPaths>
#include <QDirIterator>
#include <QUdpSocket>
#include <QDir>
#include <thread>


namespace m4e
{
namespace core
{

SingleProc::SingleProc( const QString& key, QObject* p_notifyObject ) :
 _p_server( nullptr ),
 _p_notifyObject( p_notifyObject ),
 _key( key ),
 _sharedMemKey( generateKeyHash( key, "_sharedMemKey" ) ),
 _semaSharedMemKey( generateKeyHash( key, "_semaSharedMemKey" ) ),
 _semaNotifyKey( generateKeyHash( key, "_semaNotifyKey" + QDir::homePath() ) ),
 _sharedMem( _sharedMemKey ),
 _semaSharedMem( _semaSharedMemKey, 1 ),
 _semaNotify( _semaNotifyKey, 0 ),
 _p_thread( nullptr ),
 _threadTerminate( false )
{
    _semaSharedMem.acquire();
    {
        QSharedMemory fix( _sharedMemKey );    // Fix for *nix: http://habrahabr.ru/post/173281/
        fix.attach();
    }
    _semaSharedMem.release();
}

SingleProc::~SingleProc()
{
    release();

    if ( _p_thread )
    {
        _p_notifyObject = nullptr;
        _threadTerminate = true;
        _semaNotify.release();
        _p_thread->join();
        delete _p_thread;
    }
}

bool SingleProc::isAnotherRunning()
{
    if ( _sharedMem.isAttached() )
        return false;

    _semaSharedMem.acquire();
    const bool isRunning = _sharedMem.attach();
    if ( isRunning )
        _sharedMem.detach();
    _semaSharedMem.release();

    return isRunning;
}

void SingleProc::notifyRunningInstance()
{
    // just release the notify semaphore, the running instance will handle it in 'notificationHandler()' below.
    _semaNotify.release();
}

bool SingleProc::tryToRun()
{
    if ( isAnotherRunning() )
        return false;

    _semaSharedMem.acquire();
    const bool result = _sharedMem.create( sizeof( quint64 ) );
    _semaSharedMem.release();
    if ( !result )
    {
        release();
        return false;
    }

    // start the notification handler thread
    _p_thread = new std::thread( [ this ] { notificationHandler(); } );

    return true;
}

void SingleProc::release()
{
    _semaSharedMem.acquire();
    if ( _sharedMem.isAttached() )
        _sharedMem.detach();
    _semaSharedMem.release();
}

QString SingleProc::generateKeyHash( const QString& key, const QString& salt )
{
    QByteArray data;
    data.append( key.toUtf8() );
    data.append( salt.toUtf8() );
    data = QCryptographicHash::hash( data, QCryptographicHash::Sha1 ).toHex();
    return data;
}

void SingleProc::notificationHandler()
{
    //! NOTE: this method runs in an own thread.
    while ( !_threadTerminate )
    {
        _semaNotify.acquire();
        if ( _p_notifyObject )
        {
            QEvent* p_event = new QEvent( static_cast< QEvent::Type >( M4E_APP_INSTANCE_EVENT_TYPE ) );
            //! NOTE: this function is thread-safe, no need for thread synchronization
            QApplication::postEvent( _p_notifyObject, p_event );
        }
    }
}

} // namespace core
} // namespace m4e
