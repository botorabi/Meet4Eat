/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef DIALOGSEARCHUSER_H
#define DIALOGSEARCHUSER_H

#include <configuration.h>
#include <common/basedialog.h>
#include <event/modelevent.h>
#include <user/modeluserinfo.h>
#include <webapp/webapp.h>
#include <QPushButton>


namespace Ui {
  class WidgetSearchUser;
}

namespace m4e
{
namespace user
{

/**
 * @brief A dialog providing user search functionality.
 *
 * @author boto
 * @date Nov 7, 2017
 */
class DialogSearchUser : public common::BaseDialog
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(DialogSearchUser) ";

    Q_OBJECT

    public:

        /**
         * @brief Create a dialog instance.
         *
         * @param p_webApp  Web application interface
         * @param p_parent  Parent widget
         */
                                    DialogSearchUser( webapp::WebApp* p_webApp, QWidget* p_parent );

        /**
         * @brief Destroy the instance.
         */
        virtual                     ~DialogSearchUser();

        /**
         * @brief Get the result of user search.
         *
         * @return The found user, the object may also be empty if no user was found.
         */
        user::ModelUserInfoPtr      getUserInfo() { return _userInfo; }

    protected slots:

        /**
         * @brief This signal is received when user search results were arrived.
         *
         * @param users List of user hits
         */
        void                        onUserSearch( QList< m4e::user::ModelUserInfoPtr > users );

        /**
         * @brief On user search field the enter key was pressed.
         */
        void                        onLineEditSearchReturnPressed();

        /**
         * @brief Called to remove a member from event list.
         */
        void                        onBtnSearchClicked();

    protected:

        /**
         * @brief Overridden method for handling Apply button click
         */
        virtual bool                onButton1Clicked();

        Ui::WidgetSearchUser*       _p_ui     = nullptr;

        webapp::WebApp*             _p_webApp = nullptr;

        user::ModelUserInfoPtr      _userInfo;
};

} // namespace user
} // namespace m4e

#endif // DIALOGSEARCHUSER_H
