/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include <core/log.h>
#include "resp-mailbox.h"
#include <webapp/request/rest-mailbox.h>
#include <mailbox/modelmail.h>


namespace m4e
{
namespace webapp
{

/******************************************************/
/**************** ResponseGetMails ********************/
/******************************************************/

ResponseGetMails::ResponseGetMails( RESTMailBox* p_requester ) :
 _p_requester( p_requester )
{}

void ResponseGetMails::onRESTResponseSuccess( const QJsonDocument& results )
{
    QJsonDocument datadoc;
    QString       errstring;
    QString       errcode;
    bool res = checkStatus( results, datadoc, errcode, errstring );
    if ( !res )
    {
        emit _p_requester->onRESTMailErrorGetMails( errcode, errstring );
        return;
    }

    QJsonArray mailentries = datadoc.array();
    QList< mailbox::ModelMailPtr > mails;
    for ( int i = 0; i < mailentries.size(); i++ )
    {
        QJsonObject obj = mailentries.at( i ).toObject();
        mailbox::ModelMailPtr m = new mailbox::ModelMail();
        if ( !m->fromJSON( QJsonDocument( obj ) ) )
        {
            log_warning << TAG << "invalid JSON format detected, ignoring mail!" << std::endl;
        }
        else
        {
            mails.append( m );
        }
    }
    emit _p_requester->onRESTMailGetMails( mails );
}

void ResponseGetMails::onRESTResponseError( const QString& reason )
{
    emit _p_requester->onRESTMailErrorGetMails( "", reason );
}

/******************************************************/
/***************** ResponseSendMail *******************/
/******************************************************/

ResponseSendMail::ResponseSendMail( RESTMailBox* p_requester ) :
 _p_requester( p_requester )
{}

void ResponseSendMail::onRESTResponseSuccess( const QJsonDocument& results )
{
    QJsonDocument datadoc;
    QString       errstring;
    QString       errcode;
    bool res = checkStatus( results, datadoc, errcode, errstring );
    if ( !res )
    {
        emit _p_requester->onRESTMailErrorSendMail( errcode, errstring );
        return;
    }
    emit _p_requester->onRESTMailSendMail();
}

void ResponseSendMail::onRESTResponseError( const QString& reason )
{
    emit _p_requester->onRESTMailErrorSendMail( "", reason );
}

/******************************************************/
/**************** ResponseDeleteMail ******************/
/******************************************************/

PerformMailOperation::PerformMailOperation( RESTMailBox* p_requester ) :
 _p_requester( p_requester )
{}

void PerformMailOperation::onRESTResponseSuccess( const QJsonDocument& results )
{
    QJsonDocument datadoc;
    QString       errstring;
    QString       errcode;
    bool res = checkStatus( results, datadoc, errcode, errstring );
    if ( !res )
    {
        emit _p_requester->onRESTMailErrorPerformOperation( errcode, errstring );
        return;
    }
    QJsonObject obj = datadoc.object();
    QString mailid  = obj.value( "id" ).toString( "" );
    QString op      = obj.value( "operation" ).toString( "" );
    emit _p_requester->onRESTMailPerformOperation( mailid, op );
}

void PerformMailOperation::onRESTResponseError( const QString& reason )
{
    emit _p_requester->onRESTMailErrorPerformOperation( "", reason );
}

/******************************************************/
/************ ResponseCountUnreadMails ****************/
/******************************************************/

ResponseCountUnreadMails::ResponseCountUnreadMails( RESTMailBox* p_requester ) :
 _p_requester( p_requester )
{}

void ResponseCountUnreadMails::onRESTResponseSuccess( const QJsonDocument& results )
{
    QJsonDocument datadoc;
    QString       errstring;
    QString       errcode;
    bool res = checkStatus( results, datadoc, errcode, errstring );
    if ( !res )
    {
        emit _p_requester->onRESTMailErrorCountUnreadMails( errcode, errstring );
        return;
    }

    QJsonObject obj = datadoc.object();
    int count = obj.value( "unreadMails" ).toInt( 0 );
    emit _p_requester->onRESTMailCountUnreadMails( count );
}

void ResponseCountUnreadMails::onRESTResponseError( const QString& reason )
{
    emit _p_requester->onRESTMailErrorCountUnreadMails( "", reason );
}

} // namespace webapp
} // namespace m4e
