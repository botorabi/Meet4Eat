/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef MAILBOXWINDOW_H
#define MAILBOXWINDOW_H

#include <configuration.h>
#include <webapp/webapp.h>
#include <QMainWindow>
#include <QMouseEvent>

namespace Ui {
  class MailboxWindow;
}

namespace m4e
{
namespace gui
{

/**
 * @brief Window class for the mailbox
 *
 * @author boto
 * @date Nov 1, 2017
 */
class MailboxWindow : public QMainWindow
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(MailboxWindow) ";

    Q_OBJECT

    public:

        /**
         * @brief Create the mailbox window instance.
         */
                                    MailboxWindow( webapp::WebApp* p_webApp, QWidget* p_parent );

        /**
         * @brief Destroy the instance.
         */
        virtual                     ~MailboxWindow();

    signals:

        void                        onMailWindowClosed();

    protected slots:

        void                        onBtnCloseClicked();

        void                        onBtnMinimizeClicked();

        void                        onBtnMaximizeClicked();

        void                        onBtnNewMailClicked();

        /**
         * @brief Select the mail in the list.
         *
         * @param mailId  If empty then the first mail will be selected, if available.
         */
        void                        onMailSelection( QString mailId );

        /**
         * @brief Emitted when a mail was successfully sent out.
         */
        void                        onMailSent();

    protected:

        void                        storeWindowGeometry();

        void                        restoreWindowGeometry();

        void                        mouseDoubleClickEvent( QMouseEvent* p_event );

        void                        mousePressEvent( QMouseEvent* p_event );

        void                        mouseReleaseEvent( QMouseEvent* p_event );

        void                        mouseMoveEvent( QMouseEvent* p_event );

        void                        keyPressEvent( QKeyEvent* p_event );

        void                        clearWidgetClientArea();

        void                        createWidgetMyMails();

        void                        clearWidgetMyMails();

        Ui::MailboxWindow*          _p_ui            = nullptr;

        webapp::WebApp*             _p_webApp        = nullptr;

        bool                        _dragging        = false;

        QPoint                      _draggingPos;
};

} // namespace gui
} // namespace m4e

#endif // MAILBOXWINDOW_H
