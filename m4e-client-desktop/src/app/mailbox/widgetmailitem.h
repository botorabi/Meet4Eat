/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef WIDGETMAILITEM_H
#define WIDGETMAILITEM_H

#include <configuration.h>
#include <webapp/webapp.h>
#include <mailbox/modelmail.h>
#include <QWidget>
#include <QLabel>


namespace Ui {
    class WidgetMailItem;
}

namespace m4e
{
namespace mailbox
{

/**
 * @brief Widget class for a mail item used in mail list widget
 *
 * @author boto
 * @date Nov 1, 2017
 */
class WidgetMailItem : public QWidget
{
    Q_OBJECT

    public:

        /**
         * @brief Create a new mail item widget
         *
         * @param p_webApp  Web application interface
         * @param p_parent  Parent widget
         */
        explicit                    WidgetMailItem( webapp::WebApp* p_webApp, QWidget* p_parent = nullptr );

        /**
         * @brief Destroy the instance.
         */
        virtual                     ~WidgetMailItem();

        /**
         * @brief Setup the widget.
         *
         * @param mail  The mail
         */
        void                        setupUI( mailbox::ModelMailPtr mail );

        /**
         * @brief Get the mail ID.
         *
         * @return ID
         */
        const QString&              getId() const { return _mail->getId(); }

        /**
         * @brief Set the box selection mode to normal or selected.
         *
         * @param normal    Pass true for normal, false for selection
         */
        void                        setSelectionMode( bool normal );

        /**
         * @brief Set the read/unread state. This call also modified the mail object.
         *
         * @param  unread  Unread flag
         */
        void                        setUnread( bool unread );

    signals:

        /**
         * @brief Emitted when the user clicks on the widget.
         *
         * @param id   The mail ID
         */
        void                        onClicked( QString id );

        /**
         * @brief Emited when the user clicks on delete button.
         *
         * @param id    ID of the mail which should be deleted
         */
        void                        onRequestDeleteMail( QString id );

        /**
         * @brief Emited when the user clicks on undelete button.
         *
         * @param id    ID of the mail which should be deleted
         */
        void                        onRequestUndeleteMail( QString id );

    protected slots:

        void                        onBtnDeleteClicked();

    protected:

        bool                        eventFilter( QObject* p_obj, QEvent* p_event );

        webapp::WebApp*             _p_webApp = nullptr;

        Ui::WidgetMailItem*         _p_ui     = nullptr;

        mailbox::ModelMailPtr       _mail;

        bool                        _selectionMode = false;
};

} // namespace mailbox
} // namespace m4e

#endif // WIDGETMAILITEM_H
