/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef MODELLOCATIONVOTES_H
#define MODELLOCATIONVOTES_H

#include <configuration.h>
#include <core/smartptr.h>
#include <common/modelbase.h>
#include <QDateTime>
#include <QString>
#include <QList>


namespace m4e
{
namespace event
{

/**
 * @brief Class for holding location votes
 *
 * @author boto
 * @date Nov 13, 2017
 */
class ModelLocationVotes : public m4e::core::RefCount< ModelLocationVotes >
{
    SMARTPTR_DEFAULTS( ModelLocationVotes )

    public:

        /**
         * @brief Construct an instance.
         */
                                        ModelLocationVotes() {}

        /**
         * @brief Get the unique ID.
         *
         * @return The unique ID
         */
        const QString&                  getId() const { return _id; }

        /**
         * @brief Set the unique ID.
         *
         * @param id  The unique ID
         */
        void                            setId( const QString& id ) { _id = id; }

        /**
         * @brief Get the event ID.
         *
         * @return The event ID
         */
        const QString&                  getEventId() const { return _eventId; }

        /**
         * @brief Set the event ID.
         *
         * @param eventId  The event ID
         */
        void                            setEventId( const QString& eventId ) { _eventId = eventId; }

        /**
         * @brief Get the location ID.
         *
         * @return The location ID
         */
        const QString&                  getLocationId() const { return _locationId; }

        /**
         * @brief Set the location ID.
         *
         * @param locationId  The location ID
         */
        void                            setLocationId( const QString& locationId ) { _locationId = locationId; }

        /**
         * @brief Get the location name.
         *
         * @return The location name
         */
        const QString&                  getLocationName() const { return _locationName; }

        /**
         * @brief Set the location name.
         *
         * @param locationName  The location name
         */
        void                            setLocationName( const QString& locationName ) { _locationName = locationName; }

        /**
         * @brief Get IDs of users who voted for this event location.
         *
         * @return IDs of users voted for this location
         */
        const QList< QString >&         getUserIds() const { return _userIds; }

        /**
         * @brief Set IDs of users who voted for this event location.
         *
         * @param userIds IDs of sers voted for this location
         */
        void                            setUserIds( const QList< QString >& userIds ) { _userIds = userIds; }

        /**
         * @brief Get names of users who voted for this event location.
         *
         * @return Names of users voted for this location
         */
        const QList< QString >&         getUserNames() const { return _userNames; }

        /**
         * @brief Set names of users who voted for this event location.
         *
         * @param userNames Names of sers voted for this location
         */
        void                            setUserNames( const QList< QString >& userNames ) { _userNames = userNames; }

        /**
         * @brief Get the begin of voting time window.
         *
         * @return Begin of voting time
         */
        const QDateTime&                getVoteTimeBegin() const { return _voteTimeBegin; }

        /**
         * @brief Set the begin of voting time window.
         *
         * @param timeBegin Begin of voting time
         */
        void                            setVoteTimeBegin( const QDateTime& timeBegin ) { _voteTimeBegin = timeBegin; }

        /**
         * @brief Get the end of voting time window.
         *
         * @return End of voting time
         */
        const QDateTime&                getVoteTimeEnd() const { return _voteTimeEnd; }

        /**
         * @brief Set the end of voting time window.
         *
         * @param timeEnd End of voting time
         */
        void                            setVoteTimeEnd( const QDateTime& timeEnd ) { _voteTimeEnd = timeEnd; }

        /**
         * @brief Get the time of voting entry creation.
         *
         * @return Time of voting entry creation
         */
        const QDateTime&                getVoteCreationTime() const { return _creationTime; }

        /**
         * @brief Set the time of voting entry creation.
         *
         * @param creationTime Time of voting entry creation
         */
        void                            setVoteCreationTime( const QDateTime& creationTime ) { _creationTime = creationTime; }

        /**
         * @brief Setup the location votes given a JSON formatted string.
         *
         * @param input Input string in JSON format
         * @return Return false if the input was not in proper format.
         */
        bool                            fromJSON( const QString& input );

        /**
         * @brief Setup the location votes given a JSON document.
         *
         * @param input Input in JSON document
         * @return Return false if the input was not in proper format.
         */
        bool                            fromJSON( const QJsonDocument& input );

    protected:

        QString                         _id;
        QString                         _eventId;
        QString                         _locationId;
        QString                         _locationName;
        QDateTime                       _voteTimeBegin;
        QDateTime                       _voteTimeEnd;
        QDateTime                       _creationTime;
        QList< QString >                _userIds;
        QList< QString >                _userNames;
};

typedef m4e::core::SmartPtr< ModelLocationVotes > ModelLocationVotesPtr;

} // namespace event
} // namespace m4e

Q_DECLARE_METATYPE( m4e::event::ModelLocationVotesPtr )

#endif // MODELLOCATIONVOTES_H
