/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef VOTES_H
#define VOTES_H

#include <configuration.h>
#include <event/modellocationvotes.h>


namespace m4e
{
namespace event
{

/**
 * @brief A colleciton of location votes utilities
 *
 * @author boto
 * @date Nov 29, 2017
 */
class Votes : public QObject
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(Votes) ";

    Q_OBJECT

    public:

        /**
         * @brief Construct a Votes instance.
         */
                                            Votes();

        /**
         * @brief Destruct Votes instance
         */
        virtual                             ~Votes();

        /**
         * @brief Group the given votes by time.
         *
         * @param votes Votes to group
         * @return A map containing timestamps (seconds since epoch) and the corresponding votes.
         */
        QMap< qint64, QList< ModelLocationVotesPtr > > groupByTime( const QList< ModelLocationVotesPtr >& votes );

        /**
         * @brief Sort the given votes by their count of voters.
         *
         * @param votes         The votes to sort
         * @param descending    Pass true by sorting in descending order, otherwise the votes are sorted in ascending order.
         * @return              Return the sorted list of votes
         */
        QList< ModelLocationVotesPtr >                  sortByVoteCount( const QList< ModelLocationVotesPtr >& votes, bool descending = true );

        /**
         * @brief Sort the given votes by their voting time.
         *
         * @param votes         The votes to sort
         * @param descending    Pass true by sorting in descending order, otherwise the votes are sorted in ascending order.
         * @return              Return the sorted list of votes
         */
        QList< ModelLocationVotesPtr >                  sortByTime( const QList< ModelLocationVotesPtr >& votes, bool descending = true );

        /**
         * @brief Sort the given votes by their voting time and count of voters.
         *
         * @param votes                 The votes to sort
         * @param timeDescending        Pass true by sorting by time in descending order, otherwise the votes are sorted in ascending order.
         * @param votesCountDescending  Pass true by sorting by votes count in descending order, otherwise the votes are sorted in ascending order.
         * @return                      Return the sorted list of votes
         */
        QList< QList< ModelLocationVotesPtr > >         sortByTimeAndVoteCount( const QList< ModelLocationVotesPtr >& votes, bool timeDescending = true, bool votesCountDescending = true );
};

} // namespace event
} // namespace m4e

#endif // VOTES_H
