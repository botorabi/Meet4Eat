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
        errorCode = QString::number( jobject.value( "code" ).toInt() );
        errorString = "Error (" + errorCode + "): " + description;
        return false;
    }

    // extract the data (it is also in json format)
    QByteArray    datastr = jobject.value( "data" ).toString( "" ).toUtf8();
    data = QJsonDocument::fromJson( datastr );
    return true;
}

} // namespace webapp
} // namespace m4e
