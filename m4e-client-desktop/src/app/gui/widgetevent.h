/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef WIDGETEVENT_H
#define WIDGETEVENT_H

#include <data/webapp.h>
#include <data/modelevent.h>
#include <data/modellocation.h>
#include <QListWidget>
#include <QWidget>
#include <QLabel>
#include <QMap>


namespace Ui
{
    class WidgetEvent;
}


namespace m4e
{
namespace ui
{

/**
 * @brief Class for decorating the "Event" widget.
 *
 * @author boto
 * @date Aug 2, 2017
 */
class WidgetEvent : public QWidget
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(WidgetEvent) ";

    Q_OBJECT

    public:

        /**
         * @brief Create a new "Event" widget
         * @param p_webApp          Web application interface
         * @param p_parent          Parent widget
         */
                                    WidgetEvent( m4e::data::WebApp* p_webApp, QWidget* p_parent = nullptr );

        /**
         * @brief Destroy WidgetEvent
         */
        virtual                     ~WidgetEvent();

        /**
         * @brief Set the event which is represented by this widget.
         * @param id    Event ID
         */
        void                        setEvent( const QString& id );

    protected slots:

        void                        onButtonBuzzClicked();

    signals:

        /**
         * @brief This signal is emitted when the widget's back button was pressed.
         */
        void                        onWidgetBack();

    protected:

        /**
         * @brief Setup the widget.
         */
        void                        setupUI();

        /**
         * @brief Setup the head elements in event widget (info fields, etc.)
         * @param event   The event
         */
        void                        setupWidgetHead( m4e::data::ModelEventPtr event );

        /**
         * @brief Setup a widget informing that the event has no location.
         */
        void                        setupNoLocationWidget();

        /**
         * @brief Add a new location for an event
         *
         * @param location  New location to add
         */
        void                        addLocation( data::ModelLocationPtr location );

        Ui::WidgetEvent*            _p_ui           = nullptr;

        QListWidget*                _p_clientArea   = nullptr;

        QLabel*                     _p_labelFooter  = nullptr;

        m4e::data::WebApp*          _p_webApp       = nullptr;

        typedef QMap< QString /*id*/, QString /*name*/>  Locations;

        QString                     _eventId;

        Locations                   _locations;
};

} // namespace ui
} // namespace m4e

#endif // WIDGETEVENT_H
