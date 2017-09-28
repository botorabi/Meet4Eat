/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "dialoglocationdetails.h"
#include <common/guiutils.h>
#include <ui_widgetlocationdetails.h>


namespace m4e
{
namespace event
{

DialogLocationDetails::DialogLocationDetails( webapp::WebApp* p_webApp, QWidget* p_parent ) :
 common::BaseDialog( p_parent ),
 _p_webApp( p_webApp )
{
    _p_ui = new Ui::WidgetLocationDetails();
}

DialogLocationDetails::~DialogLocationDetails()
{
    delete _p_ui;
}

QString DialogLocationDetails::formatVoteMembers() const
{
    QString text;
    for ( const auto& mem: _location->getVotedMembers() )
    {
        if ( !text.isEmpty() )
            text += ", ";
        text += mem;
    }
    return text;
}

void DialogLocationDetails::setupUI( event::ModelLocationPtr location )
{
    _location = location;

    decorate( *_p_ui );
    setTitle( location->getName() );
    QString okbtn( QApplication::translate( "DialogLocationDetails", "Ok" ) );
    setupButtons( &okbtn, nullptr, nullptr );
    setResizable( true );

    _p_ui->labelDescription->setText( _location->getDescription() );
    _p_ui->labelVoteMembers->setText( formatVoteMembers() );
    _p_ui->labelVotes->setText( QString::number( location->getVotedMembers().size() ) );

    // load  the image only if a valid photo id exits
    QString photoid = location->getPhotoId();
    if ( !photoid.isEmpty() && ( photoid != "0" ) )
    {
        connect( _p_webApp, SIGNAL( onDocumentReady( m4e::doc::ModelDocumentPtr ) ), this, SLOT( onDocumentReady( m4e::doc::ModelDocumentPtr ) ) );
        _p_webApp->requestDocument( photoid, location->getPhotoETag() );
    }
}

void DialogLocationDetails::onDocumentReady( m4e::doc::ModelDocumentPtr document )
{
    QString photoid = _location->getPhotoId();
    if ( !photoid.isEmpty() && document.valid() && ( document->getId() == photoid ) )
    {
        _p_ui->labelPhoto->setPixmap( common::GuiUtils::createRoundIcon( document ) );
    }
}

} // namespace event
} // namespace m4e
