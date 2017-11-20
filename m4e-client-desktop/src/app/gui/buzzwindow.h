/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef BUZZWINDOW_H
#define BUZZWINDOW_H

#include <configuration.h>
#include <common/basedialog.h>
#include <event/modelevent.h>
#include <QMediaPlayer>
#include <QMainWindow>
#include <QMouseEvent>
#include <QTimer>


namespace Ui {
  class BuzzWindow;
}

namespace m4e
{
namespace gui
{

class MainWindow;

/**
 * @brief A window for buzzing the user.
 *
 * @author boto
 * @date Nov 20, 2017
 */
class BuzzWindow : public QMainWindow
{
    Q_OBJECT

    public:

        /**
         * @brief Create a dialog instance.
         *
         * @param p_parent  Parent widget
         */
        explicit                    BuzzWindow( MainWindow* p_parent );

        /**
         * @brief Destroy the instance.
         */
        virtual                     ~BuzzWindow();

        /**
         * @brief Setup the window.
         *
         * @param source    The source of the buzz
         * @param title     Buzz title
         * @param text      Buzz text
         */
        void                        setupUI( const QString& source, const QString& title, const QString& text );

    protected slots:

        void                        onBtnDiscardClicked();

        void                        onTimer();

    protected:

        void                        mousePressEvent( QMouseEvent* p_event );

        void                        mouseReleaseEvent( QMouseEvent* p_event );

        void                        mouseMoveEvent( QMouseEvent* p_event );

        void                        startAnimation();

        MainWindow*                 _p_mainWindow = nullptr;

        Ui::BuzzWindow*             _p_ui         = nullptr;

        QTimer*                     _p_timer      = nullptr;

        QMediaPlayer*               _p_player     = nullptr;

        bool                        _dragging     = false;

        QPoint                      _draggingPos;
};

} // namespace gui
} // namespace m4e

#endif // BUZZWINDOW_H
