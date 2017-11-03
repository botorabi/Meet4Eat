/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef WIDGETMAILEDIT_H
#define WIDGETMAILEDIT_H

#include <configuration.h>
#include <webapp/webapp.h>
#include <mailbox/modelmail.h>
#include <QWidget>
#include <QLabel>


namespace Ui {
    class WidgetMailEdit;
}

namespace m4e
{
namespace mailbox
{

/**
 * @brief Widget class for viewing or composing and sending a mail
 *
 * @author boto
 * @date Nov 2, 2017
 */
class WidgetMailEdit : public QWidget
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(WidgetMailEdit) ";

    Q_OBJECT

    public:

        /**
         * @brief Create a mail edit widget
         *
         * @param p_webApp  Web application interface
         * @param p_parent  Parent widget
         */
        explicit                    WidgetMailEdit( webapp::WebApp* p_webApp, QWidget* p_parent = nullptr );

        /**
         * @brief Destroy the instance.
         */
        virtual                     ~WidgetMailEdit();

        /**
         * @brief Setup the widget.
         *
         * @param mail      The mail
         * @param readOnly  Pass true in order to view only the mail, pass false in order to enable composing and sending.
         */
        void                        setupUI( mailbox::ModelMailPtr mail, bool readOnly );

        /**
         * @brief Get the mail ID.
         *
         * @return ID
         */
        const QString&              getId() const { return _mail->getId(); }

    signals:

        /**
         * @brief Emitted when the was sent out.
         *
         * @param id   The mail ID
         */
        void                        onMailSent( QString mailId);

    protected slots:

        /**
         * @brief Called when the user clicks on send button.
         */
        void                        onBtnSendClicked();

        /**
         * @brief Address book button was clicked.
         */
        void                        onBtnAddrBookClicked();

        /**
         * @brief Results of mail sending
         *
         * @param success   true if the mail was successfully sent, otherwise false
         */
        void                        onResponseSendMail( bool success );

    protected:

        webapp::WebApp*             _p_webApp = nullptr;

        Ui::WidgetMailEdit*         _p_ui     = nullptr;

        mailbox::ModelMailPtr       _mail;
};

} // namespace mailbox
} // namespace m4e

#endif // WIDGETMAILEDIT_H
