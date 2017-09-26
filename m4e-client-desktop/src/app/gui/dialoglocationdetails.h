/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef DIALOGLOCATIONDETAILS_H
#define DIALOGLOCATIONDETAILS_H

#include <configuration.h>
#include <gui/basedialog.h>
#include <data/modellocation.h>
#include <data/webapp.h>


namespace Ui {
  class WidgetLocationDetails;
}

namespace m4e
{
namespace ui
{

/**
 * @brief A dialog for showing location details.
 *
 * @author boto
 * @date Sep 22, 2017
 */
class DialogLocationDetails : public BaseDialog
{
    Q_OBJECT

    public:


        /**
         * @brief Create a dialog instance.
         *
         * @param p_webApp  Web application interface
         * @param p_parent  Parent widget
         */
                                    DialogLocationDetails( data::WebApp* p_webApp, QWidget* p_parent );

        /**
         * @brief Destroy the instance.
         */
        virtual                     ~DialogLocationDetails();

        /**
         * @brief Setup the dialog.
         */
        void                        setupUI( data::ModelLocationPtr location );

    protected slots:

        /**
         * @brief This signal is received from webapp when a requested document was arrived.
         *
         * @param document   Document
         */
        void                        onDocumentReady( m4e::data::ModelDocumentPtr document );

    protected:

        /**
         * @brief Create a formatted string for members voted for this location.
         * @return
         */
        QString                     formatVoteMembers() const;

        Ui::WidgetLocationDetails*  _p_ui     = nullptr;

        data::WebApp*               _p_webApp = nullptr;

        data::ModelLocationPtr      _location;
};

} // namespace ui
} // namespace m4e

#endif // DIALOGLOCATIONDETAILS_H
