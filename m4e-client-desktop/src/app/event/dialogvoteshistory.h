/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef DIALOGVOTESHISTORY_H
#define DIALOGVOTESHISTORY_H

#include <configuration.h>
#include <common/basedialog.h>
#include <event/modellocationvotes.h>
#include <webapp/webapp.h>


namespace Ui {
  class WidgetVotesHistory;
  class WidgetVotesItem;
}

namespace m4e
{
namespace event
{

/**
 * @brief A dialog for showing location votes history.
 *
 * @author boto
 * @date Nov 28, 2017
 */
class DialogVotesHistory : public common::BaseDialog
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(DialogVotesHistory) ";

    Q_OBJECT

    public:


        /**
         * @brief Create a dialog instance.
         *
         * @param p_webApp  Web application interface
         * @param p_parent  Parent widget
         * @param autoDestroy   Pass true in order to delegate the object destruction to the instance itself.
         *                      In this case the instance creator does not need to care about deleting the dialog.
         */
                                    DialogVotesHistory( webapp::WebApp* p_webApp, QWidget* p_parent, bool autoDestroy = false );

        /**
         * @brief Destroy the instance.
         */
        virtual                     ~DialogVotesHistory();

        /**
         * @brief Setup the dialog for given event.
         *
         * @param event The event
         */
        void                        setupUI( event::ModelEventPtr event );

    protected slots:

        /**
         * @brief The time window update button was clicked.
         */
        void                        onBtnUpdateClicked();

        /**
         * @brief Item's expand button was clicked.
         */
        void                        onBtnExpandClicked();

        /**
         * @brief Results of location votes request by time range.
         *
         * @param success   true if user votes could successfully be retrieved, otherwise false
         * @param votes     The event location votes list
         */
        void                        onResponseGetLocationVotesByTime( bool success, QList< m4e::event::ModelLocationVotesPtr > votes );

    protected:

        void                        clearVotesItems();

        void                        addVotesItem( QList< m4e::event::ModelLocationVotesPtr > votes );

        Ui::WidgetVotesHistory*     _p_ui     = nullptr;

        webapp::WebApp*             _p_webApp = nullptr;

        event::ModelEventPtr        _event;
};

} // namespace event
} // namespace m4e

#endif // DIALOGVOTESHISTORY_H
