/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "mailboxwindow.h"
#include <core/log.h>
#include <common/basedialog.h>
#include <common/dialogmessage.h>
#include <mailbox/widgetmaillist.h>
#include <mailbox/widgetmailedit.h>
#include "ui_mailboxwindow.h"


namespace m4e
{
namespace gui
{

MailboxWindow::MailboxWindow( webapp::WebApp* p_webApp, QWidget* p_parent ) :
 QMainWindow( p_parent ),
 _p_ui( new Ui::MailboxWindow ),
 _p_webApp( p_webApp )
{
    setWindowFlags( Qt::Window | /*Qt::FramelessWindowHint |*/ Qt::CustomizeWindowHint );

    _p_ui->setupUi( this );

    // center the window on the parent window if one exists
    if ( p_parent )
    {
        QRect geom = geometry();
        QPoint parentcenter = p_parent->geometry().center();
        QSize size = geometry().size();
        geom.moveTopLeft( QPoint( parentcenter.x() - size.width() / 2, parentcenter.y() - size.height() / 2 ) );
        setGeometry( geom );
    }

    clearWidgetClientArea();
    createWidgetMyMails();
}

MailboxWindow::~MailboxWindow()
{
    delete _p_ui;
}

void MailboxWindow::onBtnCloseClicked()
{
    emit onMailWindowClosed();
    deleteLater();
}

void MailboxWindow::mouseDoubleClickEvent( QMouseEvent* p_event )
{
    // drag the window only by the means of head-bar
    if ( !_p_ui->widgetHead->geometry().contains( p_event->pos() ) )
        return;

    onBtnMaximizeClicked();
}

void MailboxWindow::mousePressEvent( QMouseEvent* p_event )
{
    // drag the window only by the means of head-bar
    if ( !_p_ui->widgetHead->geometry().contains( p_event->pos() ) )
        return;

    _draggingPos = p_event->pos();
    _dragging = true;
}

void MailboxWindow::mouseReleaseEvent( QMouseEvent* /*p_event*/ )
{
    _dragging = false;
}

void MailboxWindow::mouseMoveEvent( QMouseEvent* p_event )
{
    if ( _dragging )
    {
        move( p_event->globalPos() - _draggingPos );
    }
}

void MailboxWindow::onBtnMinimizeClicked()
{
    setWindowState( Qt::WindowMinimized );
}

void MailboxWindow::onBtnMaximizeClicked()
{
    if ( windowState() & Qt::WindowMaximized )
    {
        setWindowState( windowState() & ~Qt::WindowMaximized );
    }
    else
    {
        setWindowState( Qt::WindowMaximized );
    }
}

void MailboxWindow::onBtnNewMailClicked()
{
    clearWidgetClientArea();

    mailbox::WidgetMailEdit* p_widget = new mailbox::WidgetMailEdit( _p_webApp, this );
    mailbox::ModelMailPtr mail = new mailbox::ModelMail();
    mail->setSenderId( _p_webApp->getUser()->getUserData()->getId() );
    p_widget->setupUI( mail, false );

    _p_ui->widgetClientArea->layout()->addWidget( p_widget );
}

void MailboxWindow::onMailSelection( QString mailId )
{
    clearWidgetClientArea();

    mailbox::ModelMailPtr mail;
    // empty id means take the first mail if any exist
    if ( mailId.isEmpty() )
    {
        if ( _p_webApp->getMailBox()->getAllMails().size() > 0 )
            mail = _p_webApp->getMailBox()->getAllMails().at( 0 );
    }
    else
    {
        mail = _p_webApp->getMailBox()->getMail( mailId );
    }

    if ( !mail.valid() )
        return;

    mailbox::WidgetMailEdit* p_widget = new mailbox::WidgetMailEdit( _p_webApp, this );
    p_widget->setupUI( mail, true );
    _p_ui->widgetClientArea->layout()->addWidget( p_widget );
}

void MailboxWindow::clearWidgetClientArea()
{
    QLayoutItem* p_item;
    QLayout* p_layout = _p_ui->widgetClientArea->layout();
    while ( ( p_layout->count() > 0 ) && ( nullptr != ( p_item = _p_ui->widgetClientArea->layout()->takeAt( 0 ) ) ) )
    {
        p_item->widget()->deleteLater();
        delete p_item;
    }
}

void MailboxWindow::clearWidgetMyMails()
{
    QLayoutItem* p_item;
    QLayout* p_layout = _p_ui->widgetMailItems->layout();
    while ( ( p_layout->count() > 0 ) && ( nullptr != ( p_item = _p_ui->widgetMailItems->layout()->takeAt( 0 ) ) ) )
    {
        p_item->widget()->deleteLater();
        delete p_item;
    }
}

void MailboxWindow::createWidgetMyMails()
{
    clearWidgetClientArea();

    mailbox::WidgetMailList* p_widget = new mailbox::WidgetMailList( _p_webApp, this );
    _p_ui->widgetMailItems->layout()->addWidget( p_widget );
    connect( p_widget, SIGNAL( onMailSelection( QString /*id*/ ) ), this, SLOT( onMailSelection( QString /*id*/ ) ) );
    p_widget->selectFirstMail();
}

} // namespace gui
} // namespace m4e
