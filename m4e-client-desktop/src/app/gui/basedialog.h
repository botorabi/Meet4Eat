/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef BASEDIALOG_H
#define BASEDIALOG_H

#include <configuration.h>
#include <QDialog>


namespace Ui {
  class BaseDlg;
}

namespace m4e
{
namespace ui
{

/**
 * @brief Customized dialog used for all kinds of dialogs in the app.
 *
 * @author boto
 * @date Sep 22, 2017
 */
class BaseDialog : public QDialog
{
    Q_OBJECT

    public:

        /**
         * @brief Button IDs used for return values on closing the dialog.
         */
        enum BtnIDs
        {
            BtnClose,
            Btn1,
            Btn2,
            Btn3
        };

        /**
         * @brief Create a base dialog instance.
         *
         * @param p_parent          Parent widget
         */
        explicit                    BaseDialog( QWidget* p_parent );

        /**
         * @brief Destroy the instance.
         */
        virtual                     ~BaseDialog();

        /**
         * Decorate the dialog using given decorator class.
         *
         * @param decoClass  Decorator class usually created by QtDesigner
         */
        template < class Type >
        void                        decorate( Type& decoClass );

        /**
         * @brief Set the dialog title.
         *
         * @param title Dialog title
         */
        void                        setTitle( const QString& title );

        /**
         * @brief Setup the dialog buttons. Pass nullptr for every of the buttons which should be hidden.
         *
         * @param p_btn1Text    Text of button 1
         * @param p_btn2Text    Text of button 2
         * @param p_btn3Text    Text of button 3
         */
        void                        setupButtons( QString* p_btn1Text, QString* p_btn2Text, QString* p_btn3Text );

        /**
         * @brief If the dialog should be resizable then pass true. Call this method after
         *         decorating the dialog using 'decorate' method.
         *
         * @param resizable True for resizable dialog, false for fixed-size dialog.
         */
        void                        setResizable( bool resizable );

        /**
         * @brief The user UI is put into client area widget.
         *
         * @return Client area widget.
         */
        QWidget*                    getClientArea();

        /**
         * @brief This method is called when the dialog's close button was clicked.
         *         Override and return false in order to avoid the dialog closing.
         *
         * @return If true is returned then the dialog will be closed, otherwise the close button
         *          click will be ignored.
         */
        virtual bool                onClose() { return true; }

    protected slots:

        void                        onBtnCloseClicked();

        void                        onBtn1Clicked();

        void                        onBtn2Clicked();

        void                        onBtn3Clicked();

    protected:

        void                        mousePressEvent( QMouseEvent* p_event );

        void                        mouseReleaseEvent( QMouseEvent* p_event );

        void                        mouseMoveEvent( QMouseEvent* p_event );

        Ui::BaseDlg*                _p_ui     = nullptr;

        bool                        _dragging = false;

        QPoint                      _draggingPos;
};

/**
 * Decorate the dialog using given decorator class.
 */
template< class Type >
void BaseDialog::decorate( Type& decoClass )
{
    decoClass.setupUi( getClientArea() );
}

} // namespace ui
} // namespace m4e

#endif // BASEDIALOG_H
