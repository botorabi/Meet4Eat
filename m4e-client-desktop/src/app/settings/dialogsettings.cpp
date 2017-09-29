/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "dialogsettings.h"
#include <core/log.h>
#include <common/dialogmessage.h>
#include <ui_widgetsettings.h>
#include <settings/appsettings.h>


namespace m4e
{
namespace settings
{

DialogSettings::DialogSettings( webapp::WebApp* p_webApp, QWidget* p_parent ) :
 common::BaseDialog( p_parent ),
 _p_webApp( p_webApp )
{
    _p_ui = new Ui::WidgetSettings();
    setupUI();
}

DialogSettings::~DialogSettings()
{
    if ( _p_ui )
        delete _p_ui;
}

bool DialogSettings::onButton1Clicked()
{
    storeCredentials();
    return true;
}

void DialogSettings::storeCredentials()
{
    QString server   = _p_ui->lineEditServer->text();
    QString login    = _p_ui->lineEditLogin->text();
    QString passwd   = _p_ui->lineEditPassword->text();
    QString remember = _p_ui->checkBoxRememberPw->isChecked() ? "yes" : "no";

    settings::AppSettings::get()->writeSettingsValue( M4E_SETTINGS_CAT_SRV, M4E_SETTINGS_KEY_SRV_URL, server );
    settings::AppSettings::get()->writeSettingsValue( M4E_SETTINGS_CAT_USER, M4E_SETTINGS_KEY_USER_LOGIN, login );
    if ( !passwd.isEmpty() )
    {
        passwd = webapp::RESTAuthentication::createHash( passwd );
        settings::AppSettings::get()->writeSettingsValue( M4E_SETTINGS_CAT_USER, M4E_SETTINGS_KEY_USER_PW, passwd );
    }
    settings::AppSettings::get()->writeSettingsValue( M4E_SETTINGS_CAT_USER, M4E_SETTINGS_KEY_USER_PW_REM, remember );
}

void DialogSettings::setupUI()
{
    connect( _p_webApp, SIGNAL( onUserSignedIn( bool, QString ) ), this, SLOT( onUserSignedIn( bool, QString ) ) );

    decorate( *_p_ui );
    setTitle( QApplication::translate( "DialogSettings", "Settings" ) );
    QString btnok( QApplication::translate( "DialogSettings", "Ok" ) );
    QString btncancel( QApplication::translate( "DialogSettings", "Cancel" ) );
    setupButtons( &btnok, &btncancel, nullptr );
    setResizable( false );

    connect( _p_ui->pushButtonSignOut, SIGNAL( clicked() ), this, SLOT( onBtnSignOutClicked() ) );
    connect( _p_ui->pushButtonSignIn, SIGNAL( clicked() ), this, SLOT( onBtnSignInClicked() ) );

    QString server   = settings::AppSettings::get()->readSettingsValue( M4E_SETTINGS_CAT_SRV, M4E_SETTINGS_KEY_SRV_URL, "" );
    QString login    = settings::AppSettings::get()->readSettingsValue( M4E_SETTINGS_CAT_USER, M4E_SETTINGS_KEY_USER_LOGIN, "" );
    QString remember = settings::AppSettings::get()->readSettingsValue( M4E_SETTINGS_CAT_USER, M4E_SETTINGS_KEY_USER_PW_REM, "yes" );
    //QString passwd   = settings::AppSettings::get()->readSettingsValue( M4E_SETTINGS_CAT_USER, M4E_SETTINGS_KEY_USER_PW, "" );

    _p_ui->lineEditServer->setText( server );
    _p_ui->lineEditLogin->setText( login );
    _p_ui->lineEditPassword->setText( "" );
    _p_ui->checkBoxRememberPw->setChecked( remember == "yes" );

    bool connestablished = _p_webApp->getConnectionState() == webapp::WebApp::ConnEstablished;
    _p_ui->pushButtonSignOut->setEnabled( connestablished );
    _p_ui->pushButtonSignIn->setEnabled( !connestablished );
}

void DialogSettings::onBtnSignInClicked()
{
    if ( _p_webApp->getConnectionState() == webapp::WebApp::ConnEstablished )
        return;

    QString server   = _p_ui->lineEditServer->text();
    QString login    = _p_ui->lineEditLogin->text();
    QString passwd   = _p_ui->lineEditPassword->text();
    if ( passwd.isEmpty() )
    {
        passwd = settings::AppSettings::get()->readSettingsValue( M4E_SETTINGS_CAT_USER, M4E_SETTINGS_KEY_USER_PW, "" );
    }
    else
    {
        passwd = webapp::RESTAuthentication::createHash( passwd );
    }

    if ( !server.isEmpty() && !login.isEmpty() && !passwd.isEmpty() )
    {
        // the webapp gets the credentials from central settings, so store them before trying a connection
         storeCredentials();
        _p_webApp->establishConnection();
    }
    else
    {
        common::DialogMessage msg( this );
        msg.setupUI( QApplication::translate( "DialogSettings", "User Authentication" ),
                     QApplication::translate( "DialogSettings", "Please, first enter the account information." ),
                     common::DialogMessage::BtnOk );
        msg.exec();
    }
}

void DialogSettings::onBtnSignOutClicked()
{
    if ( _p_webApp->getConnectionState() != webapp::WebApp::ConnEstablished )
        return;

    _p_webApp->shutdownConnection();
    _p_ui->pushButtonSignIn->setEnabled( true );
    _p_ui->pushButtonSignOut->setEnabled( false );
}

void DialogSettings::onUserSignedIn( bool success, QString /*userId*/ )
{
    _p_ui->pushButtonSignIn->setEnabled( !success );
    _p_ui->pushButtonSignOut->setEnabled( success );

    if ( success )
    {
        log_debug << "successfully signed in user" << std::endl;
        common::DialogMessage msg( this );
        msg.setupUI( QApplication::translate( "DialogSettings", "User Authentication" ),
                     QApplication::translate( "DialogSettings", "You were successfully signed in." ),
                     common::DialogMessage::BtnOk );
        msg.exec();
    }
    else
    {
        log_debug << "failed to sign in user" << std::endl;
        common::DialogMessage msg( this );
        msg.setupUI( QApplication::translate( "DialogSettings", "User Authentication" ),
                     QApplication::translate( "DialogSettings", "Could not sign in. Check your credentials and try again." ),
                     common::DialogMessage::BtnOk );
        msg.exec();
    }
}

} // namespace settings
} // namespace m4e
