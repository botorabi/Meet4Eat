/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "dialogvoteshistory.h"
#include <core/log.h>
#include <common/guiutils.h>
#include <ui_widgetvoteshistory.h>
#include <ui_widgetvotesitem.h>
#include <QListWidgetItem>


namespace m4e
{
namespace event
{


DialogVotesHistory::DialogVotesHistory( webapp::WebApp* p_webApp, QWidget* p_parent, bool autoDestroy ) :
 common::BaseDialog( p_parent ),
 _p_webApp( p_webApp )
{
    _p_ui = new Ui::WidgetVotesHistory();

    if ( autoDestroy )
        setAttribute( Qt::WA_DeleteOnClose );
}

DialogVotesHistory::~DialogVotesHistory()
{
    delete _p_ui;
}

void DialogVotesHistory::setupUI( event::ModelEventPtr event )
{
    _event = event;

    decorate( *_p_ui );
    setTitle( event->getName() );
    QString okbtn( QApplication::translate( "DialogLocationDetails", "Ok" ) );
    setupButtons( &okbtn, nullptr, nullptr );
    setResizable( true );

    connect( _p_ui->pushButtonUpdate, SIGNAL( clicked() ), this, SLOT( onBtnUpdateClicked() ) );
    connect( _p_webApp->getEvents(), SIGNAL( onResponseGetLocationVotesByTime( bool, QList< m4e::event::ModelLocationVotesPtr > ) ), this,
                                     SLOT( onResponseGetLocationVotesByTime( bool, QList< m4e::event::ModelLocationVotesPtr > ) ) );

    _p_ui->dateTimeEditBegin->setDate( QDate::currentDate().addDays( -7 ) );
    _p_ui->dateTimeEditEnd->setDate( QDate::currentDate() );
    onBtnUpdateClicked();
}

void DialogVotesHistory::onBtnUpdateClicked()
{
    QDateTime begin = _p_ui->dateTimeEditBegin->dateTime();
    QDateTime end   = _p_ui->dateTimeEditEnd->dateTime();
    end.setTime( QTime::currentTime() );
    _p_webApp->getEvents()->requestGetLocationVotesByTime( _event->getId(), begin, end );
}

void DialogVotesHistory::onBtnExpandClicked()
{
    log_verbose << TAG << "TODO onBtnExpandClicked" << std::endl;
}

void DialogVotesHistory::onResponseGetLocationVotesByTime( bool success, QList< m4e::event::ModelLocationVotesPtr > votes )
{
    if ( !success )
    {
        log_warning << TAG << "could not get the location votes, reason: " << _p_webApp->getEvents()->getLastError() << std::endl;
        return;
    }

    clearVotesItems();

    // group all events by voting time
    QMap< qint64 /*sec votes end*/, QList< ModelLocationVotesPtr > > votingtime;
    for ( ModelLocationVotesPtr v: votes )
    {
        qint64 vt = v->getVoteTimeEnd().toSecsSinceEpoch();
        if ( !votingtime.contains( vt ) )
            votingtime.insert( vt, QList< ModelLocationVotesPtr >() );
        votingtime[ vt ].append( v );
    }

    // sort the voting times in descending order
    QList< qint64 > times = votingtime.keys();
    std::sort( times.begin(), times.end(), std::greater< qint64 >() );
    for ( qint64 t: times )
    {
        QList< ModelLocationVotesPtr > votes = votingtime.value( t );

        // sort the locations for count of votes
        class CountVotesGreater
        {
            public:
                bool operator()( ModelLocationVotesPtr a, ModelLocationVotesPtr b ) const
                {
                    return a->getUserNames().size() > b->getUserNames().size();
                }
        };
        std::sort( votes.begin(), votes.end(), CountVotesGreater() );

        addVotesItem( votes );
    }
}

void DialogVotesHistory::clearVotesItems()
{
    _p_ui->listWidgetVotes->clear();
}

void DialogVotesHistory::addVotesItem( QList< ModelLocationVotesPtr > votes )
{
    if (  votes.size() < 1 )
        return;

    QString labeldate;
    QString results;

    for ( ModelLocationVotesPtr v: votes )
    {
        QString locname = v->getLocationName();
        if ( locname.isEmpty() )
            locname = "[" + v->getLocationId() + "]";

        if ( labeldate.isEmpty() )
            labeldate = v->getVoteTimeEnd().toString();

        QString votedusers;
        for ( const QString& name: v->getUserNames() )
        {
            if ( !votedusers.isEmpty() )
                votedusers += ", ";
            votedusers += name;
        }

        // skip locations with no votes
        if ( !votedusers.isEmpty() )
        {
            results += "<p>" + locname + " (" + QString::number( v->getUserNames().size() ) + "): ";
            results += votedusers + "</p>";
        }
    }

    QWidget* p_widget = new QWidget( this );
    Ui::WidgetVotesItem votesui;
    votesui.setupUi( p_widget );
    votesui.labelDate->setText( labeldate );
    votesui.labelResults->setText( results );
    p_widget->resize( p_widget->sizeHint() );

    QListWidgetItem* p_listitem = new QListWidgetItem( _p_ui->listWidgetVotes );
    p_listitem->setSizeHint( p_widget->size() );

    _p_ui->listWidgetVotes->addItem( p_listitem );
    _p_ui->listWidgetVotes->setItemWidget( p_listitem, p_widget );
}

} // namespace event
} // namespace m4e
