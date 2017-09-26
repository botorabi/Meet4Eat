/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef EVENTS_H
#define EVENTS_H

#include <configuration.h>
#include <core/smartptr.h>
#include <data/modelevent.h>
#include <QString>
#include <QList>

namespace m4e
{
namespace data
{

/**
 * @brief This class holds and manages events.
 *
 * @author boto
 * @date Sep 9, 2017
 */
class Events: public m4e::core::RefCount< Events >
{
    DECLARE_SMARTPTR_ACCESS( Events )

    public:

        /**
         * @brief Construct an instance.
         */
                                        Events();

        /**
         * @brief Add a new event.
         *
         * @param event Event to add
         */
        void                            addEvent( ModelEventPtr event ) { _events.append( event); }

        /**
         * @brief Update an existing event.
         *
         * @param event Event to be updated
         * @return false if the event ID was not found, otherwise true
         */
        bool                            updateEvent( ModelEventPtr event );

        /**
         * @brief Get an event given its ID.
         *
         * @param eventId   Event ID
         * @return Return the event, or an invalid object if the event was not found (use smart pointer's 'valid()' on returned object to check).
         */
        ModelEventPtr                   getEvent( const QString& eventId );

        /**
         * @brief Get all events.
         *
         * @return All events
         */
        const QList< ModelEventPtr >&   getAllEvents() { return _events; }

        /**
         * @brief Set all events.
         *
         * @param events All events
         */
        void                            setAllEvents( const QList< ModelEventPtr >& events ) { _events = events; }

        /**
         * @brief Remove an event given its ID.
         *
         * @param id    Event's unique ID
         * @return false if the ID could not be found, otherwise true.
         */
        bool                            removeEvent( const QString& id );

        /**
         * @brief Remove all events.
         */
        void                            removeAllEvents() { _events.clear(); }

    protected:

        virtual                         ~Events() {}

        //! Omit copy construction!
                                        Events( const Events& );

        QList< ModelEventPtr >          _events;
};

typedef m4e::core::SmartPtr< Events > EventsPtr;

} // namespace data
} // namespace m4e

#endif // EVENTS_H
