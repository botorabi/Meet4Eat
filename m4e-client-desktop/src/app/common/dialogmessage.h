/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef DIALOGMESSAGE_H
#define DIALOGMESSAGE_H

#include <configuration.h>
#include <common/basedialog.h>


namespace m4e
{
namespace common
{

/**
 * @brief A message box dialog
 *
 * @author boto
 * @date Sep 28, 2017
 */
class DialogMessage : public common::BaseDialog
{
    Q_OBJECT

    public:

        /**
         * @brief Dialog buttons
         */
        enum Buttons
        {
            BtnOk  = BaseDialog::Btn1,
            BtnYes = BaseDialog::Btn2,
            BtnNo  = BaseDialog::Btn3,
        };

        /**
         * @brief Create a dialog instance.
         *
         * @param p_parent  Parent widget
         */
                                    DialogMessage( QWidget* p_parent );

        /**
         * @brief Destroy the instance.
         */
        virtual                     ~DialogMessage();

        /**
         * @brief Setup the dialog.
         *
         * @param title     Dialog's title
         * @param message   Message text
         * @param button    Predefined buttons, combination of Buttons enum. Custom buttons can be setup via BaseDialog's 'setupButtons'.
         */
        void                        setupUI( const QString& title, const QString& message, unsigned int buttons = BtnOk );
};

} // namespace common
} // namespace m4e

#endif // DIALOGMESSAGE_H
