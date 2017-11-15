/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include "modelevent.h"
#include <core/log.h>
#include <QJsonDocument>
#include <QJsonObject>
#include <QJsonArray>
#include <QTimeZone>


namespace m4e
{
namespace event
{

QJsonDocument ModelEvent::toJSON()
{
    QJsonObject obj;

    if ( !getId().isEmpty() )
        obj.insert( "id", getId() );

    obj.insert( "name",            getName() );
    obj.insert( "description",     getDescription() );
    obj.insert( "public",          getIsPublic() );
    obj.insert( "eventStart",      getStartDate().toSecsSinceEpoch() );
    //! NOTE dont use getRepeatedDayTime() here as we need the UTC time for the server
    obj.insert( "repeatDayTime",   _repeatDayTime.msecsSinceStartOfDay() / 1000 );
    obj.insert( "votingTimeBegin", getVotingTimeBegin() );
    obj.insert( "repeatWeekDays",  int( getRepeatWeekDays() ) );

    // is there a photo update?
    if ( getUpdatedPhoto().valid() )
    {
        obj.insert( "photo", QString( getUpdatedPhoto()->getContent() ) );
    }

    QJsonDocument doc( obj );
    return doc;
}

bool ModelEvent::fromJSON( const QString& input )
{
    QJsonParseError err;
    QJsonDocument doc = QJsonDocument::fromJson( input.toUtf8(), &err );
    if ( err.error != QJsonParseError::NoError )
        return false;

    return fromJSON( doc );
}

bool ModelEvent::fromJSON( const QJsonDocument& input )
{
    QJsonObject data = input.object();

    QString    id             = data.value( "id" ).toString( "" );
    QString    name           = data.value( "name" ).toString( "" );
    QString    desc           = data.value( "description" ).toString( "" );
    QString    photoid        = data.value( "photoId" ).toString( "" );
    QString    photoetag      = data.value( "photoETag" ).toString( "" );
    bool       ispublic       = data.value( "public" ).toBool();
    qint64     eventstart     = ( qint64 )data.value( "eventStart" ).toDouble( 0.0 );
    int        repweekdays    = data.value( "repeatWeekDays" ).toInt( 0 );
    int        repdaytime     = data.value( "repeatDayTime" ).toInt( 0 );
    qint64     votingtime     = ( qint64 )data.value( "votingTimeBegin" ).toDouble( 0.0 );
    QJsonArray locations      = data.value( "locations" ).toArray();
    QJsonArray members        = data.value( "members" ).toArray();
    QString    ownerid        = data.value( "ownerId" ).toString( "" );
    QString    ownername      = data.value( "ownerName" ).toString( "" );
    QString    ownerphotoid   = data.value( "ownerPhotoId" ).toString( "" );
    QString    ownerphotoetag = data.value( "ownerPhotoETag" ).toString( "" );
    QString    ownerstatus    = data.value( "status" ).toString( "" );

    setId( id );
    setName( name );
    setDescription( desc );
    setIsPublic( ispublic );
    setPhotoId( photoid );
    setPhotoETag( photoetag );
    setRepeatWeekDays( static_cast< uint >( repweekdays ) );

    QDateTime start;
    start.setSecsSinceEpoch( eventstart );
    setStartDate( start );

    setVotingTimeBegin( votingtime );

    int hour = static_cast< int >( repdaytime / ( 60 * 60 ) );
    int min = static_cast< int >( repdaytime / 60 ) - hour * 60;
    QTime reptime( hour, min, 0 );
    //! NOTE dont use setRepeatedDayTime() here as we need to store the UTC time
    _repeatDayTime = reptime;

    // extract the locations
    QList< event::ModelLocationPtr > locs;
    for ( int i = 0; i < locations.size(); i++ )
    {
        QJsonObject obj = locations.at( i ).toObject();
        QString id        = obj.value( "id" ).toString( "" );
        QString name      = obj.value( "name" ).toString( "" );
        QString desc      = obj.value( "description" ).toString( "" );
        QString photoid   = obj.value( "photoId" ).toString( "" );
        QString photoetag = obj.value( "photoETag" ).toString( "" );

        event::ModelLocationPtr l = new event::ModelLocation();
        l->setId( id );
        l->setName( name );
        l->setDescription( desc );
        l->setPhotoId( photoid );
        l->setPhotoETag( photoetag );

        locs.append( l );
    }
    setLocations( locs );

    // extract the members
    QList< user::ModelUserInfoPtr > mems;
    for ( int i = 0; i < members.size(); i++ )
    {
        QJsonObject obj = members.at( i ).toObject();
        user::ModelUserInfoPtr u = new user::ModelUserInfo();
        if ( !u->fromJSON( QJsonDocument( obj ) ) )
        {
            log_warning << TAG << "invalid JSON format detected, ignoring member information" << std::endl;
        }
        else
        {
            mems.append( u );
        }
    }
    setMembers( mems );

    user::ModelUserInfoPtr owner = new user::ModelUserInfo();
    owner->setId( ownerid );
    owner->setName( ownername );
    owner->setPhotoId( ownerphotoid );
    owner->setPhotoETag( ownerphotoetag );
    owner->setStatus( ownerstatus );
    setOwner( owner );

    return true;
}

bool ModelEvent::checkIsRepeatedDay( unsigned int currentDay ) const
{
    unsigned int d = ( 0x1 << currentDay );
    return ( d & _repeatWeekDays ) != 0;
}

QTime ModelEvent::getRepeatDayTime() const
{
    QTime time;
    int utcoffset = QTimeZone::systemTimeZone().offsetFromUtc( QDateTime::currentDateTime() );
    time = _repeatDayTime.addSecs( utcoffset );
    return time;
}

void ModelEvent::setRepeatDayTime( const QTime& repeatDayTime )
{
    int utcoffset = QTimeZone::systemTimeZone().offsetFromUtc( QDateTime::currentDateTime() );
    _repeatDayTime = repeatDayTime.addSecs( -utcoffset );
}

QDateTime ModelEvent::getStartDateVotingBegin() const
{
    QDateTime begintime;
    if ( _startDate.isValid() && ( _votingTimeBegin > 0 ) )
    {
        begintime.setSecsSinceEpoch( _startDate.toSecsSinceEpoch() - _votingTimeBegin );
    }

    return begintime;
}

QTime ModelEvent::getRepeatDayVotingBegin() const
{
    QTime begintime;
    int utcoffset = QTimeZone::systemTimeZone().offsetFromUtc( QDateTime::currentDateTime() );
    begintime = _repeatDayTime.addSecs( -_votingTimeBegin + utcoffset );

    return begintime;
}

ModelLocationPtr ModelEvent::getLocation( const QString &id )
{
    ModelLocationPtr location;
    QList< ModelLocationPtr >::iterator loc = _locations.begin();
    for ( ; loc != _locations.end(); ++loc )
    {
        if ( ( *loc )->getId() == id )
        {
            location = *loc;
            break;
        }
    }
    return location;
}

bool ModelEvent::removeLocation( const QString& id )
{
    QList< ModelLocationPtr >::iterator loc = _locations.begin();
    for ( ; loc != _locations.end(); ++loc )
    {
        if ( ( *loc )->getId() == id )
        {
            _locations.erase( loc );
            return true;
        }
    }
    return false;
}

bool ModelEvent::addOrUpdateLocation( ModelLocationPtr location )
{
    QList< ModelLocationPtr >::iterator loc = _locations.begin();
    for ( ; loc != _locations.end(); ++loc )
    {
        if ( ( *loc )->getId() == location->getId() )
        {
            *loc = location;
            return false;
        }
    }

    _locations.append( location );
    return true;
}

} // namespace event
} // namespace m4e
