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
#include <gui/basedialog.h>


namespace Ui {
  class WidgetSettings;
}

namespace m4e
{
namespace ui
{

/**
 * @brief Class for app settings dialog
 *
 * @author boto
 * @date Sep 12, 2017
 */
class DialogSettings : public BaseDialog
{
    Q_OBJECT

    public:

        /**
         * @brief Create a settings dialog instance.
         *
         * @param p_parent          Parent widget
         */
        explicit                    DialogSettings( QWidget* p_parent );

        /**
         * @brief Destroy the instance.
         */
        virtual                     ~DialogSettings();

        void                        accept();

    protected slots:

        void                        onBtnSignInClicked();

        void                        onBtnSignOutClicked();

        void                        onResponseAuthState( bool authenticated, QString userId );

        void                        onResponseSignInResult( bool success, QString userId, enum m4e::user::UserAuthentication::AuthResultsCode code, QString reason );

        void                        onResponseSignOutResult( bool success, enum m4e::user::UserAuthentication::AuthResultsCode code, QString reason );

    protected:

        void                        setupUI();

        user::UserAuthentication*   getOrCreateUserAuth();

        Ui::WidgetSettings*         _p_ui = nullptr;

        user::UserAuthentication*   _p_userAuth = nullptr;
};

} // namespace ui
} // namespace m4e

#endif // DIALOGSETTINGS_H
