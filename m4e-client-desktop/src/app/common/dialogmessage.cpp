/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "dialogmessage.h"
#include "guiutils.h"
#include <QApplication>
#include <QHBoxLayout>
#include <QLabel>

namespace m4e
{
namespace common
{

DialogMessage::DialogMessage( QWidget* p_parent ) :
 common::BaseDialog( p_parent )
{
}

DialogMessage::~DialogMessage()
{
}

void DialogMessage::setupUI( const QString& title, const QString& message, unsigned int buttons )
{
    setTitle( title );
    getClientArea()->setLayout( new QHBoxLayout() ) ;

    QString  btn1   = QApplication::translate( "DialogMessage", "Ok" );
    QString  btn2   = QApplication::translate( "DialogMessage", "Yes" );
    QString  btn3   = QApplication::translate( "DialogMessage", "No" );
    QString* p_btn1 = ( buttons & BtnOk )  != 0 ? &btn1 : nullptr;
    QString* p_btn2 = ( buttons & BtnYes ) != 0 ? &btn2 : nullptr;
    QString* p_btn3 = ( buttons & BtnNo )  != 0 ? &btn3 : nullptr;
    setupButtons(p_btn1, p_btn2, p_btn3 );

    QLabel* p_label = new QLabel();
    p_label->setText( message );
    getClientArea()->layout()->addWidget( p_label );
    setResizable( false );
}

} // namespace common
} // namespace m4e
