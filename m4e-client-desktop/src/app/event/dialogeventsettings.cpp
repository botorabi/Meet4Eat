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

    _userIsOwner = _p_webApp->getUser()->isUserId( _event->getOwner()->getId() );

    // if the user is not the event owner then disable editting ui
    if ( !_userIsOwner )
        setupReadOnly();

    connect( _p_webApp, SIGNAL( onDocumentReady( m4e::doc::ModelDocumentPtr ) ), this, SLOT( onDocumentReady( m4e::doc::ModelDocumentPtr ) ) );
    connect( _p_webApp, SIGNAL( onUserSearch( QList< m4e::user::ModelUserInfoPtr > ) ), this, SLOT( onUserSearch( QList< m4e::user::ModelUserInfoPtr > ) ) );
    connect( _p_webApp->getEvents(), SIGNAL( onResponseUpdateEvent( bool, QString ) ), this, SLOT( onResponseUpdateEvent( bool, QString ) ) );
    connect( _p_webApp->getEvents(), SIGNAL( onResponseAddMember( bool, QString, QString ) ), this, SLOT( onResponseAddMember( bool, QString, QString ) ) );
    connect( _p_webApp->getEvents(), SIGNAL( onResponseRemoveMember( bool, QString, QString ) ), this, SLOT( onResponseRemoveMember( bool, QString, QString ) ) );

    connect( _p_ui->pushButtonPhoto, SIGNAL( clicked() ), this, SLOT( onBtnPhotoClicked() ) );
    connect( _p_ui->pushButtonAddMember, SIGNAL( clicked() ), this, SLOT( onBtnAddMemberClicked() ) );
    connect( _p_ui->lineEditSearchMember, SIGNAL( returnPressed() ), this, SLOT( onLineEditSeachtReturnPressed() ) );
    connect( _p_ui->lineEditSearchMember, SIGNAL( editingFinished() ), this, SLOT( onLineEditSeachtReturnPressed() ) );

    setTitle( QApplication::translate( "DialogEventSettings", "Event Settings" ) );
    if ( _userIsOwner )
    {
        QString applybtn( QApplication::translate( "DialogEventSettings", "Apply" ) );
        QString cancelbtn( QApplication::translate( "DialogEventSettings", "Cancel" ) );
        setupButtons( &applybtn, &cancelbtn, nullptr );
    }
    else
    {
        QString dismissbtn( QApplication::translate( "DialogEventSettings", "Dismiss" ) );
        setupButtons( &dismissbtn, nullptr, nullptr );
    }

    setResizable( false );
    setupCommonElements( event );
    setupMembers( event );
    _p_ui->lineEditOwner->setText( event->getOwner()->getName() );

    // load  the image only if a valid photo id exits
    QString photoid = event->getPhotoId();
    if ( !photoid.isEmpty() && ( photoid != "0" ) )
    {
        _p_webApp->requestDocument( photoid, event->getPhotoETag() );
    }
    else
    {
        _p_ui->pushButtonPhoto->setIcon( common::GuiUtils::createRoundIcon( common::GuiUtils::getDefaultPixmap() ) );
    }
}

void DialogEventSettings::setupNewEventUI( event::ModelEventPtr event )
{
    _event = event;
    decorate( *_p_ui );

    _userIsOwner = true;
    _editNewEvent = true;

    connect( _p_webApp, SIGNAL( onDocumentReady( m4e::doc::ModelDocumentPtr ) ), this, SLOT( onDocumentReady( m4e::doc::ModelDocumentPtr ) ) );
    connect( _p_webApp->getEvents(), SIGNAL( onResponseNewEvent( bool, QString ) ), this, SLOT( onResponseNewEvent( bool, QString ) ) );
    connect( _p_ui->pushButtonPhoto, SIGNAL( clicked() ), this, SLOT( onBtnPhotoClicked() ) );

    setTitle( QApplication::translate( "DialogEventSettings", "Create New Event" ) );
    QString applybtn( QApplication::translate( "DialogEventSettings", "Apply" ) );
    QString cancelbtn( QApplication::translate( "DialogEventSettings", "Cancel" ) );
    setupButtons( &applybtn, &cancelbtn, nullptr );

    _p_ui->widgetOwner->hide();
    _p_ui->labelMembers->hide();
    _p_ui->groupBoxMembers->hide();

    // as we hide the members ui in dialog, we have to adjust the dialog size
    adjustSize();

    setResizable( false );

    setupCommonElements( event );
}

