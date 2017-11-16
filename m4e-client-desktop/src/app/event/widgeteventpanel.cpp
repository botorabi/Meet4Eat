/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include "widgeteventpanel.h"
#include <core/log.h>
#include <common/guiutils.h>
#include <common/dialogmessage.h>
#include <chat/chatmessage.h>
#include <ui_widgeteventpanel.h>
#include "widgeteventitem.h"
#include "widgetlocation.h"
#include "dialogbuzz.h"


namespace m4e
{
namespace event
{

WidgetEventPanel::WidgetEventPanel( webapp::WebApp* p_webApp, QWidget* p_parent ) :
 QWidget( p_parent ),
 _p_webApp( p_webApp )
{
    setupUI();
}

WidgetEventPanel::~WidgetEventPanel()
{
    if ( _p_ui )
        delete _p_ui;
}

void WidgetEventPanel::setupEvent( const QString& id )
{
    QList< event::ModelEventPtr > events = _p_webApp->getEvents()->getUserEvents();
    event::ModelEventPtr event;
    for ( event::ModelEventPtr ev: events )
    {
        if ( ev->getId() == id )
        {
            event = ev;
            break;
        }
    }

    if ( !event.valid() )
    {
        log_error << TAG << "could not find the event with id: " << id << std::endl;
    }
    else
    {
        _event = event;
        setupLocations();
        setupWidgetHead();
        setEventMembers();
    }
}

QString WidgetEventPanel::getEventId() const
{
    if ( _event.valid() )
        return _event->getId();
    return "";
}

void WidgetEventPanel::setChatSystem( chat::ChatSystem* p_chatSystem )
{
    _p_chatSystem = p_chatSystem;
    connect( _p_chatSystem, SIGNAL( onReceivedChatMessageEvent( m4e::chat::ChatMessagePtr ) ), this, SLOT( onReceivedChatMessageEvent( m4e::chat::ChatMessagePtr ) ) );

    // restore the messages already received
    QList< chat::ChatMessagePtr > messages = _p_chatSystem->getEventMessages( _event->getId() );
    for ( auto msg: messages )
    {
        onReceivedChatMessageEvent( msg );
    }
}

void WidgetEventPanel::onLocationVotingStart( m4e::event::ModelEventPtr event )
{
    if ( event != _event )
        return;

    log_verbose << TAG << "time to vote for event locations" << std::endl;

    _p_ui->widgetVotingTime->setVisible( true );
    for ( auto p_widget: _widgets )
    {
        p_widget->enableVotingUI( true );
    }

    requestCurrentLoctionVotes();
}

void WidgetEventPanel::onLocationVotingEnd( m4e::event::ModelEventPtr event )
{
    if ( event != _event )
        return;

    log_verbose << TAG << "end of to voting time reached" << std::endl;

    _p_ui->widgetVotingTime->setVisible( false );
    for ( auto p_widget: _widgets )
    {
        p_widget->enableVotingUI( false );
    }
}

void WidgetEventPanel::onLinkActivated( QString link )
{
    if ( link == "CREATE_LOCATION" )
    {
        if ( _event.valid() )
        {
            emit onCreateNewLocation( _event->getId() );
        }
        else
        {
            log_error << TAG << "cannot requesr for creating new location, invalid event!" << std::endl;
        }
    }
}

void WidgetEventPanel::setupUI()
{
    _p_ui = new Ui::WidgetEventPanel;
    _p_ui->setupUi( this );
    _widgets.clear();

    _p_ui->widgetChat->setupUI( _p_webApp );
    connect( _p_ui->widgetChat, SIGNAL( onSendMessage( m4e::chat::ChatMessagePtr ) ), this, SLOT( onSendMessage( m4e::chat::ChatMessagePtr ) ) );
    connect( _p_webApp->getEvents(), SIGNAL( onResponseRemoveLocation( bool, QString, QString ) ), this, SLOT( onResponseRemoveLocation( bool, QString, QString ) ) );
    connect( _p_webApp->getEvents(), SIGNAL( onResponseGetLocationVotesByTime( bool, QList< m4e::event::ModelLocationVotesPtr > ) ), this,
                                     SLOT( onResponseGetLocationVotesByTime( bool, QList< m4e::event::ModelLocationVotesPtr > ) ) );

    connect( _p_webApp->getEvents(), SIGNAL( onLocationVotingStart( m4e::event::ModelEventPtr ) ), this, SLOT( onLocationVotingStart( m4e::event::ModelEventPtr ) ) );
    connect( _p_webApp->getEvents(), SIGNAL( onLocationVotingEnd( m4e::event::ModelEventPtr ) ), this, SLOT( onLocationVotingEnd( m4e::event::ModelEventPtr ) ) );

    QColor shadowcolor( 150, 150, 150, 110 );
    common::GuiUtils::createShadowEffect( _p_ui->widgetInfo, shadowcolor, QPoint( -3, 3 ), 3 );
    common::GuiUtils::createShadowEffect( _p_ui->widgetMembers, shadowcolor, QPoint( -3, 3 ), 3 );
    common::GuiUtils::createShadowEffect( _p_ui->pushButtonBuzz, shadowcolor, QPoint( -2, 2 ), 1 );

    _p_clientArea = _p_ui->listWidget;
    _p_clientArea->setUniformItemSizes( true );
    _p_clientArea->setSizePolicy( QSizePolicy::Expanding, QSizePolicy::Expanding );
    _p_clientArea->setAutoScroll( true );
    _p_clientArea->setViewMode( QListView::IconMode );
    _p_clientArea->setWrapping( true );
    _p_clientArea->setSpacing( 10 );
}

void WidgetEventPanel::setupWidgetHead()
{
    _p_ui->labelInfoHead->setText( _event->getName() );
    QString info;
    info += QApplication::translate( "WidgetEventPanel", "Owner: " ) + _event->getOwner()->getName();
    if ( _event->isRepeated() )
    {
        unsigned int days = _event->getRepeatWeekDays();
        QString weekdays;
        weekdays += ( days & event::ModelEvent::WeekDayMonday )    != 0 ? QApplication::translate( "WidgetEventPanel", "Mon" ) + " " : "";
        weekdays += ( days & event::ModelEvent::WeekDayTuesday )   != 0 ? QApplication::translate( "WidgetEventPanel", "Tue" ) + " " : "";
        weekdays += ( days & event::ModelEvent::WeekDayWednesday ) != 0 ? QApplication::translate( "WidgetEventPanel", "Wed" ) + " " : "";
        weekdays += ( days & event::ModelEvent::WeekDayThursday )  != 0 ? QApplication::translate( "WidgetEventPanel", "Thu" ) + " " : "";
        weekdays += ( days & event::ModelEvent::WeekDaySaturday )  != 0 ? QApplication::translate( "WidgetEventPanel", "Sat" ) + " " : "";
        weekdays += ( days & event::ModelEvent::WeekDaySunday )    != 0 ? QApplication::translate( "WidgetEventPanel", "Sun" ) + " " : "";
        info += "\n" + QApplication::translate( "WidgetEventPanel", "Repeated Event" );
        info += "\n * " + QApplication::translate( "WidgetEventPanel", "Week Days" ) + ": " + weekdays;
        info += "\n * " + QApplication::translate( "WidgetEventPanel", "At" ) + " " + _event->getRepeatDayTime().toString( "HH:mm" );
    }
    else
    {
        info += "\n" + QApplication::translate( "WidgetEventPanel", "Event Date" ) + ": " + _event->getStartDate().toString( "yyyy-M-dd HH:mm" );
    }
    _p_ui->labelInfoBody->setText( info );
}

void WidgetEventPanel::setupLocations()
{
    bool userisowner = common::GuiUtils::userIsOwner( _event->getOwner()->getId(), _p_webApp );
    if ( _event->getLocations().size() > 0 )
    {
        _p_ui->labelEmptyPanel->hide();
        for ( auto location: _event->getLocations() )
        {
            addLocation( _event, location, userisowner );
        }
        bool votinginprogress = _p_webApp->getEvents()->getIsVotingTime( _event->getId() );
        _p_ui->widgetVotingTime->setVisible( votinginprogress );
        if ( votinginprogress )
            requestCurrentLoctionVotes();
    }
    else
    {
        _p_ui->widgetVotingTime->setVisible( false );
        _p_ui->labelEmptyPanel->show();
    }
}

void WidgetEventPanel::addLocation( event::ModelEventPtr event, event::ModelLocationPtr location, bool userIsOwner )
{
    WidgetLocation* p_widget = new WidgetLocation( _p_webApp, _p_clientArea );
    p_widget->setupUI( event, location, userIsOwner );
    connect( p_widget, SIGNAL( onDeleteLocation( QString ) ), this, SLOT( onDeleteLocation( QString ) ) );

    QListWidgetItem* p_item = new QListWidgetItem( _p_clientArea );
    p_item->setSizeHint( p_widget->size() );

    _p_clientArea->setItemWidget( p_item, p_widget );
    p_item->setData( Qt::UserRole, location->getId() );

    //! NOTE it seems that this is really needed after every item insertion, otherwise the items get draggable
    _p_clientArea->setDragDropMode( QListWidget::NoDragDrop );

    _widgets.append( p_widget );
}

QListWidgetItem* WidgetEventPanel::findLocationItem( const QString& locationId )
{
    for ( int i = 0; i < _p_clientArea->count(); i++ )
    {
        QListWidgetItem* p_item = _p_clientArea->item( i );
        QString locid = p_item->data( Qt::UserRole ).toString();
        if ( locid == locationId )
        {
            return p_item;
        }
    }
    return nullptr;
}

void WidgetEventPanel::setEventMembers()
{
    auto eventmembers = _event->getMembers();
    eventmembers.append( _event->getOwner() );
    _p_ui->widgetChat->setMembers( eventmembers );

    // set the members lable
    QString members;
    for ( auto member: _event->getMembers() )
    {
        if ( !members.isEmpty() )
            members += ", ";
        members += member->getName();
    }
    // limit the string
    const int MAX_MEMSTR_LEN = 120;
    if ( members.length() > MAX_MEMSTR_LEN )
    {
        members = members.mid( 0, MAX_MEMSTR_LEN - 3 );
        members += "...";
    }
    _p_ui->labelMembersBody->setText( members );
}

bool WidgetEventPanel::requestCurrentLoctionVotes()
{
    if ( !_event.valid() )
    {
        log_error << TAG << "cannot request for event location votes, invalid event" << std::endl;
        return false;
    }

    QDateTime tend, tbeg;
    if ( !_p_webApp->getEvents()->getVotingTimeWindow( _event->getId(), tbeg, tend ) )
    {
        return false;
    }

    _p_webApp->getEvents()->requestGetLocationVotesByTime( _event->getId(), tbeg, tend );
    return true;
}

WidgetLocation* WidgetEventPanel::findWidgetLocation( const QString& locationId )
{
    for ( auto p_widget: _widgets )
    {
        if ( p_widget->getId() == locationId )
            return p_widget;
    }
    return nullptr;
}

void WidgetEventPanel::onBtnBuzzClicked()
{
    DialogBuzz* p_dlg = new DialogBuzz( _p_webApp, this );
    p_dlg->setupUI( _event );
    p_dlg->exec();
    delete p_dlg;
}

void WidgetEventPanel::onDeleteLocation( QString id )
{
    _p_webApp->getEvents()->requestRemoveLocation( _event->getId(), id );
}

void WidgetEventPanel::onSendMessage( m4e::chat::ChatMessagePtr msg )
{
    if ( !_p_chatSystem )
        return;

    msg->setReceiverId( _event->getId() );
    _p_chatSystem->sendToEventMembers( msg );
}

void WidgetEventPanel::onReceivedChatMessageEvent( chat::ChatMessagePtr msg )
{
    _p_ui->widgetChat->appendChatText( msg );
}

void WidgetEventPanel::onResponseRemoveLocation( bool success, QString eventId, QString locationId )
{
    QString text;
    if ( !success )
    {
        text = QApplication::translate( "WidgetEvent", "Could not remove location.\nReason: " ) + _p_webApp->getEvents()->getLastError();
        log_debug << TAG << "event removal failed: " + eventId << "/" << locationId << std::endl;
    }
    else
    {
        text = QApplication::translate( "WidgetEvent", "Event location was successfully removed" );
        log_debug << TAG << "event successfully removed: " + eventId << "/" << locationId << std::endl;

        auto* p_item = findLocationItem( locationId );
        if ( p_item )
        {
            delete p_item;
        }

        if ( !_event->removeLocation( locationId ) )
        {
            log_warning << TAG << "could not remove location, it was not found in event" << std::endl;
        }
    }

    common::DialogMessage msg( this );
    msg.setupUI( QApplication::translate( "WidgetEvent", "Remove Location" ),
                 text,
                 common::DialogMessage::BtnOk );

    msg.exec();
}

void WidgetEventPanel::onResponseGetLocationVotesByTime( bool success, QList< ModelLocationVotesPtr > votes )
{
    if ( !success )
    {
        QString err = _p_webApp->getEvents()->getLastError();
        log_warning << TAG << "problem occured while retrieving the event location votes: " << err << std::endl;
    }
    else
    {
        for ( ModelLocationVotesPtr v: votes )
        {
            WidgetLocation* p_widget = findWidgetLocation( v->getLocationId() );
            if ( p_widget )
            {
                p_widget->updateVotes( v );
            }
        }
    }
}

} // namespace event
} // namespace m4e
