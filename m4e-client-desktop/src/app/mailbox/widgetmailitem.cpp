/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "widgetmailitem.h"
#include <core/log.h>
#include <common/guiutils.h>
#include <common/dialogmessage.h>
#include <ui_widgetmailitem.h>


namespace m4e
{
namespace mailbox
{

//! Mail Box style
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

//! Border color for selected mode
static const QString boxBorderColorUnread = "rgb(228,107, 0)";


WidgetMailItem::WidgetMailItem( webapp::WebApp* p_webApp, QWidget* p_parent ) :
 QWidget( p_parent ),
 _p_webApp( p_webApp)
{
    _p_ui = new Ui::WidgetMailItem();
}

WidgetMailItem::~WidgetMailItem()
{
    delete _p_ui;
}

void WidgetMailItem::setupUI( ModelMailPtr mail )
{
    _mail = mail;
    _p_ui->setupUi( this );

    common::GuiUtils::createShadowEffect( this, QColor( 100, 100, 100, 180), QPoint( -2, 2 ), 4 );
    setSelectionMode( true );

    // we need to handle mouse clicks manually
    _p_ui->groupBoxMain->installEventFilter( this );

   bool outgoing = mail->getSenderId() == _p_webApp->getUser()->getUserData()->getId();

    QString sender = ( mail->getSenderId() != "0" )  ? mail->getSenderName() : M4E_MAIL_SENDER_SYSTEM_NAME;
    const QDateTime& timestamp = mail->getDate();
    _p_ui->labelDate->setText( timestamp.toString( "yyyy-MM-dd  HH:mm" ) );
    QString shorttext = ( outgoing ? "↑ <strong>(me)</strong> - " : "↓ <strong>" + sender + "</strong> - " ) + mail->getSubject();
    if ( shorttext.length() > 60 )
        shorttext = shorttext.mid( 0, 60 ) + "...";
    _p_ui->labelShortText->setText( shorttext );
    _p_ui->pushButtonDelete->setChecked( _mail->isTrashed() );
    if ( _mail->isTrashed() )
        _p_ui->pushButtonDelete->setToolTip( QApplication::translate( "WidgetMailItem", "Untrash Mail" ) );
    else
        _p_ui->pushButtonDelete->setToolTip( QApplication::translate( "WidgetMailItem", "Trash Mail" ) );
}

void WidgetMailItem::setSelectionMode( bool normal )
{
    _selectionMode = normal;
    QString style = boxStyle;
    QColor shadowcolor;
    if ( _mail->isUnread() && normal )
    {
        style.replace( "@BORDERCOLOR@", boxBorderColorUnread );
        shadowcolor = QColor( 228,107, 0, 180);
    }
    else
    {
        style.replace( "@BORDERCOLOR@", ( normal ? boxBorderColorNormal : boxBorderColorSelect ) );
        shadowcolor = normal ? QColor( 100, 100, 100, 180) : QColor( 231, 247, 167 , 180 );
    }

    _p_ui->groupBoxMain->setStyleSheet( style );
    common::GuiUtils::createShadowEffect( this, shadowcolor, QPoint( -3, 3 ), 6 );
}

void WidgetMailItem::setUnread( bool unread )
{
    _mail->setUnread( unread );
    setSelectionMode( _selectionMode );
}

void WidgetMailItem::onBtnDeleteClicked()
{
    emit onRequestDeleteMail( _mail->getId() );
}

bool WidgetMailItem::eventFilter( QObject* p_obj, QEvent* p_event )
{
    if ( p_event->type() == QEvent::MouseButtonPress )
    {
        emit onClicked( _mail->getId() );
        return true;
    }

    return QObject::eventFilter( p_obj, p_event );
}

} // namespace mailbox
} // namespace m4e
