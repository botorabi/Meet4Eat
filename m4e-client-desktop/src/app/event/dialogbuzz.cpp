/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "dialogbuzz.h"
#include <common/guiutils.h>
#include <common/dialogmessage.h>
#include <ui_widgetbuzz.h>


namespace m4e
{
namespace event
{

DialogBuzz::DialogBuzz( webapp::WebApp* p_webApp, QWidget* p_parent ) :
 common::BaseDialog( p_parent ),
 _p_webApp( p_webApp )
{
    _p_ui = new Ui::WidgetBuzz();
}

DialogBuzz::~DialogBuzz()
{
    delete _p_ui;
}

void DialogBuzz::setupUI( event::ModelEventPtr event )
{
    _event = event;

    decorate( *_p_ui );

    setTitle( QApplication::translate( "DialogBuzz", "Send Buzz to Event Members" ) );
    QString okbtn( QApplication::translate( "DialogBuzz", "Send" ) );
    QString cancelbtn( QApplication::translate( "DialogBuzz", "Cancel" ) );
    setupButtons( &okbtn, &cancelbtn, nullptr );
    setResizable( true );
}

bool DialogBuzz::onButton1Clicked()
{
    bool res = _p_webApp->getNotifications()->sendEventMessage( _event->getId(), _p_ui->lineTitle->text(), _p_ui->textEditDescription->toPlainText() );
    if ( !res )
    {
        common::DialogMessage msg( this );
        msg.setupUI( QApplication::translate( "DialogBuzz", "Problem Sending Message" ),
                     QApplication::translate( "DialogBuzz", "Could not send out the message!" ),
                     common::DialogMessage::BtnOk );
        msg.exec();
        return false;
    }
    return true;
}

} // namespace event
} // namespace m4e
