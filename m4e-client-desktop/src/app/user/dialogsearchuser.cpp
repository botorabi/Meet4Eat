/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "dialogsearchuser.h"
#include <core/log.h>
#include <common/dialogmessage.h>
#include <ui_widgetsearchuser.h>


namespace m4e
{
namespace user
{

DialogSearchUser::DialogSearchUser( webapp::WebApp* p_webApp, QWidget* p_parent ) :
 common::BaseDialog( p_parent ),
 _p_webApp( p_webApp )
{
    _p_ui = new Ui::WidgetSearchUser();
    decorate( *_p_ui );

    setTitle( QApplication::translate( "DialogSearchUser", "Search for User" ) );
    QString addbtn( QApplication::translate( "DialogSearchUser", "Add" ) );
    QString cancelbtn( QApplication::translate( "DialogSearchUser", "Cancel" ) );
    setupButtons( &addbtn, &cancelbtn, nullptr );
    setResizable( true );

    connect( _p_webApp, SIGNAL( onUserSearch( QList< m4e::user::ModelUserInfoPtr > ) ), this, SLOT( onUserSearch( QList< m4e::user::ModelUserInfoPtr > ) ) );
    connect( _p_ui->lineEditSearch, SIGNAL( returnPressed() ), this, SLOT( onLineEditSearchReturnPressed() ) );
    connect( _p_ui->lineEditSearch, SIGNAL( editingFinished() ), this, SLOT( onLineEditSearchReturnPressed() ) );
}

DialogSearchUser::~DialogSearchUser()
{
    delete _p_ui;
}

void DialogSearchUser::onLineEditSearchReturnPressed()
{
    QString keyword = _p_ui->lineEditSearch->text();
    keyword = keyword.trimmed();

    if ( keyword.length() < 3 )
        return;

    if ( keyword.length() > 32 )
        keyword = keyword.mid( 0, 32 );

    _p_webApp->requestUserSearch( keyword );
}

void DialogSearchUser::onBtnSearchClicked()
{
    onLineEditSearchReturnPressed();
}

void DialogSearchUser::onUserSearch( QList< user::ModelUserInfoPtr > users )
{
    _p_ui->comboBoxSearch->clear();
    for ( auto user: users )
    {
        QVariant data;
        data.setValue( user );
        _p_ui->comboBoxSearch->addItem( user->getName(), data );
    }
}

bool DialogSearchUser::onButton1Clicked()
{
    int index = _p_ui->comboBoxSearch->currentIndex();
    if ( index < 0 )
        return true;

    QVariant data = _p_ui->comboBoxSearch->itemData( index );
    _userInfo = data.value< user::ModelUserInfoPtr >();
    return true;
}

} // namespace user
} // namespace m4e
