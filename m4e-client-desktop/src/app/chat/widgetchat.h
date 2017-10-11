/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef WIDGETCHAT_H
#define WIDGETCHAT_H

#include <webapp/webapp.h>
#include <chat/chatmessage.h>
#include <document/modeldocument.h>
#include <QWidget>
#include <QLabel>


namespace Ui
{
    class WidgetChat;
}


namespace m4e
{
namespace chat
{

/**
 * @brief A widget class providing a chat gui.
 *
 * @author boto
 * @date Sep 30, 2017
 */
class WidgetChat : public QWidget
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(WidgetChat) ";

    Q_OBJECT

    public:

        /**
         * @brief Create a new "Chat" widget
         *
         * @param p_parent          Parent widget
         */
                                    WidgetChat( QWidget* p_parent = nullptr );
        /**
         * @brief Setup the widget.
         *
         * @param p_webApp          Web application interface
         */
        void                        setupUI( webapp::WebApp* p_webApp );

        /**
         * @brief Destroy WidgetEvent
         */
        virtual                     ~WidgetChat();

        /**
         * @brief Set the event members.
         *
         * @param users Event members
         */
        void                        setMembers( const QList< user::ModelUserInfoPtr > users );

        /**
         * @brief Append a new chat message.
         *
         * @param msg Chat message
         */
        void                        appendChatText( m4e::chat::ChatMessagePtr msg );

    protected slots:

        void                        onBtnEmotieClicked();

        void                        onEditLineReturnPressed();

        void                        onBtnSendClicked();

        void                        onBtnCollapseClicked();

        void                        onDocumentReady( m4e::doc::ModelDocumentPtr document );

    signals:

        /**
         * @brief This signal is emitted when the user posted a new message.
         *
         * @param msg  Chat message to send.
         */
        void                        onSendMessage( m4e::chat::ChatMessagePtr msg );

    protected:

        webapp::WebApp*             _p_webApp = nullptr;

        Ui::WidgetChat*             _p_ui = nullptr;

        QString                     _senderName;

        QMap< QString, int >        _memberPhotos;
};

} // namespace chat
} // namespace m4e

#endif // WIDGETCHAT_H
