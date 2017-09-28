/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include "documentcache.h"
#include <core/log.h>
#include <settings/appsettings.h>
#include <QDir>
#include <QFile>
#include <QDateTime>


namespace m4e
{
namespace doc
{

/**
 * @brief Used for serialization of documents in cache
 */
static const QString FIELD_SEP = "|";

/**
 * @brief Cache file magic string
 */
static const QString M4E_FILEFORMAT_FIELD_NAME = "M4E_CACHE_FORMAT_VER";

/**
 * @brief Cache file format version
 */
static const int M4E_FILEFORMAT_VERSION = 1;

/**
 * @brief Cache file header for creation date
 */
static const QString M4E_FILECREATION_FIELD_NAME = "M4E_CACHE_CREATED";

/**
 * @brief Cache file header for last fetch date
 */
static const QString M4E_FILEFETCH_FIELD_NAME = "M4E_CACHE_LASTFETCH";


DocumentCache::DocumentCache( QObject* p_parent ) :
 QObject( p_parent )
{
}

DocumentCache::~DocumentCache()
{
}

void DocumentCache::requestDocument( const QString& id, const QString& eTag )
{
    // first check the cache
    ModelDocumentPtr document = findDocument( id, eTag );
    if ( document.valid() )
    {
        emit onDocumentReady( document );
        return;
    }

    getOrCreateRESTDocument()->getDocument( id );
}

void DocumentCache::clearCache()
{
    log_info << TAG << "removing all cached documents" << std::endl;

    QString cachedir = getOrCreateCacheDirectory();
    QDir dir( cachedir );
    if ( !dir.removeRecursively() )
    {
        log_warning << TAG << "could not delete the cache folder: " << cachedir.toStdString() << std::endl;
    }
    _cacheDir = "";
}

void DocumentCache::purgeCache( int expirationDays )
{
    log_info << TAG << "purging document cache" << std::endl;

    QString     cachedir = getOrCreateCacheDirectory();
    QByteArray  data;
    int         offset;
    ulong       age;
    ulong       expire = expirationDays * 24 * 60 * 60 * 1000;

    QDir dir( cachedir );
    QStringList allfiles = dir.entryList( QDir::Files );

    for ( const auto& fn: allfiles )
    {
        QString filename = cachedir + QDir::separator() + fn;
        QFile file( filename );
        if ( !file.open( QFile::ReadWrite ) )
            continue;
        if ( !checkAndUpdateCacheFileHeader( file, data, offset, age, false ) )
            continue;

        if ( age > expire )
        {
            if ( !file.remove() )
            {
                log_warning << TAG << " could not remove cache file: " << filename.toStdString() << std::endl;
            }
            else
            {
                log_debug << TAG << " expired cache file removed: " << filename.toStdString() << std::endl;
            }
        }
    }
}

void DocumentCache::onRESTDocumentGet( ModelDocumentPtr document )
{
    cacheDocument( document );
    emit onDocumentReady( document );
}

void DocumentCache::onRESTDocumentErrorGet( QString errorCode, QString reason )
{
    log_warning << TAG << "could not get document from server (" << errorCode.toStdString() << "), reason: " << reason.toStdString() << std::endl;
    emit onDocumentReady( ModelDocumentPtr() );
}

webapp::RESTDocument* DocumentCache::getOrCreateRESTDocument()
{
    if ( !_p_restDocument )
    {
        _p_restDocument = new webapp::RESTDocument( this );

        connect( _p_restDocument, SIGNAL( onRESTDocumentGet( m4e::doc::ModelDocumentPtr ) ), this, SLOT( onRESTDocumentGet( m4e::doc::ModelDocumentPtr ) ) );

        connect( _p_restDocument, SIGNAL( onRESTDocumentErrorGet( QString, QString ) ), this, SLOT( onRESTDocumentErrorGet( QString, QString ) ) );

        QString server = settings::AppSettings::get()->readSettingsValue( M4E_SETTINGS_CAT_SRV, M4E_SETTINGS_KEY_SRV_URL, "" );
        _p_restDocument->setServerURL( server );
    }
    return _p_restDocument;
}

ModelDocumentPtr DocumentCache::findDocument( const QString& id, const QString& eTag )
{
    QString cachedir = getOrCreateCacheDirectory();
    if ( cachedir.isEmpty() )
    {
        log_warning << TAG << "cannot use document cache!" << std::endl;
        return ModelDocumentPtr();
    }

    QString filename = cachedir + QDir::separator() + eTag;
    QFile file( filename );
    if ( !file.open( QFile::ReadWrite ) )
    {
        return ModelDocumentPtr();
    }

    ModelDocumentPtr document = new ModelDocument();

    log_verbose << TAG << "loading document " << id.toStdString() << " ..." << std::endl;

    // check if the file can be read from cache. if not then the cache file format may have been changed
    if ( !loadDocument( file, document ) )
    {
        // remove the cache file
        file.close();
        log_verbose << TAG << " cached document is not in proper format, it will be removed from cache: " << filename.toStdString() << std::endl;
        file.remove();
        return ModelDocumentPtr();
    }

    file.close();
    return document;
}

bool DocumentCache::cacheDocument( ModelDocumentPtr document )
{
    QString cachedir = getOrCreateCacheDirectory();
    if ( cachedir.isEmpty() )
    {
        log_warning << TAG << "cannot cache document!" << std::endl;
        return false;
    }

    QString filename = cachedir + QDir::separator() + document->getETag();
    QFile file( filename );
    if ( !file.exists() )
    {
        log_verbose << TAG << "caching document: " << document->getId().toStdString() << std::endl;

        // create a new file
        if ( !file.open( QFile::WriteOnly ) )
        {
            log_warning << TAG << "  could not create cache file " << filename.toStdString() << std::endl;
            return false;
        }

        storeDocument( file, document );
        file.close();
    }

    return true;
}

const QString& DocumentCache::getOrCreateCacheDirectory()
{
    if ( !_cacheDir.isEmpty() )
        return _cacheDir;

    QString cachedir = QStandardPaths::writableLocation( QStandardPaths::AppDataLocation ) + QDir::separator() + M4E_LOCAL_CACHE_DIR;
    QDir dir;
    if ( !dir.exists( cachedir ) )
    {
        QDir dir;
        if ( !dir.mkpath( cachedir ) )
        {
            cachedir = "";
            log_warning << TAG << "could not create cache folder in " << cachedir.toStdString() << std::endl;
        }
    }

    _cacheDir = cachedir;

    log_verbose << TAG << "using cache directory " << cachedir.toStdString() << std::endl;

    return _cacheDir;
}

void DocumentCache::storeDocument( QFile& file, ModelDocumentPtr document )
{
    QByteArray data;

    // write the file magic string and creation time stamp, first
    QString magic       = M4E_FILEFORMAT_FIELD_NAME   + "=" + QString::number( M4E_FILEFORMAT_VERSION );
    // time stamps have exact 16 digits with leading zeros, this makes in-place upating in file simpler
    QString tscreatio   = M4E_FILECREATION_FIELD_NAME + "=" + QString( "%1" ).arg( QDateTime::currentMSecsSinceEpoch(), 16, 10, QChar( '0' ) );
    QString tslastfetch = M4E_FILEFETCH_FIELD_NAME    + "=" + QString( "%1" ).arg( QDateTime::currentMSecsSinceEpoch(), 16, 10, QChar( '0' ) );

    data.append( FIELD_SEP + QString::number( magic.size() ) + ":" + magic );
    data.append( FIELD_SEP + QString::number( tscreatio.size() ) + ":" + tscreatio );
    data.append( FIELD_SEP + QString::number( tslastfetch.size() ) + ":" + tslastfetch );

    // document properties
    QString id   = "id=" + document->getId();
    QString name = "name=" + document->getName();
    QString etag = "etag=" + document->getETag();
    QString enc  = "encoding=" + document->getEncoding();
    QString type = "encoding=" + document->getType();
    QString cont = "content=";

    // append the head
    data.append( FIELD_SEP + QString::number( id.size() ) + ":" + id );
    data.append( FIELD_SEP + QString::number( name.size() ) + ":" + name );
    data.append( FIELD_SEP + QString::number( etag.size() ) + ":" + etag );
    data.append( FIELD_SEP + QString::number( enc.size() ) + ":" + enc );
    data.append( FIELD_SEP + QString::number( type.size() ) + ":" + type );
    // now append the content
    data.append( FIELD_SEP + QString::number( cont.size() + document->getContent().size() ) + ":" + cont );
    data.append( document->getContent() );

    file.write( data);
}

bool DocumentCache::loadDocument( QFile& file, ModelDocumentPtr document )
{
    QByteArray data;
    int        offset;
    ulong      age;

    if ( !checkAndUpdateCacheFileHeader( file, data, offset, age, true ) )
        return false;

    // read all document fields
    QString    name;
    QByteArray value;

    while ( readNextField( data, offset, name, value ) )
    {
        if ( name == "id" )
            document->setId( value );
        else if ( name == "name" )
            document->setName( value );
        else if ( name == "type" )
            document->setType( value );
        else if ( name == "encoding" )
            document->setEncoding( value );
        else if ( name == "etag" )
            document->setETag( value );
        else if ( name == "content" )
            document->setContent( value );
    }

    return true;
}

bool DocumentCache::checkAndUpdateCacheFileHeader( QFile& file, QByteArray& data, int& offset, ulong& age, bool updateFetchTime )
{
    bool       res;
    QString    magic;
    QByteArray magicvalue;
    QString    tscreation;
    QByteArray tscreationvalue;
    QString    tslastfetch;
    QByteArray tslastfetchvalue;

    data   = file.readAll();
    offset = 0;

    // check the file format header
    res = readNextField( data, offset, magic, magicvalue );
    if ( !res ||  magic != M4E_FILEFORMAT_FIELD_NAME || magicvalue.toInt() != M4E_FILEFORMAT_VERSION )
    {
        log_verbose << TAG << " unexpected cache file format, file format mismatch detected" << std::endl;
        return false;
    }

    res = readNextField( data, offset, tscreation, tscreationvalue );
    if ( !res || tscreation != M4E_FILECREATION_FIELD_NAME )
    {
        log_verbose << TAG << " unexpected cache file format, missing creation timestamp" << std::endl;
        return false;
    }

    // store the offset of last fetch header field in file, we will update this field
    int tslastfetchoffset = offset;

    res = readNextField( data, offset, tslastfetch, tslastfetchvalue );
    if ( !res || tslastfetch != M4E_FILEFETCH_FIELD_NAME )
    {
        log_verbose << TAG << " unexpected cache file format, missing last fetch timestamp" << std::endl;
        return false;
    }

    // calculate the age since last fetch
    age = QDateTime::currentMSecsSinceEpoch() - tslastfetchvalue.toULongLong();

    if ( updateFetchTime )
    {
        // update the last fetch timestamp
        QString updatetslastfetch = M4E_FILEFETCH_FIELD_NAME + "=" + QString( "%1" ).arg( QDateTime::currentMSecsSinceEpoch(), 16, 10, QChar( '0' ) );
        updatetslastfetch = FIELD_SEP + QString::number( updatetslastfetch.size() ) + ":" + updatetslastfetch;
        // replace the timestamp
        file.seek( tslastfetchoffset );
        file.write( updatetslastfetch.toStdString().c_str(), updatetslastfetch.size() );

        // output some stats
        /*
        QDateTime datecreation;
        QDateTime datelastfetch;
        datecreation.setMSecsSinceEpoch( tscreationvalue.toULongLong() );
        datelastfetch.setMSecsSinceEpoch( tslastfetchvalue.toULongLong() );
        qint64 agedays = datecreation.daysTo( datelastfetch );
        qint64 agelastfetch = datelastfetch.daysTo( QDateTime::currentDateTime() );

        log_verbose << TAG << " document cache creation date: " << datecreation.toString().toStdString() <<
                              ", age: " << agedays << " days" <<
                              ", days since last fetch: " << agelastfetch <<
                              " (" << age << " ms)" << std::endl;
        */
    }

    return true;
}

bool DocumentCache::readNextField( QByteArray& data, int& offset, QString& fieldName, QByteArray& fieldValue )
{
    int index = data.indexOf( ':', offset );
    if ( index < 0 )
    {
        return false;
    }

    QString size = data.mid( offset + FIELD_SEP.size(), index - offset - FIELD_SEP.size() );
    offset = index + 1;
    QString field = data.mid( offset, size.toInt() );
    int fieldpos = field.indexOf( "=", 0 );
    if ( fieldpos < 0 )
    {
        log_warning << TAG << " invalid cache file format" << std::endl;
        return false;
    }

    // read field name and value
    int valuesize = size.toInt() - fieldpos - 1;
    fieldName = field.mid( 0, fieldpos );
    fieldValue = data.mid( offset + fieldName.size() + 1, valuesize );

    // update the offset in byte array
    offset = index + field.size() + 1;

    return true;
}

} // namespace doc
} // namespace m4e
