/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "guiutils.h"
#include <QPainter>


namespace m4e
{
namespace ui
{

QPixmap GuiUtils::createRoundIcon( const QPixmap& input )
{
    QSize size = input.size();
    int dim = qMin( size.width(), size.height() );
    int offsetx = ( size.width() - dim ) / 2;
    int offsety = ( size.height() - dim ) / 2;

    QPixmap target( QSize( dim, dim ) );
    target.fill( Qt::transparent );

    QPainter painter( &target );
    painter.setRenderHints( QPainter::Antialiasing | QPainter::SmoothPixmapTransform, true );

    QRegion clip( QRect( 0, 0, dim, dim ), QRegion::Ellipse );
    painter.setClipRegion( clip );
    painter.drawPixmap( 0, 0, input, offsetx, offsety, dim, dim );
    return target;
}

QPixmap GuiUtils::createRoundIcon( data::ModelDocumentPtr input )
{
    QString    format;
    QByteArray data;
    if ( input->extractImageData( data, format ) )
    {
        QPixmap img;
        if ( img.loadFromData( data, format.toStdString().c_str() ) )
        {
            return createRoundIcon( img );
        }
    }
    return QPixmap();
}

bool GuiUtils::userIsOwner( const QString& ownerId, data::WebApp* p_webApp )
{
    return p_webApp->getUserData().valid() &&  ( p_webApp->getUserData()->getId() == ownerId );
}

} // namespace ui
} // namespace m4e
