/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include <core/log.h>
#include "mailbox.h"
#include <QApplication>


namespace m4e
{
namespace mailbox
{

MailBox::MailBox( QObject* p_parent ) :
 QObject( p_parent )
{
    _p_restMailBox  = new webapp::RESTMailBox( this );
    connect( _p_restMailBox, SIGNAL( onRESTMailCountUnreadMails( int ) ), this, SLOT( onRESTMailCountUnreadMails( int ) ) );
    connect( _p_restMailBox, SIGNAL( onRESTMailErrorCountUnreadMails( QString, QString ) ), this, SLOT( onRESTMailErrorCountUnreadMails( QString, QString ) ) );
    connect( _p_restMailBox, SIGNAL( onRESTMailGetMails( QList< m4e::mailbox::ModelMailPtr > ) ), this, SLOT( onRESTMailGetMails( QList< m4e::mailbox::ModelMailPtr > ) ) );
    connect( _p_restMailBox, SIGNAL( onRESTMailErrorGetMails( QString, QString ) ), this, SLOT( onRESTMailErrorGetMails( QString, QString ) ) );
    connect( _p_restMailBox, SIGNAL( onRESTMailSendMail() ), this, SLOT( onRESTMailSendMail() ) );
    connect( _p_restMailBox, SIGNAL( onRESTMailErrorSendMail( QString, QString ) ), this, SLOT( onRESTMailErrorSendMail( QString, QString ) ) );
    connect( _p_restMailBox, SIGNAL( onRESTMailPerformOperation( QString, QString ) ), this, SLOT( onRESTMailPerformOperation( QString, QString ) ) );
    connect( _p_restMailBox, SIGNAL( onRESTMailErrorPerformOperation( QString, QString ) ), this, SLOT( onRESTMailErrorPerformOperation( QString, QString ) ) );
}

MailBox::~MailBox()
{
}

void MailBox::setServerURL( const QString &url )
{
    _p_restMailBox->setServerURL( url );
}

const QString& MailBox::getServerURL() const
{
    return _p_restMailBox->getServerURL();
}

ModelMailPtr MailBox::getMail( const QString& mailId )
{
    for ( mailbox::ModelMailPtr mail: _mails )
    {
        if ( mail->getId() == mailId )
        {
            return mail;
        }
    }
    return ModelMailPtr();
}

QList< ModelMailPtr > MailBox::getAllMails()
{
    return _mails;
}

void MailBox::requestCountUnreadMails()
{
    setLastError();
    _p_restMailBox->getCountUnreadMails();
}

void MailBox::requestMails( int from, int to )
{
    setLastError();
    _p_restMailBox->getMails( from, to );
}

void MailBox::requestSendMail( ModelMailPtr mail )
{
    setLastError();
    _p_restMailBox->sendMail( mail );
}

void MailBox::requestDeleteMail( const QString& mailId )
{
    setLastError();
    _p_restMailBox->performMailOperation( mailId, "trash" );
}

void MailBox::requestUndeleteMail( const QString& mailId )
{
    setLastError();
    _p_restMailBox->performMailOperation( mailId, "untrash" );
}

void MailBox::requestMarkMail( const QString& mailId, bool read )
{
    setLastError();
    _p_restMailBox->performMailOperation( mailId, read ? "read" : "unread" );
}

void MailBox::onRESTMailCountUnreadMails( int count )
{
    log_verbose << TAG << "got count of unread mails: " << count << std::endl;
    emit onResponseCountUnreadMails( true, count );
}

void MailBox::onRESTMailErrorCountUnreadMails( QString errorCode, QString reason )
{
    log_verbose << TAG << "failed to get count of unread mails: " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponseCountUnreadMails( false, 0 );
}

void MailBox::onRESTMailGetMails( QList< m4e::mailbox::ModelMailPtr > mails )
{
    log_verbose << TAG << "got user's mails" << std::endl;

    _mails = mails;
    emit onResponseMails( true, mails );
}

void MailBox::onRESTMailErrorGetMails( QString errorCode, QString reason )
{
    log_verbose << TAG << "failed to get user's mails: " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponseMails( false, QList< m4e::mailbox::ModelMailPtr >() );
}

void MailBox::onRESTMailSendMail()
{
    log_verbose << TAG << "mail was sent successfully" << std::endl;
    emit onResponseSendMail( true );
}

void MailBox::onRESTMailErrorSendMail( QString errorCode, QString reason )
{
    log_verbose << TAG << "failed to send a mail: " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponseSendMail( false );
}

void MailBox::onRESTMailPerformOperation( QString mailId, QString operation )
{
    log_verbose << TAG << "mail operation was successfully performed: " << mailId << "/" << operation << std::endl;
    emit onResponsePerformOperation( true, mailId, operation );
}

void MailBox::onRESTMailErrorPerformOperation( QString errorCode, QString reason )
{
    log_verbose << TAG << "failed to delete user's mails: " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponsePerformOperation( false, "", "" );
}

void MailBox::setLastError( const QString& error, const QString& errorCode )
{
    _lastError = error;
    _lastErrorCode = errorCode;
}

} // namespace mailbox
} // namespace m4e
