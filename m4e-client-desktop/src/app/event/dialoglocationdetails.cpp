/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "dialoglocationdetails.h"
#include <core/log.h>
#include <common/guiutils.h>
#include <ui_widgetlocationdetails.h>


namespace m4e
{
namespace event
{

/** URLs are automatically converted to clickable links, this string contains the URL styling */
static const QString DESC_LINK_FORMAT = "<a href='\\1'><span style='text-decoration: underline; color:white;'>\\1</span></a> ";


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

void DialogLocationDetails::setupUI( event::ModelLocationPtr location )
{
    _location = location;

    decorate( *_p_ui );
    setTitle( location->getName() );
    QString okbtn( QApplication::translate( "DialogLocationDetails", "Ok" ) );
    setupButtons( &okbtn, nullptr, nullptr );
    setResizable( true );

    _p_ui->labelDescription->setText( formatDescription( _location->getDescription() ) );
    _p_ui->labelVoteMembers->setText( "" );
    _p_ui->labelVotes->setText( "0" );

    // load  the image only if a valid photo id exits
    QString photoid = location->getPhotoId();
    if ( !photoid.isEmpty() && ( photoid != "0" ) )
    {
        connect( _p_webApp, SIGNAL( onDocumentReady( m4e::doc::ModelDocumentPtr ) ), this, SLOT( onDocumentReady( m4e::doc::ModelDocumentPtr ) ) );
        _p_webApp->requestDocument( photoid, location->getPhotoETag() );
    }
    else
    {
        _p_ui->pushButtonPhoto->setIcon( common::GuiUtils::createRoundIcon( common::GuiUtils::getDefaultPixmap() ) );
    }
}

void DialogLocationDetails::setupVotes( ModelLocationVotesPtr votes )
{
    if ( !votes.valid() )
        return;

    _p_ui->labelVoteMembers->setText( formatVoteMembers( votes ) );
    _p_ui->labelVotes->setText( QString::number( votes->getUserNames().size() ) );
}

void DialogLocationDetails::onDocumentReady( m4e::doc::ModelDocumentPtr document )
{
    QString photoid = _location->getPhotoId();
    if ( !photoid.isEmpty() && document.valid() && ( document->getId() == photoid ) )
    {
        _p_ui->pushButtonPhoto->setIcon( common::GuiUtils::createRoundIcon( document ) );
    }
}

QString DialogLocationDetails::formatVoteMembers( event::ModelLocationVotesPtr votes ) const
{
    QString text;
    for ( const auto& mem: votes->getUserNames() )
    {
        if ( !text.isEmpty() )
            text += ", ";
        text += mem;
    }
    return text;
}

QString DialogLocationDetails::formatDescription( const QString& text )
{
    QString input  = text;
    input.replace( QRegExp( "((http|https|ftp)://\\S+)" ), DESC_LINK_FORMAT );
    input.replace( "\n", "<br>" );

    QString output = "<p>" + input + "</p>";
    return output;
}

} // namespace event
} // namespace m4e
