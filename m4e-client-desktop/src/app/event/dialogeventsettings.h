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
#include <common/basedialog.h>
#include <event/modelevent.h>
#include <webapp/webapp.h>
#include <QPushButton>


namespace Ui {
  class WidgetEventSettings;
}

namespace m4e
{
namespace event
{

/**
 * @brief A dialog for event settings.
 *
 * @author boto
 * @date Sep 25, 2017
 */
class DialogEventSettings : public common::BaseDialog
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(DialogEventSettings) ";

    Q_OBJECT

    public:


        /**
         * @brief Create a dialog instance.
         *
         * @param p_webApp  Web application interface
         * @param p_parent  Parent widget
         */
                                    DialogEventSettings( webapp::WebApp* p_webApp, QWidget* p_parent );

        /**
         * @brief Destroy the instance.
         */
        virtual                     ~DialogEventSettings();

        /**
         * @brief Setup the dialog.
         */
        void                        setupUI( event::ModelEventPtr event );

    protected slots:

        /**
         * @brief This signal is received from webapp when a requested document was arrived.
         *
         * @param document   Document
         */
        void                        onDocumentReady( m4e::doc::ModelDocumentPtr document );

        /**
         * @brief This signal is received when user search results were arrived.
         *
         * @param users List of user hits
         */
        void                        onUserSearch( QList< m4e::user::ModelUserInfoPtr > users );

        /**
         * @brief Called to remove a member from event list.
         */
        void                        onMemberRemoveClicked();

        /**
         * @brief Called when a new member is being added to event.
         */
        void                        onBtnAddMemberClicked();

        /**
         * @brief On memebr search field the enter key was pressed.
         */
        void                        onLineEditSeachtReturnPressed();

    protected:

        /**
         * @brief Overridden method for handling Apply button click
         */
        virtual bool                onButton1Clicked();

        /**
         * @brief Overridden method for handling Cancel button click
         */
        virtual bool                onButton2Clicked();

        void                        setupWeekDays( unsigned int weekDays );

        void                        setupMembers( event::ModelEventPtr event );

        QWidget*                    createRemoveMemberButton( const QString& memberId );

        Ui::WidgetEventSettings*    _p_ui     = nullptr;

        webapp::WebApp*             _p_webApp = nullptr;

        event::ModelEventPtr        _event;

        QMap< QString, int >        _memberPhotos;

        QSet< QString >             _members;
};

} // namespace event
} // namespace m4e

#endif // DIALOGEVENTSETTINGS_H
