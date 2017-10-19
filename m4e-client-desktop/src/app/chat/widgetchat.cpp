/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include "widgetchat.h"
#include <core/log.h>
#include <core/utils.h>
#include <common/guiutils.h>
#include <ui_widgetchat.h>
#include <QListWidgetItem>
#include <assert.h>


namespace m4e
{
namespace chat
{

WidgetChat::WidgetChat( QWidget* p_parent ) :
 QWidget( p_parent )
{
}

WidgetChat::~WidgetChat()
{
    if ( _p_ui )
        delete _p_ui;
}

void WidgetChat::setupUI(  webapp::WebApp* p_webApp )
{
    _p_webApp = p_webApp;
    _p_ui = new Ui::WidgetChat;
    _p_ui->setupUi( this );
    connect( _p_webApp, SIGNAL( onDocumentReady( m4e::doc::ModelDocumentPtr ) ), this, SLOT( onDocumentReady( m4e::doc::ModelDocumentPtr ) ) );
}

void WidgetChat::setMembers( const QList< user::ModelUserInfoPtr > users )
{
    assert( _p_webApp && "widget was not setup before!" );

    int row = 0;
    for ( user::ModelUserInfoPtr user: users )
    {
        QListWidgetItem* p_item = new QListWidgetItem( QIcon(), user->getName() );

        if ( user->getStatus() != "online" )
        {
            p_item->setForeground( QBrush( QColor( 150, 150, 150 ) ) );
            p_item->setToolTip( QApplication::translate( "WidgetChat", "User is currently offline") );
        }

        _p_ui->listWidgetMembers->insertItem( row, p_item );

        QString photoid = user->getPhotoId();
        if ( !photoid.isEmpty() && ( photoid != "0" ) )
        {
            _memberPhotos.insert( photoid, row );
            _p_webApp->requestDocument( photoid, user->getPhotoETag() );
        }
        else
        {
            QPixmap pix = common::GuiUtils::createRoundIcon( QPixmap( M4E_DEFAULT_USER_ICON ) );
            p_item->setIcon( QIcon( pix ) );
        }
        row++;
    }
}

void WidgetChat::appendChatText( ChatMessagePtr msg )
{
    if ( msg->getText().isEmpty() && !msg->getDocument().valid() )
        return;

    const QDateTime& timestamp = msg->getTime();
    QString ts = "[" + timestamp.toString( "yyyy-M-dd HH:mm:ss" ) + "]";
    QString output = "<p>";
    output += "<span style='color: gray;'>" + ts + "</span>";
    output += " <span style='color: brown;'>" + msg->getSender() + "</span>";
    output += "  <strong style='color: black;'>" + msg->getText() + "</strong>";
    output += "</p>";

    _p_ui->textOutput->appendHtml( output );
}

void WidgetChat::onBtnEmotieClicked()
{
    log_debug << TAG << "TODO onBtnEmotieClicked" << std::endl;
}

void WidgetChat::onEditLineReturnPressed()
{
    onBtnSendClicked();
}

void WidgetChat::onBtnSendClicked()
{
    ChatMessagePtr msg = new ChatMessage();
    // NOTE the remaing data such as recipient and sender will be added by signal receiver
    msg->setText( _p_ui->lineEditChat->text() );
    emit onSendMessage( msg );
    _p_ui->lineEditChat->setText( "" );
}

void WidgetChat::onBtnCollapseClicked()
{
    _p_ui->widgetMain->setVisible( !_p_ui->widgetMain->isVisible() );
    _p_ui->widgetMain->parentWidget()->updateGeometry();
}

void WidgetChat::onDocumentReady( m4e::doc::ModelDocumentPtr document )
{
    if ( !document.valid() )
        return;

    // check if a member photo arrived
    if ( !_memberPhotos.contains( document->getId() ) )
        return;

    int row = _memberPhotos.value( document->getId() );
    QPixmap pix = common::GuiUtils::createRoundIcon( document );
    QListWidgetItem* p_item = _p_ui->listWidgetMembers->item( row );
    p_item->setIcon( QIcon( pix ) );
}

} // namespace chat
} // namespace m4e
