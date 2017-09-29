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
#include <event/events.h>
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


    protected slots:

        /**
         * @brief Initial webapp setup happens in this slot.
         */
        void                        onTimerInit();

        void                        onBtnCloseClicked();

        void                        onBtnMinimizeClicked();

        void                        onBtnMaximizeClicked();

        void                        onBtnEventsClicked();

        void                        onBtnSettingsClicked();

        void                        onBtnAboutClicked();

        void                        onEventSelection( QString id );

        void                        onWidgetEventBack();

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
        void                        onUserSignedOff( bool success );

        /**
         * @brief This signal is received when user events were arrived.
         *
         * @param success  true for successful access
         * @param events   User events
         */
        void                        onResponseGetEvents( bool success, QList< m4e::event::ModelEventPtr > events );

    protected:

        void                        closeEvent( QCloseEvent* p_event );

        void                        storeWindowGeometry();

        void                        restoreWindowGeometry();

        void                        mouseDoubleClickEvent( QMouseEvent* p_event );

        void                        mousePressEvent( QMouseEvent* p_event );

        void                        mouseReleaseEvent( QMouseEvent* p_event );

        void                        mouseMoveEvent( QMouseEvent* p_event );

        void                        clearClientWidget();

        void                        createWidgetMyEvents();

        void                        clearMyEventsWidget();

        void                        createWidgetEvent( const QString& groupId );

        Ui::MainWindow*             _p_ui           = nullptr;

        QTimer*                     _p_initTimer    = nullptr;

        webapp::WebApp*             _p_webApp       = nullptr;

        bool                        _dragging       = false;

        QPoint                      _draggingPos;
};

} // namespace gui
} // namespace m4e

#endif // MAINWINDOW_H
