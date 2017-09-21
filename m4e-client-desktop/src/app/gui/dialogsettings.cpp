/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "dialogsettings.h"
#include <core/log.h>
#include <ui_dlgsettings.h>
#include <data/appsettings.h>
#include <webapp/rest-authentication.h>
#include <QMessageBox>


namespace m4e
{
namespace ui
{

DialogSettings::DialogSettings( QWidget* p_parent ) :
 QDialog( p_parent )
{
    _p_ui = new Ui::DlgSettings();
    _p_ui->setupUi( this );
    setupUI();
}

DialogSettings::~DialogSettings()
{
}

void DialogSettings::setupUI()
{
    QString server   = m4e::data::AppSettings::get()->readSettingsValue( M4E_SETTINGS_CAT_SRV, M4E_SETTINGS_KEY_SRV_URL, "" );
    QString login    = m4e::data::AppSettings::get()->readSettingsValue( M4E_SETTINGS_CAT_USER, M4E_SETTINGS_KEY_USER_LOGIN, "" );
    QString passwd   = m4e::data::AppSettings::get()->readSettingsValue( M4E_SETTINGS_CAT_USER, M4E_SETTINGS_KEY_USER_PW, "" );
    QString remember = m4e::data::AppSettings::get()->readSettingsValue( M4E_SETTINGS_CAT_USER, M4E_SETTINGS_KEY_USER_PW_REM, "yes" );

    _p_ui->lineEditServer->setText( server );
    _p_ui->lineEditLogin->setText( login );
    _p_ui->lineEditPassword->setText( "" );
    _p_ui->checkBoxRememberPw->setChecked( remember == "yes" );
    _p_ui->pushButtonSignOut->setEnabled( false );

    user::UserAuthentication* p_user = getOrCreateUserAuth();
    p_user->setServerURL( server );
    if ( !server.isEmpty() && !login.isEmpty() && !passwd.isEmpty() )
    {
        _p_ui->pushButtonSignIn->setEnabled( false );
        p_user->requestAuthState();
    }
}

void DialogSettings::accept()
{
    QString server   = _p_ui->lineEditServer->text();
    QString login    = _p_ui->lineEditLogin->text();
    QString passwd   = _p_ui->lineEditPassword->text();
    QString remember = _p_ui->checkBoxRememberPw->isChecked() ? "yes" : "no";

    m4e::data::AppSettings::get()->writeSettingsValue( M4E_SETTINGS_CAT_SRV, M4E_SETTINGS_KEY_SRV_URL, server );
    m4e::data::AppSettings::get()->writeSettingsValue( M4E_SETTINGS_CAT_USER, M4E_SETTINGS_KEY_USER_LOGIN, login );
    if ( !passwd.isEmpty() )
    {
        passwd = m4e::webapp::RESTAuthentication::createHash( passwd );
        m4e::data::AppSettings::get()->writeSettingsValue( M4E_SETTINGS_CAT_USER, M4E_SETTINGS_KEY_USER_PW, passwd );
    }
    m4e::data::AppSettings::get()->writeSettingsValue( M4E_SETTINGS_CAT_USER, M4E_SETTINGS_KEY_USER_PW_REM, remember );

    QDialog::accept();
}

void DialogSettings::onBtnSignInClicked()
{
    QString server   = _p_ui->lineEditServer->text();
    QString login    = _p_ui->lineEditLogin->text();
    QString passwd   = _p_ui->lineEditPassword->text();
    if ( passwd.isEmpty() )
    {
        passwd = m4e::data::AppSettings::get()->readSettingsValue( M4E_SETTINGS_CAT_USER, M4E_SETTINGS_KEY_USER_PW, "" );
    }
    else
    {
        passwd = m4e::webapp::RESTAuthentication::createHash( passwd );
    }

    user::UserAuthentication* p_user = getOrCreateUserAuth();
    if ( !server.isEmpty() && !login.isEmpty() && !passwd.isEmpty() )
    {
        p_user->requestSignIn( login, passwd );
    }
    else
    {
        QMessageBox msgbox( QMessageBox::Information, "Sign In", QApplication::translate( "DialogSettings", "Please, first enter the account information." ), QMessageBox::Ok, this );
        msgbox.exec();
    }
}

void DialogSettings::onBtnSignOutClicked()
{
    user::UserAuthentication* p_user = getOrCreateUserAuth();
    p_user->requestSignOut();
}

void DialogSettings::onResponseAuthState( bool authenticated, QString /*userId*/ )
{
    _p_ui->pushButtonSignIn->setEnabled( !authenticated );
    _p_ui->pushButtonSignOut->setEnabled( authenticated );
}

void DialogSettings::onResponseSignInResult( bool success, QString /*userId*/, m4e::user::UserAuthentication::AuthResultsCode code, QString reason )
{
    _p_ui->pushButtonSignIn->setEnabled( !success );
    _p_ui->pushButtonSignOut->setEnabled( success );

    if ( success )
    {
        log_debug << "successfully signed in user" << std::endl;
        QMessageBox msgbox( QMessageBox::Information, "Sign In", QApplication::translate( "DialogSettings", "Your were successfully signed in." ), QMessageBox::Ok, this );
        msgbox.exec();
    }
    else
    {
        log_debug << "failed to sign in user (" << QString::number( code ).toStdString() << "), reason: " << reason.toStdString() << std::endl;
        QString text = QApplication::translate( "DialogSettings", "Could not sign in. Check your credentials and try again." );
        QMessageBox msgbox( QMessageBox::Warning, "Sign In", text, QMessageBox::Ok, this );
        msgbox.exec();
    }
}

void DialogSettings::onResponseSignOutResult( bool /*success*/, user::UserAuthentication::AuthResultsCode /*code*/, QString /*reason*/ )
{
    user::UserAuthentication* p_user = getOrCreateUserAuth();
    p_user->requestAuthState();
}

user::UserAuthentication* DialogSettings::getOrCreateUserAuth()
{
    if ( !_p_userAuth )
    {
        _p_userAuth = new user::UserAuthentication( this );
        connect( _p_userAuth, SIGNAL( onResponseSignInResult( bool, QString, enum m4e::user::UserAuthentication::AuthResultsCode, QString ) ),
                 this, SLOT( onResponseSignInResult( bool, QString, enum m4e::user::UserAuthentication::AuthResultsCode, QString ) ) );

        connect( _p_userAuth, SIGNAL( onResponseSignOutResult( bool, enum m4e::user::UserAuthentication::AuthResultsCode, QString ) ),
                 this, SLOT( onResponseSignOutResult( bool, enum m4e::user::UserAuthentication::AuthResultsCode, QString ) ) );

        connect( _p_userAuth, SIGNAL( onResponseAuthState( bool, QString ) ), this, SLOT( onResponseAuthState( bool, QString ) ) );
    }

    _p_userAuth->setServerURL( _p_ui->lineEditServer->text() );

    return _p_userAuth;
}

} // namespace ui
} // namespace m4e
