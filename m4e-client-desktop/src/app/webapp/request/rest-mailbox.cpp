/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include <core/log.h>
#include "rest-mailbox.h"
#include <webapp/response/resp-mailbox.h>


namespace m4e
{
namespace webapp
{

RESTMailBox::RESTMailBox( QObject* p_parent ) :
 Meet4EatREST( p_parent )
{
}

RESTMailBox::~RESTMailBox()
{
}

void RESTMailBox::getCountMails()
{
    QUrl url( getResourcePath() + "/rest/mails/count" );
    auto p_callback = new ResponseCountMails( this );
    getRESTOps()->GET( url, createResultsCallback( p_callback ) );
}

void RESTMailBox::getCountUnreadMails()
{
    QUrl url( getResourcePath() + "/rest/mails/countUnread" );
    auto p_callback = new ResponseCountUnreadMails( this );
    getRESTOps()->GET( url, createResultsCallback( p_callback ) );
}

void RESTMailBox::getMails( int from, int to )
{
    QUrl url( getResourcePath() + "/rest/mails/" + QString::number( from ) + "/" + QString::number( to ) );
    auto p_callback = new ResponseGetMails( this );
    getRESTOps()->GET( url, createResultsCallback( p_callback ) );
}

void RESTMailBox::sendMail( mailbox::ModelMailPtr mail )
{
    QUrl url( getResourcePath() + "/rest/mails/send" );
    auto p_callback = new ResponseSendMail( this );
    getRESTOps()->POST( url, createResultsCallback( p_callback ), mail->toJSON() );
}

void RESTMailBox::performMailOperation( const QString& mailId, const QString& operation )
{
    QUrl url( getResourcePath() + "/rest/mails/operate/" + mailId );
    QJsonObject obj;
    obj.insert( "operation", operation );
    QJsonDocument doc( obj );
    auto p_callback = new PerformMailOperation( this );
    getRESTOps()->POST( url, createResultsCallback( p_callback ), doc );
}

} // namespace webapp
} // namespace m4e
