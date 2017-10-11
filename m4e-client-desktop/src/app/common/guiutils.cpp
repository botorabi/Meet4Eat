/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "guiutils.h"
#include <QGraphicsDropShadowEffect>
#include <QPainter>


namespace m4e
{
namespace common
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

QPixmap GuiUtils::createRoundIcon( doc::ModelDocumentPtr input )
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

void GuiUtils::createShadowEffect( QWidget* p_widget, const QColor& color, const QPoint& offset, int blurr )
{
    QGraphicsDropShadowEffect* p_effect = new QGraphicsDropShadowEffect();
    p_effect->setBlurRadius( blurr );
    p_effect->setColor( color );
    p_effect->setXOffset( offset.x() );
    p_effect->setYOffset( offset.y() );
    p_widget->setGraphicsEffect( p_effect );
}

bool GuiUtils::userIsOwner( const QString& ownerId, webapp::WebApp* p_webApp )
{
    user::ModelUserPtr user = p_webApp->getUser()->getUserData();
    return user.valid() &&  ( user->getId() == ownerId );
}

} // namespace common
} // namespace m4e
