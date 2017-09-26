/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "testevents.h"
#include <data/events.h>


void TestEvents::initTestCase()
{
}

void TestEvents::cleanupTestCase()
{
}

void TestEvents::testLocation()
{
    QList< QString > votes;
    votes.append( "User A" );
    votes.append( "User B" );

    m4e::data::ModelLocationPtr loc1 = new m4e::data::ModelLocation();
    m4e::data::ModelLocationPtr loc2 = new m4e::data::ModelLocation();

    loc1->setId( "AA" );
    loc1->setName( "Location1" );
    loc1->setVotedMembers( votes );
    loc1->setPhotoId( "photoid" );
    loc1->setPhotoETag( "photoetag" );

    QVERIFY2( loc1->getId() == "AA", "getId() failed" );
    QVERIFY2( loc1->getName() == "Location1", "getName() failed" );
    QVERIFY2( loc1->getPhotoId() == "photoid", "getPhotoId() failed" );
    QVERIFY2( loc1->getPhotoETag() == "photoetag", "getPhotoETag() failed" );
    QVERIFY2( loc1->getVotedMembers() == votes, "getVotedMembers() failed" );

    *loc2 = *loc1;
    QVERIFY2( *loc1 == *loc2, "comparison operator failed" );
    loc2->setId( "BB" );
    QVERIFY2( *loc1 != *loc2, "comparison operator failed" );

    votes.append( "User C" );
    loc2->setVotedMembers( votes );
    QVERIFY2( loc1->getVotedMembers() != loc2->getVotedMembers(), "getVotedMembers() failed" );
}

void TestEvents::testEvent()
{
    m4e::data::ModelLocationPtr loc1 = new m4e::data::ModelLocation();
    loc1->setId( "AA" );
    loc1->setName( "Location1" );

    QList< m4e::data::ModelLocationPtr > locations;
    locations.append( loc1 );
    loc1->setId( "BB" );
    loc1->setName( "Location2" );
    locations.append( loc1 );

    m4e::data::ModelEventPtr event1 = new m4e::data::ModelEvent();
    m4e::data::ModelEventPtr event2 = new m4e::data::ModelEvent();
    event1->setId( "AA" );
    event1->setName( "Event1" );
    event1->setDescription( "Description" );
    event1->setLocations( locations );
    event1->setPhotoId( "photoid" );
    event1->setPhotoETag( "photoetag" );

    m4e::data::ModelLocationPtr dummylocation = new m4e::data::ModelLocation();
    dummylocation = event1->getLocation( "BB" );
    QVERIFY2( dummylocation.valid(), "getLocations() failed" );
    QVERIFY2( ( dummylocation->getId() == "BB" ) && ( dummylocation->getName() == "Location2" ), "getLocations() failed, invalid content" );

    QVERIFY2( event1->getId() == "AA", "getId() failed" );
    QVERIFY2( event1->getName() == "Event1", "getName() failed" );
    QVERIFY2( event1->getDescription() == "Description", "getDescription() failed" );
    QVERIFY2( event1->getPhotoId() == "photoid", "getPhotoId() failed" );
    QVERIFY2( event1->getPhotoETag() == "photoetag", "getPhotoETag() failed" );

    *event2 = *event1;
    QVERIFY2( *event1 == *event2, "comparison operator failed" );
    event2->setId( "BB" );
    QVERIFY2( *event1 != *event2, "comparison operator failed" );
}

void TestEvents::testManipulateEvent()
{
    m4e::data::ModelLocationPtr loc1 = new m4e::data::ModelLocation();
    loc1->setId( "AA" );
    loc1->setName( "Location1" );

    QList< m4e::data::ModelLocationPtr > locations;
    locations.append( loc1 );
    loc1->setId( "BB" );
    loc1->setName( "Location2" );
    locations.append( loc1 );

    m4e::data::ModelEventPtr event1 = new m4e::data::ModelEvent();
    m4e::data::ModelEventPtr event2 = new m4e::data::ModelEvent();

    event1->setId( "AA" );
    event1->setName( "Event1" );
    event1->setLocations( locations );

    event2->setId( "BB" );
    event2->setName( "Group2" );

    m4e::data::EventsPtr events = new m4e::data::Events();
    events->addEvent( event1 );
    events->addEvent( event2 );

    QList< m4e::data::ModelEventPtr > evs = events->getAllEvents();

    QVERIFY2( evs.size() == 2, "adding or retrieving events failed" );
    QVERIFY( evs.at( 0 )->getId() == "AA" );
    QVERIFY( evs.at( 1 )->getId() == "BB" );
    events->removeAllEvents();
    QVERIFY2( events->getAllEvents().size() == 0, "could not remove all events" );

    events->addEvent( event1 );
    events->addEvent( event2 );

    QVERIFY2( events->removeEvent( "CC" ) == false, "removing event failed" );
    QVERIFY2( events->removeEvent( "AA" ) == true, "removing event failed" );
    QVERIFY2( events->getAllEvents().size() == 1, QString( "wrong event count: " + QString::number( evs.size() ) ).toStdString().c_str() );
    QVERIFY2( events->removeEvent( "BB" ) == true, "removing event failed" );
    QVERIFY2( events->getAllEvents().size() == 0, QString( "wrong event count: " + QString::number( evs.size() ) ).toStdString().c_str() );
}
