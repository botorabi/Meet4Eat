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
    QString from = ( mail->getSenderId() == curruserid ) ? currusername : mail->getSenderId();
    QString to   = ( mail->getReceiverId() == curruserid ) ? currusername : mail->getReceiverId();

    _p_ui->lineEditFrom->setText( from );
    _p_ui->lineEditTo->setText( to );
    _p_ui->lineEditSubject->setText( mail->getSubject() );
    _p_ui->textEditBody->setPlainText( mail->getContent() );
    const QDateTime& timestamp = mail->getDate();
    _p_ui->labelSendDate->setText( timestamp.toString( "yyyy-MM-dd  HH:mm" ) );

    _p_ui->lineEditFrom->setReadOnly( true );
    _p_ui->lineEditTo->setReadOnly( readOnly );
    _p_ui->lineEditSubject->setReadOnly( readOnly );
    _p_ui->textEditBody->setReadOnly( readOnly );
    _p_ui->pushButtonAddressBook->setVisible( !readOnly );
    _p_ui->pushButtonSend->setVisible( !readOnly );
    _p_ui->labelDate->setVisible( readOnly );
    _p_ui->labelSendDate->setVisible( readOnly );
}

void WidgetMailEdit::onBtnSendClicked()
{
    log_verbose << TAG << "TODO onBtnSendClicked" << std::endl;

    bool invalid = _p_ui->lineEditTo->text().isEmpty();
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

    _mail->setReceiverId( _p_ui->lineEditTo->text() );
    _mail->setSubject( _p_ui->lineEditSubject->text() );
    _mail->setContent( _p_ui->textEditBody->toPlainText() );
    _p_webApp->getMailBox()->requestSendMail( _mail );
}

void WidgetMailEdit::onBtnAddrBookClicked()
{
    log_verbose << TAG << "TODO onBtnAddrBookClicked" << std::endl;
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
}

} // namespace mailbox
} // namespace m4e
