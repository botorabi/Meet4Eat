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
#include <settings/appsettings.h>
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
    connect( _p_webApp->getUser(), SIGNAL( onResponseUpdateUserData( bool, QString ) ), this, SLOT( onResponseUpdateUserData( bool, QString ) ) );

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
    QString problem;
    if ( !validateInput( problem ) )
    {
        common::DialogMessage msg( this );
        msg.setupUI( QApplication::translate( "DialogUserSettings", "User Settings" ),
                     problem,
                     common::DialogMessage::BtnOk );
        msg.exec();
        return false;
    }

    QString name = _p_ui->lineEditName->text();
    QString pw   = _p_ui->lineEditPw->text();
    if ( pw.length() > 0 )
        pw = webapp::RESTAuthentication::createHash( pw );

    // check for changes
    QString storedpasswd = settings::AppSettings::get()->readSettingsValue( M4E_SETTINGS_CAT_USER, M4E_SETTINGS_KEY_USER_PW, "" );
    if ( storedpasswd == pw )
        pw = "";
    if ( name == _user->getName() )
        name = "";

    // there is no need for an update request, nothing was changed
    if ( name.isEmpty() && pw.isEmpty() && !_updatedPhoto.valid() )
        return true;

    // start the update request
    _p_webApp->getUser()->requestUpdateUserData( name, pw, _updatedPhoto );

    return false;
}

bool DialogUserSettings::validateInput( QString& problem )
{
    QString name   = _p_ui->lineEditName->text();
    QString pwcurr = _p_ui->lineEditPwCurrent->text();
    QString pw     = _p_ui->lineEditPw->text();
    QString pwrep  = _p_ui->lineEditPwRepeat->text();

    if ( name.isEmpty() || name.length() < 6 )
    {
        problem = QApplication::translate( "DialogUserSettings", "User name must have at least 6 characters" );
        return false;
    }
    if ( ( pwcurr.length() > 0 ) || ( pw.length() > 0 ) || ( pwrep.length() > 0 ) )
    {
        pwcurr = webapp::RESTAuthentication::createHash( pwcurr );
        QString storedpw = settings::AppSettings::get()->readSettingsValue( M4E_SETTINGS_CAT_USER, M4E_SETTINGS_KEY_USER_PW, "" );
        if ( pwcurr != storedpw )
        {
            problem = QApplication::translate( "DialogUserSettings", "Cannot change your password, wrong current password!" );
            return false;
        }
        if ( pw != pwrep )
        {
            problem = QApplication::translate( "DialogUserSettings", "Cannot change your password! New password and its repetition do not match." );
            return false;
        }
        if ( pw.length() < 8 )
        {
            problem = QApplication::translate( "DialogUserSettings", "Cannot change your password! The password must have at least 8 characters." );
            return false;
        }
    }

    return true;
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
    _updatedPhoto = doc;
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

void DialogUserSettings::onResponseUpdateUserData( bool success, QString userId )
{
    // just to be on the safe side
    if ( userId != _user->getId() )
        return;

    if ( success )
    {
        // store the new password if it was changed
        QString pw = _p_ui->lineEditPw->text();
        if ( pw.length() > 0 )
        {
            pw = webapp::RESTAuthentication::createHash( pw );
            settings::AppSettings::get()->writeSettingsValue( M4E_SETTINGS_CAT_USER, M4E_SETTINGS_KEY_USER_PW, pw );
        }

        common::DialogMessage msg( this );
        msg.setupUI( QApplication::translate( "DialogUserSettings", "User Data Update" ),
                     QApplication::translate( "DialogUserSettings", "Your settings were successfully updated." ),
                     common::DialogMessage::BtnOk );
        msg.exec();

        done( DialogUserSettings::BtnApply );
    }
    else
    {
        common::DialogMessage msg( this );
        msg.setupUI( QApplication::translate( "DialogUserSettings", "User Data Update" ),
                     QApplication::translate( "DialogUserSettings", "Your settings could not be updated!\nResons: " ) + _p_webApp->getUser()->getLastError(),
                     common::DialogMessage::BtnOk );
        msg.exec();
    }
}

} // namespace user
} // namespace m4e
