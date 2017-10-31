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
#include <QIcon>


namespace Ui {
  class WidgetLocationEdit;
}

namespace m4e
{
namespace event
{

/**
 * @brief A dialog for creating/updating event locations.
 *
 * @author boto
 * @date Oct 11, 2017
 */
class DialogLocationEdit : public common::BaseDialog
{
    Q_OBJECT

    public:


        /**
         * @brief Create a dialog instance.
         *
         * @param p_webApp  Web application interface
         * @param p_parent  Parent widget
         */
                                    DialogLocationEdit( webapp::WebApp* p_webApp, QWidget* p_parent );

        /**
         * @brief Destroy the instance.
         */
        virtual                     ~DialogLocationEdit();

        /**
         * @brief Setup the dialog for creating a new location for given event.
         *
         * @param event  The event which should get a new location
         */
        void                        setupUINewLocation( event::ModelEventPtr event );

        /**
         * @brief Setup the dialog for editting an existing location.
         *
         * @param event     The event containing the location
         * @param location  The location which is goind to be updated
         */
        void                        setupUIEditLocation( event::ModelEventPtr event, event::ModelLocationPtr location );

    protected slots:

        /**
         * @brief Called when the photo icon was clicked.
         */
        void                        onBtnPhotoClicked();

        /**
         * @brief This signal is received from webapp when a requested document was arrived.
         *
         * @param document   Document
         */
        void                        onDocumentReady( m4e::doc::ModelDocumentPtr document );

        /**
         * @brief This signal notifies about he results of adding a new location.
         *
         * @param success    true if user data could successfully be retrieved, otherwise false
         * @param eventId    ID of event
         * @param locationId ID of location to remove
         */
        void                        onResponseAddLocation( bool success, QString eventId, QString locationId );

        /**
         * @brief This signal notifies about he results of updating a location.
         *
         * @param success    true if user data could successfully be retrieved, otherwise false
         * @param eventId    ID of event
         * @param locationId ID of location to update
         */
        void                        onResponseUpdateLocation( bool success, QString eventId, QString locationId );

    protected:

        virtual bool                onButton1Clicked();

        void                        resetDialog();

        Ui::WidgetLocationEdit*     _p_ui     = nullptr;

        webapp::WebApp*             _p_webApp = nullptr;

        event::ModelEventPtr        _event;

        event::ModelLocationPtr     _location;

        QIcon                       _defaultPhoto;
};

} // namespace event
} // namespace m4e

#endif // DIALOGLOCATIONCREATE_H
