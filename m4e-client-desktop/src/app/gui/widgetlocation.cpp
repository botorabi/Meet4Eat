/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "widgetlocation.h"
#include <core/log.h>
#include <data/modeluser.h>
#include "guiutils.h"
#include "basedialog.h"
#include "dialoglocationdetails.h"
#include <ui_widgetlocation.h>
#include <QGraphicsDropShadowEffect>


namespace m4e
{
namespace ui
{

WidgetLocation::WidgetLocation( m4e::data::WebApp* p_webApp, QWidget* p_parent ) :
 QWidget( p_parent ),
 _p_webApp( p_webApp)
{
    _p_ui = new Ui::WidgetLocation();
}

WidgetLocation::~WidgetLocation()
{
    delete _p_ui;
}

void WidgetLocation::setupUI( data::ModelLocationPtr location )
{
    _location = location;

    _p_ui->setupUi( this );
    _p_ui->labelHead->setText( _location->getName() );
    _p_ui->labelDescription->setText( _location->getDescription() );

    QGraphicsDropShadowEffect* p_effect = new QGraphicsDropShadowEffect();
    p_effect->setBlurRadius( 6.0 );
    p_effect->setColor( QColor( 100, 100, 100, 80 ) );
    p_effect->setXOffset( -4.0 );
    p_effect->setYOffset( 4.0 );
    setGraphicsEffect( p_effect );

    // load  the image only if a valid photo id exits
    QString photoid = _location->getPhotoId();
    if ( !photoid.isEmpty() && ( photoid != "0" ) )
    {
        connect( _p_webApp, SIGNAL( onDocumentReady( m4e::data::ModelDocumentPtr ) ), this, SLOT( onDocumentReady( m4e::data::ModelDocumentPtr ) ) );
        _p_webApp->requestDocument( photoid, _location->getPhotoETag() );
    }

    // we need to handle mouse clicks manually
    _p_ui->labelHead->installEventFilter( this );
    _p_ui->labelDescription->installEventFilter( this );
    _p_ui->labelPhoto->installEventFilter( this );
}

void WidgetLocation::onBtnSettingsClicked()
{
    //! TODO
    log_verbose << TAG << "onBtnSettingsClicked TODO" << std::endl;
}

void WidgetLocation::onBtnInfoClicked()
{
    DialogLocationDetails* p_dlg = new DialogLocationDetails( _p_webApp, this );
    p_dlg->setupUI( _location );
    p_dlg->exec();
    delete p_dlg;
}

void WidgetLocation::onBtnVoteUpClicked()
{
    data::ModelUserPtr user = _p_webApp->getUserData();
    if ( !user.valid() )
    {
        log_warning << TAG << "invalid user!" << std::endl;
        return;
    }

    QString tooltip;
    auto votes = _location->getVotedMembers();
    for ( const auto& v: votes )
    {
        // check if already voted
        if ( v == user->getName() )
            return;

        if ( !tooltip.isEmpty() )
            tooltip += ", ";
        tooltip += v;
    }

    votes.append( user->getName() );
    _location->setVotedMembers( votes );
    _p_ui->labelVotes->setText( QString::number( votes.size() ) );
    tooltip += user->getName();
    _p_ui->widgetVotes->setToolTip( tooltip.isEmpty() ? QApplication::translate( "WidgetLocation", "No votes" ) : tooltip );

    //! TODO this must be sent to server
}

void WidgetLocation::onBtnVoteDownClicked()
{
    data::ModelUserPtr user = _p_webApp->getUserData();
    if ( !user.valid() )
    {
        log_warning << TAG << "invalid user!" << std::endl;
        return;
    }

    QString tooltip;
    auto votes = _location->getVotedMembers();
    votes.removeOne( user->getName() );
    for ( const auto& v: votes )
    {
        if ( !tooltip.isEmpty() )
            tooltip += ", ";
        tooltip += v;
    }

    _location->setVotedMembers( votes );
    _p_ui->labelVotes->setText( QString::number( votes.size() ) );
    _p_ui->widgetVotes->setToolTip( tooltip.isEmpty() ? QApplication::translate( "WidgetLocation", "No votes" ) : tooltip );

    //! TODO this must be sent to server
}

void WidgetLocation::onDocumentReady( m4e::data::ModelDocumentPtr document )
{
    QString photoid = _location->getPhotoId();
    if ( !photoid.isEmpty() && document.valid() && ( document->getId() == photoid ) )
    {
        _p_ui->labelPhoto->setPixmap( GuiUtils::createRoundIcon( document ) );
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

} // namespace ui
} // namespace m4e
