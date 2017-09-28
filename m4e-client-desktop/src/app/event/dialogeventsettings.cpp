/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "dialogeventsettings.h"
#include <core/log.h>
#include <common/guiutils.h>
#include <common/dialogmessage.h>
#include <ui_widgeteventsettings.h>
#include <assert.h>


namespace m4e
{
namespace event
{

DialogEventSettings::DialogEventSettings( webapp::WebApp* p_webApp, QWidget* p_parent ) :
 common::BaseDialog( p_parent ),
 _p_webApp( p_webApp )
{
    _p_ui = new Ui::WidgetEventSettings();
}

DialogEventSettings::~DialogEventSettings()
{
    delete _p_ui;
}

void DialogEventSettings::setupUI( event::ModelEventPtr event )
{
    _event = event;
    decorate( *_p_ui );

    connect( _p_webApp, SIGNAL( onDocumentReady( m4e::doc::ModelDocumentPtr ) ), this, SLOT( onDocumentReady( m4e::doc::ModelDocumentPtr ) ) );
    connect( _p_webApp, SIGNAL( onUserSearch( QList< m4e::user::ModelUserInfoPtr > ) ), this, SLOT( onUserSearch( QList< m4e::user::ModelUserInfoPtr > ) ) );
    connect( _p_ui->pushButtonAddMember, SIGNAL( clicked() ), this, SLOT( onBtnAddMemberClicked() ) );
    connect( _p_ui->lineEditSearchMember, SIGNAL( returnPressed() ), this, SLOT( onLineEditSeachtReturnPressed() ) );

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

void DialogEventSettings::onDocumentReady( m4e::doc::ModelDocumentPtr document )
{
    if ( !document.valid() )
        return;

    QString photoid = _event->getPhotoId();
    if ( !photoid.isEmpty() && ( document->getId() == photoid ) )
    {
        _p_ui->labelPhoto->setPixmap( common::GuiUtils::createRoundIcon( document ) );
    }

    // check if a member photo arrived
    if ( !_memberPhotos.contains( document->getId() ) )
        return;

    int row = _memberPhotos.value( document->getId() );
    QPixmap pix = common::GuiUtils::createRoundIcon( document );
    QTableWidgetItem* p_item = _p_ui->tableWidgetMembers->item( row, 0 );
    p_item->setIcon( QIcon( pix ) );
}

void DialogEventSettings::onUserSearch( QList< user::ModelUserInfoPtr > users )
{
    _p_ui->comboBoxSearchMember->clear();
    for ( auto user: users )
    {
        QVariant data;
        data.setValue( user );
        _p_ui->comboBoxSearchMember->addItem( user->getName(), data );
    }
}

void DialogEventSettings::onMemberRemoveClicked()
{
    QPushButton* p_btn = dynamic_cast< QPushButton* >( sender() );
    assert ( p_btn && "unexpected event sender, a button was expected!" );

    QString memberId = p_btn->property( "userId" ).toString();
    log_verbose << TAG << "TODO remove member: " << memberId.toStdString() << std::endl;
}

void DialogEventSettings::onBtnAddMemberClicked()
{
    int index = _p_ui->comboBoxSearchMember->currentIndex();
    if ( index < 0 )
        return;

    QVariant data = _p_ui->comboBoxSearchMember->itemData( index );
    user::ModelUserInfoPtr user = data.value< user::ModelUserInfoPtr >();

    if ( _members.contains( user->getId() ) )
    {
        common::DialogMessage msg( this );
        msg.setupUI( "Add Member", "This user is already a member of event.",  common::DialogMessage::BtnOk );
        msg.exec();
        return;
    }

    log_verbose << TAG << "TODO onBtnAddMemberClicked, adding member: " << user->getId().toStdString() << std::endl;

    QList< user::ModelUserInfoPtr > members = _event->getMembers();
    user::ModelUserInfoPtr newmember = new user::ModelUserInfo();
    newmember->setId( user->getId() );
    newmember->setName( user->getName() );
    newmember->setPhotoId( user->getPhotoId() );
    newmember->setPhotoETag( user->getPhotoETag() );
    members.append( newmember );

    _event->setMembers( members );
    setupMembers( _event );
}

void DialogEventSettings::onLineEditSeachtReturnPressed()
{
    log_verbose << TAG << "onLineEditSeachtReturnPressed" << std::endl;

    QString keyword = _p_ui->lineEditSearchMember->text();
    keyword = keyword.trimmed();

    if ( keyword.length() < 3 )
        return;

    if ( keyword.length() > 32 )
        keyword = keyword.mid( 0, 32 );

    _p_webApp->requestUserSearch( keyword );
}

bool DialogEventSettings::onButton1Clicked()
{
    log_verbose << TAG << "handle apply" << std::endl;
    return true;
}

bool DialogEventSettings::onButton2Clicked()
{
    log_verbose << TAG << "handle cancel" << std::endl;
    return true;
}

void DialogEventSettings::setupWeekDays( unsigned int weekDays )
{
    _p_ui->pushButtonWDMon->setChecked( ( weekDays & event::ModelEvent::WeekDayMonday ) != 0 );
    _p_ui->pushButtonWDTue->setChecked( ( weekDays & event::ModelEvent::WeekDayTuesday ) != 0 );
    _p_ui->pushButtonWDWed->setChecked( ( weekDays & event::ModelEvent::WeekDayWednesday ) != 0 );
    _p_ui->pushButtonWDThu->setChecked( ( weekDays & event::ModelEvent::WeekDayThursday ) != 0 );
    _p_ui->pushButtonWDFri->setChecked( ( weekDays & event::ModelEvent::WeekDayFriday ) != 0 );
    _p_ui->pushButtonWDSat->setChecked( ( weekDays & event::ModelEvent::WeekDaySaturday ) != 0 );
    _p_ui->pushButtonWDSun->setChecked( ( weekDays & event::ModelEvent::WeekDaySunday ) != 0 );
}

void DialogEventSettings::setupMembers( event::ModelEventPtr event )
{
    QList< user::ModelUserInfoPtr > members = event->getMembers();

    _p_ui->tableWidgetMembers->clear();
    _p_ui->tableWidgetMembers->setColumnCount( 2 );
    _p_ui->tableWidgetMembers->setRowCount( members.size() );

    int row = 0;
    for ( user::ModelUserInfoPtr member: members )
    {
        QTableWidgetItem* p_item = new QTableWidgetItem( QIcon(), member->getName() );
        p_item->setFlags( p_item->flags() ^ Qt::ItemIsEditable );
        _p_ui->tableWidgetMembers->setItem( row, 0, p_item );
        _members.insert( member->getId() );

        if ( event->getOwner().valid() && common::GuiUtils::userIsOwner( event->getOwner()->getId(), _p_webApp ) )
        {
            _p_ui->tableWidgetMembers->setCellWidget( row, 1, createRemoveMemberButton( member->getId() ) );
        }

        // request the photo
        QString photoid = member->getPhotoId();
        if ( !photoid.isEmpty() && ( photoid != "0" ) )
        {
            _memberPhotos.insert( photoid, row );
            _p_webApp->requestDocument( photoid, member->getPhotoETag() );
        }
        row++;
    }

    _p_ui->tableWidgetMembers->resizeColumnsToContents();
}

QWidget *DialogEventSettings::createRemoveMemberButton( const QString& memberId )
{
    QWidget* p_widget = new QWidget( this );
    p_widget->setStyleSheet( "background: transparent;" );
    p_widget->setLayout( new QHBoxLayout( nullptr ) );
    p_widget->layout()->addItem( new QSpacerItem( 20, 10, QSizePolicy::Expanding, QSizePolicy::Minimum ) );

    QPushButton* p_btn = new QPushButton( QIcon( ":/icon-close.png" ), "", p_widget );
    p_btn->setFocusPolicy( Qt::NoFocus );
    p_btn->setToolTip( QApplication::translate( "DialogEventSettings", "Remove member from event" ) );
    p_btn->setProperty( "userId", memberId );
    p_btn->setIconSize( QSize( 20, 20 ) );
    p_btn->setMaximumSize( QSize( 20, 20 ) );
    p_btn->setMinimumSize( QSize( 20, 20 ) );
    p_widget->layout()->addWidget( p_btn );

    connect( p_btn, SIGNAL( clicked() ), this, SLOT( onMemberRemoveClicked() ) );

    return p_widget;
}

} // namespace event
} // namespace m4e
