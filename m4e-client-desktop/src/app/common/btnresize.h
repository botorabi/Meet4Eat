/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef BTNRESIZE_H
#define BTNRESIZE_H

#include <configuration.h>
#include <QPushButton>


namespace m4e
{
namespace common
{

/**
 * @brief A button used for resizing widgets.
 *
 * @author boto
 * @date Dec 4, 2017
 */
class ButtonResize : public QPushButton
{
    Q_OBJECT

    public:

        /**
         * @brief Create a resize button instance.
         *
         * @param p_parent          Parent widget
         */
        explicit                    ButtonResize( QWidget* p_parent );

        /**
         * @brief Destroy the instance.
         */
        virtual                     ~ButtonResize();

        /**
         * @brief Set the widget which is under control of this button. The button will resize this widget while dragging.
         *
         * @param p_widget  The widget whose size is under the control of this button
         */
        void                        setControlledWidget( QWidget* p_widget );

        /**
         * @brief Get the widget whose size is controlled by this button.
         *
         * @return The controlled widget.
         */
        QWidget*                    getControlledWidget();

    protected:

        void                        mousePressEvent( QMouseEvent* p_event );

        void                        mouseReleaseEvent( QMouseEvent* p_event );

        void                        mouseMoveEvent( QMouseEvent* p_event );

        QWidget*                    _p_widget = nullptr;

        bool                        _dragging = false;

        QPoint                      _draggingPos;
};

} // namespace common
} // namespace m4e

#endif // BTNRESIZE_H
