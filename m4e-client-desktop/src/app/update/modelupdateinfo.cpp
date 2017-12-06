/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include "modelupdateinfo.h"
#include <QJsonDocument>
#include <QJsonObject>


namespace m4e
{
namespace update
{

bool ModelUpdateInfo::fromJSON( const QString& input )
{
    QJsonParseError err;
    QJsonDocument doc = QJsonDocument::fromJson( input.toUtf8(), &err );
    if ( err.error != QJsonParseError::NoError )
        return false;

    return fromJSON( doc );
}

bool ModelUpdateInfo::fromJSON( const QJsonDocument& input )
{
    QJsonObject data      = input.object();
    QString     version   = data.value( "updateVersion" ).toString( "" );
    QString     url       = data.value( "url" ).toString( "" );
    qint64      reldate   = ( qint64 )data.value( "releaseDate" ).toDouble( 0.0 );

    setVersion( version );
    setURL( url );

    QDateTime date;
    if ( reldate > 0 )
    {
        date.setSecsSinceEpoch( reldate );
    }
    setReleaseDate( date );

    return true;
}

} // namespace update
} // namespace m4e
