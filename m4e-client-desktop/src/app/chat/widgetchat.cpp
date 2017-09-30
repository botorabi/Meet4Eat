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

void WidgetChat::setWebApp(webapp::WebApp *p_webApp)
{
     _p_webApp = p_webApp;
}

void WidgetChat::setChannel( const QString& channel )
{
    _channel = channel;
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
    appendChatText( _p_ui->lineEditChat->text() );
    _p_ui->lineEditChat->setText( "" );
}

void WidgetChat::setupUI()
{
    _p_ui = new Ui::WidgetChat;
    _p_ui->setupUi( this );
}

void WidgetChat::appendChatText( const QString& text )
{
    if ( text.isEmpty() )
        return;

    QString ts = "[" + QString::fromStdString( core::getFormatedDateAndTime() ) + "]";
    QString sender;
    if ( _p_webApp )
        sender = " " + _p_webApp->getUser()->getUserData()->getName();

    QString output = "<p>";
    output += "<span style='color: gray;'>" + ts + "</span>";
    output += " <span style='color: brown;'>" + sender + "</span>";
    output += "  <strong style='color: black;'>" + text + "</strong>";
    output += "</p>";
    _p_ui->textOutput->appendHtml( output );
}

} // namespace chat
} // namespace m4e
