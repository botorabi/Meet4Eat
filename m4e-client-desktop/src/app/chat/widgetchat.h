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
         * @param p_webApp          Web application interface
         * @param p_parent          Parent widget
         */
                                    WidgetChat( QWidget* p_parent = nullptr );

        /**
         * @brief Destroy WidgetEvent
         */
        virtual                     ~WidgetChat();

        /**
         * @brief Set the web app handler.
         *
         * @param p_webApp WebApp instance
         */
        void                        setWebApp( webapp::WebApp* p_webApp );

        /**
         * @brief Set the chat channel which is represented by this widget.
         *
         * @param channel   Chat channel
         */
        void                        setChannel( const QString& channel );

    protected slots:

        void                        onBtnEmotieClicked();

        void                        onEditLineReturnPressed();

        void                        onBtnSendClicked();

    signals:


    protected:

        /**
         * @brief Setup the widget.
         */
        void                        setupUI();

        void                        appendChatText( const QString& text );

        Ui::WidgetChat*             _p_ui     = nullptr;

        webapp::WebApp*             _p_webApp = nullptr;

        QString                     _channel;
};

} // namespace chat
} // namespace m4e

#endif // WIDGETCHAT_H