void DialogEventSettings::setupCommonElements( event::ModelEventPtr event )
{
    _p_ui->lineEditName->setText( event->getName() );
    _p_ui->textEditDescription->setPlainText( event->getDescription() );
    _p_ui->checkBoxIsPublic->setChecked( event->getIsPublic() );
    _p_ui->dateTimeEditStart->setDateTime( event->getStartDate() );
    _p_ui->timeEditDayTime->setTime( event->getRepeatDayTime() );

    setupWeekDays( event->getRepeatWeekDays() );

    qint64 votingtime = event->getVotingTimeBegin();
    if ( votingtime == 0 )
    {
        _p_ui->timeEditVotingTimeBegin->setTime( QTime( 0, 0 ) );
    }
    else
    {
        int hours = votingtime / ( 60 * 60 );
        int mins  = ( votingtime % ( 60 * 60 ) ) / 60;
        _p_ui->timeEditVotingTimeBegin->setTime( QTime( hours, mins ) );
    }
}

void DialogEventSettings::onDocumentReady( m4e::doc::ModelDocumentPtr document )
{
    if ( !document.valid() )
        return;

    QString photoid = _event->getPhotoId();
    if ( !photoid.isEmpty() && ( document->getId() == photoid ) )
    {
        _p_ui->pushButtonPhoto->setIcon( common::GuiUtils::createRoundIcon( document ) );
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
        // exclude all event members from hit list
        if ( ( _event->getOwner()->getId() != user->getId() ) && !_members.contains( user->getId() ) )
            _p_ui->comboBoxSearchMember->addItem( user->getName(), data );
    }
}

void DialogEventSettings::onResponseAddMember( bool success, QString eventId, QString memberId )
{
    // is this the response of our own request?
    if ( ( eventId != _event->getId() ) || ( memberId != _newMember->getId() ) )
        return;

    if ( !_newMember.valid() )
        return;

    if ( !success )
    {
        log_warning << TAG << "could not add member!" << std::endl;

        common::DialogMessage msg( this );
        msg.setupUI( QApplication::translate( "DialogEventSettings", "Add Member" ),
                     QApplication::translate( "DialogEventSettings", "Could not add new member!" ),
                     common::DialogMessage::BtnOk );
        msg.exec();
        return;
    }

    log_verbose << TAG << "new member added: " << _newMember->getId() << std::endl;

    QList< user::ModelUserInfoPtr > members = _event->getMembers();
    members.append( _newMember );
    _event->setMembers( members );

    _newMember = nullptr;
    setupMembers( _event );

    common::DialogMessage msg( this );
    msg.setupUI( QApplication::translate( "DialogEventSettings", "Add Member" ),
                 QApplication::translate( "DialogEventSettings", "New member was successfully added to event." ),
                 common::DialogMessage::BtnOk );
    msg.exec();
}

