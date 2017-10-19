/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef DIALOGLOCATIONCREATE_H
#define DIALOGLOCATIONCREATE_H

#include <configuration.h>
#include <common/basedialog.h>
#include <event/modellocation.h>
#include <webapp/webapp.h>


namespace Ui {
  class WidgetLocationCreate;
}

namespace m4e
{
namespace event
{

/**
 * @brief A dialog for creating new event locations.
 *
 * @author boto
 * @date Oct 11, 2017
 */
class DialogLocationCreate : public common::BaseDialog
{
    Q_OBJECT

    public:


        /**
         * @brief Create a dialog instance.
         *
         * @param p_webApp  Web application interface
         * @param p_parent  Parent widget
         */
                                    DialogLocationCreate( webapp::WebApp* p_webApp, QWidget* p_parent );

        /**
         * @brief Destroy the instance.
         */
        virtual                     ~DialogLocationCreate();

        /**
         * @brief Setup the dialog.
         *
         * @param event  The event which should get a new location
         */
        void                        setupUI( event::ModelEventPtr event );

    protected slots:

        /**
         * @brief This signal notifies about he results of adding a new location.
         *
         * @param success    true if user data could successfully be retrieved, otherwise false
         * @param eventId    ID of event
         * @param locationId ID of location to remove
         */
        void                        onResponseAddLocation( bool success, QString eventId, QString locationId );

    protected:

        virtual bool                onButton1Clicked();

        void                        resetDialog();

        Ui::WidgetLocationCreate*   _p_ui     = nullptr;

        webapp::WebApp*             _p_webApp = nullptr;

        event::ModelEventPtr        _event;

        event::ModelLocationPtr     _location;

        QPixmap                     _defaultPhoto;
};

} // namespace event
} // namespace m4e

#endif // DIALOGLOCATIONCREATE_H
