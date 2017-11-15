/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "events.h"
#include <core/log.h>
#include <assert.h>


namespace m4e
{
namespace event
{

Events::Events( QObject* p_parent ) :
 QObject( p_parent )
{
    _p_restEvent = new webapp::RESTEvent( this );
    connect( _p_restEvent, SIGNAL( onRESTEventGetEvents( QList< m4e::event::ModelEventPtr > ) ), this, SLOT( onRESTEventGetEvents( QList< m4e::event::ModelEventPtr > ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventErrorGetEvents( QString, QString ) ), this, SLOT( onRESTEventErrorGetEvents( QString, QString ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventGetEvent( m4e::event::ModelEventPtr ) ), this, SLOT( onRESTEventGetEvent( m4e::event::ModelEventPtr ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventErrorGetEvent( QString, QString ) ), this, SLOT( onRESTEventErrorGetEvent( QString, QString ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventNewEvent( QString ) ), this, SLOT( onRESTEventNewEvent( QString ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventErrorNewEvent( QString, QString ) ), this, SLOT( onRESTEventErrorNewEvent( QString, QString ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventDeleteEvent( QString ) ), this, SLOT( onRESTEventDeleteEvent( QString ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventErrorDeleteEvent( QString, QString ) ), this, SLOT( onRESTEventErrorDeleteEvent( QString, QString ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventUpdateEvent( QString ) ), this, SLOT( onRESTEventUpdateEvent( QString ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventErrorUpdateEvent( QString, QString ) ), this, SLOT( onRESTEventErrorUpdateEvent( QString, QString ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventAddMember( QString, QString ) ), this, SLOT( onRESTEventAddMember( QString, QString ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventErrorAddMember( QString, QString ) ), this, SLOT( onRESTEventErrorAddMember( QString, QString ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventRemoveMember( QString, QString ) ), this, SLOT( onRESTEventRemoveMember( QString, QString ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventErrorRemoveMember( QString, QString ) ), this, SLOT( onRESTEventErrorRemoveMember( QString, QString ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventGetLocation( m4e::event::ModelLocationPtr ) ), this, SLOT( onRESTEventGetLocation( m4e::event::ModelLocationPtr ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventErrorGetLocation( QString, QString ) ), this, SLOT( onRESTEventErrorGetLocation( QString, QString ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventAddLocation( QString, QString ) ), this, SLOT( onRESTEventAddLocation( QString, QString ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventErrorAddLocation( QString, QString ) ), this, SLOT( onRESTEventErrorAddLocation( QString, QString ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventRemoveLocation( QString, QString ) ), this, SLOT( onRESTEventRemoveLocation( QString, QString ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventErrorRemoveLocation( QString, QString ) ), this, SLOT( onRESTEventErrorRemoveLocation( QString, QString ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventUpdateLocation( QString, QString ) ), this, SLOT( onRESTEventUpdateLocation( QString, QString ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventErrorUpdateLocation( QString, QString ) ), this, SLOT( onRESTEventErrorUpdateLocation( QString, QString ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventSetLocationVote( QString, QString, QString, bool ) ), this, SLOT( onRESTEventSetLocationVote( QString, QString, QString, bool ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventErrorSetLocationVote( QString, QString ) ), this, SLOT( onRESTEventErrorSetLocationVote( QString, QString ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventGetLocationVotesByTime( QList< m4e::event::ModelLocationVotesPtr > ) ), this, SLOT( onRESTEventGetLocationVotesByTime( QList< m4e::event::ModelLocationVotesPtr > ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventErrorGetLocationVotesByTime( QString, QString ) ), this, SLOT( onRESTEventErrorGetLocationVotesByTime( QString, QString ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventGetLocationVotesById( m4e::event::ModelLocationVotesPtr ) ), this, SLOT( onRESTEventGetLocationVotesById( m4e::event::ModelLocationVotesPtr ) ) );
    connect( _p_restEvent, SIGNAL( onRESTEventErrorGetLocationVotesById( QString, QString ) ), this, SLOT( onRESTEventErrorGetLocationVotesById( QString, QString ) ) );
}

Events::~Events()
{
}

void Events::setServerURL(const QString &url)
{
    _p_restEvent->setServerURL( url );
}

const QString& Events::getServerURL() const
{
    return _p_restEvent->getServerURL();
}

QList< event::ModelEventPtr > Events::getUserEvents()
{
    return _events;
}

ModelEventPtr Events::getUserEvent( const QString& id )
{
    for ( ModelEventPtr event: _events )
    {
        if ( event->getId() == id )
            return event;
    }
    return ModelEventPtr();
}

//######### Requests ############//

void Events::requestGetEvents()
{
    setLastError();
    _p_restEvent->getEvents();
}

void Events::requestGetEvent( const QString& eventId )
{
    setLastError();
    _p_restEvent->getEvent( eventId );
}

void Events::requestDeleteEvent( const QString& eventId )
{
    setLastError();
    _p_restEvent->deleteEvent( eventId );
}

void Events::requestUpdateEvent( ModelEventPtr event )
{
    setLastError();
    _p_restEvent->updateEvent( event );
}

void Events::requestNewEvent( ModelEventPtr event )
{
    setLastError();
    _p_restEvent->createEvent( event );
}

void Events::requestAddMember (const QString& eventId, const QString& memberId )
{
    setLastError();
    _p_restEvent->addMember( eventId, memberId );
}

void Events::requestRemoveMember( const QString& eventId, const QString& memberId )
{
    setLastError();
    _p_restEvent->removeMember( eventId, memberId );
}

void Events::requestGetLocation(const QString &eventId, const QString &locationId)
{
    setLastError();
    _p_restEvent->getLocation( eventId, locationId );
}

void Events::requestAddLocation( const QString& eventId, ModelLocationPtr location )
{
    setLastError();
    _p_restEvent->addLocation( eventId, location );
}

void Events::requestRemoveLocation( const QString& eventId, const QString& locationId )
{
    setLastError();
    _p_restEvent->removeLocation( eventId, locationId );
}

void Events::requestUpdateLocation( const QString& eventId, ModelLocationPtr location )
{
    setLastError();
    _p_restEvent->updateLocation( eventId, location );
}

bool Events::getVotingTimeWindow( const QString& eventId, QDateTime& timeBegin, QDateTime& timeEnd )
{
    ModelEventPtr event = getUserEvent( eventId );
    if ( !event.valid() )
        return false;

    if ( event->isRepeated() )
    {
        // calculate the next event time considering the week days
        int today = QDate::currentDate().dayOfWeek() - 1;
        int daystonextmatch = 0;
        for ( int i = 0; i < 7; i++ )
        {
            if ( event->checkIsRepeatedDay( ( today + i ) % 7 ) )
            {
                daystonextmatch = i;
                break;
            }
        }

        QDate currdate = QDate::currentDate();
        timeEnd.setDate( currdate );
        timeEnd.setTime( event->getRepeatDayTime() );
        timeEnd = timeEnd.addDays( daystonextmatch );
        timeBegin = timeEnd;
        timeBegin.setTime( event->getRepeatDayVotingBegin()  );
    }
    else
    {
        timeEnd   = event->getStartDate();
        timeBegin = event->getStartDateVotingBegin();
    }
    qint64 secscurr = QDateTime::currentDateTime().toSecsSinceEpoch();
    qint64 secsbeg  = timeBegin.toSecsSinceEpoch();
    qint64 secsend  = timeEnd.toSecsSinceEpoch();
    if ( (secscurr < secsbeg ) || ( secscurr > secsend ) )
        return false;

    return true;
}

void Events::requestSetLocationVote( const QString& eventId, const QString& locationId, bool vote )
{
    setLastError();
    _p_restEvent->setLocationVote( eventId, locationId, vote );
}

void Events::requestGetLocationVotesByTime( const QString& eventId, const QDateTime& timeBegin, const QDateTime& timeEnd )
{
    setLastError();
    _p_restEvent->getLocationVotesByTime( eventId, timeBegin, timeEnd );
}

void Events::requestGetLocationVotesById( const QString& locationVotesId )
{
    setLastError();
    _p_restEvent->getLocationVotesById( locationVotesId );
}

//######### Responses ############//

void Events::onRESTEventGetEvents( QList< event::ModelEventPtr > events )
{
    log_verbose << TAG << "got events: " << QString::number( events.size() ) << std::endl;
    _events = events;
    updateAlarms();
    emit onResponseGetEvents( true, events );
}

void Events::onRESTEventErrorGetEvents( QString errorCode, QString reason )
{
    log_verbose << TAG << "failed to get events: " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponseGetEvents( false, QList< event::ModelEventPtr >() );
}

void Events::onRESTEventGetEvent( ModelEventPtr event )
{
    log_verbose << TAG << "got user event: " << event->getId() << std::endl;

    // update the event in our local copy
    for ( int i = 0; i < _events.size(); i++ )
    {
        ModelEventPtr ev = _events[ i ];
        if ( ev->getId() == event->getId() )
        {
            log_verbose << TAG << "  updating the local copy of event" << std::endl;
            _events[ i ] = event;
            emit onResponseGetEvent( true, event );
            return;
        }
    }
    log_verbose << TAG << "  add new event to local copy of events" << std::endl;
    _events.append( event );
    updateAlarms();
    emit onResponseGetEvent( true, event );
}

void Events::onRESTEventErrorGetEvent( QString errorCode, QString reason )
{
    log_verbose << TAG << "failed to get user event: " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponseGetEvent( false, event::ModelEventPtr() );
}

void Events::onRESTEventDeleteEvent( QString eventId )
{
    log_verbose << TAG << "event was deleted: " << eventId << std::endl;
    // remove the event from internal event container
    for ( int i = 0; i < _events.size(); i++ )
    {
        ModelEventPtr ev = _events[ i ];
        if ( ev->getId() == eventId )
        {
            _events.removeAt( i );
            break;
        }
    }
    updateAlarms();
    emit onResponseDeleteEvent( true, eventId );
}

void Events::onRESTEventErrorDeleteEvent( QString errorCode, QString reason )
{
    log_verbose << TAG << "failed to delete event: " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponseDeleteEvent( false, "" );
}

void Events::onRESTEventNewEvent( QString eventId )
{
    log_verbose << TAG << "new event was created: " << eventId << std::endl;
    emit onResponseNewEvent( true, eventId );
}

void Events::onRESTEventErrorNewEvent( QString errorCode, QString reason )
{
    log_verbose << TAG << "failed to create new event: " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponseNewEvent( false, "" );
}

void Events::onRESTEventUpdateEvent( QString eventId )
{
    log_verbose << TAG << "event was updated: " << eventId << std::endl;
    updateAlarms();
    emit onResponseUpdateEvent( true, eventId );
}

void Events::onRESTEventErrorUpdateEvent( QString errorCode, QString reason )
{
    log_verbose << TAG << "failed to update event: " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponseUpdateEvent( false, "" );
}

void Events::onRESTEventAddMember( QString eventId, QString memberId )
{
    log_verbose << TAG << "new member added to event: " << eventId << "/" << memberId << std::endl;
    emit onResponseAddMember( true, eventId, memberId );
}

void Events::onRESTEventErrorAddMember( QString errorCode, QString reason )
{
    log_verbose << TAG << "failed to add new member to event: " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponseAddMember( false, "", "" );
}

void Events::onRESTEventRemoveMember( QString eventId, QString memberId )
{
    log_verbose << TAG << "member removed from event: " << eventId << "/" << memberId << std::endl;
    emit onResponseRemoveMember( true, eventId, memberId );
}

void Events::onRESTEventErrorRemoveMember( QString errorCode, QString reason )
{
    log_verbose << TAG << "failed to remove member from event: " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponseRemoveMember( false, "", "" );
}

void Events::onRESTEventGetLocation( ModelLocationPtr location )
{
    log_verbose << TAG << "location data arrived: " << location->getId() << std::endl;
    emit onResponseGetLocation( true, location );
}

void Events::onRESTEventErrorGetLocation( QString errorCode, QString reason )
{
    log_verbose << TAG << "failed to add new location to event: " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponseGetLocation( false, ModelLocationPtr() );
}

void Events::onRESTEventAddLocation( QString eventId, QString locationId )
{
    log_verbose << TAG << "new location added to event: " << eventId << "/" << locationId << std::endl;
    emit onResponseAddLocation( true, eventId, locationId );
}

void Events::onRESTEventErrorAddLocation( QString errorCode, QString reason )
{
    log_verbose << TAG << "failed to add new location to event: " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponseAddLocation( false, "", "" );
}

void Events::onRESTEventRemoveLocation( QString eventId, QString locationId )
{
    log_verbose << TAG << "location removed from event: " << eventId << "/" << locationId << std::endl;
    emit onResponseRemoveLocation( true, eventId, locationId );
}

void Events::onRESTEventErrorRemoveLocation( QString errorCode, QString reason )
{
    log_verbose << TAG << "failed to remove location from event: " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponseRemoveLocation( false, "", "" );
}

void Events::onRESTEventUpdateLocation( QString eventId, QString locationId )
{
    log_verbose << TAG << "location was updated event: " << eventId << "/" << locationId << std::endl;
    emit onResponseUpdateLocation( true, eventId, locationId );
}

void Events::onRESTEventErrorUpdateLocation( QString errorCode, QString reason )
{
    log_verbose << TAG << "failed to update location of event: " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponseUpdateLocation( false, "", "" );
}

void Events::onRESTEventSetLocationVote( QString eventId, QString locationId, QString votesId, bool vote )
{
    log_verbose << TAG << "location vote was set: " << eventId << "/" << locationId << "/" << ( vote ? "vote" : "unvote" ) << std::endl;
    emit onResponseSetLocationVote( true, eventId, locationId, votesId, vote );
}

void Events::onRESTEventErrorSetLocationVote( QString errorCode, QString reason )
{
    log_verbose << TAG << "failed to set the location vote: " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponseSetLocationVote( false, "", "", "", false );
}

void Events::onRESTEventGetLocationVotesByTime( QList< ModelLocationVotesPtr > votes )
{
    log_verbose << TAG << "location votes were received (by time)" << std::endl;
    emit onResponseGetLocationVotesByTime( true, votes );
}

void Events::onRESTEventErrorGetLocationVotesByTime( QString errorCode, QString reason )
{
    log_verbose << TAG << "failed to get location votes (by time): " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponseGetLocationVotesByTime( true, QList< ModelLocationVotesPtr >() );
}

void Events::onRESTEventGetLocationVotesById( ModelLocationVotesPtr votes )
{
    log_verbose << TAG << "location votes were received (by id)" << std::endl;
    emit onResponseGetLocationVotesById( true, votes );
}

void Events::onRESTEventErrorGetLocationVotesById( QString errorCode, QString reason )
{
    log_verbose << TAG << "failed to get location votes (by id): " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponseGetLocationVotesById( true, ModelLocationVotesPtr() );
}

void Events::setLastError( const QString& error, const QString& errorCode )
{
    _lastError = error;
    _lastErrorCode = errorCode;
}

void Events::onAlarm()
{
    QTimer* p_timer = dynamic_cast< QTimer* >( sender() );
    assert( p_timer && "invalid event sender type!" );

    QString eventid = p_timer->property( "id" ).toString();
    ModelEventPtr event = getUserEvent( eventid );
    if ( !event.valid() )
        return;

    log_verbose << "handling alarm for event: " << event->getName() << std::endl;

    // if the timer needs no re-scheduling then destroy it
    if ( !scheduleAlarmTimer( event, p_timer ) )
    {
        log_verbose << "  removing alarm for event: " << event->getName() << std::endl;
        _alarms.remove( eventid );
        p_timer->deleteLater();
    }

    // for repeated events checke the day in addition to day time
    if ( event->isRepeated() )
    {
        if ( !event->checkIsRepeatedDay( QDate::currentDate().dayOfWeek() - 1 ) )
            return;
    }

    emit onEventAlarm( event );
}

void Events::destroyAlarms()
{
    QMap< QString/*id*/, QTimer* >::iterator iter = _alarms.begin(), iterend = _alarms.end();
    for ( ; iter != iterend; ++iter )
    {
        iter.value()->stop();
        delete iter.value();
    }
    _alarms.clear();
}

void Events::updateAlarms()
{
    log_verbose << TAG << "updating event alarms" << std::endl;

    destroyAlarms();

    for ( ModelEventPtr event: _events )
    {
        QTimer* p_timer = new QTimer();
        // check if the timer needed a schedule at all
        if ( !scheduleAlarmTimer( event, p_timer ) )
        {
            delete p_timer;
            continue;
        }

        connect( p_timer, SIGNAL( timeout() ), this, SLOT( onAlarm() ) );
        _alarms.insert( event->getId(), p_timer );
    }
}

bool Events::scheduleAlarmTimer( ModelEventPtr event, QTimer* p_timer )
{
    // get the next voting begin time window. if it's begin time is in the past (no repeated event) then we
    //  need no timer anymore, otherwise setup an alarm timer for begin of event's voting time
    QDateTime tbegin, tend;
    getVotingTimeWindow( event->getId(), tbegin, tend );
    qint64 currtime  = QDateTime::currentMSecsSinceEpoch();
    qint64 nextalarm = tbegin.toMSecsSinceEpoch();
    qint64 alarm     = nextalarm - currtime;
    if ( ( alarm - 10000 ) < 0 )
        return false;

    p_timer->stop();
    p_timer->setSingleShot( true );
    p_timer->setInterval( alarm );
    p_timer->setProperty( "id", QVariant( event->getId() ) );
    p_timer->start();
    return true;
}

} // namespace event
} // namespace m4e
