/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef WIDGETEVENTITEM_H
#define WIDGETEVENTITEM_H

#include <configuration.h>
#include <webapp/webapp.h>
#include <event/modelevent.h>
#include <QWidget>
#include <QLabel>


namespace Ui {
    class WidgetEventItem;
}

namespace m4e
{
namespace event
{

/**
 * @brief Widget class for an event item
 *
 * @author boto
 * @date Sep 16, 2017
 */
class WidgetEventItem : public QWidget
{
    Q_OBJECT

    public:

        /**
         * @brief Create a new event item widget
         *
         * @param p_webApp  Web application interface
         * @param p_parent  Parent widget
         */
        explicit                    WidgetEventItem( webapp::WebApp* p_webApp, QWidget* p_parent = nullptr );

        /**
         * @brief Destroy the instance.
         */
        virtual                     ~WidgetEventItem();

        /**
         * @brief Setup the widget.
         */
        void                        setupUI( event::ModelEventPtr event );

        /**
         * @brief Get the ID which was defined on setup.
         *
         * @return ID
         */
        const QString&              getId() const { return _event->getId(); }

        /**
         * @brief Set the box selection mode to normal or selected.
         *
         * @param normal Pass true for normal, false for selection
         */
        void                        setSelectionMode( bool normal );

    signals:

        /**
         * @brief Emitted when the user clicks on the widget.
         *
         * @param id   The id which was used for setup
         */
        void                        onClicked( QString id );

    protected slots:

        void                        onBtnOptionsClicked();

        void                        onBtnNewLocationClicked();

        /**
         * @brief This signal is received from webapp when a requested document was arrived.
         *
         * @param document   Document
         */
        void                        onDocumentReady( m4e::doc::ModelDocumentPtr document );

    protected:

        bool                        eventFilter( QObject* p_obj, QEvent* p_event );

        webapp::WebApp*             _p_webApp = nullptr;

        Ui::WidgetEventItem*        _p_ui     = nullptr;

        event::ModelEventPtr        _event;

        bool                        _userIsOwner = false;
};

} // namespace event
} // namespace m4e

#endif // WIDGETEVENTITEM_H
