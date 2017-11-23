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
#include <common/dialogmessage.h>
#include "widgeteventitem.h"
#include <QApplication>
#include <QListWidgetItem>
#include <QScrollBar>
#include <QVBoxLayout>


namespace m4e
{
namespace event
{

const static QString EVENT_LIST_STYLESHEET = \
"QListWidget {" \
" background-color: transparent;" \
" border: 0;" \
"}" \
"QListWidget::item:selected {" \
" background: transparent;" \
" background-color: transparent;" \
"}" \
"QScrollBar::vertical {" \
" background-color: transparent;" \
" color: rgb(151,167, 187);" \
"}";


WidgetEventList::WidgetEventList( webapp::WebApp* p_webApp, QWidget* p_parent ) :
 QListWidget( p_parent ),
 _p_webApp( p_webApp )
{
    setupUI();
}

void WidgetEventList::selectEvent( const QString& eventId )
{
    // check if the event exists at all
    if ( findEventItem( eventId ) )
    {
        onClicked( eventId );
        return;
    }
    selectFirstEvent();
}

void WidgetEventList::selectFirstEvent()
{
    if ( _widgets.size() > 0 )
        onClicked( _widgets.at( 0 )->getId() );
}

void WidgetEventList::createNewLocation( const QString& eventId )
{
    WidgetEventItem* p_item = findEventItem( eventId );
    if ( p_item )
        p_item->createNewLocation();
}

void WidgetEventList::setupUI()
{
    connect( _p_webApp->getEvents(), SIGNAL( onResponseGetEvent( bool, m4e::event::ModelEventPtr ) ), this, SLOT( onResponseGetEvent( bool, m4e::event::ModelEventPtr ) ) );
    connect( _p_webApp->getEvents(), SIGNAL( onResponseDeleteEvent( bool, QString ) ), this, SLOT( onResponseDeleteEvent( bool, QString ) ) );

    setupListView();

    QList< m4e::event::ModelEventPtr > events = _p_webApp->getEvents()->getUserEvents();
    for ( auto ev: events )
    {
        addEvent( ev );
    }
}

void WidgetEventList::setupListView()
{
    // setup the list view
    setStyleSheet( EVENT_LIST_STYLESHEET );
    setSizePolicy( QSizePolicy::Policy::Expanding, QSizePolicy::Policy::Expanding );
    setVerticalScrollMode( ScrollPerPixel );
    setSizeAdjustPolicy( SizeAdjustPolicy::AdjustToContents );
    setHorizontalScrollBarPolicy( Qt::ScrollBarAlwaysOff );
    setVerticalScrollBarPolicy( Qt::ScrollBarAsNeeded );
    setAutoFillBackground( false );
    verticalScrollBar()->setSingleStep( 5 );
    setViewMode( QListView::IconMode );
    setDragEnabled( false );
    QVBoxLayout* p_layout = new QVBoxLayout( this );
    p_layout->setSpacing( 8 );
    p_layout->setContentsMargins( 0, 0, 0, 0 );
    setLayout( p_layout );
}

void WidgetEventList::addEvent( m4e::event::ModelEventPtr event )
{
    WidgetEventItem* p_itemwidget = new WidgetEventItem( _p_webApp, this );
    p_itemwidget->setupUI( event );
    connect( p_itemwidget, SIGNAL( onClicked( QString ) ), this, SLOT( onClicked( QString ) ) );
    connect( p_itemwidget, SIGNAL( onRequestDeleteEvent( QString ) ), this, SLOT( onRequestDeleteEvent( QString ) ) );

    QListWidgetItem* p_listitem = new QListWidgetItem( this );
    p_listitem->setSizeHint( p_itemwidget->size() );
    p_listitem->setFlags( Qt::NoItemFlags );

    addItem( p_listitem );
    setItemWidget( p_listitem, p_itemwidget );

    _widgets.append( p_itemwidget );
}

WidgetEventItem* WidgetEventList::findEventItem( const QString& eventId )
{
    for ( WidgetEventItem* p_item: _widgets )
    {
        if ( p_item->getId() == eventId )
        {
            return p_item;
        }
    }
    return nullptr;
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

void WidgetEventList::onRequestDeleteEvent( QString id )
{
    _p_webApp->getEvents()->requestDeleteEvent( id );
}

void WidgetEventList::onResponseGetEvent( bool success, m4e::event::ModelEventPtr event )
{
    if ( !success )
    {
        log_warning << TAG << "could not get event data" << std::endl;
    }
    else
    {
        // update the event widget
        for ( WidgetEventItem* p_item: _widgets )
        {
            if ( p_item->getId() == event->getId() )
            {
                p_item->updateEvent( event );
                selectEvent( event->getId() );
                break;
            }
        }
    }
}

void WidgetEventList::onResponseDeleteEvent( bool success, QString /*eventId*/ )
{
    if ( !success )
    {
        QString text = QApplication::translate( "WidgetEventList", "Could not delete the event.\nReason: " ) + _p_webApp->getEvents()->getLastError();
        common::DialogMessage* p_msg = new common::DialogMessage( this );
        p_msg->setupUI( QApplication::translate( "WidgetEventList", "Delete Event" ),
                        text,
                        common::DialogMessage::BtnOk );

        p_msg->show();
        delete p_msg;
    }
    else
    {
        _p_webApp->getEvents()->requestGetEvents();
    }
}

} // namespace event
} // namespace m4e
