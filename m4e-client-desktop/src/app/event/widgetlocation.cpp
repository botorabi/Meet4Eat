/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "widgetlocation.h"
#include <core/log.h>
#include <user/modeluser.h>
#include <common/guiutils.h>
#include <common/basedialog.h>
#include <common/dialogmessage.h>
#include "dialoglocationdetails.h"
#include "dialoglocationedit.h"
#include <ui_widgetlocation.h>


namespace m4e
{
namespace event
{

/** Minimal time interval between two location votes, this is used for minimize spamming */
static const int M4E_VOTE_ANTI_SPAM_TIME = 5000;


WidgetLocation::WidgetLocation( webapp::WebApp* p_webApp, QWidget* p_parent ) :
 QWidget( p_parent ),
 _p_webApp( p_webApp)
{
    _p_ui = new Ui::WidgetLocation();
}

WidgetLocation::~WidgetLocation()
{
    delete _p_ui;
}

void WidgetLocation::setupUI( event::ModelEventPtr event, event::ModelLocationPtr location, bool userIsOwner )
{
    _event = event;
    _location = location;

    _p_spamTimer = new QTimer( this );
    _p_spamTimer->setSingleShot( true );
    connect( _p_spamTimer, SIGNAL( timeout() ), this, SLOT( onAntiSpamTimeout() ) );

    connect( _p_webApp->getEvents(), SIGNAL( onResponseSetLocationVote( bool, QString, QString, QString, bool ) ), this,
                                     SLOT( onResponseSetLocationVote( bool, QString, QString, QString, bool ) ) );

    connect( _p_webApp->getEvents(), SIGNAL( onResponseGetLocationVotesById( bool, m4e::event::ModelLocationVotesPtr ) ), this,
                                     SLOT( onResponseGetLocationVotesById( bool, m4e::event::ModelLocationVotesPtr ) ) );

    _p_ui->setupUi( this );
    _p_ui->labelHead->setText( _location->getName() );    
    _p_ui->labelDescription->setText( _location->getDescription() );

    // the buttons "delete" and "edit" are only visible for event owner
    _p_ui->pushButtonDelete->setHidden( !userIsOwner );
    _p_ui->pushButtonEdit->setHidden( !userIsOwner );

    common::GuiUtils::createShadowEffect( this, QColor( 100, 100, 100, 80), QPoint( -4, 4 ), 6 );

    // load  the image only if a valid photo id exits
    QString photoid = _location->getPhotoId();
    if ( !photoid.isEmpty() && ( photoid != "0" ) )
    {
        connect( _p_webApp, SIGNAL( onDocumentReady( m4e::doc::ModelDocumentPtr ) ), this, SLOT( onDocumentReady( m4e::doc::ModelDocumentPtr ) ) );
        _p_webApp->requestDocument( photoid, _location->getPhotoETag() );
    }
    else
    {
        _p_ui->labelPhoto->setPixmap( common::GuiUtils::createRoundIcon( common::GuiUtils::getDefaultPixmap() ) );
    }

    // we need to handle mouse clicks manually
    _p_ui->labelHead->installEventFilter( this );
    _p_ui->labelDescription->installEventFilter( this );
    _p_ui->labelPhoto->installEventFilter( this );

    enableVotingUI( _p_webApp->getEvents()->getIsVotingTime( _event->getId() ) );
}

void WidgetLocation::enableVotingUI( bool enable )
{
    _enableVotingUI = enable;

    if ( !_event.valid() )
    {
        log_error << TAG << "cannot upate voting UI, invalid event" << std::endl;
        return;
    }

    if ( enable )
    {
        updateVotingButtons();
    }
    else
    {
        _p_ui->pushButtonVoteUp->setVisible( false );
        _p_ui->pushButtonVoteDown->setVisible( false );
    }
    _p_ui->widgetVotes->setVisible( enable );
}

void WidgetLocation::updateVotes( ModelLocationVotesPtr votes )
{
    if ( !_enableVotingUI )
        return;

    if ( ( votes->getEventId() != _event->getId() ) || ( votes->getLocationId() != _location->getId() ) )
        return;

    _votes = votes;
    QString textnovotes = QApplication::translate( "WidgetLocation", "No votes" );

    if ( !_votes.valid() )
    {
        _p_ui->labelVotes->setText( "0" );
        _p_ui->widgetVotes->setToolTip( textnovotes );
        return;
    }

    QString tooltip;
    for ( const auto& v: votes->getUserNames() )
    {
        if ( !tooltip.isEmpty() )
            tooltip += ", ";
        tooltip += v;
    }

    _p_ui->labelVotes->setText( QString::number( votes->getUserNames().size() ) );
    _p_ui->widgetVotes->setToolTip( tooltip.isEmpty() ? textnovotes : tooltip );
    updateVotingButtons();
}

void WidgetLocation::onBtnEditClicked()
{
    DialogLocationEdit* p_dlg = new DialogLocationEdit( _p_webApp, nullptr );
    p_dlg->setupUIEditLocation( _event, _location );
    p_dlg->exec();
    delete p_dlg;
}

void WidgetLocation::onBtnDeleteClicked()
{
    common::DialogMessage msg( this );
    msg.setupUI( QApplication::translate( "WidgetLocation", "Remove Location" ),
                 QApplication::translate( "WidgetLocation", "Do you really want to remove the location?" ),
                 common::DialogMessage::BtnYes | common::DialogMessage::BtnNo );

    if ( msg.exec() == common::DialogMessage::BtnNo )
    {
        return;
    }

    // the actual deletion is delegated
    emit onDeleteLocation( _location->getId() );
}

void WidgetLocation::onBtnInfoClicked()
{
    DialogLocationDetails* p_dlg = new DialogLocationDetails( _p_webApp, this );
    p_dlg->setupUI( _location );
    p_dlg->setupVotes( _votes );
    p_dlg->exec();
    delete p_dlg;
}

void WidgetLocation::onBtnVoteUpClicked()
{
    requestSetLocationVote( true );
    spamProtection( true );
}

void WidgetLocation::onBtnVoteDownClicked()
{
    requestSetLocationVote( false );
    spamProtection( true );
}

void WidgetLocation::onAntiSpamTimeout()
{
    spamProtection( false );
}

void WidgetLocation::onDocumentReady( m4e::doc::ModelDocumentPtr document )
{
    QString photoid = _location->getPhotoId();
    if ( !photoid.isEmpty() && document.valid() && ( document->getId() == photoid ) )
    {
        _p_ui->labelPhoto->setPixmap( common::GuiUtils::createRoundIcon( document ) );
    }
}

void WidgetLocation::onResponseSetLocationVote( bool success, QString eventId, QString locationId, QString votesId, bool vote )
{
    if ( !_event.valid() || !_location.valid() || ( eventId != _event->getId() ) || ( locationId != _location->getId() ) )
    {
        return;
    }

    if ( !success )
    {
        log_warning << TAG << "could not vote for location, reason: " << _p_webApp->getEvents()->getLastError() << std::endl;
    }
    else
    {
        log_verbose << TAG << "voting was successful: " << ( vote ? "voted" : "unvoted" ) << " location " << _location->getName() << std::endl;
        _p_webApp->getEvents()->requestGetLocationVotesById( votesId );
    }
}

void WidgetLocation::onResponseGetLocationVotesById( bool success, ModelLocationVotesPtr votes )
{
    if ( !success )
    {
        log_warning << TAG << "could not get votes results" << std::endl;
    }
    else
    {
        // the method checks if the incoming votes is for this location
        updateVotes( votes );
    }
}

bool WidgetLocation::eventFilter( QObject* p_obj, QEvent* p_event )
{
    if ( p_event->type() == QEvent::MouseButtonPress )
    {
        onBtnInfoClicked();
        return true;
    }

    return QObject::eventFilter( p_obj, p_event );
}

void WidgetLocation::requestSetLocationVote( bool vote )
{
    user::ModelUserPtr user = _p_webApp->getUser()->getUserData();
    if ( !user.valid() || !_event.valid() || !_location.valid() )
    {
        log_warning << TAG << "cannot vote, invalid inputs" << std::endl;
        return;
    }

    // check if we have already voted, if so then ignore the button click
    if ( _votes.valid() )
    {
        bool alreadyvoted = _votes->getUserIds().contains( user->getId() );
        if ( ( vote && alreadyvoted ) || ( !vote && !alreadyvoted ) )
            return;
    }

    _p_webApp->getEvents()->requestSetLocationVote( _event->getId(), _location->getId(), vote );
}

void WidgetLocation::spamProtection( bool start )
{
    if ( start )
        _p_spamTimer->start( M4E_VOTE_ANTI_SPAM_TIME );

    _p_ui->pushButtonVoteDown->setEnabled( !start );
    _p_ui->pushButtonVoteUp->setEnabled( !start );
}

void WidgetLocation::updateVotingButtons()
{
    user::ModelUserPtr user = _p_webApp->getUser()->getUserData();
    if ( !user.valid() )
    {
        _p_ui->pushButtonVoteDown->setVisible( false );
        _p_ui->pushButtonVoteUp->setVisible( false );
        return;
    }

    bool enableupvote = !_votes.valid() || !_votes->getUserIds().contains( user->getId() );

    _p_ui->pushButtonVoteDown->setVisible( !enableupvote );
    _p_ui->pushButtonVoteUp->setVisible( enableupvote );
}

} // namespace event
} // namespace m4e
