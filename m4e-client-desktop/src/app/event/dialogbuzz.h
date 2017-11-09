/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef DIALOGBUZZ_H
#define DIALOGBUZZ_H

#include <configuration.h>
#include <common/basedialog.h>
#include <event/modelevent.h>
#include <webapp/webapp.h>


namespace Ui {
  class WidgetBuzz;
}

namespace m4e
{
namespace event
{

/**
 * @brief A dialog for creating a buzz message.
 *
 * @author boto
 * @date Oct 29, 2017
 */
class DialogBuzz : public common::BaseDialog
{
    Q_OBJECT

    public:


        /**
         * @brief Create a dialog instance.
         *
         * @param p_webApp  Web application interface
         * @param p_parent  Parent widget
         */
                                    DialogBuzz( webapp::WebApp* p_webApp, QWidget* p_parent );

        /**
         * @brief Destroy the instance.
         */
        virtual                     ~DialogBuzz();

        /**
         * @brief Setup the dialog for creating a new event buzz message.
         *
         * @param event  The event receiving the buzz message
         */
        void                        setupUI( event::ModelEventPtr event );

    protected slots:

    protected:

        virtual bool                onButton1Clicked();

        Ui::WidgetBuzz*             _p_ui     = nullptr;

        webapp::WebApp*             _p_webApp = nullptr;

        event::ModelEventPtr        _event;
};

} // namespace event
} // namespace m4e

#endif // DIALOGBUZZ_H
