/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "basedialog.h"
#include <ui_basedialog.h>
#include <QMouseEvent>


namespace m4e
{
namespace common
{

BaseDialog::BaseDialog( QWidget* p_parent ) :
 QDialog( p_parent )
{
    setWindowFlags( Qt::Window | Qt::FramelessWindowHint | Qt::CustomizeWindowHint );

    _p_ui = new Ui::BaseDlg();
    _p_ui->setupUi( this );
    _p_ui->pushButtonResizer->setControlledWidget( this );
    _p_ui->labelTitle->setText( "" );
}

BaseDialog::~BaseDialog()
{
    delete _p_ui;
}

void BaseDialog::setTitle( const QString& title )
{
    _p_ui->labelTitle->setText( title );
}

void BaseDialog::setupButtons( QString* p_btn1Text, QString* p_btn2Text, QString* p_btn3Text )
{
    // if only one button is defined then center it on dialog bottom
    int numbtns = 0;
    numbtns += p_btn1Text ? 1 : 0;
    numbtns += p_btn2Text ? 1 : 0;
    numbtns += p_btn2Text ? 1 : 0;
    if ( numbtns > 1 )
        _p_ui->layoutFooter->removeItem( _p_ui->spacerRight );

    _p_ui->pushButton1->setText( p_btn1Text ? *p_btn1Text : "" );
    _p_ui->pushButton1->setVisible( p_btn1Text ? true : false );
    _p_ui->pushButton2->setText( p_btn2Text ? *p_btn2Text : "" );
    _p_ui->pushButton2->setVisible( p_btn2Text ? true : false );
    _p_ui->pushButton3->setText( p_btn3Text ? *p_btn3Text : "" );
    _p_ui->pushButton3->setVisible( p_btn3Text ? true : false );
}

QWidget*BaseDialog::getClientArea()
{
    return _p_ui->widgetClientArea;
}

void BaseDialog::setResizable( bool resizable )
{
    adjustSize();
    if ( !resizable )
    {
        QRect geom = geometry();
        setMinimumSize( geom.size() );
        setMaximumSize( geom.size() );
    }
    else
    {
        setMinimumSize( QSize( 0, 0 ) );
        setMaximumSize( QSize( 16777215, 16777215 ) );
    }
    _p_ui->widgetFoot->setVisible( resizable );
}

void BaseDialog::onBtnCloseClicked()
{
    if ( onClose() )
        done( BtnClose );
}

void BaseDialog::onBtn1Clicked()
{
    if ( onButton1Clicked() )
        done( Btn1 );
}

void BaseDialog::onBtn2Clicked()
{
    if ( onButton2Clicked() )
        done( Btn2 );
}

void BaseDialog::onBtn3Clicked()
{
    if ( onButton3Clicked() )
        done( Btn3 );
}

void BaseDialog::mousePressEvent( QMouseEvent* p_event )
{
    // drag the window only by the means of head-bar
    if ( !_p_ui->widgetHead->geometry().contains( p_event->pos() ) )
        return;

    _draggingPos = p_event->pos();
    _dragging = true;
}

void BaseDialog::mouseReleaseEvent( QMouseEvent* /*p_event*/ )
{
    _dragging = false;
}

void BaseDialog::mouseMoveEvent( QMouseEvent* p_event )
{
    if ( _dragging )
    {
        move( p_event->globalPos() - _draggingPos );
    }
}

} // namespace common
} // namespace m4e
