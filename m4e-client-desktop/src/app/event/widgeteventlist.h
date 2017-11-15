/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */


#ifndef WIDGETEVENTLIST_H
#define WIDGETEVENTLIST_H

#include <configuration.h>
#include <webapp/webapp.h>
#include <QListWidget>
#include <QMap>


namespace m4e
{
namespace event
{

class WidgetEventItem;


/**
 * @brief Class representing an event list.
 *
 * @author boto
 * @date Aug 2, 2017
 */
class WidgetEventList : public QListWidget
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(WidgetEventList) ";

    Q_OBJECT

    public:

        /**
         * @brief Create a new event list widget
         *
         * @param p_webApp  Web application interface
         * @param p_parent  Parent widget
         */
                                    WidgetEventList( webapp::WebApp* p_webApp, QWidget* p_parent = nullptr );

        /**
         * @brief Visually select the event widget with given event ID. If the ID was not found then try to select the first event.
         *
         * @param eventId  Event ID
         */
        void                        selectEvent( const QString& eventId );

        /**
         * @brief Visually select the first event.
         */
        void                        selectFirstEvent();

        /**
         * @brief Start creating a new event location.
         *
         * @param eventId  ID of the event which should get a new location.
         */
        void                        createNewLocation( const QString& eventId );

    signals:

        void                        onEventSelection( QString id );

    protected slots:

        /**
         * @brief Received when the user clicks on the info label widget.
         *
         * @param id   ID
         */
        void                        onClicked( QString id );

        /**
         * @brief This signal is used for updating the event data from app server.
         *
         * @param id   The event ID
         */
        void                        onRequestUpdateEvent( QString id );

        /**
         * @brief This signal is used if the event should be deleted.
         *
         * @param id    The event ID
         */
        void                        onRequestDeleteEvent( QString id );

        /**
         * @brief This signal is emitted by Events and brings fresh event data.
         *
         * @param success  true if user events could successfully be retrieved, otherwise false
         * @param event    User event
         */
        void                        onResponseGetEvent( bool success, m4e::event::ModelEventPtr event );

        /**
         * @brief This signal is emitted by Events and delivers the results of deleting a user event request.
         *
         * @param success  true if user event could successfully be retrieved, otherwise false
         * @param eventId  User event which was deleted
         */
        void                        onResponseDeleteEvent( bool success, QString eventId );

    protected:

        /**
         * @brief Setup the widget.
         */
        void                        setupUI();

        void                        setupListView();

        /**
         * @brief Add a new event to widget
         *
         * @param event       Event model
         */
        void                        addEvent( m4e::event::ModelEventPtr event );

        /**
         * @brief Given an event ID try to find its list item.
         *
         * @param eventId   The event ID
         * @return          List item, or null if the ID was not found in current list item.
         */
        WidgetEventItem*            findEventItem( const QString& eventId );

        webapp::WebApp*             _p_webApp     = nullptr;

        QList< WidgetEventItem* >   _widgets;
};

} // namespace event
} // namespace m4e

#endif // WIDGETEVENTLIST_H
