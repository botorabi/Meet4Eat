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
#include <user/modeluserinfo.h>
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
 * @brief A dialog for editting event settings and create a new event.
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
         *
         * @param event The event which is represented by the dialog.
         */
        void                        setupUI( event::ModelEventPtr event );

        /**
         * @brief Setup the dialog for creating a new event.
         *
         * @param event The event which is used by the dialog. You can pre-define some settings in event.
         */
        void                        setupNewEventUI( event::ModelEventPtr event );

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
         * @brief This signal is received when the results of adding a new member were arrived.
         *
         * @param success   true if the request was successful
         * @param eventId   Event ID, should be the one of this event
         * @param memberId  User ID of new member
         */
        void                        onResponseAddMember( bool success, QString eventId, QString memberId );

        /**
         * @brief This signal is received when the results of removing a member were arrived.
         *
         * @param success   true if the request was successful
         * @param eventId   Event ID, should be the one of this event
         * @param memberId  User ID of removed member
         */
        void                        onResponseRemoveMember( bool success, QString eventId, QString memberId );

        /**
         * @brief This signal is received when results of event update request.
         *
         * @param success  true if user data could successfully be retrieved, otherwise false
         * @param eventId  ID of event which was updated
         */
        void                        onResponseUpdateEvent( bool success, QString eventId );

        /**
         * @brief This signal is received when results of event creation request.
         *
         * @param success  true if the event could successfully be created, otherwise false
         * @param eventId  ID of event which was created
         */
        void                        onResponseNewEvent( bool success, QString eventId );

        /**
         * @brief Called when the photo icon was clicked.
         */
        void                        onBtnPhotoClicked();

        /**
         * @brief Called to remove a member from event list.
         */
        void                        onBtnMemberRemoveClicked();

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

        unsigned int                getWeekDays();

        void                        setupMembers( event::ModelEventPtr event );

        QWidget*                    createRemoveMemberButton( const QString& memberId );

        void                        setupReadOnly();

        Ui::WidgetEventSettings*    _p_ui         = nullptr;

        webapp::WebApp*             _p_webApp     = nullptr;

        bool                        _userIsOwner  = false;

        bool                        _editNewEvent = false;

        event::ModelEventPtr        _event;

        QMap< QString, int >        _memberPhotos;

        QSet< QString >             _members;

        user::ModelUserInfoPtr      _newMember;

        QString                     _removeMemberId;
};

} // namespace event
} // namespace m4e

#endif // DIALOGEVENTSETTINGS_H
