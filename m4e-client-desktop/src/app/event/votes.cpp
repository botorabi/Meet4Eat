/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "votes.h"
#include <core/log.h>


namespace m4e
{
namespace event
{

//! Comparator class used for soring votes by their end of voting time
class VoteTimeComparator
{
    public:

        VoteTimeComparator( bool descending ) : _descending( descending ) {}

        bool operator()( ModelLocationVotesPtr a, ModelLocationVotesPtr b ) const
        {
            bool res;
            if ( _descending )
                res = a->getVoteTimeEnd().toSecsSinceEpoch() > b->getVoteTimeEnd().toSecsSinceEpoch();
            else
                res = a->getVoteTimeEnd().toSecsSinceEpoch() < b->getVoteTimeEnd().toSecsSinceEpoch();
            return res;
        }

    protected:

        bool    _descending;
};

//! Comparator class used for soring votes by their count of voted users
class CountVotesComparator
{
    public:

        CountVotesComparator( bool descending ) : _descending( descending ) {}

        bool operator()( ModelLocationVotesPtr a, ModelLocationVotesPtr b ) const
        {
            bool res;
            if ( _descending )
                res = a->getUserNames().size() > b->getUserNames().size();
            else
                res = a->getUserNames().size() < b->getUserNames().size();
            return res;
        }

    protected:

        bool    _descending;
};


Votes::Votes()
{
}

Votes::~Votes()
{
}

QMap< qint64, QList< ModelLocationVotesPtr > > Votes::groupByTime( const QList< ModelLocationVotesPtr >& votes )
{
    // group all events by end of voting time
    QMap< qint64 /*sec votes end*/, QList< ModelLocationVotesPtr > > votingtime;
    for ( ModelLocationVotesPtr v: votes )
    {
        qint64 vt = v->getVoteTimeEnd().toSecsSinceEpoch();

        if ( !votingtime.contains( vt ) )
            votingtime.insert( vt, QList< ModelLocationVotesPtr >() );

        votingtime[ vt ].append( v );
    }

    return votingtime;
}

QList< ModelLocationVotesPtr > Votes::sortByVoteCount( const QList< ModelLocationVotesPtr >& votes, bool descending )
{
    QList< ModelLocationVotesPtr > sortedvotes = votes;
    std::sort( sortedvotes.begin(), sortedvotes.end(), CountVotesComparator( descending ) );
    return sortedvotes;
}

QList< ModelLocationVotesPtr > Votes::sortByTime( const QList< ModelLocationVotesPtr >& votes, bool descending )
{
    QList< ModelLocationVotesPtr > sortedvotes = votes;
    std::sort( sortedvotes.begin(), sortedvotes.end(), VoteTimeComparator( descending ) );
    return sortedvotes;
}

QList< QList< ModelLocationVotesPtr > > Votes::sortByTimeAndVoteCount( const QList<ModelLocationVotesPtr>& votes, bool timeDescending, bool votesCountDescending )
{
    QList< QList< ModelLocationVotesPtr > > sortedvotes;

    // group the votes by their "end of votes" time
    QMap< qint64, QList< ModelLocationVotesPtr > > timegroups = groupByTime( votes );

    QList< qint64 > times = timegroups.keys();
    if ( timeDescending )
        std::sort( times.begin(), times.end(), std::greater< qint64 >() );
    else
        std::sort( times.begin(), times.end(), std::less< qint64 >() );

    for ( qint64 t: times )
    {
        QList< ModelLocationVotesPtr > votesgroup = timegroups.value( t );
        std::sort( votesgroup.begin(), votesgroup.end(), CountVotesComparator( votesCountDescending ) );
        sortedvotes.append( votesgroup );
    }

    return sortedvotes;
}

} // namespace event
} // namespace m4e
