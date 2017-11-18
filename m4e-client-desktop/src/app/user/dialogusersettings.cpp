/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "dialogusersettings.h"
#include <core/log.h>
#include <common/dialogmessage.h>
#include <common/guiutils.h>
#include <ui_widgetusersettings.h>


namespace m4e
{
namespace user
{

DialogUserSettings::DialogUserSettings( webapp::WebApp* p_webApp, QWidget* p_parent ) :
 common::BaseDialog( p_parent ),
 _p_webApp( p_webApp )
{
    _p_ui = new Ui::WidgetUserSettings();
    decorate( *_p_ui );

    setTitle( QApplication::translate( "DialogUserSettings", "User Settings" ) );
    QString addbtn( QApplication::translate( "DialogUserSettings", "Apply" ) );
    QString cancelbtn( QApplication::translate( "DialogUserSettings", "Cancel" ) );
    setupButtons( &addbtn, &cancelbtn, nullptr );
    setResizable( true );
}

DialogUserSettings::~DialogUserSettings()
{
    delete _p_ui;
}

void DialogUserSettings::setupUI( ModelUserPtr user )
{
    _user = user;
    if ( !_user.valid() )
    {
        log_error << TAG << "cannot setup user settings dialog, invalid user" << std::endl;
        return;
    }

    connect( _p_ui->pushButtonPhoto, SIGNAL( clicked() ), this, SLOT( onBtnPhotoClicked() ) );
    connect( _p_webApp, SIGNAL( onDocumentReady( m4e::doc::ModelDocumentPtr ) ), this, SLOT( onDocumentReady( m4e::doc::ModelDocumentPtr ) ) );

    QString photoid = _user->getPhotoId();
    if ( !photoid.isEmpty() && ( photoid != "0" ) )
    {
        _p_webApp->requestDocument( photoid, _user->getPhotoETag() );
    }
    else
    {
        _p_ui->pushButtonPhoto->setIcon( common::GuiUtils::createRoundIcon( common::GuiUtils::getDefaultPixmap() ) );
    }

    _p_ui->lineEditLogin->setText( _user->getLogin() );
    _p_ui->lineEditName->setText( _user->getName() );
    _p_ui->lineEditEmail->setText( _user->getEmail() );

}

bool DialogUserSettings::onButton1Clicked()
{
    log_verbose << TAG << "TODO apply changes" << std::endl;

    common::DialogMessage msg( this );
    msg.setupUI( QApplication::translate( "DialogUserSettings", "User Settings" ),
                 QApplication::translate( "DialogUserSettings", "Applying changes is under construction!" ),
                 common::DialogMessage::BtnOk );
    msg.exec();

    return false;
}

void DialogUserSettings::onBtnPhotoClicked()
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
        msg.setupUI( QApplication::translate( "DialogUserSettings", "Set Image" ),
                     QApplication::translate( "DialogUserSettings", "Cannot update the image. The file format is not supported!" ),
                     common::DialogMessage::BtnOk );
        msg.exec();
        return;
    }

    m4e::doc::ModelDocumentPtr doc = new m4e::doc::ModelDocument();
    doc->setContent( imagecontent, "image", format );
    _user->setUpdatedPhoto( doc );
    //_p_ui->pushButtonPhoto->setIcon( image );
    _p_ui->pushButtonPhoto->setIcon( common::GuiUtils::createRoundIcon( doc ) );
}

void DialogUserSettings::onDocumentReady( m4e::doc::ModelDocumentPtr document )
{
    if ( !document.valid() )
        return;

    QString photoid = _user->getPhotoId();
    if ( !photoid.isEmpty() && ( document->getId() == photoid ) )
    {
        _p_ui->pushButtonPhoto->setIcon( common::GuiUtils::createRoundIcon( document ) );
    }
}

} // namespace user
} // namespace m4e
