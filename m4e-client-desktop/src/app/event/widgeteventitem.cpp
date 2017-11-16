/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "widgeteventitem.h"
#include <core/log.h>
#include <common/guiutils.h>
#include <common/dialogmessage.h>
#include "dialogeventsettings.h"
#include "dialoglocationedit.h"
#include <ui_widgeteventitem.h>


namespace m4e
{
namespace event
{

//! Event Box style
static const QString boxStyle = \
     "#groupBoxMain { \
        border-radius: 0px; \
        border: 1px solid @BORDERCOLOR@; \
        background-color: rgb(80,112,125); \
      }";

//! Normal border color
static const QString boxBorderColorNormal = "rgb(131, 147, 167)";

//! Border color for selected mode
static const QString boxBorderColorSelect = "rgb(231, 247, 167)";


WidgetEventItem::WidgetEventItem( webapp::WebApp* p_webApp, QWidget* p_parent ) :
 QWidget( p_parent ),
 _p_webApp( p_webApp)
{
    _p_ui = new Ui::WidgetEventItem();
}

WidgetEventItem::~WidgetEventItem()
{
    delete _p_ui;
}

void WidgetEventItem::setupUI( event::ModelEventPtr event )
{
    _p_ui->setupUi( this );

    connect( _p_webApp->getNotifications(), SIGNAL( onEventChanged( m4e::notify::Notifications::ChangeType, QString ) ), this,
                                            SLOT( onEventChanged( m4e::notify::Notifications::ChangeType, QString ) ) );

    connect( _p_webApp->getNotifications(), SIGNAL( onEventLocationChanged( m4e::notify::Notifications::ChangeType, QString, QString ) ), this,
                                            SLOT( onEventLocationChanged( m4e::notify::Notifications::ChangeType, QString, QString ) ) );

    connect( _p_webApp, SIGNAL( onDocumentReady( m4e::doc::ModelDocumentPtr ) ), this,
                        SLOT( onDocumentReady( m4e::doc::ModelDocumentPtr ) ) );

    connect( _p_webApp->getEvents(), SIGNAL( onLocationVotingStart( m4e::event::ModelEventPtr ) ), this,
                                     SLOT( onLocationVotingStart( m4e::event::ModelEventPtr ) ) );

    connect( _p_webApp->getEvents(), SIGNAL( onLocationVotingEnd( m4e::event::ModelEventPtr ) ), this,
                                     SLOT( onLocationVotingEnd( m4e::event::ModelEventPtr ) ) );

    common::GuiUtils::createShadowEffect( this, QColor( 100, 100, 100, 180), QPoint( -2, 2 ), 4 );
    setSelectionMode( true );
    updateEvent( event );
}

void WidgetEventItem::updateEvent( ModelEventPtr event )
{
    _event = event;

    _p_ui->widgetVotingTime->setVisible( _p_webApp->getEvents()->getIsVotingTime( _event->getId() ) );

    // is the user also the owner of the event? some operations are only permitted to owner
    _userIsOwner = common::GuiUtils::userIsOwner( event->getOwner()->getId(), _p_webApp );
    _p_ui->labelHead->setText( event->getName() );
    _p_ui->labelDescription->setText( event->getDescription() );
    _p_ui->pushButtonEdit->setHidden( !_userIsOwner );
    _p_ui->pushButtonDelete->setHidden( !_userIsOwner );
    _p_ui->pushButtonNewLocation->setHidden( !_userIsOwner );
    _p_ui->pushButtonNotification->hide();

    // we need to handle mouse clicks manually
    _p_ui->labelHead->installEventFilter( this );
    _p_ui->labelDescription->installEventFilter( this );
    _p_ui->groupBoxMain->installEventFilter( this );
    _p_ui->labelPhoto->installEventFilter( this );

    // load  the image only if a valid photo id exits
    QString photoid = event->getPhotoId();
    if ( !photoid.isEmpty() && ( photoid != "0" ) )
    {
        _p_webApp->requestDocument( photoid, event->getPhotoETag() );
    }
    else
    {
        _p_ui->labelPhoto->setPixmap( common::GuiUtils::createRoundIcon( common::GuiUtils::getDefaultPixmap() ) );
    }
}

void WidgetEventItem::setSelectionMode( bool normal )
{
    QString style = boxStyle;
    style.replace( "@BORDERCOLOR@", ( normal ? boxBorderColorNormal : boxBorderColorSelect ) );
    _p_ui->groupBoxMain->setStyleSheet( style );

    QColor shadowcolor = normal ? QColor( 100, 100, 100, 180) : QColor( 231, 247, 167 , 180 );
    common::GuiUtils::createShadowEffect( this, shadowcolor, QPoint( -3, 3 ), 6 );
}

void WidgetEventItem::notifyUpdate( const QString& text )
{
    _p_ui->pushButtonNotification->show();
    _p_ui->pushButtonNotification->setToolTip( text );
}

void WidgetEventItem::createNewLocation()
{
    onBtnNewLocationClicked();
}

void WidgetEventItem::onBtnEditClicked()
{
    DialogEventSettings dlg( _p_webApp, this );
    dlg.setupUI( _event );
    if ( dlg.exec() != DialogEventSettings::BtnApply )
        return;

    // update event data
    onBtnNotificationClicked();
    emit onClicked( _event->getId() );
}

void WidgetEventItem::onBtnDeleteClicked()
{
    common::DialogMessage msg( this );
    msg.setupUI( QApplication::translate( "WidgetEventItem", "Delete Event" ),
                 QApplication::translate( "WidgetEventItem", "Do you really want to delete the event?" ),
                 common::DialogMessage::BtnYes | common::DialogMessage::BtnNo );

    if ( msg.exec() == common::DialogMessage::BtnNo )
    {
        return;
    }

    // the actual deletion is delegated
    emit onRequestDeleteEvent( _event->getId() );
}

void WidgetEventItem::onBtnNewLocationClicked()
{
    DialogLocationEdit* p_dlg = new DialogLocationEdit( _p_webApp, this );
    p_dlg->setupUINewLocation( _event );
    p_dlg->exec();
    delete p_dlg;

    // update event data
    onBtnNotificationClicked();
}

void WidgetEventItem::onBtnNotificationClicked()
{
    _p_ui->pushButtonNotification->hide();
    emit onRequestUpdateEvent( _event->getId() );
}

void WidgetEventItem::onDocumentReady( m4e::doc::ModelDocumentPtr document )
{
    QString photoid = _event->getPhotoId();
    if ( !photoid.isEmpty() && document.valid() && ( document->getId() == photoid ) )
    {
        _p_ui->labelPhoto->setPixmap( common::GuiUtils::createRoundIcon( document ) );
    }
}

void WidgetEventItem::onEventChanged( notify::Notifications::ChangeType /*changeType*/, QString eventId )
{
    if ( !_event.valid() || ( _event->getId() != eventId ) )
        return;

    notifyUpdate( QApplication::translate( "WidgetEventItem", "Event settings were changed, click to updage!") );
}

void WidgetEventItem::onEventLocationChanged( notify::Notifications::ChangeType /*changeType*/, QString eventId, QString /*locationId*/ )
{
    if ( !_event.valid() || ( _event->getId() != eventId ) )
        return;

    notifyUpdate( QApplication::translate( "WidgetEventItem", "Event location settings were changed, click to updage!") );
}

void WidgetEventItem::onLocationVotingStart( m4e::event::ModelEventPtr event )
{
    if ( event != _event )
        return;

    log_verbose << TAG << "time to vote for event locations" << std::endl;
    _p_ui->widgetVotingTime->setVisible( true );
}

void WidgetEventItem::onLocationVotingEnd( m4e::event::ModelEventPtr event )
{
    if ( event != _event )
        return;

    log_verbose << TAG << "end of to voting time reached" << std::endl;
    _p_ui->widgetVotingTime->setVisible( false );
}

bool WidgetEventItem::eventFilter( QObject* p_obj, QEvent* p_event )
{
    if ( p_event->type() == QEvent::MouseButtonPress )
    {
        emit onClicked( _event->getId() );
        return true;
    }

    return QObject::eventFilter( p_obj, p_event );
}

} // namespace event
} // namespace m4e
