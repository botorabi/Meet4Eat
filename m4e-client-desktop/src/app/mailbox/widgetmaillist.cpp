/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include "widgetmaillist.h"
#include <core/log.h>
#include <common/dialogmessage.h>
#include "widgetmailitem.h"
#include <QApplication>
#include <QListWidgetItem>
#include <QScrollBar>
#include <QVBoxLayout>


namespace m4e
{
namespace mailbox
{

const static QString MAIL_LIST_STYLESHEET = \
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


WidgetMailList::WidgetMailList( webapp::WebApp* p_webApp, QWidget* p_parent ) :
 QListWidget( p_parent ),
 _p_webApp( p_webApp )
{
    setupUI();
}

void WidgetMailList::selectMail( const QString& mailId )
{
    onClicked( mailId );
}

void WidgetMailList::selectFirstMail()
{
    if ( _widgets.size() > 0 )
        onClicked( _widgets.at( 0 )->getId() );
}

void WidgetMailList::setupUI()
{
    connect( _p_webApp->getMailBox(), SIGNAL( onResponseMails( bool, QList< m4e::mailbox::ModelMailPtr > ) ), this, SLOT( onResponseMails( bool, QList< m4e::mailbox::ModelMailPtr > ) ) );
    connect( _p_webApp->getMailBox(), SIGNAL( onResponsePerformOperation( bool, QString, QString ) ), this, SLOT( onResponsePerformOperation( bool, QString, QString ) ) );

    setupListView();

    _p_webApp->getMailBox()->requestMails( 0, 10 );
}

void WidgetMailList::setupListView()
{
    // setup the list view
    setStyleSheet( MAIL_LIST_STYLESHEET );
    setSizePolicy( QSizePolicy::Policy::Expanding, QSizePolicy::Policy::Expanding );
    setVerticalScrollMode( ScrollPerPixel );
    setSizeAdjustPolicy( SizeAdjustPolicy::AdjustToContents );
    setHorizontalScrollBarPolicy( Qt::ScrollBarAlwaysOff );
    setVerticalScrollBarPolicy( Qt::ScrollBarAsNeeded );
    setAutoFillBackground( false );
    verticalScrollBar()->setSingleStep( 5 );
    setViewMode( QListView::IconMode );
    setDragEnabled( false );
    setFlow( QListWidget::TopToBottom );
    QVBoxLayout* p_layout = new QVBoxLayout( this );
    p_layout->setSpacing( 8 );
    p_layout->setContentsMargins( 4, 4, 4, 4 );
    setLayout( p_layout );
}

WidgetMailItem* WidgetMailList::findWidget( const QString& mailId )
{
    for ( WidgetMailItem* p_widget: _widgets )
    {
        if ( p_widget->getId() == mailId )
            return p_widget;
    }
    return nullptr;
}

void WidgetMailList::addMail( m4e::mailbox::ModelMailPtr mail )
{
    WidgetMailItem* p_itemwidget = new WidgetMailItem( _p_webApp, this );
    p_itemwidget->setupUI( mail );
    connect( p_itemwidget, SIGNAL( onClicked( QString ) ), this, SLOT( onClicked( QString ) ) );
    connect( p_itemwidget, SIGNAL( onRequestDeleteMail( QString ) ), this, SLOT( onRequestDeleteMail( QString ) ) );

    QListWidgetItem* p_listitem = new QListWidgetItem( this );
    p_listitem->setSizeHint( p_itemwidget->sizeHint() );
    p_listitem->setFlags( Qt::NoItemFlags );

    addItem( p_listitem );
    setItemWidget( p_listitem, p_itemwidget );

    _widgets.append( p_itemwidget );
}

void WidgetMailList::onClicked( QString id )
{
    for ( WidgetMailItem* p_widget: _widgets )
    {
        p_widget->setSelectionMode( p_widget->getId() != id );
    }

    // forward the signal
    emit onMailSelection( id );

    mailbox::ModelMailPtr mail = _p_webApp->getMailBox()->getMail( id );
    if ( mail.valid() && mail->isUnread() )
        _p_webApp->getMailBox()->requestMarkMail( id, true );
}

void WidgetMailList::onRequestDeleteMail( QString id )
{
    _p_webApp->getMailBox()->requestDeleteMail( id );
}

void WidgetMailList::onResponseMails( bool success, QList< ModelMailPtr > mails )
{
    int trashedmails = 0;
    if ( success )
    {
        for ( auto mail: mails )
        {
            //! NOTE currently we do not show trashed mails. in future we may provide a "untrash" function, though
            if ( !mail->isTrashed() )
                addMail( mail );
            else
                trashedmails++;
        }
    }
    log_verbose << TAG << "user has " << mails.size() << " mails, " << trashedmails << " trashed" << std::endl;

    // select the first mail in list
    emit onMailSelection( "" );
}

void WidgetMailList::onResponsePerformOperation( bool success, QString mailId, QString operation )
{
    if ( !success )
    {
        QString text = QApplication::translate( "WidgetMailList", "Could not perform the mail operation.\nReason: " ) + _p_webApp->getMailBox()->getLastError();
        common::DialogMessage msg( this );
        msg.setupUI( QApplication::translate( "WidgetMailList", "Mail Operation" ),
                     text,
                     common::DialogMessage::BtnOk );
        msg.exec();
    }

    WidgetMailItem* p_widget = findWidget( mailId );
    mailbox::ModelMailPtr mail = _p_webApp->getMailBox()->getMail( mailId );
    if ( p_widget )
    {
        if ( operation == "trash" )
        {
            // remove the item from list
            p_widget->deleteLater();
            _widgets.removeAll( p_widget );
            onClicked( "" );
        }
        else if ( operation == "read" )
        {
            if ( mail.valid() )
            {
                p_widget->setUnread( false );
            }
        }
        else if ( operation == "unread" )
        {
            if ( mail.valid() )
            {
                p_widget->setUnread( true );
            }
        }
    }
}

} // namespace mailbox
} // namespace m4e
