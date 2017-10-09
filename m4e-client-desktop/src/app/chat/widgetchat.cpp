/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include "widgetchat.h"
#include <core/log.h>
#include <core/utils.h>
#include <ui_widgetchat.h>


namespace m4e
{
namespace chat
{

WidgetChat::WidgetChat( QWidget* p_parent ) :
 QWidget( p_parent )
{
    setupUI();
}

WidgetChat::~WidgetChat()
{
    if ( _p_ui )
        delete _p_ui;
}

void WidgetChat::appendChatText( ChatMessagePtr msg )
{
    if ( msg->getText().isEmpty() && !msg->getDocument().valid() )
        return;

    const QDateTime& timestamp = msg->getTime();
    QString ts = "[" + timestamp.toString( Qt::ISODate ) + "]";
    QString output = "<p>";
    output += "<span style='color: gray;'>" + ts + "</span>";
    output += " <span style='color: brown;'>" + msg->getSender() + "</span>";
    output += "  <strong style='color: black;'>" + msg->getText() + "</strong>";
    output += "</p>";
    _p_ui->textOutput->appendHtml( output );
}

void WidgetChat::onBtnEmotieClicked()
{
    log_debug << TAG << "TODO onBtnEmotieClicked" << std::endl;
}

void WidgetChat::onEditLineReturnPressed()
{
    onBtnSendClicked();
}

void WidgetChat::onBtnSendClicked()
{
    ChatMessagePtr msg = new ChatMessage();
    // NOTE the remaing data such as recipient and sender will be added by signal receiver
    msg->setText( _p_ui->lineEditChat->text() );
    emit onSendMessage( msg );
    _p_ui->lineEditChat->setText( "" );
}

void WidgetChat::setupUI()
{
    _p_ui = new Ui::WidgetChat;
    _p_ui->setupUi( this );
}

} // namespace chat
} // namespace m4e
