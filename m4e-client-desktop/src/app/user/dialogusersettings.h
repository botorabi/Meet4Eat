/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef DIALOGUSERSETTINGS_H
#define DIALOGUSERSETTINGS_H

#include <configuration.h>
#include <common/basedialog.h>
#include <event/modelevent.h>
#include <user/modeluserinfo.h>
#include <webapp/webapp.h>
#include <QPushButton>


namespace Ui {
  class WidgetUserSettings;
}

namespace m4e
{
namespace user
{

/**
 * @brief A dialog for user settings.
 *
 * @author boto
 * @date Nov 18, 2017
 */
class DialogUserSettings : public common::BaseDialog
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(DialogUserSettings) ";

    Q_OBJECT

    public:

        /**
         * @brief Dialog buttons
         */
        enum Buttons
        {
            BtnApply  = common::BaseDialog::Btn1,
            BtnCancel = common::BaseDialog::Btn2
        };

        /**
         * @brief Create a dialog instance.
         *
         * @param p_webApp  Web application interface
         * @param p_parent  Parent widget
         */
                                    DialogUserSettings( webapp::WebApp* p_webApp, QWidget* p_parent );

        /**
         * @brief Destroy the instance.
         */
        virtual                     ~DialogUserSettings();

        /**
         * @brief Setup the dialog for given user. Usually this user is currently authenticated one.
         *
         * @param user The user
         */
        void                        setupUI( user::ModelUserPtr user );

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
         * @brief Results of user data update request.
         *
         * @param success   true if user data could successfully be updated, otherwise false
         * @param userId    ID of the user who was updated on server.
         */
        void                        onResponseUpdateUserData( bool success, QString userId );

    protected:

        /**
         * @brief Overridden method for handling Apply button click
         */
        virtual bool                onButton1Clicked();

        /**
         * @brief Validate the input fields and report any problem.
         *
         * @param problem   If returning false then this string contains a text describing the problem.
         * @return          Return false if something is wrong with input fields.
         */
        bool                        validateInput( QString& problem );

        Ui::WidgetUserSettings*     _p_ui     = nullptr;

        webapp::WebApp*             _p_webApp = nullptr;

        user::ModelUserPtr          _user;

        doc::ModelDocumentPtr       _updatedPhoto;
};

} // namespace user
} // namespace m4e

#endif // DIALOGUSERSETTINGS_H
