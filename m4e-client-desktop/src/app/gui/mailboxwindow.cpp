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
#include <settings/appsettings.h>
#include <mailbox/widgetmaillist.h>
#include <mailbox/widgetmailedit.h>
#include "ui_mailboxwindow.h"


namespace m4e
{
namespace gui
{

MailboxWindow::MailboxWindow( webapp::WebApp* p_webApp, QWidget* p_parent ) :
 QMainWindow( nullptr ),
 _p_ui( new Ui::MailboxWindow ),
 _p_webApp( p_webApp )
{
    setWindowFlags( Qt::Window /*| Qt::FramelessWindowHint*/ | Qt::CustomizeWindowHint );

    _p_ui->setupUi( this );

    restoreWindowGeometry();

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
    storeWindowGeometry();
}

void MailboxWindow::storeWindowGeometry()
{
    QSettings* p_settings = settings::AppSettings::get()->getSettings();
    QByteArray geom = saveGeometry();
    p_settings->setValue( M4E_SETTINGS_KEY_MAILBOX_GEOM, geom );
}

void MailboxWindow::restoreWindowGeometry()
{
    QSettings* p_settings = settings::AppSettings::get()->getSettings();
    QByteArray geom =  p_settings->value( M4E_SETTINGS_KEY_MAILBOX_GEOM ).toByteArray();
    restoreGeometry( geom );
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

void MailboxWindow::keyPressEvent( QKeyEvent* p_event )
{
    if ( p_event->key() == Qt::Key_Escape )
    {
        onBtnCloseClicked();
        return;
    }

    QMainWindow::keyPressEvent( p_event );
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

    connect( p_widget, SIGNAL( onMailSent() ), this, SLOT( onMailSent() ) );

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

void MailboxWindow::onMailSent()
{
    //! NOTE we may implement a more resource friendly update of mail list in future. for now we just refresh the mymails list
    clearWidgetClientArea();
    clearWidgetMyMails();
    createWidgetMyMails();
    onMailSelection( "" );
}

void MailboxWindow::clearWidgetClientArea()
{
    QLayoutItem* p_item;
    QLayout* p_layout = _p_ui->widgetClientArea->layout();
    while ( ( p_layout->count() > 0 ) && ( nullptr != ( p_item = p_layout->takeAt( 0 ) ) ) )
    {
        p_item->widget()->deleteLater();
        delete p_item;
    }
}

void MailboxWindow::clearWidgetMyMails()
{
    QLayoutItem* p_item;
    QLayout* p_layout = _p_ui->widgetMailItems->layout();
    while ( ( p_layout->count() > 0 ) && ( nullptr != ( p_item = p_layout->takeAt( 0 ) ) ) )
    {
        p_item->widget()->deleteLater();
        delete p_item;
    }
}

void MailboxWindow::createWidgetMyMails()
{
    clearWidgetClientArea();

    mailbox::WidgetMailList* p_widget = new mailbox::WidgetMailList( _p_webApp, this, _p_ui );
    _p_ui->widgetMailItems->layout()->addWidget( p_widget );
    connect( p_widget, SIGNAL( onMailSelection( QString /*id*/ ) ), this, SLOT( onMailSelection( QString /*id*/ ) ) );
    p_widget->selectFirstMail();
}

} // namespace gui
} // namespace m4e
