/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "widgeteventitem.h"
#include <core/log.h>
#include "guiutils.h"
#include <QGraphicsDropShadowEffect>
#include <ui_widgeteventitem.h>


namespace m4e
{
namespace ui
{

//! Event Box style
static const QString boxStyle = \
     "#groupBoxMain { \
        border-radius: 10px; \
        border: 3px solid @BORDERCOLOR@; \
        background-color: rgb(80,112,125); \
      }";

//! Normal border color
static const QString boxBorderColorNormal = "rgb(131, 147, 167)";

//! Border color for selected mode
static const QString boxBorderColorSelect = "rgb(231, 247, 167)";


WidgetEventItem::WidgetEventItem( data::WebApp* p_webApp, QWidget* p_parent ) :
 QWidget( p_parent ),
 _p_webApp( p_webApp)
{
    _p_ui = new Ui::WidgetEventItem();
}

WidgetEventItem::~WidgetEventItem()
{
    delete _p_ui;
}

void WidgetEventItem::setupUI( data::ModelEventPtr event )
{
    _event = event;

    _p_ui->setupUi( this );
    _p_ui->labelHead->setText( event->getName() );
    _p_ui->labelDescription->setText( event->getDescription() );

    setSelectionMode( true );

    QGraphicsDropShadowEffect* p_effect = new QGraphicsDropShadowEffect();
    p_effect->setBlurRadius( 4.0 );
    p_effect->setColor( QColor( 100, 100, 100, 180 ) );
    p_effect->setXOffset( -2.0 );
    p_effect->setYOffset( 2.0 );
    setGraphicsEffect( p_effect );

    // we need to handle mouse clicks manually
    _p_ui->labelHead->installEventFilter( this );
    _p_ui->labelDescription->installEventFilter( this );
    _p_ui->groupBoxMain->installEventFilter( this );
    _p_ui->labelPhoto->installEventFilter( this );


    // load  the image only if a valid photo id exits
    QString photoid = event->getPhotoId();
    if ( !photoid.isEmpty() && ( photoid != "0" ) )
    {
        connect( _p_webApp, SIGNAL( onDocumentReady( m4e::data::ModelDocumentPtr ) ), this, SLOT( onDocumentReady( m4e::data::ModelDocumentPtr ) ) );
        _p_webApp->requestDocument( photoid, event->getPhotoETag() );
    }
}

void WidgetEventItem::setSelectionMode( bool normal )
{
    QString style = boxStyle;
    style.replace( "@BORDERCOLOR@", ( normal ? boxBorderColorNormal : boxBorderColorSelect ) );
    _p_ui->groupBoxMain->setStyleSheet( style );
}

void WidgetEventItem::onBtnOptionsClicked()
{
    //! TODO
}

void WidgetEventItem::onDocumentReady( m4e::data::ModelDocumentPtr document )
{
    QString photoid = _event->getPhotoId();
    if ( !photoid.isEmpty() && document.valid() && ( document->getId() == photoid ) )
    {
         setupImage( document );
    }
}

bool WidgetEventItem::eventFilter( QObject* p_obj, QEvent* p_event )
{
    if ( p_event->type() == QEvent::MouseButtonPress )
    {
        emit onClicked( _event->getId() );
        return true;
    }

    return QObject::eventFilter( p_obj, p_event );
}

void WidgetEventItem::setupImage( data::ModelDocumentPtr image )
{
    QString    format;
    QByteArray data;
    if ( image->extractImageData( data, format ) )
    {
        //QImage img;
        QPixmap img;
        if ( img.loadFromData( data, format.toStdString().c_str() ) )
        {
            _p_ui->labelPhoto->setPixmap( GuiUtils::createRoundIcon( img ) );
        }
    }
}

} // namespace ui
} // namespace m4e
