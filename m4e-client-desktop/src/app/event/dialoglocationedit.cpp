/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "dialoglocationedit.h"
#include <common/guiutils.h>
#include <common/dialogmessage.h>
#include <ui_widgetlocationedit.h>


namespace m4e
{
namespace event
{

DialogLocationEdit::DialogLocationEdit( webapp::WebApp* p_webApp, QWidget* p_parent ) :
 common::BaseDialog( p_parent ),
 _p_webApp( p_webApp )
{
    _p_ui = new Ui::WidgetLocationEdit();
    connect( _p_webApp->getEvents(), SIGNAL( onResponseAddLocation( bool, QString, QString ) ), this, SLOT( onResponseAddLocation( bool, QString, QString ) ) );
    connect( _p_webApp->getEvents(), SIGNAL( onResponseUpdateLocation( bool, QString, QString ) ), this, SLOT( onResponseUpdateLocation( bool, QString, QString ) ) );
}

DialogLocationEdit::~DialogLocationEdit()
{
    delete _p_ui;
}

void DialogLocationEdit::setupUINewLocation( event::ModelEventPtr event )
{
    event::ModelLocationPtr location = new ModelLocation();
    setupUIEditLocation( event, location );
}

void DialogLocationEdit::setupUIEditLocation( event::ModelEventPtr event, ModelLocationPtr location )
{
    _event = event;
    _location = location;

    decorate( *_p_ui );

    setTitle( QApplication::translate( "DialogLocationEdit", "Edit Location" ) );
    QString okbtn( QApplication::translate( "DialogLocationEdit", "Ok" ) );
    QString cancelbtn( QApplication::translate( "DialogLocationEdit", "Cancel" ) );
    setupButtons( &okbtn, &cancelbtn, nullptr );
    setResizable( true );

    connect( _p_ui->pushButtonPhoto, SIGNAL( clicked() ), this, SLOT( onBtnPhotoClicked() ) );
    connect( _p_webApp, SIGNAL( onDocumentReady( m4e::doc::ModelDocumentPtr ) ), this, SLOT( onDocumentReady( m4e::doc::ModelDocumentPtr ) ) );

    _p_ui->lineEditName->setText( _location->getName() );
    _p_ui->textEditDescription->setPlainText( _location->getDescription() );
    _defaultPhoto = _p_ui->pushButtonPhoto->icon();

    QString photoid = _location->getPhotoId();
    if ( !photoid.isEmpty() && ( photoid != "0" ) )
    {
        _p_webApp->requestDocument( photoid, _location->getPhotoETag() );
    }
    else
    {
        _p_ui->pushButtonPhoto->setIcon( common::GuiUtils::createRoundIcon( common::GuiUtils::getDefaultPixmap() ) );
    }
}

void DialogLocationEdit::onBtnPhotoClicked()
{
    QString     dir;
    QString     format;
    QPixmap     image;
    QByteArray  imagecontent;
    bool        aborted;
    bool res = common::GuiUtils::createImageFromFile( this, dir, image, imagecontent, format, aborted );

    if ( aborted )
        return;

    if ( !res )
    {
        common::DialogMessage msg( this );
        msg.setupUI( QApplication::translate( "DialogLocationEdit", "Set Image" ),
                     QApplication::translate( "DialogLocationEdit", "Cannot update the image. The file format is not supported!" ),
                     common::DialogMessage::BtnOk );
        msg.exec();
        return;
    }

    m4e::doc::ModelDocumentPtr doc = new m4e::doc::ModelDocument();
    doc->setContent( imagecontent, "image", format );
    _location->setUpdatedPhoto( doc );
    _p_ui->pushButtonPhoto->setIcon( common::GuiUtils::createRoundIcon( doc ) );
}

void DialogLocationEdit::onDocumentReady( m4e::doc::ModelDocumentPtr document )
{
    QString photoid = _location->getPhotoId();
    if ( !photoid.isEmpty() && document.valid() && ( document->getId() == photoid ) )
    {
        _p_ui->pushButtonPhoto->setIcon( common::GuiUtils::createRoundIcon( document ) );
    }
}

void DialogLocationEdit::onResponseAddLocation( bool success, QString /*eventId*/, QString /*locationId*/ )
{
    if ( success )
    {
        common::DialogMessage msg( this );
        msg.setupUI( QApplication::translate( "DialogLocationEdit", "Add Location" ),
                     QApplication::translate( "DialogLocationEdit", "New location was successfully created." ),
                     common::DialogMessage::BtnOk );

        msg.exec();
        done( common::DialogMessage::BtnOk );
    }
    else
    {
        const QString& reason = _p_webApp->getEvents()->getLastError();
        common::DialogMessage msg( this );
        msg.setupUI( QApplication::translate( "DialogLocationEdit", "Add Location" ),
                     QApplication::translate( "DialogLocationEdit", "Failed to create a new location.\nReason: " ) + reason,
                     common::DialogMessage::BtnOk );

        msg.exec();
    }
}

void DialogLocationEdit::onResponseUpdateLocation( bool success, QString /*eventId*/, QString /*locationId*/ )
{
    if ( success )
    {
        common::DialogMessage msg( this );
        msg.setupUI( QApplication::translate( "DialogLocationEdit", "Update Location" ),
                     QApplication::translate( "DialogLocationEdit", "Event location was successfully updated." ),
                     common::DialogMessage::BtnOk );

        msg.exec();
        done( common::DialogMessage::BtnOk );
    }
    else
    {
        const QString& reason = _p_webApp->getEvents()->getLastError();
        common::DialogMessage msg( this );
        msg.setupUI( QApplication::translate( "DialogLocationEdit", "Update Location" ),
                     QApplication::translate( "DialogLocationEdit", "Event location could not be updated.\nReason: " ) + reason,
                     common::DialogMessage::BtnOk );

        msg.exec();
    }

}

bool DialogLocationEdit::onButton1Clicked()
{
    _location->setName( _p_ui->lineEditName->text().trimmed() );
    _location->setDescription( _p_ui->textEditDescription->toPlainText().trimmed() );
    // create a new location or edit an existing one? a new location has no ID
    if ( _location->getId().isEmpty() )
    {
        // try to create the event location
        Events* p_events = _p_webApp->getEvents();
        p_events->requestAddLocation( _event->getId(), _location );
    }
    else
    {
        // try to update the event location
        Events* p_events = _p_webApp->getEvents();
        p_events->requestUpdateLocation( _event->getId(), _location );
    }

    return false;
}

void DialogLocationEdit::resetDialog()
{
    _p_ui->lineEditName->setText( "" );
    _p_ui->textEditDescription->setPlainText( "" );
    _p_ui->pushButtonPhoto->setIcon( _defaultPhoto );
}

} // namespace event
} // namespace m4e