void DialogEventSettings::onResponseRemoveMember( bool success, QString eventId, QString memberId )
{
    // is this the response of our own request?
    if ( ( eventId != _event->getId() ) || ( memberId != _removeMemberId ) )
        return;

    if ( !success )
    {
        log_warning << TAG << "could not remove member!" << std::endl;

        common::DialogMessage msg( this );
        msg.setupUI( QApplication::translate( "DialogEventSettings", "Remove Member" ),
                     QApplication::translate( "DialogEventSettings", "Could not remove member from event!" ),
                     common::DialogMessage::BtnOk );
        msg.exec();
        return;
    }
    QList< user::ModelUserInfoPtr > members = _event->getMembers();
    for ( int i = 0; i < members.size(); i++ )
    {
        user::ModelUserInfoPtr member = members.at( i );
        if ( member->getId() == memberId )
        {
            members.removeAt( i );
            break;
        }
    }
    _event->setMembers( members );
    _members.remove( memberId );
    setupMembers( _event );

    common::DialogMessage msg( this );
    msg.setupUI( QApplication::translate( "DialogEventSettings", "Remove Member" ),
                 QApplication::translate( "DialogEventSettings", "Member was successfully removed from event." ),
                 common::DialogMessage::BtnOk );
    msg.exec();
}

void DialogEventSettings::onResponseUpdateEvent( bool success, QString /*eventId*/ )
{
    if ( !success )
    {
        common::DialogMessage msg( this );
        msg.setupUI( QApplication::translate( "DialogEventSettings", "Update Event" ),
                     QApplication::translate( "DialogEventSettings", "Event could not be updated!" ),
                     common::DialogMessage::BtnOk );
        msg.exec();
        return;
    }
    done( common::BaseDialog::Btn1 );
}

void DialogEventSettings::onResponseNewEvent( bool success, QString eventId )
{
    if ( success )
    {
        _event->setId( eventId );

        common::DialogMessage msg( this );
        msg.setupUI( QApplication::translate( "DialogEventSettings", "New Event" ),
                     QApplication::translate( "DialogEventSettings", "New event was successfully created.\nPlease don't forget to edit the event and add your friends." ),
                     common::DialogMessage::BtnOk );
        msg.exec();
        done( common::BaseDialog::Btn1 );
    }
    else
    {
        QString reason = _p_webApp->getEvents()->getLastError();
        common::DialogMessage msg( this );
        msg.setupUI( QApplication::translate( "DialogEventSettings", "Problem Creating New Event" ),
                     QApplication::translate( "DialogEventSettings", "New event could not be created!\nReason: " ) + reason,
                     common::DialogMessage::BtnOk );
        msg.exec();
    }
}

void DialogEventSettings::onBtnPhotoClicked()
{
    QString     dir;
    QString     format;
    QPixmap     image;
    QByteArray  imagecontent;
    bool        aborted;
    bool res = common::GuiUtils::createImageFromFile( this, dir, image, imagecontent, format, aborted );

    if ( aborted )
        return;

    if ( !res )
    {
        common::DialogMessage msg( this );
        msg.setupUI( QApplication::translate( "DialogEventSettings", "Set Image" ),
                     QApplication::translate( "DialogEventSettings", "Cannot update the image. The file format is not supported!" ),
                     common::DialogMessage::BtnOk );
        msg.exec();
        return;
    }

    m4e::doc::ModelDocumentPtr doc = new m4e::doc::ModelDocument();
    doc->setContent( imagecontent, "image", format );
    _event->setUpdatedPhoto( doc );
    _p_ui->pushButtonPhoto->setIcon( common::GuiUtils::createRoundIcon( doc ) );
}

void DialogEventSettings::onBtnMemberRemoveClicked()
{
    QPushButton* p_btn = dynamic_cast< QPushButton* >( sender() );
    assert ( p_btn && "unexpected event sender, a button was expected!" );

    _removeMemberId = p_btn->property( "userId" ).toString();
    _p_webApp->getEvents()->requestRemoveMember( _event->getId(), _removeMemberId );
}

void DialogEventSettings::onBtnAddMemberClicked()
{
    int index = _p_ui->comboBoxSearchMember->currentIndex();
    if ( index < 0 )
        return;

    QVariant data = _p_ui->comboBoxSearchMember->itemData( index );
    user::ModelUserInfoPtr user = data.value< user::ModelUserInfoPtr >();
    _newMember = nullptr;

    if ( _members.contains( user->getId() ) )
    {
        common::DialogMessage msg( this );
        msg.setupUI( QApplication::translate( "DialogEventSettings", "Add Member" ),
                     QApplication::translate( "DialogEventSettings", "This user is already a member of event." ),
                     common::DialogMessage::BtnOk );
        msg.exec();
        return;
    }

    // store the user until we get the response from server
    _newMember = user;
    _p_webApp->getEvents()->requestAddMember( _event->getId(), user->getId() );
}

