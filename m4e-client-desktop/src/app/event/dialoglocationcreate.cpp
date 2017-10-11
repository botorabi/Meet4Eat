/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "dialoglocationcreate.h"
#include <common/guiutils.h>
#include <common/dialogmessage.h>
#include <ui_widgetlocationcreate.h>


namespace m4e
{
namespace event
{

DialogLocationCreate::DialogLocationCreate( webapp::WebApp* p_webApp, QWidget* p_parent ) :
 common::BaseDialog( p_parent ),
 _p_webApp( p_webApp )
{
    _p_ui = new Ui::WidgetLocationCreate();
    connect( _p_webApp->getEvents(), SIGNAL( onResponseAddLocation( bool, QString, QString ) ), this, SLOT( onResponseAddLocation( bool, QString, QString ) ) );
}

DialogLocationCreate::~DialogLocationCreate()
{
    delete _p_ui;
}

void DialogLocationCreate::setupUI( event::ModelEventPtr event )
{
    _location = new ModelLocation();
    _event = event;

    decorate( *_p_ui );

    setTitle( QApplication::translate( "DialogLocationCreate", "Create Location" ) );
    QString okbtn( QApplication::translate( "DialogLocationCreate", "Ok" ) );
    QString cancelbtn( QApplication::translate( "DialogLocationCreate", "Cancel" ) );
    setupButtons( &okbtn, &cancelbtn, nullptr );
    setResizable( true );

    _p_ui->textEditDescription->setPlainText( _location->getDescription() );
    _defaultPhoto = *_p_ui->labelPhoto->pixmap();
}

void DialogLocationCreate::onResponseAddLocation( bool success, QString /*eventId*/, QString /*locationId*/ )
{
    if ( success )
    {
        common::DialogMessage msg( this );
        msg.setupUI( QApplication::translate( "DialogLocationCreate", "Add Location" ),
                     QApplication::translate( "DialogLocationCreate", "New location was successfully created. Need to create a new location?" ),
                     common::DialogMessage::BtnYes | common::DialogMessage::BtnNo );

        if ( msg.exec() == common::DialogMessage::BtnNo )
        {
            done( common::DialogMessage::BtnOk );
        }
        else
        {
            resetDialog();
        }
    }
    else
    {
        const QString& reason = _p_webApp->getEvents()->getLastError();
        common::DialogMessage msg( this );
        msg.setupUI( QApplication::translate( "DialogLocationCreate", "Add Location" ),
                     QApplication::translate( "DialogLocationCreate", "Failed to create a new location.\nReason: " ) + reason,
                     common::DialogMessage::BtnOk );

        msg.exec();
    }
}

bool DialogLocationCreate::onButton1Clicked()
{
    _location->setName( _p_ui->lineEditName->text() );
    _location->setDescription( _p_ui->textEditDescription->toPlainText() );

    //! TODO photo

    // try to create the event location
    Events* p_events = _p_webApp->getEvents();
    p_events->requestAddLocation( _event->getId(), _location );

    return false;
}

void DialogLocationCreate::resetDialog()
{
    _p_ui->lineEditName->setText( "" );
    _p_ui->textEditDescription->setPlainText( "" );
    _p_ui->labelPhoto->setPixmap( _defaultPhoto );
}

} // namespace event
} // namespace m4e
