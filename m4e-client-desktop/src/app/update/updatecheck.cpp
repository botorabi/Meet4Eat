/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "updatecheck.h"
#include <core/log.h>


namespace m4e
{
namespace update
{

UpdateCheck::UpdateCheck( QObject* p_parent ) :
 QObject( p_parent )
{
    _p_restUpdateCheck = new webapp::RESTUpdateCheck( this );
    connect( _p_restUpdateCheck, SIGNAL( onRESTUpdateInfo( m4e::update::ModelUpdateInfoPtr ) ), this, SLOT( onRESTUpdateInfo( m4e::update::ModelUpdateInfoPtr ) ) );
    connect( _p_restUpdateCheck, SIGNAL( onRESTUpdateInfoError( QString, QString ) ), this, SLOT( onRESTUpdateInfoError( QString, QString ) ) );
}

UpdateCheck::~UpdateCheck()
{
}

void UpdateCheck::setServerURL(const QString &url)
{
    _p_restUpdateCheck->setServerURL( url );
}

const QString& UpdateCheck::getServerURL() const
{
    return _p_restUpdateCheck->getServerURL();
}

update::ModelUpdateInfoPtr UpdateCheck::getUpdateInfo()
{
    return _updateInfo;
}

void UpdateCheck::requestGetUpdateInfo()
{
    setLastError();

    QString os;
#ifdef Q_OS_LINUX
    os = "Linux";
#elif defined(Q_OS_WIN32)
    os = "MSWin";
#elif defined(Q_OS_MAC)
    os = "MacOS";
#elif defined(Q_OS_ANDROID)
    os = "Android";
#endif

    ModelRequestUpdateInfoPtr request = new ModelRequestUpdateInfo();
    request->setName( M4E_APP_NAME );
    request->setVersion( M4E_APP_VERSION );
    request->setFlavor( "" );
    request->setOS( os );
    _p_restUpdateCheck->requestUpdateInfo( request );
}

void UpdateCheck::onRESTUpdatetGetInfo( update::ModelUpdateInfoPtr updateInfo )
{
    _updateInfo = updateInfo;
    emit onResponseGetUpdateInfo( true, updateInfo );
}

void UpdateCheck::onRESTUpdateErrorGetInfo( QString errorCode, QString reason )
{
    log_warning << TAG << "failed to get client update information: " << errorCode << ", reason: " << reason << std::endl;
    setLastError( reason, errorCode );
    emit onResponseGetUpdateInfo( false, update::ModelUpdateInfoPtr() );
}

void UpdateCheck::setLastError( const QString& error, const QString& errorCode )
{
    _lastError = error;
    _lastErrorCode = errorCode;
}

} // namespace update
} // namespace m4e
