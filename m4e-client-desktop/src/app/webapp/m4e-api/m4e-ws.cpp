/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include <core/log.h>
#include "m4e-restops.h"
#include "m4e-ws.h"
#include <QAbstractSocket>
#include <QNetworkRequest>


namespace m4e
{
namespace webapp
{


Meet4EatWebSocket::Meet4EatWebSocket( QObject* p_parent ) :
 QObject( p_parent )
{
    _p_webSocket = new QWebSocket();
    _p_webSocket->setParent( this );

#if !M4E_DISALLOW_INSECURE_CONNECTION
    QSslConfiguration conf = _p_webSocket->sslConfiguration();
    conf.setPeerVerifyMode( QSslSocket::VerifyNone );
    _p_webSocket->setSslConfiguration( conf );
#endif

    int keepaliveperiod = M4E_PERIOD_SRV_UPDATE_STATUS * 1000 * 60;
    _p_pingTimer = new QTimer( this );
    _p_pingTimer->setSingleShot( false );
    _p_pingTimer->setInterval( keepaliveperiod );

    connect( _p_pingTimer, SIGNAL( timeout() ), this, SLOT( onPingTimer() ) );
    connect( _p_webSocket, SIGNAL( connected() ), this, SLOT( onConnected() ) );
    connect( _p_webSocket, SIGNAL( error( QAbstractSocket::SocketError) ), this, SLOT( onError( QAbstractSocket::SocketError ) ) );
    connect( _p_webSocket, SIGNAL( disconnected() ), this, SLOT( onDisconnected() ) );
    connect( _p_webSocket, SIGNAL( textMessageReceived( QString ) ), this, SLOT( onTextMessageReceived( QString ) ) );
    connect( _p_webSocket, SIGNAL( pong( quint64, QByteArray ) ), this, SLOT( onPongReceived( quint64, QByteArray ) ) );
}

Meet4EatWebSocket::~Meet4EatWebSocket()
{
}

const QString& Meet4EatWebSocket::getWsURL() const
{
    return _wsURL;
}

void Meet4EatWebSocket::setWsURL( const QString& wsURL )
{
    QString url = wsURL;
    if ( !url.startsWith( "http://" ) && !url.startsWith( "https://" ) )
    {
        url = "http://" + url;
    }
    url = url.replace( "http", "ws" );

    _wsURL = url + M4E_WS_SRV_RESOURCE_PATH;
}

void Meet4EatWebSocket::setupKeepAlive( bool enable, int interval )
{
    _pingIntrerval = std::min( 60000, interval );
    _pingEnable    = enable;
    _lastLifeSign  = 0;
    _pingAverage   = 0;

    _p_pingTimer->setInterval( _pingIntrerval );

    // if a connection already exists then update the ping timer
    if ( _p_webSocket->state() == QAbstractSocket::ConnectedState )
    {
        if ( enable )
            _p_pingTimer->start();
        else
            _p_pingTimer->stop();
    }
}

void Meet4EatWebSocket::getSetupKeepAlive( bool& enable, int& interval )
{
    interval = _pingIntrerval;
    enable   = _pingEnable;
}

bool Meet4EatWebSocket::establishConnection()
{
    // if a connection is open then close it first
    if ( _p_webSocket->state() == QAbstractSocket::ConnectedState )
    {
        _p_webSocket->close();
    }

    QNetworkRequest req;
    if ( !setupNetworkRequest( req ) )
        return false;

    _p_webSocket->open( req );
    return true;
}

const QString& Meet4EatWebSocket::getWebAppProtocolVersion() const
{
    return _webAppProtVersion;
}

void Meet4EatWebSocket::shutdownConnection()
{
    _p_webSocket->close();
    _webAppProtVersion = "";
}

bool Meet4EatWebSocket::sendPacket( comm::PacketPtr packet )
{
    // if no timestamp was set then set it now
    if ( !packet->getTime().isValid() )
        packet->setTime( QDateTime::currentDateTime() );

    return sendMessage( packet->toJSON().toJson() );
}

void Meet4EatWebSocket::onConnected()
{
    log_info << TAG << "connection established" << std::endl;
    emit onConnectionEstablished();

    if ( _pingEnable )
        _p_pingTimer->start();
}

void Meet4EatWebSocket::onDisconnected()
{
    log_info << TAG << "disconnected from server" << std::endl;
    emit onConnectionClosed();

    _p_pingTimer->stop();
}

void Meet4EatWebSocket::onError( QAbstractSocket::SocketError error )
{
    log_warning << TAG << "communication problem occured, error code: " << error << ", reason: " << _p_webSocket->errorString() << std::endl;
}

void Meet4EatWebSocket::onTextMessageReceived( QString message )
{
    comm::PacketPtr packet = new comm::Packet();
    if ( packet->fromJSON( message ) )
    {
        // the very first packet right after connection contains protocol information, don't distribute it
        if ( _webAppProtVersion.isEmpty() )
        {
            _webAppProtVersion = getProtocolVersion( packet );
            log_info << TAG << "web app protocol version: " << _webAppProtVersion << std::endl;
        }
        else
        {
            emit onReceivedPacket( packet );
        }
    }
    else
    {
        log_warning << TAG << "invalid net packet received!" << std::endl;
        log_warning << TAG << " packet content: " << message << std::endl;
    }
}

void Meet4EatWebSocket::onPingTimer()
{
    if ( _p_webSocket )
        _p_webSocket->ping();
}

void Meet4EatWebSocket::onPongReceived( quint64 elapsedTime, const QByteArray& /*payload*/ )
{
    _lastLifeSign = QDateTime::currentMSecsSinceEpoch();
    if ( _pingAverage == 0 )
        _pingAverage = elapsedTime;
    else
        _pingAverage = ( quint64 )( 0.1 * ( double )elapsedTime + 0.9 * ( double )_pingAverage );
}

bool Meet4EatWebSocket::setupNetworkRequest( QNetworkRequest& request )
{
    request.setUrl( QUrl( _wsURL ) );

    // it is important to use the session cookie got previously during
    //  sing-in process, otherwise the server will deny the connection!
    QNetworkCookieJar* p_cookie = Meet4EatRESTOperations::getCookies();
    if ( !p_cookie )
    {
        log_error << TAG << "cannot establish a WebSocket connection without a prior sign-in!" << std::endl;
        return false;
    }
    // transfer the cookie to the network request header
    QString cookieurl = _wsURL;
    cookieurl.replace( "ws", "http" );
    QList< QNetworkCookie > cookies = p_cookie->cookiesForUrl( QUrl( cookieurl ) );
    for ( QNetworkCookie cookie: cookies )
    {
        // search for session cookie
        if ( cookie.isSessionCookie() )
        {
            //! NOTE for some reason setHeader does not work! we use the method setRawHeader
            //req.setHeader( QNetworkRequest::SetCookieHeader, QVariant::fromValue( c ) );
            request.setRawHeader( QString( "Cookie" ).toUtf8(), ( QString( cookie.name() ) + "=" + QString( cookie.value() ) ).toUtf8() );
            break;
        }
    }

    return true;
}

QString Meet4EatWebSocket::getProtocolVersion( comm::PacketPtr packet )
{
    QJsonDocument data = packet->getData();
    return data.object().value( "protocolVersion" ).toString( "???" );
}

bool Meet4EatWebSocket::sendMessage( const QString& message )
{
    if ( _p_webSocket->state() != QAbstractSocket::ConnectedState )
        return false;

    // we do not send zero-length strings
    qint64 len = message.length();
    if ( len < 1 )
        return false;

    // handle send segmentation
    qint64 cnt = 0;
    do
    {
        const QString buf = message.mid( cnt );
        cnt = _p_webSocket->sendTextMessage( buf );
    }
    while ( cnt < len );

    return true;
}

} // namespace webapp
} // namespace m4e
