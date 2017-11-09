/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef SINGLEPROC_H
#define SINGLEPROC_H

#include <configuration.h>
#include <QObject>
#include <QSharedMemory>
#include <QSystemSemaphore>
#include <QUdpSocket>
#include <thread>

namespace m4e
{
namespace core
{

/**
 This class is used to prevent multiple instances of the application per user, i.e.
 one single instance of the application can be started for every user.
 The code is basing on Dmitry Sazonov's work 
 See http://stackoverflow.com/questions/5006547/qt-best-practice-for-a-single-instance-app-protection
*/
class SingleProc : public QObject
{
    Q_OBJECT 

    public:

        /**
         * Create an application instance guard with given key. Pass an object which will get an event on notification about another instance.
         */
                            SingleProc( const QString& key, QObject* p_notifyObject );

        /**
         * @brief Destroy the instance
         */
        virtual             ~SingleProc();

        /**
         * Is an instance of the application already running?
         */
        bool                isAnotherRunning();

        /**
         * Notify the running instance.
         */
        void                notifyRunningInstance();

        /**
         * Try to run a new application instance.
         */
        bool                tryToRun();

        /**
         * Release the application instance guard. Usually you don't need to call this, it is handled internally.
         */
        void                release();

    protected:

        QString             generateKeyHash( const QString& key, const QString& salt );

        void                notificationHandler();

        QUdpSocket*         _p_server;
        QObject*            _p_notifyObject;
        const QString       _key;
        const QString       _sharedMemKey;
        const QString       _semaSharedMemKey;
        const QString       _semaNotifyKey;
        QSharedMemory       _sharedMem;
        QSystemSemaphore    _semaSharedMem;
        QSystemSemaphore    _semaNotify;
        std::thread*        _p_thread;
        bool                _threadTerminate;

        Q_DISABLE_COPY( SingleProc )
};

} // namespace core
} // namespace m4e

#endif // SINGLEPROC_H
