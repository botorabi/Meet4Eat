/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef WIDGETLOCATION_H
#define WIDGETLOCATION_H
#include <configuration.h>
#include <data/webapp.h>
#include <data/modellocation.h>
#include <QWidget>
#include <QLabel>


namespace Ui {
    class WidgetLocation;
}

namespace m4e
{
namespace ui
{

/**
 * @brief Widget class for an event location
 *
 * @author boto
 * @date Sep 19, 2017
 */
class WidgetLocation : public QWidget
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(WidgetLocation) ";

    Q_OBJECT

    public:

        /**
         * @brief Create a new event location widget
         *
         * @param p_webApp  Web application interface
         * @param p_parent  Parent widget
         */
        explicit                    WidgetLocation( m4e::data::WebApp* p_webApp, QWidget* p_parent = nullptr );

        /**
         * @brief Destroy the instance.
         */
        virtual                     ~WidgetLocation();

        /**
         * @brief Setup the widget.
         */
        void                        setupUI( data::ModelLocationPtr location );

        /**
         * @brief Get the ID which was defined on setup.
         *
         * @return ID
         */
        const QString&              getId() const { return _location->getId(); }

    signals:

        /**
         * @brief Emitted when the user clicks on the widget.
         *
         * @param id   The id which was used for setup
         */
        void                        onClicked( QString id );

    protected slots:

        void                        onBtnSettingsClicked();

        void                        onBtnInfoClicked();

        void                        onBtnVoteUpClicked();

        void                        onBtnVoteDownClicked();

        /**
         * @brief This signal is received from webapp when a requested document was arrived.
         *
         * @param document   Document
         */
        void                        onDocumentReady( m4e::data::ModelDocumentPtr document );

    protected:

        bool                        eventFilter( QObject* p_obj, QEvent* p_event );

        m4e::data::WebApp*          _p_webApp = nullptr;

        Ui::WidgetLocation*         _p_ui     = nullptr;

        data::ModelLocationPtr      _location;
};

} // namespace ui
} // namespace m4e

#endif // WIDGETLOCATION_H
