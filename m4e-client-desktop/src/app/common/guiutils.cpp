/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "guiutils.h"
#include <QGraphicsDropShadowEffect>
#include <QApplication>
#include <QFileDialog>
#include <QPainter>


namespace m4e
{
namespace common
{

//! Max image size used in createImageFromFile
static int MAX_IMG_WIDTH  = 512;
static int MAX_IMG_HEIGHT = 512;


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

QPixmap GuiUtils::getDefaultPixmap()
{
    QPixmap pix( ":/logo.png" );
    return pix;
}

bool GuiUtils::createImageFromFile( QWidget* p_parent, QString dir, QPixmap& image, QByteArray& imageContent, QString& format, bool& aborted )
{
    aborted = false;
    if ( dir.isEmpty() )
    {
        QStringList dirs = QStandardPaths::standardLocations( QStandardPaths::DocumentsLocation );
        if ( dirs.size() > 0 )
            dir = dirs.at( 0 );
    }

    QString filename = QFileDialog::getOpenFileName( p_parent,
                                                     QApplication::translate( "GuiUtils", "Open Image" ),
                                                     dir,
                                                     QApplication::translate( "GuiUtils", "Image Files (*.png *.jpg *.jpeg *.bmp *.svg);;All Files (*.*)" ) );

    if ( filename.isEmpty() )
    {
        aborted = true;
        return false;
    }

    bool res = image.load( filename );
    if ( !res )
    {
        return false;
    }
    // rescale if necessary
    if ( ( image.width() > MAX_IMG_WIDTH ) || ( image.height() > MAX_IMG_HEIGHT) )
        image = image.scaled( QSize( MAX_IMG_WIDTH, MAX_IMG_HEIGHT ), Qt::KeepAspectRatio );

    QBuffer buffer( &imageContent );
    buffer.open( QIODevice::WriteOnly );
    image.save( &buffer, "PNG" );
    format = "png";

    return true;
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

void GuiUtils::bringWidgetToFront( QWidget* p_widget )
{
    if ( p_widget->windowState() & Qt::WindowMinimized )
        p_widget->setWindowState( ( p_widget->windowState() & ~Qt::WindowMinimized ) | Qt::WindowActive );

    Qt::WindowFlags flags = p_widget->windowFlags();
    flags |= Qt::WindowStaysOnTopHint;
    p_widget->setWindowFlags( flags );
    p_widget->show();
    flags &= ~Qt::WindowStaysOnTopHint;
    p_widget->setWindowFlags( flags );
    p_widget->raise();
    p_widget->show();
    p_widget->activateWindow();
}

void GuiUtils::adaptLookAndFeel( QWidget* p_widget )
{
#ifdef Q_OS_MAC
    // get rid of the focus outline on MacOS
    p_widget->setAttribute( Qt::WA_MacShowFocusRect, false );
    const QList< QWidget* >& c = p_widget->findChildren< QWidget* >();
    QList< QWidget* >::const_iterator p_beg = c.begin(), p_end = c.end();
    for ( ; p_beg != p_end; ++p_beg )
    {
        QWidget* p_w = const_cast< QWidget* >( *p_beg );
        p_w->setAttribute( Qt::WA_MacShowFocusRect, false );
    }
#else
    Q_UNUSED(p_widget);
#endif
}

} // namespace common
} // namespace m4e
