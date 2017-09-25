/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "dialogeventsettings.h"
#include <core/log.h>
#include "guiutils.h"
#include <ui_widgeteventsettings.h>
#include <QTableWidgetItem>


namespace m4e
{
namespace ui
{

DialogEventSettings::DialogEventSettings( data::WebApp* p_webApp, QWidget* p_parent ) :
 BaseDialog( p_parent ),
 _p_webApp( p_webApp )
{
    _p_ui = new Ui::WidgetEventSettings();
}

DialogEventSettings::~DialogEventSettings()
{
    delete _p_ui;
}

void DialogEventSettings::setupUI( data::ModelEventPtr event )
{
    _event = event;

    connect( _p_webApp, SIGNAL( onDocumentReady( m4e::data::ModelDocumentPtr ) ), this, SLOT( onDocumentReady( m4e::data::ModelDocumentPtr ) ) );

    decorate( *_p_ui );
    setTitle( QApplication::translate( "DialogEventSettings", "Event Settings" ) );
    QString applybtn( QApplication::translate( "DialogEventSettings", "Apply" ) );
    QString cancelbtn( QApplication::translate( "DialogEventSettings", "Cancel" ) );
    setupButtons( &applybtn, &cancelbtn, nullptr );
    setResizable( true );

    _p_ui->lineEditName->setText( event->getName() );
    _p_ui->textEditDescription->setPlainText( event->getDescription() );
    _p_ui->checkBoxIsPublic->setChecked( event->getIsPublic() );
    _p_ui->dateTimeEditStart->setDateTime( event->getStartDate() );
    _p_ui->timeEditDayTime->setTime( event->getRepeatDayTime() );

    setupMembers( event );
    setupWeekDays( _event->getRepeatWeekDays() );

    // load  the image only if a valid photo id exits
    QString photoid = event->getPhotoId();
    if ( !photoid.isEmpty() && ( photoid != "0" ) )
    {
        _p_webApp->requestDocument( photoid, event->getPhotoETag() );
    }
}

void DialogEventSettings::onDocumentReady( m4e::data::ModelDocumentPtr document )
{
    if ( !document.valid() )
        return;

    QString photoid = _event->getPhotoId();
    if ( !photoid.isEmpty() && ( document->getId() == photoid ) )
    {
        _p_ui->labelPhoto->setPixmap( GuiUtils::createRoundIcon( document ) );
    }

    // check if a member photo arrived
    if ( !_memberPhotos.contains( document->getId() ) )
        return;

    int row = _memberPhotos.value( document->getId() );
    QPixmap pix = GuiUtils::createRoundIcon( document );
    QTableWidgetItem* p_item = _p_ui->tableWidgetMembers->item( row, 0 );
    p_item->setIcon( QIcon( pix ) );
}

void DialogEventSettings::setupWeekDays( unsigned int weekDays )
{
    _p_ui->pushButtonWDMon->setChecked( ( weekDays & data::ModelEvent::WeekDayMonday ) != 0 );
    _p_ui->pushButtonWDTue->setChecked( ( weekDays & data::ModelEvent::WeekDayTuesday ) != 0 );
    _p_ui->pushButtonWDWed->setChecked( ( weekDays & data::ModelEvent::WeekDayWednesday ) != 0 );
    _p_ui->pushButtonWDThu->setChecked( ( weekDays & data::ModelEvent::WeekDayThursday ) != 0 );
    _p_ui->pushButtonWDFri->setChecked( ( weekDays & data::ModelEvent::WeekDayFriday ) != 0 );
    _p_ui->pushButtonWDSat->setChecked( ( weekDays & data::ModelEvent::WeekDaySaturday ) != 0 );
    _p_ui->pushButtonWDSun->setChecked( ( weekDays & data::ModelEvent::WeekDaySunday ) != 0 );
}

void DialogEventSettings::setupMembers( data::ModelEventPtr event )
{
    QList< data::ModelUserInfoPtr > members = event->getMembers();

    _p_ui->tableWidgetMembers->setColumnCount( 2 );
    _p_ui->tableWidgetMembers->setRowCount( members.size() );

    int row = 0;
    for ( data::ModelUserInfoPtr member: members )
    {
        QTableWidgetItem* p_item = new QTableWidgetItem( QIcon(), member->getName() );
        p_item->setFlags( p_item->flags() ^ Qt::ItemIsEditable );
        _p_ui->tableWidgetMembers->setItem( row, 0, p_item );

        QPushButton* p_btn = new QPushButton( QIcon( ":/icon-close.png" ), "", this );
        p_btn->setMaximumSize( QSize( 26, 26 ) );
        p_btn->setMinimumSize( QSize( 26, 26 ) );
        _p_ui->tableWidgetMembers->setCellWidget( row, 1, p_btn );

        // request the photo
        _memberPhotos.insert( member->getPhotoId(), row );
        _p_webApp->requestDocument( member->getPhotoId(), member->getPhotoETag() );

        row++;
    }

    _p_ui->tableWidgetMembers->resizeColumnsToContents();
}

} // namespace ui
} // namespace m4e
