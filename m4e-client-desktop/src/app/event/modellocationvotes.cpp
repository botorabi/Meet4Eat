/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include "modellocationvotes.h"
#include <QJsonDocument>
#include <QJsonArray>
#include <QJsonObject>


namespace m4e
{
namespace event
{

bool ModelLocationVotes::fromJSON( const QString& input )
{
    QJsonParseError err;
    QJsonDocument doc = QJsonDocument::fromJson( input.toUtf8(), &err );
    if ( err.error != QJsonParseError::NoError )
        return false;

    return fromJSON( doc );
}

bool ModelLocationVotes::fromJSON( const QJsonDocument& input )
{
    QJsonObject data          = input.object();
    QString     id            = data.value( "id" ).toString( "" );
    QString     eventid       = data.value( "eventId" ).toString( "" );
    QString     locid         = data.value( "locationId" ).toString( "" );
    QString     locname       = data.value( "locationName" ).toString( "" );
    qint64      timecreation  = ( qint64 )data.value( "creationTime" ).toDouble( 0.0 );
    qint64      timebegin     = ( qint64 )data.value( "timeBegin" ).toDouble( 0.0 );
    qint64      timeend       = ( qint64 )data.value( "timeEnd" ).toDouble( 0.0 );
    QJsonArray  userids       = data.value( "userIds" ).toArray();
    QJsonArray  usernames     = data.value( "userNames" ).toArray();

    setId( id );
    setEventId( eventid );
    setLocationId( locid );
    setLocationName( locname );

    QDateTime t;
    t.setSecsSinceEpoch( timecreation );
    setVoteCreationTime( t );
    t.setSecsSinceEpoch( timebegin );
    setVoteTimeBegin( t );
    t.setSecsSinceEpoch( timeend );
    setVoteTimeEnd( t );

    QList< QString > votedusers;
    for (QVariant v: userids.toVariantList() )
    {
        votedusers.append( v.toString() );
    }
    setUserIds( votedusers );
    votedusers.clear();
    for (QVariant v: usernames.toVariantList() )
    {
        votedusers.append( v.toString() );
    }
    setUserNames( votedusers );

    return true;
}

} // namespace event
} // namespace m4e
