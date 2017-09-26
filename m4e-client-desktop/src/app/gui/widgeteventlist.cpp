/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include "widgeteventlist.h"
#include <core/log.h>
#include "widgeteventitem.h"
#include <QLayout>


namespace m4e
{
namespace ui
{

WidgetEventList::WidgetEventList( m4e::data::WebApp* p_webApp, QWidget* p_parent ) :
 QWidget( p_parent ),
 _p_webApp( p_webApp )
{
    setupUI();
}

void WidgetEventList::setupUI()
{
    QVBoxLayout* p_layout = new QVBoxLayout();
    setLayout( p_layout );

    QList< m4e::data::ModelEventPtr > events = _p_webApp->getUserEvents()->getAllEvents();
    for ( auto ev: events )
    {
        addEvent( ev );
    }
}

void WidgetEventList::addEvent( m4e::data::ModelEventPtr event )
{
    WidgetEventItem* p_item = new WidgetEventItem( _p_webApp, this );
    p_item->setupUI( event );
    connect( p_item, SIGNAL( onClicked( QString ) ), this, SLOT( onClicked( QString ) ) );

    layout()->addWidget( p_item );
    _widgets.append( p_item );
}

void WidgetEventList::onClicked( QString id )
{
    for ( WidgetEventItem* p_widget: _widgets )
    {
        p_widget->setSelectionMode( p_widget->getId() != id );
    }

    // forward the signal
    emit onEventSelection( id );
}

} // namespace ui
} // namespace m4e
