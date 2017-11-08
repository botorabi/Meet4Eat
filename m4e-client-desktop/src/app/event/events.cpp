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
            updateAlarms();
            break;
        }
    }
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
        if ( event->getAlarmOffset() == 0 )
            continue;

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
    qint64 alarmoffset = event->getAlarmOffset() * 1000;
    // check if an alarm is enabled at all
    if ( alarmoffset == 0 )
        return false;

    // there is a difference between repeated events and one-shot events
    qint64 msec2alarm;
    if ( event->isRepeated() )
    {
        qint64 currtime  = QTime::currentTime().msecsSinceStartOfDay();
        qint64 alarmtime = event->getRepeatDayTime().msecsSinceStartOfDay() - alarmoffset;
        msec2alarm = alarmtime - currtime;
        // missed the time for today? do we have to schedule for next day?
        if ( msec2alarm < 0 )
            msec2alarm += ( 24 * 60 * 60 * 1000 );
    }
    else
    {
        qint64 currtime  = QDateTime::currentMSecsSinceEpoch();
        qint64 alarmtime = event->getStartDate().toMSecsSinceEpoch() - alarmoffset;
        msec2alarm = alarmtime - currtime;
        // the event date&time is in the past, no need for alarm anymore
        if ( msec2alarm < 0 )
            return false;
    }

    p_timer->stop();
    p_timer->setSingleShot( true );
    p_timer->setInterval( msec2alarm );
    p_timer->setProperty( "id", QVariant( event->getId() ) );
    p_timer->start();
    return true;
}

} // namespace event
} // namespace m4e
