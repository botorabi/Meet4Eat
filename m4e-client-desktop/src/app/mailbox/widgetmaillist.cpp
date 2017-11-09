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
#include "ui_mailboxwindow.h"
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

static int MAIL_PAGE_SIZE = 10;

WidgetMailList::WidgetMailList( webapp::WebApp* p_webApp, QWidget* p_parent, Ui::MailboxWindow* p_ui ) :
 QListWidget( p_parent ),
 _p_webApp( p_webApp ),
 _p_ui( p_ui )
{
    setupUI();

    _rangeFrom = 0;
    _rangeTo   = _rangeFrom + MAIL_PAGE_SIZE;
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
    connect( _p_webApp->getMailBox(), SIGNAL( onResponseCountMails( bool, int, int ) ), this,
                                      SLOT( onResponseCountMails( bool, int, int ) ) );

    connect( _p_ui->pushButtonNext, SIGNAL( clicked() ), this, SLOT( onBtnNextClicked() ) );
    connect( _p_ui->pushButtonPrev, SIGNAL( clicked() ), this, SLOT( onBtnPrevClicked() ) );

    setupListView();
    _p_webApp->getMailBox()->requestCountMails();
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
    setWrapping( true );
    setViewMode( QListView::IconMode );
    setDragEnabled( false );
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

void WidgetMailList::clearMails()
{
    for ( WidgetMailItem* p_widget: _widgets )
    {
        p_widget->deleteLater();
    }
    _widgets.clear();
    clear();
}

void WidgetMailList::addMail( m4e::mailbox::ModelMailPtr mail )
{
    WidgetMailItem* p_itemwidget = new WidgetMailItem( _p_webApp, this );
    p_itemwidget->setupUI( mail );
    connect( p_itemwidget, SIGNAL( onClicked( QString ) ), this, SLOT( onClicked( QString ) ) );
    connect( p_itemwidget, SIGNAL( onRequestDeleteMail( QString ) ), this, SLOT( onRequestDeleteMail( QString ) ) );

    QListWidgetItem* p_listitem = new QListWidgetItem( this );
    p_listitem->setFlags( Qt::NoItemFlags );
    p_listitem->setSizeHint( p_itemwidget->size() );

    addItem( p_listitem );
    setItemWidget( p_listitem, p_itemwidget );

    _widgets.append( p_itemwidget );
}

void WidgetMailList::onBtnNextClicked()
{
    if ( _rangeTo >= _countMails )
        return;

    _rangeFrom += MAIL_PAGE_SIZE;
    _rangeTo    = _rangeFrom + MAIL_PAGE_SIZE;
    _rangeFrom  = std::min( _countMails, _rangeFrom );
    _rangeTo    = std::min( _countMails, _rangeTo );
    _p_webApp->getMailBox()->requestMails( _rangeFrom, _rangeTo - 1 );
}

void WidgetMailList::onBtnPrevClicked()
{
    if ( _rangeFrom < 1 )
        return;

    _rangeFrom -= MAIL_PAGE_SIZE;
    _rangeTo    = _rangeFrom + MAIL_PAGE_SIZE;
    _rangeFrom  = std::max( 0, _rangeFrom );
    _rangeTo    = std::max( 1, _rangeTo );
    _p_webApp->getMailBox()->requestMails( _rangeFrom, _rangeTo - 1 );
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

void WidgetMailList::onResponseCountMails( bool success, int countTotal, int /*countUnread*/ )
{
    if ( success )
    {
        // adapt the range if necessary
        _countMails = countTotal;
        _rangeTo = std::min( _countMails, _rangeTo );
        _rangeFrom = _rangeTo - MAIL_PAGE_SIZE;
        _rangeFrom = std::max( 0, _rangeFrom );
        // request for the mails
        _p_webApp->getMailBox()->requestMails( _rangeFrom, _rangeTo - 1 );
    }
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
        clearMails();
        for ( auto mail: mails )
        {
            //! NOTE currently we do not show trashed mails. in future we may provide a "untrash" function, though
            if ( !mail->isTrashed() )
                addMail( mail );
            else
                trashedmails++;
        }

        QString range = QString::number( _rangeFrom ) + "-" + QString::number( _rangeTo ) +
                        " (" + QString::number( _countMails ) + ")";
        _p_ui->labelMailRange->setText( range );
    }
    log_verbose << TAG << "got user's mails, trashedmails" << " trashed" << std::endl;

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
            _p_webApp->getMailBox()->requestMails( _rangeFrom, _rangeTo - 1 );
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