void DialogEventSettings::onLineEditSeachtReturnPressed()
{
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
    if ( !_userIsOwner )
        return true;

    _event->setName( _p_ui->lineEditName->text() );
    _event->setDescription( _p_ui->textEditDescription->toPlainText() );
    _event->setIsPublic( _p_ui->checkBoxIsPublic->isChecked() );
    _event->setStartDate( _p_ui->dateTimeEditStart->dateTime() );
    _event->setRepeatDayTime( _p_ui->timeEditDayTime->time() );
    _event->setRepeatWeekDays( getWeekDays() );
    _event->setVotingTimeBegin( _p_ui->timeEditVotingTimeBegin->time().msecsSinceStartOfDay() / 1000 );

    if ( _editNewEvent )
        _p_webApp->getEvents()->requestNewEvent( _event );
    else
        _p_webApp->getEvents()->requestUpdateEvent( _event );

    return false;
}

bool DialogEventSettings::onButton2Clicked()
{
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

unsigned int DialogEventSettings::getWeekDays()
{
    unsigned int weekdays = 0;
    weekdays |= _p_ui->pushButtonWDMon->isChecked() ? event::ModelEvent::WeekDayMonday : 0;
    weekdays |= _p_ui->pushButtonWDTue->isChecked() ? event::ModelEvent::WeekDayTuesday : 0;
    weekdays |= _p_ui->pushButtonWDWed->isChecked() ? event::ModelEvent::WeekDayWednesday : 0;
    weekdays |= _p_ui->pushButtonWDThu->isChecked() ? event::ModelEvent::WeekDayThursday : 0;
    weekdays |= _p_ui->pushButtonWDFri->isChecked() ? event::ModelEvent::WeekDayFriday : 0;
    weekdays |= _p_ui->pushButtonWDSat->isChecked() ? event::ModelEvent::WeekDaySaturday : 0;
    weekdays |= _p_ui->pushButtonWDSun->isChecked() ? event::ModelEvent::WeekDaySunday : 0;
    return weekdays;
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

        if ( _userIsOwner )
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
        else
        {
            QPixmap pix = common::GuiUtils::createRoundIcon( QPixmap( M4E_DEFAULT_USER_ICON ) );
            p_item->setIcon( QIcon( pix ) );
        }
        row++;
    }

    _p_ui->tableWidgetMembers->resizeColumnsToContents();
}

QWidget* DialogEventSettings::createRemoveMemberButton( const QString& memberId )
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

    connect( p_btn, SIGNAL( clicked() ), this, SLOT( onBtnMemberRemoveClicked() ) );

    return p_widget;
}

void DialogEventSettings::setupReadOnly()
{
    _p_ui->lineEditName->setReadOnly( true );
    _p_ui->textEditDescription->setReadOnly( true );
    _p_ui->dateTimeEditStart->setReadOnly( true );
    _p_ui->timeEditDayTime->setReadOnly( true );
    _p_ui->widgetSearchMember->hide();
    _p_ui->checkBoxIsPublic->setEnabled( false );
    _p_ui->pushButtonWDMon->setEnabled( false );
    _p_ui->pushButtonWDTue->setEnabled( false );
    _p_ui->pushButtonWDWed->setEnabled( false );
    _p_ui->pushButtonWDThu->setEnabled( false );
    _p_ui->pushButtonWDFri->setEnabled( false );
    _p_ui->pushButtonWDSat->setEnabled( false );
    _p_ui->pushButtonWDSun->setEnabled( false );
}

} // namespace event
} // namespace m4e
