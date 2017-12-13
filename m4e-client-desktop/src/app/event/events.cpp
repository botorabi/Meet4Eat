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

/* Timer name prefixes */
static const QString PREFIX_TIMER_START = "START_";
static const QString PREFIX_TIMER_END   = "END_";

/* All alarms are updated every 24 hours, this is the day time when it happens: shortly after midnight */
static const QTime M4E_TIMER_UPDATE_TIME( 0, 5, 0 );


Events::Events( QObject* p_parent ) :
 QObject( p_parent )
{
    setupTimerUpdate( M4E_TIMER_UPDATE_TIME );

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

void Events::updateUserStatus( const QString& userId, bool online )
{
    QString status = online ? "online" : "offline";
    for ( ModelEventPtr event: _events )
    {
        if ( event->getOwner()->getId() == userId )
            event->getOwner()->setStatus( status );

        for ( user::ModelUserInfoPtr user: event->getMembers() )
        {
            if ( user->getId() == userId )
            {
                user->setStatus( status );
                break;
            }
        }
    }
}

void Events::updateUserMembership( const QString& userId, const QString& eventId, bool added )
{
    event::ModelEventPtr event = getUserEvent( eventId );
    if ( !event.valid() )
        return;

    user::ModelUserInfoPtr user = event->getMember( userId );
    if ( !user.valid() )
        return;

    if ( added )
    {
        if ( !user.valid() )
            event->addMember( user );
    }
    else
    {
        if ( user.valid() )
            event->removeMember( userId );
    }
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

bool Events::getIsVotingTime( const QString& eventId )
{
    QDateTime timebegin, timeend;
    return getVotingTimeWindow( eventId, timebegin, timeend );
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
    _events = events;
    updateVotingTimers();
    emit onResponseGetEvents( true, events );
}

void Events::onRESTEventErrorGetEvents( QString errorCode, QString reason )
{
    log_warning << TAG << "failed to get events: " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponseGetEvents( false, QList< event::ModelEventPtr >() );
}

void Events::onRESTEventGetEvent( ModelEventPtr event )
{
    // update the event in our local copy
    for ( int i = 0; i < _events.size(); i++ )
    {
        ModelEventPtr ev = _events[ i ];
        if ( ev->getId() == event->getId() )
        {
            //log_verbose << TAG << "  updating the local copy of event" << std::endl;
            _events[ i ] = event;
            updateVotingTimer( event, true, true );
            emit onResponseGetEvent( true, event );
            return;
        }
    }
    log_verbose << TAG << "  add new event to local copy of events" << std::endl;
    _events.append( event );
    updateVotingTimer( event, true, true );
    emit onResponseGetEvent( true, event );
}

void Events::onRESTEventErrorGetEvent( QString errorCode, QString reason )
{
    log_warning << TAG << "failed to get user event: " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponseGetEvent( false, event::ModelEventPtr() );
}

void Events::onRESTEventDeleteEvent( QString eventId )
{
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
    updateVotingTimers();
    emit onResponseDeleteEvent( true, eventId );
}

void Events::onRESTEventErrorDeleteEvent( QString errorCode, QString reason )
{
    log_warning << TAG << "failed to delete event: " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponseDeleteEvent( false, "" );
}

void Events::onRESTEventNewEvent( QString eventId )
{
    emit onResponseNewEvent( true, eventId );
}

void Events::onRESTEventErrorNewEvent( QString errorCode, QString reason )
{
    log_warning << TAG << "failed to create new event: " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponseNewEvent( false, "" );
}

void Events::onRESTEventUpdateEvent( QString eventId )
{
    ModelEventPtr event = getUserEvent( eventId );
    if ( event.valid() )
        updateVotingTimer( event, true, true );

    emit onResponseUpdateEvent( true, eventId );
}

void Events::onRESTEventErrorUpdateEvent( QString errorCode, QString reason )
{
    log_warning << TAG << "failed to update event: " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponseUpdateEvent( false, "" );
}

void Events::onRESTEventAddMember( QString eventId, QString memberId )
{
    emit onResponseAddMember( true, eventId, memberId );
}

void Events::onRESTEventErrorAddMember( QString errorCode, QString reason )
{
    log_warning << TAG << "failed to add new member to event: " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponseAddMember( false, "", "" );
}

void Events::onRESTEventRemoveMember( QString eventId, QString memberId )
{
    emit onResponseRemoveMember( true, eventId, memberId );
}

void Events::onRESTEventErrorRemoveMember( QString errorCode, QString reason )
{
    log_warning << TAG << "failed to remove member from event: " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponseRemoveMember( false, "", "" );
}

void Events::onRESTEventGetLocation( ModelLocationPtr location )
{
    emit onResponseGetLocation( true, location );
}

void Events::onRESTEventErrorGetLocation( QString errorCode, QString reason )
{
    log_warning << TAG << "failed to add new location to event: " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponseGetLocation( false, ModelLocationPtr() );
}

void Events::onRESTEventAddLocation( QString eventId, QString locationId )
{
    emit onResponseAddLocation( true, eventId, locationId );
}

void Events::onRESTEventErrorAddLocation( QString errorCode, QString reason )
{
    log_warning << TAG << "failed to add new location to event: " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponseAddLocation( false, "", "" );
}

void Events::onRESTEventRemoveLocation( QString eventId, QString locationId )
{
    emit onResponseRemoveLocation( true, eventId, locationId );
}

void Events::onRESTEventErrorRemoveLocation( QString errorCode, QString reason )
{
    log_warning << TAG << "failed to remove location from event: " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponseRemoveLocation( false, "", "" );
}

void Events::onRESTEventUpdateLocation( QString eventId, QString locationId )
{
    emit onResponseUpdateLocation( true, eventId, locationId );
}

void Events::onRESTEventErrorUpdateLocation( QString errorCode, QString reason )
{
    log_warning << TAG << "failed to update location of event: " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponseUpdateLocation( false, "", "" );
}

void Events::onRESTEventSetLocationVote( QString eventId, QString locationId, QString votesId, bool vote )
{
    emit onResponseSetLocationVote( true, eventId, locationId, votesId, vote );
}

void Events::onRESTEventErrorSetLocationVote( QString errorCode, QString reason )
{
    log_warning << TAG << "failed to set the location vote: " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponseSetLocationVote( false, "", "", "", false );
}

void Events::onRESTEventGetLocationVotesByTime( QList< ModelLocationVotesPtr > votes )
{
    emit onResponseGetLocationVotesByTime( true, votes );
}

void Events::onRESTEventErrorGetLocationVotesByTime( QString errorCode, QString reason )
{
    log_warning << TAG << "failed to get location votes (by time): " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponseGetLocationVotesByTime( true, QList< ModelLocationVotesPtr >() );
}

void Events::onRESTEventGetLocationVotesById( ModelLocationVotesPtr votes )
{
    emit onResponseGetLocationVotesById( true, votes );
}

void Events::onRESTEventErrorGetLocationVotesById( QString errorCode, QString reason )
{
    log_warning << TAG << "failed to get location votes (by id): " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponseGetLocationVotesById( true, ModelLocationVotesPtr() );
}

void Events::setLastError( const QString& error, const QString& errorCode )
{
    _lastError = error;
    _lastErrorCode = errorCode;
}

void Events::onAlarmVotingStart()
{
    QTimer* p_timer = dynamic_cast< QTimer* >( sender() );
    assert( p_timer && "invalid event sender type!" );

    QString eventid = p_timer->property( "id" ).toString();
    ModelEventPtr event = getUserEvent( eventid );
    if ( !event.valid() )
        return;

    log_verbose << TAG << "start voting for event: " << event->getName() << std::endl;

    updateVotingTimer( event, true, false );

    emit onLocationVotingStart( event );
}

void Events::onAlarmVotingEnd()
{
    QTimer* p_timer = dynamic_cast< QTimer* >( sender() );
    assert( p_timer && "invalid event sender type!" );

    QString eventid = p_timer->property( "id" ).toString();
    ModelEventPtr event = getUserEvent( eventid );
    if ( !event.valid() )
        return;

    log_verbose << TAG << "end voting for event: " << event->getName() << std::endl;

    updateVotingTimer( event, false, true );

    emit onLocationVotingEnd( event );
}

void Events::onAlarmUpdateTimer()
{
    log_verbose << "daily alarm time updating" << std::endl;
    updateVotingTimers();
    setupTimerUpdate( M4E_TIMER_UPDATE_TIME );
}

void Events::destroyVotingTimers()
{
    QMap< QString/*id*/, QTimer* >::iterator iter = _alarms.begin(), iterend = _alarms.end();
    for ( ; iter != iterend; ++iter )
    {
        iter.value()->stop();
        delete iter.value();
    }
    _alarms.clear();
}

void Events::setupTimerUpdate( const QTime& updateTime )
{
    if ( !_p_alarmUpdateTimer )
    {
        _p_alarmUpdateTimer = new QTimer( this );
        _p_alarmUpdateTimer->setSingleShot( true );
        connect( _p_alarmUpdateTimer, SIGNAL( timeout() ), this, SLOT( onAlarmUpdateTimer() ) );
    }

    // the timer is meant to trigger every 24 hours
    int interval = QTime::currentTime().msecsTo( updateTime );
    if ( interval <= 0 )
        interval += 24 * 60 * 60 * 1000;

    if ( _p_alarmUpdateTimer->isActive() )
        _p_alarmUpdateTimer->stop();

    _p_alarmUpdateTimer->setInterval( interval );
    _p_alarmUpdateTimer->start();
}

void Events::updateVotingTimers()
{
    log_verbose << TAG << "updating event alarms" << std::endl;

    destroyVotingTimers();

    for ( ModelEventPtr event: _events )
    {
        // setup start and end timers
        updateVotingTimer( event, true, true );
    }
}

void Events::updateVotingTimer( ModelEventPtr event, bool startTimer, bool endTimer )
{
    QDateTime now = QDateTime::currentDateTime();

    QDateTime timebegin, timeend;
    getVotingTimeWindow( event->getId(), timebegin, timeend );

    qint64 intvstart = now.msecsTo( timebegin );
    qint64 intvend   = now.msecsTo( timeend );

    // minimum distance to start of event
    const qint64 TIMER_THRESHOLD_START    = 10000;
    // minimum duration (end time - start time)
    const qint64 TIMER_THRESHOLD_DURATION = 60000;

    if ( startTimer )
    {
        // first delete the old timer if any exist
        QString alarmname = PREFIX_TIMER_START + event->getId();
        if ( _alarms.contains( alarmname ) )
        {
            _alarms.value( alarmname )->deleteLater();
            _alarms.remove( alarmname );
        }

        if ( intvstart > TIMER_THRESHOLD_START )
        {
            setupVotingTimer( event, intvstart, true );
        }
    }

    if ( endTimer )
    {
        // first delete the old timer if any exist
        QString alarmname = PREFIX_TIMER_END + event->getId();
        if ( _alarms.contains( alarmname ) )
        {
            _alarms.value( alarmname )->deleteLater();
            _alarms.remove( alarmname );
        }

        if ( ( ( intvend - intvstart ) > TIMER_THRESHOLD_DURATION ) && ( intvend > TIMER_THRESHOLD_START ) )
        {
            setupVotingTimer( event, intvend, false );
        }
    }
}

void Events::setupVotingTimer( ModelEventPtr event, quint64 interval, bool startTimer )
{
    if ( interval <= 0 )
    {
        log_error << TAG << "cannot setup timer, invalid interval!" << std::endl;
        return;
    }

    QTimer* p_timer = new QTimer( this );
    p_timer->setSingleShot( true );
    p_timer->setInterval( interval );
    p_timer->setProperty( "id", QVariant( event->getId() ) );
    p_timer->start();

    if ( startTimer )
        connect( p_timer, SIGNAL( timeout() ), this, SLOT( onAlarmVotingStart() ) );
    else
        connect( p_timer, SIGNAL( timeout() ), this, SLOT( onAlarmVotingEnd() ) );

    QString alarmname = ( startTimer ? PREFIX_TIMER_START : PREFIX_TIMER_END ) + event->getId();
    _alarms.insert( alarmname, p_timer );
}

} // namespace event
} // namespace m4e
