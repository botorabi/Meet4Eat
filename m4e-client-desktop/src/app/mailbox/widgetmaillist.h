/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */


#ifndef WIDGETMAILLIST_H
#define WIDGETMAILLIST_H

#include <configuration.h>
#include <webapp/webapp.h>
#include <mailbox/modelmail.h>
#include <QListWidget>
#include <QMap>


namespace m4e
{
namespace mailbox
{

class WidgetMailItem;


/**
 * @brief Class representing a mail list.
 *
 * @author boto
 * @date Nov 1, 2017
 */
class WidgetMailList : public QListWidget
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(WidgetMailList) ";

    Q_OBJECT

    public:

        /**
         * @brief Create a new mail list widget
         *
         * @param p_webApp  Web application interface
         * @param p_parent  Parent widget
         */
                                    WidgetMailList( webapp::WebApp* p_webApp, QWidget* p_parent = nullptr );

        /**
         * @brief Visually select the mail widget with given mail ID.
         *
         * @param mailId  Mail ID
         */
        void                        selectMail( const QString& mailId );

        /**
         * @brief Visually select the first mail in list.
         */
        void                        selectFirstMail();

    signals:

        void                        onMailSelection( QString id );

    protected slots:

        /**
         * @brief Emitted when the user clicks on the widget.
         *
         * @param id   The event ID
         */
        void                        onClicked( QString id );

        /**
         * @brief Received from mail item widget when the user clicks on delete button.
         *
         * @param id    ID of the mail which should be deleted
         */
        void                        onRequestDeleteMail( QString id );

        /**
         * @brief Results of user's mails request.
         *
         * @param success  true if user data could successfully be retrieved, otherwise false
         * @param mails    User's mails
         */
        void                        onResponseMails( bool success, QList< m4e::mailbox::ModelMailPtr > mails );

        /**
         * @brief Results of request for performing a mail operation
         *
         * @param success  true if operation could successfully be performed, otherwise false
         * @param mailId   ID of the deleted mail
         */
        void                        onResponsePerformOperation( bool success, QString mailId, QString operation );

    protected:

        /**
         * @brief Setup the widget.
         */
        void                        setupUI();

        void                        setupListView();

        WidgetMailItem*             findWidget( const QString& mailId );

        /**
         * @brief Add a new mail to widget
         *
         * @param mail       The mail
         */
        void                        addMail( m4e::mailbox::ModelMailPtr mail );

        webapp::WebApp*             _p_webApp     = nullptr;

        QList< WidgetMailItem* >    _widgets;
};

} // namespace mailbox
} // namespace m4e

#endif // WIDGETMAILLIST_H
