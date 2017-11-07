/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef MODELEVENT_H
#define MODELEVENT_H

#include <configuration.h>
#include <core/smartptr.h>
#include <common/modelbase.h>
#include <user/modeluserinfo.h>
#include <event/modellocation.h>
#include <QDateTime>
#include <QString>
#include <QList>
#include <QTime>


namespace m4e
{
namespace event
{

/**
 * @brief A class for defining an event
 *
 * @author boto
 * @date Sep 8, 2017
 */
class ModelEvent : public common::ModelBase, public m4e::core::RefCount< ModelEvent >
{
    SMARTPTR_DEFAULTS( ModelEvent )

    public:

        /**
         * @brief Week days used for repetetion settings.
         */
        enum WeekDays
        {
            WeekDayMonday    = 0x01,
            WeekDayTuesday   = 0x02,
            WeekDayWednesday = 0x04,
            WeekDayThursday  = 0x08,
            WeekDayFriday    = 0x10,
            WeekDaySaturday  = 0x20,
            WeekDaySunday    = 0x40
        };

        /**
         * @brief Construct an instance.
         */
                                            ModelEvent() {}

        /**
         * @brief Is the event public or private?
         *
         * @return Return true if the event is public, otherwise return false.
         */
        bool                                getIsPublic() const { return _isPublic; }

        /**
         * @brief Set the event to be a public or private event.
         *
         * @param isPublic Public or private flag
         */
        void                                setIsPublic( bool isPublic ) { _isPublic = isPublic; }

        /**
         * @brief Get event's start date.
         *
         * @return Event start date
         */
        const QDateTime&                    getStartDate() const { return _startDate; }

        /**
         * @brief Set event's start date.
         *
         * @param startDate Event's star date
         */
        void                                setStartDate( const QDateTime& startDate ) { _startDate = startDate; }

        /**
         * @brief Is the event repeated?
         *
         * @return Return true if this is a repeating event, otherwise return false;
         */
        bool                                isRepeated() const { return _repeatWeekDays != 0; }

        /**
         * @brief Get repetetion's day time, only valid for repeated events.
         *
         * @return Repetetion's day time
         */
        const QTime&                        getRepeatDayTime() const { return _repeatDayTime; }

        /**
         * @brief Set repetetion's day time.
         *
         * @param repeatDayTime Repetetion's day time.
         */
        void                                setRepeatDayTime( const QTime& repeatDayTime ) { _repeatDayTime = repeatDayTime; }

        /**
         * @brief Get the alarm time, a time offset before the event takes place.
         *
         * @return Alarm time
         */
        const QDateTime&                    getAlarmTime() const { return _alarmTime; }

        /**
         * @brief Set the alarm time.
         *
         * @param alarmTime Alarm time
         */
        void                                setAlarmTime( const QDateTime& alarmTime ) { _alarmTime = alarmTime; }

        /**
         * @brief Repetetion's week days if this is a repeated event.
         *
         * @return Repetetion's week days, a combination of enum WeekDays
         */
        unsigned int                        getRepeatWeekDays() const { return _repeatWeekDays; }

        /**
         * @brief If the event should be repeated then set the week days for the repetetion.
         *
         * @param repeatWeekDays Repetetion's week days, a combination of enum WeekDays
         */
        void                                setRepeatWeekDays( unsigned int repeatWeekDays ) { _repeatWeekDays = repeatWeekDays; }

        /**
         * @brief Get all locations of this event.
         *
         *        NOTE: The locations may not be loaded (lazy loading), but their IDs are available, see method getLocationIDs.
         *
         * @return Return all locations.
         */
        const QList< ModelLocationPtr >&    getLocations() const {  return _locations; }

        /**
         * @brief Set all locations of this event.
         *
         * @param locations   Locations of this event
         */
        void                                setLocations( const QList< ModelLocationPtr >& locations ) { _locations = locations; }

        /**
         * @brief Get the location with given ID.
         *
         * @param id        Location ID
         * @return Return the location, or an invalid object if the location was not found (use smart pointer's 'valid()' on returned object to check).
         */
        ModelLocationPtr                    getLocation( const QString &id );

        /**
         * @brief Remove a location given its ID.
         *
         * @param id Location ID
         * @return   true if sccessfully removed, false if the ID was not found.
         */
        bool                                removeLocation( const QString &id );

        /**
         * @brief Add a new location to event or modify an existing location.
         *
         * @param location Location to add or modify
         * @return   Return true if a new location was added or false if an existing location was updated.
         */
        bool                                addOrUpdateLocation( ModelLocationPtr location );

        /**
         * @brief Get the event owner.
         *
         * NOTE: the returned user model does not contain the complete user data.
         *
         * @return Event owner
         */
        user::ModelUserInfoPtr              getOwner() { return _owner; }

        /**
         * @brief Set the evetn owner.
         *
         * @param owner Event owner
         */
        void                                setOwner( user::ModelUserInfoPtr owner ) { _owner = owner; }

        /**
         * @brief Get all event members.
         *
         * NOTE: the member models do not contain complete user data.
         *
         * @return List of event members
         */
        QList< user::ModelUserInfoPtr >     getMembers() { return _members; }

        /**
         * @brief Set the event members.
         *
         * @param members List of event members
         */
        void                                setMembers( const QList< user::ModelUserInfoPtr >& members ) { _members = members; }

        /**
         * @brief Comparison operator which considers the event ID.
         *
         * @param right     Right hand of operation.
         * @return Return true if both events have the same ID, otherwise false.
         */
        bool                                operator == ( const ModelEvent& right ) { return _id == right.getId(); }

        /**
         * @brief Unequal operator which considers the event ID.
         *
         * @param right     Right hand of operation.
         * @return true if both events have the same ID, otherwise false.
         */
        bool                                operator != ( const ModelEvent& right ) { return _id != right.getId(); }

    protected:

        bool                                _isPublic = false;
        QDateTime                           _startDate;
        QTime                               _repeatDayTime;
        QDateTime                           _alarmTime;
        unsigned int                        _repeatWeekDays = 0;
        QList< ModelLocationPtr >           _locations;
        user::ModelUserInfoPtr              _owner;
        QList< user::ModelUserInfoPtr >     _members;
};

typedef m4e::core::SmartPtr< ModelEvent > ModelEventPtr;

} // namespace event
} // namespace m4e

Q_DECLARE_METATYPE( m4e::event::ModelEventPtr )

#endif // MODELEVENT_H
