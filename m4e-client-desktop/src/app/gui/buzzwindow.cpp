/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "buzzwindow.h"
#include <common/guiutils.h>
#include <ui_buzzwindow.h>
#include "mainwindow.h"
#include <QPropertyAnimation>
#include <QApplication>
#include <QDesktopWidget>


namespace m4e
{
namespace gui
{

static const QString M4E_BUZZ_SOUND = "qrc:/buzz.mp3";

BuzzWindow::BuzzWindow( MainWindow* p_parent ) :
 QMainWindow( p_parent ),
 _p_mainWindow( p_parent ),
 _p_ui( new Ui::BuzzWindow() )
{
    setWindowFlags( Qt::Window | Qt::FramelessWindowHint | Qt::CustomizeWindowHint );
    setAttribute( Qt::WA_NoSystemBackground );
    setAttribute( Qt::WA_TranslucentBackground );
    _p_ui->setupUi( this );
    move( QApplication::desktop()->screen()->rect().center() - rect().center() );

    _p_player = new QMediaPlayer( this );
}

BuzzWindow::~BuzzWindow()
{
    delete _p_ui;
}

void BuzzWindow::mousePressEvent( QMouseEvent* p_event )
{
    _draggingPos = p_event->pos();
    _dragging = true;
}

void BuzzWindow::mouseReleaseEvent( QMouseEvent* /*p_event*/ )
{
    _dragging = false;
}

void BuzzWindow::mouseMoveEvent( QMouseEvent* p_event )
{
    if ( _dragging )
    {
        move( p_event->globalPos() - _draggingPos );
    }
}

void BuzzWindow::setupUI( const QString& source, const QString& title, const QString& text )
{
    _p_timer = new QTimer();
    _p_timer->setSingleShot( false );
    connect( _p_timer, SIGNAL( timeout() ), this, SLOT( onTimer() ) );
    _p_timer->start( M4E_BUZZ_ANIM_PERIOD * 1000 );

    _p_ui->labelTitle->setText( _p_ui->labelTitle->text().replace( "@TITLE@", title ) );
    _p_ui->labelText->setText( _p_ui->labelText->text().replace( "@TEXT@", text ) );
    _p_ui->labelSource->setText( _p_ui->labelSource->text().replace( "@SOURCE@", source ) );
    _p_ui->labelText->setAttribute( Qt::WA_TransparentForMouseEvents );
    _p_ui->labelTitle->setAttribute( Qt::WA_TransparentForMouseEvents );
    _p_ui->labelSource->setAttribute( Qt::WA_TransparentForMouseEvents );

    onTimer();
}

void BuzzWindow::startAnimation()
{
    QRect geom = geometry();

    QPropertyAnimation* p_anim1 = new QPropertyAnimation( this, "geometry" );
    p_anim1->setDuration( 10 );
    p_anim1->setStartValue( geom );
    geom.moveLeft( geom.left() + 10 );
    p_anim1->setEndValue( geom );

    QPropertyAnimation* p_anim2 = new QPropertyAnimation( this, "geometry" );
    p_anim2->setDuration( 10 );
    p_anim2->setStartValue( geom );
    geom.moveLeft( geom.left() - 10 );
    p_anim2->setEndValue( geom );

    QSequentialAnimationGroup* p_anim = new QSequentialAnimationGroup( this );
    p_anim->addAnimation( p_anim1 );
    p_anim->addAnimation( p_anim2 );
    p_anim->setLoopCount( 40 );
    p_anim->start( QAbstractAnimation::DeleteWhenStopped );
}

void BuzzWindow::onBtnDiscardClicked()
{
    common::GuiUtils::bringWidgetToFront( _p_mainWindow );
    deleteLater();
}

void BuzzWindow::onTimer()
{
    common::GuiUtils::bringWidgetToFront( this );
    startAnimation();

    _p_player->setMedia( QUrl( M4E_BUZZ_SOUND ) );
    _p_player->setVolume( 70 );
    _p_player->play();
}

} // namespace gui
} // namespace m4e
