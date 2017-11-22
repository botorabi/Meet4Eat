/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "alarmwindow.h"
#include <common/guiutils.h>
#include <ui_alarmwindow.h>
#include "mainwindow.h"
#include <QPropertyAnimation>
#include <QApplication>
#include <QDesktopWidget>


namespace m4e
{
namespace gui
{

AlarmWindow::AlarmWindow( MainWindow* p_parent ) :
 QMainWindow( nullptr ),
 _p_mainWindow( p_parent ),
 _p_ui( new Ui::AlarmWindow() )
{
    setWindowFlags( Qt::Window | Qt::FramelessWindowHint | Qt::CustomizeWindowHint );
    setAttribute( Qt::WA_NoSystemBackground );
    setAttribute( Qt::WA_TranslucentBackground );
    _p_ui->setupUi( this );
    move( QApplication::desktop()->screen()->rect().center() - rect().center() );
}

AlarmWindow::~AlarmWindow()
{
    delete _p_ui;
}

void AlarmWindow::mousePressEvent( QMouseEvent* p_event )
{
    _draggingPos = p_event->pos();
    _dragging = true;
}

void AlarmWindow::mouseReleaseEvent( QMouseEvent* /*p_event*/ )
{
    _dragging = false;
}

void AlarmWindow::mouseMoveEvent( QMouseEvent* p_event )
{
    if ( _dragging )
    {
        move( p_event->globalPos() - _draggingPos );
    }
}

void AlarmWindow::setupUI( event::ModelEventPtr event )
{
    _event = event;

    _p_timer = new QTimer();
    _p_timer->setSingleShot( false );
    connect( _p_timer, SIGNAL( timeout() ), this, SLOT( onTimer() ) );
    _p_timer->start( M4E_ALARM_ANIM_PERIOD * 1000 );

    QString text = QApplication::translate( "AlarmWindow", "It's time to prepare for an upcoming event!<br>"
                                                           "<br>Event: @EVENTNAME@"
                                                           "<br>Time: @TIME@"
                                            );
    text.replace( "@EVENTNAME@", event->getName() );
    if ( event->isRepeated() )
        text.replace( "@TIME@", event->getRepeatDayTime().toString( "HH:mm" ) );
    else
        text.replace( "@TIME@", event->getStartDate().toString( "yyyy-M-dd HH:mm" ) );

    _p_ui->labelText->setText( _p_ui->labelText->text().replace( "@TEXT@", text ) );
    _p_ui->labelText->setAttribute( Qt::WA_TransparentForMouseEvents );
    _p_ui->labelTitle->setAttribute( Qt::WA_TransparentForMouseEvents );
}

void AlarmWindow::startAnimation()
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

void AlarmWindow::onBtnDiscardClicked()
{
    deleteLater();
}

void AlarmWindow::onBtnDisplayEventClicked()
{
    _p_mainWindow->selectEvent( _event->getId() );
    common::GuiUtils::bringWidgetToFront( _p_mainWindow );
    deleteLater();
}

void AlarmWindow::onTimer()
{
    common::GuiUtils::bringWidgetToFront( this );
    startAnimation();
}

} // namespace gui
} // namespace m4e
