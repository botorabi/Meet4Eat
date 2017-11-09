/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "widgetmailedit.h"
#include <core/log.h>
#include <common/guiutils.h>
#include <common/dialogmessage.h>
#include <user/dialogsearchuser.h>
#include <ui_widgetmailedit.h>


namespace m4e
{
namespace mailbox
{

WidgetMailEdit::WidgetMailEdit( webapp::WebApp* p_webApp, QWidget* p_parent ) :
 QWidget( p_parent ),
 _p_webApp( p_webApp)
{
    _p_ui = new Ui::WidgetMailEdit();
}

WidgetMailEdit::~WidgetMailEdit()
{
    delete _p_ui;
}

void WidgetMailEdit::setupUI( ModelMailPtr mail, bool readOnly )
{
    _mail = mail;
    _p_ui->setupUi( this );

    connect( _p_webApp->getMailBox(), SIGNAL( onResponseSendMail( bool ) ), this, SLOT( onResponseSendMail( bool ) ) );

    QString curruserid, currusername;
    curruserid   = _p_webApp->getUser()->getUserData()->getId();
    currusername = _p_webApp->getUser()->getUserData()->getName();
    // sender ID 0 means the sender was the system
    QString from = ( mail->getSenderId() == curruserid ) ? currusername : ( mail->getSenderId() == "0" ) ? M4E_MAIL_SENDER_SYSTEM_NAME : mail->getSenderName();
    QString to   = mail->getReceiverName();

    _p_ui->lineEditFrom->setText( from );
    _p_ui->listWidgetTo->addItem( to );
    _p_ui->lineEditSubject->setText( mail->getSubject() );
    _p_ui->textEditBody->setPlainText( mail->getContent() );
    const QDateTime& timestamp = mail->getDate();
    _p_ui->labelSendDate->setText( timestamp.toString( "yyyy-MM-dd  HH:mm" ) );

    _p_ui->lineEditFrom->setReadOnly( true );
    _p_ui->lineEditSubject->setReadOnly( readOnly );
    _p_ui->textEditBody->setReadOnly( readOnly );
    _p_ui->pushButtonSearchUser->setVisible( !readOnly );
    _p_ui->pushButtonSend->setVisible( !readOnly );
    _p_ui->labelDate->setVisible( readOnly );
    _p_ui->labelSendDate->setVisible( readOnly );
}

void WidgetMailEdit::onBtnSendClicked()
{
    bool invalid = !_recipient.valid();
    invalid = invalid || _p_ui->lineEditSubject->text().isEmpty();
    invalid = invalid || _p_ui->textEditBody->toPlainText().isEmpty();
    if ( invalid )
    {
        common::DialogMessage msg( this );
        msg.setupUI( QApplication::translate( "WidgetMailEdit", "Send Mail" ),
                     QApplication::translate( "WidgetMailEdit", "Cannot send the mail.\nRecipient, subject, and mail text must not be empty!" ),
                     common::DialogMessage::BtnOk );

        msg.exec();
        return;
    }

    _mail->setReceiverId( _recipient->getId() );
    _mail->setSubject( _p_ui->lineEditSubject->text() );
    _mail->setContent( _p_ui->textEditBody->toPlainText() );
    _p_webApp->getMailBox()->requestSendMail( _mail );
}

void WidgetMailEdit::onBtnSearchUserClicked()
{
    user::DialogSearchUser dlg( _p_webApp, this );
    if ( dlg.exec() == common::BaseDialog::Btn1 )
    {
        user::ModelUserInfoPtr userinfo = dlg.getUserInfo();
        if ( userinfo.valid() )
        {
            setRecipient( userinfo );
        }
    }
}

void WidgetMailEdit::onResponseSendMail( bool success )
{
    QString text = success ? QApplication::translate( "WidgetMailEdit", "Mail was successfully sent." ) :
                             QApplication::translate( "WidgetMailEdit", "Mail could not be delivered!\nReason: " ) + _p_webApp->getMailBox()->getLastError();

    common::DialogMessage msg( this );
    msg.setupUI( QApplication::translate( "WidgetMailEdit", "Send Mail" ),
                 text,
                 common::DialogMessage::BtnOk );

    msg.exec();

    if ( success )
    {
        emit onMailSent();
    }
}

void WidgetMailEdit::setRecipient( user::ModelUserInfoPtr userInfo )
{
    if ( _recipient.valid() && ( _recipient->getId() == userInfo->getId() ) )
            return;

    _recipient = userInfo;
    // we consider a list of recipients in the gui for a possible future extention allowing more than one mail recipient
    _p_ui->listWidgetTo->clear();
    _p_ui->listWidgetTo->addItem( userInfo->getName() );
}

} // namespace mailbox
} // namespace m4e
