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
#include <data/webapp.h>
#include <QListWidget>
#include <QWidget>
#include <QMap>


namespace m4e
{
namespace ui
{

class WidgetEventItem;


/**
 * @brief Class representing an event list.
 *
 * @author boto
 * @date Aug 2, 2017
 */
class WidgetEventList : public QWidget
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(WidgetMyEvents) ";

    Q_OBJECT

    public:

        /**
         * @brief Create a new event list widget
         *
         * @param p_webApp  Web application interface
         * @param p_parent  Parent widget
         */
                                    WidgetEventList( m4e::data::WebApp* p_webApp, QWidget* p_parent = nullptr );

    signals:

        void                        onEventSelection( QString id );

    protected slots:

        /**
         * @brief Received when the user clicks on the info label widget.
         *
         * @param id   ID
         */
        void                        onClicked( QString id );

    protected:

        /**
         * @brief Setup the widget.
         */
        void                        setupUI();

        /**
         * @brief Add a new group to widget
         *
         * @param event       Event model
         */
        void                        addEvent( m4e::data::ModelEventPtr event );

        m4e::data::WebApp*          _p_webApp     = nullptr;

        QList< WidgetEventItem* >   _widgets;
};

} // namespace ui
} // namespace m4e

#endif // WIDGETEVENTLIST_H
