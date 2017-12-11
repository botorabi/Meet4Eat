/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "btnresize.h"
#include <core/log.h>
#include <QMouseEvent>


namespace m4e
{
namespace common
{

ButtonResize::ButtonResize( QWidget* p_parent ) :
 QPushButton( p_parent )
{
}

ButtonResize::~ButtonResize()
{
}

void ButtonResize::setControlledWidget( QWidget* p_widget )
{
    _p_widget = p_widget;
}

QWidget*ButtonResize::getControlledWidget()
{
    return _p_widget;
}

void ButtonResize::mousePressEvent( QMouseEvent* p_event )
{
    if ( !_p_widget )
        return;

    _draggingPos = p_event->globalPos();
    _dragging = true;
}

void ButtonResize::mouseReleaseEvent( QMouseEvent* /*p_event*/ )
{
    if ( !_p_widget )
        return;

    _dragging = false;
}

void ButtonResize::mouseMoveEvent( QMouseEvent* p_event )
{
    if ( _dragging && _p_widget )
    {
        //! NOTE strict checking for the mouse leaving the button area results in a bad UIX!
        //QPoint mousepos = mapFromGlobal( p_event->globalPos() );
        //if ( !rect().contains( mousepos ) )
        //    return;

        // if the window is in maximized state then we have to remove that state first
        if ( ( _p_widget->windowState() & Qt::WindowMaximized ) != 0 )
        {
            _p_widget->setWindowState( windowState() & ~Qt::WindowMaximized );
        }

        QPoint currpos = p_event->globalPos();
        QPoint delta = currpos - _draggingPos;
        _draggingPos = currpos;
        QSize winsize = _p_widget->size();
        winsize.setHeight( winsize.height() + delta.y() );
        winsize.setWidth( winsize.width() + delta.x() );
        _p_widget->resize( winsize );
    }
}

} // namespace common
} // namespace m4e
