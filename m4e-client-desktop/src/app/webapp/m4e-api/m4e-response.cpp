/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include "m4e-response.h"
#include <QJsonObject>
#include <QJsonArray>

namespace m4e
{
namespace webapp
{

bool Meet4EatRESTResponse::checkStatus( const QJsonDocument& results, QJsonDocument& data, QString& errorCode, QString& errorString )
{
    QJsonObject jobject = results.object();
    QJsonValue  status  = jobject.value( "status" );
    // check the results status
    if ( status.toString( "" ) != "ok" )
    {
        QString description = jobject.value( "description" ).toString( "" );
        errorCode = QString::number( jobject.value( "code" ).toInt( 0 ) );
        errorString = "Error (" + errorCode + "): " + description;
        return false;
    }

    //! TODO in next future we will have a flat json structure, no need for parsing the data field as json!
    QJsonValue d = jobject.value( "data" );
    if ( d.isString() )
    {
        QByteArray datastr = jobject.value( "data" ).toString( "" ).toUtf8();
        data = QJsonDocument::fromJson( datastr );
    }
    else if ( d.isObject() )
    {
        data = QJsonDocument( d.toObject() );
    }
    else if ( d.isArray() )
    {
        data = QJsonDocument( d.toArray() );
    }

    return true;
}

} // namespace webapp
} // namespace m4e
