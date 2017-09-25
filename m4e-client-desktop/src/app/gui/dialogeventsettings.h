/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef DIALOGEVENTSETTINGS_H
#define DIALOGEVENTSETTINGS_H

#include <configuration.h>
#include <gui/basedialog.h>
#include <data/modelevent.h>
#include <data/webapp.h>


namespace Ui {
  class WidgetEventSettings;
}

namespace m4e
{
namespace ui
{

/**
 * @brief A dialog for event settings.
 *
 * @author boto
 * @date Sep 25, 2017
 */
class DialogEventSettings : public BaseDialog
{
    Q_OBJECT

    public:


        /**
         * @brief Create a dialog instance.
         *
         * @param p_webApp  Web application interface
         * @param p_parent  Parent widget
         */
                                    DialogEventSettings( data::WebApp* p_webApp, QWidget* p_parent );

        /**
         * @brief Destroy the instance.
         */
        virtual                     ~DialogEventSettings();

        /**
         * @brief Setup the dialog.
         */
        void                        setupUI( data::ModelEventPtr event );

    protected slots:

        /**
         * @brief This signal is received from webapp when a requested document was arrived.
         *
         * @param document   Document
         */
        void                        onDocumentReady( m4e::data::ModelDocumentPtr document );

    protected:

        void                        setupWeekDays( unsigned int weekDays );

        void                        setupMembers( data::ModelEventPtr event );

        Ui::WidgetEventSettings*    _p_ui     = nullptr;

        data::WebApp*               _p_webApp = nullptr;

        data::ModelEventPtr         _event;

        QMap< QString, int >        _memberPhotos;
};

} // namespace ui
} // namespace m4e

#endif // DIALOGEVENTSETTINGS_H
