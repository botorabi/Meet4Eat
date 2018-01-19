/**
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "widgeteventitem.h"
#include <core/log.h>
#include <common/guiutils.h>
#include <common/dialogmessage.h>
#include <chat/chatsystem.h>
#include "dialogeventsettings.h"
#include "dialoglocationedit.h"
#include <ui_widgeteventitem.h>
#include <QPropertyAnimation>


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

    connect( _p_webApp->getNotifications(), SIGNAL( onEventLocationVote( QString, QString, QString, QString, bool ) ), this,
                                            SLOT( onEventLocationVote( QString, QString, QString, QString, bool ) ) );

    connect( _p_webApp->getNotifications(), SIGNAL( onEventMessage( QString, QString, QString, m4e::notify::NotifyEventPtr ) ), this,
                                            SLOT( onEventMessage( QString, QString, QString, m4e::notify::NotifyEventPtr ) ) );

    connect( _p_webApp, SIGNAL( onDocumentReady( m4e::doc::ModelDocumentPtr ) ), this,
                        SLOT( onDocumentReady( m4e::doc::ModelDocumentPtr ) ) );

    connect( _p_webApp->getEvents(), SIGNAL( onLocationVotingStart( m4e::event::ModelEventPtr ) ), this,
                                     SLOT( onLocationVotingStart( m4e::event::ModelEventPtr ) ) );

    connect( _p_webApp->getEvents(), SIGNAL( onLocationVotingEnd( m4e::event::ModelEventPtr ) ), this,
                                     SLOT( onLocationVotingEnd( m4e::event::ModelEventPtr ) ) );

    connect( _p_webApp->getChatSystem(), SIGNAL( onReceivedChatMessageEvent( m4e::chat::ChatMessagePtr ) ), this, SLOT( onReceivedChatMessageEvent( m4e::chat::ChatMessagePtr ) ) );

    _p_ui->labelPhotoIcon->hide();

    common::GuiUtils::createShadowEffect( this, QColor( 100, 100, 100, 180), QPoint( -2, 2 ), 4 );
    setSelectionMode( true );
    updateEvent( event );
}

void WidgetEventItem::updateEvent( ModelEventPtr event )
{
    _event = event;

    _p_ui->widgetVotingTime->setVisible( _p_webApp->getEvents()->getIsVotingTime( _event->getId() ) );

    // is the user also the owner of the event? some operations are only permitted to owner
    _userIsOwner = _p_webApp->getUser()->isUserId( _event->getOwner()->getId() );
    _p_ui->labelHead->setText( event->getName() );
    _p_ui->labelDescription->setText( event->getDescription() );
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
        _p_ui->labelPhotoIcon->setPixmap( common::GuiUtils::createRoundIcon( common::GuiUtils::getDefaultPixmap() ) );
    }
}

void WidgetEventItem::setSelectionMode( bool normal )
{
    QString style = boxStyle;
    style.replace( "@BORDERCOLOR@", ( normal ? boxBorderColorNormal : boxBorderColorSelect ) );
    _p_ui->groupBoxMain->setStyleSheet( style );

    QColor shadowcolor = normal ? QColor( 100, 100, 100, 180) : QColor( 231, 247, 167 , 180 );
    common::GuiUtils::createShadowEffect( this, shadowcolor, QPoint( -3, 3 ), 6 );
    _selected = !normal;
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
    DialogEventSettings* p_dlg = new DialogEventSettings( _p_webApp, nullptr, true );
    p_dlg->setupUI( _event );
    p_dlg->show();
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
    DialogLocationEdit* p_dlg = new DialogLocationEdit( _p_webApp, nullptr );
    p_dlg->setupUINewLocation( _event );
    p_dlg->exec();
    delete p_dlg;
}

void WidgetEventItem::onBtnNotificationClicked()
{
    _p_ui->pushButtonNotification->hide();
}

void WidgetEventItem::onAnimationFinished()
{
    _animating = false;
}

void WidgetEventItem::onBtnCollapseToggled( bool toggled )
{
    // hide the description body and adapt the item height
    _p_ui->widgetBody->setVisible( !toggled );
    setMinimumHeight( toggled? height() -  _p_ui->widgetBody->height() : height() +  _p_ui->widgetBody->height() );
    resize( size().width(), minimumHeight() );
    updateGeometry();
    _p_ui->labelPhotoIcon->setVisible( toggled );
    // let the list containing this item know about the geometry change
    emit onItemGeometryChanged( _event->getId() );
}

void WidgetEventItem::onDocumentReady( m4e::doc::ModelDocumentPtr document )
{
    QString photoid = _event->getPhotoId();
    if ( !photoid.isEmpty() && document.valid() && ( document->getId() == photoid ) )
    {
        _p_ui->labelPhoto->setPixmap( common::GuiUtils::createRoundIcon( document ) );
        _p_ui->labelPhotoIcon->setPixmap( common::GuiUtils::createRoundIcon( document ) );
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

void WidgetEventItem::onReceivedChatMessageEvent( chat::ChatMessagePtr msg )
{
    if ( msg->getReceiverId() == _event->getId() && ( msg->getSenderId() != _p_webApp->getUser()->getUserData()->getId() ) )
        notifyUpdate( QApplication::translate( "WidgetEventItem", "New chat message arrived!") );
}

void WidgetEventItem::onEventMessage( QString senderId, QString /*senderName*/, QString eventId, notify::NotifyEventPtr /*notify*/ )
{
    if ( ( eventId != _event->getId() ) || ( senderId == _p_webApp->getUser()->getUserData()->getId() ) )
        return;

    notifyUpdate( QApplication::translate( "WidgetEventItem", "New message arrived!") );
}

void WidgetEventItem::onEventLocationVote( QString senderId, QString /*senderName*/, QString eventId, QString /*locationId*/, bool /*vote*/ )
{
    // suppress echo
    QString userid = _p_webApp->getUser()->getUserData()->getId();
    if ( senderId == userid )
        return;

    // is this vote for one of the locations of this event?
    if ( eventId != _event->getId() )
         return;

    animateItemWidget();
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
        if ( !_selected )
            emit onClicked( _event->getId() );

        _p_ui->pushButtonNotification->hide();
    }

    return QObject::eventFilter( p_obj, p_event );
}

void WidgetEventItem::animateItemWidget()
{
    if ( _animating )
        return;

    _animating = true;

    QRect geom = geometry();

    QPropertyAnimation* p_anim1 = new QPropertyAnimation( this, "geometry" );
    p_anim1->setDuration( 25 );
    p_anim1->setStartValue( geom );
    geom.moveLeft( geom.left() + 10 );
    p_anim1->setEndValue( geom );

    QPropertyAnimation* p_anim2 = new QPropertyAnimation( this, "geometry" );
    p_anim2->setDuration( 25 );
    p_anim2->setStartValue( geom );
    geom.moveLeft( geom.left() - 10 );
    p_anim2->setEndValue( geom );

    QSequentialAnimationGroup* p_anim = new QSequentialAnimationGroup( this );
    p_anim->addAnimation( p_anim1 );
    p_anim->addAnimation( p_anim2 );
    p_anim->setLoopCount( 10 );
    p_anim->start( QAbstractAnimation::DeleteWhenStopped );
    connect( p_anim, SIGNAL( finished() ),this, SLOT( onAnimationFinished() ) );
}

} // namespace event
} // namespace m4e
