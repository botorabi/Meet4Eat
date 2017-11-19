/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef DIALOGSETTINGS_H
#define DIALOGSETTINGS_H

#include <configuration.h>
#include <user/userauth.h>
#include <webapp/webapp.h>
#include <common/basedialog.h>

namespace Ui {
  class WidgetSettings;
}

namespace m4e
{
namespace settings
{

/**
 * @brief Class for app settings dialog
 *
 * @author boto
 * @date Sep 12, 2017
 */
class DialogSettings : public common::BaseDialog
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(DialogSettings) ";

    Q_OBJECT

    public:

        /**
         * @brief Dialog buttons
         */
        enum Buttons
        {
            BtnOk = common::BaseDialog::Btn1
        };

        /**
         * @brief Create a settings dialog instance.
         *
         * @param p_webApp  Web application interface
         * @param p_parent  Parent widget
         */
        explicit                    DialogSettings( webapp::WebApp* p_webApp, QWidget* p_parent );

        /**
         * @brief Destroy the instance.
         */
        virtual                     ~DialogSettings();

    protected slots:

        /**
         * @brief Overridden method for handling Apply button click
         */
        virtual bool                onButton1Clicked();

        void                        onBtnSignInClicked();

        void                        onBtnSignOutClicked();

        void                        onLinkActivated( QString link );

        /**
         * @brief This signal is received to notify about user authentication results.
         *
         * @param success  true if the user was successfully authenticated, otherwise false
         * @param userId   User ID, valid if success is true
         */
        void                        onUserSignedIn( bool success, QString userId );

    protected:

        void                        setupUI();

        void                        storeCredentials();

        bool                        validateInput();

        Ui::WidgetSettings*         _p_ui     = nullptr;

        webapp::WebApp*             _p_webApp = nullptr;
};

} // namespace settings
} // namespace m4e

#endif // DIALOGSETTINGS_H
